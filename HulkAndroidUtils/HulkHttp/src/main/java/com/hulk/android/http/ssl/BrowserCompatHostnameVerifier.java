package com.hulk.android.http.ssl;

import com.hulk.android.http.utils.AddressUtils;

import org.apache.http.conn.ssl.X509HostnameVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

/*
 * 该类从源码中移过来，由于需要修改的方法无法复写，合并了AbstractVerifier和BrowserCompatHostnameVerifier中的方法；
 */
public class BrowserCompatHostnameVerifier implements HostnameVerifier {

    final static int DNS_NAME_TYPE = 2;
    final static int IP_ADDRESS_TYPE = 7;


    final static String[] BAD_COUNTRY_2LDS = {
            "ac", "co", "com", "ed", "edu", "go", "gouv", "gov", "info",
            "lg", "ne", "net", "or", "org"
    };

    static {
        // Just in case developer forgot to manually sort the array. :-)
        Arrays.sort(BAD_COUNTRY_2LDS);
    }

    public final void verify(final String host, final SSLSocket ssl)
            throws IOException {

        if (host == null) {
            throw new IllegalArgumentException("Host may not be null");
        }
        SSLSession session = ssl.getSession();
        if (session == null) {
            final InputStream in = ssl.getInputStream();
            in.available();
            session = ssl.getSession();
            if (session == null) {
                ssl.startHandshake();
                session = ssl.getSession();
            }
        }

        final Certificate[] certs = session.getPeerCertificates();
        final X509Certificate x509 = (X509Certificate) certs[0];
        verify(host, x509);
    }

    @Override
    public final boolean verify(final String host, final SSLSession session) {
        try {
            final Certificate[] certs = session.getPeerCertificates();
            final X509Certificate x509 = (X509Certificate) certs[0];
            verify(host, x509);
            return true;
        } catch (final SSLException ex) {
            // if (log.isDebugEnabled()) {
            // log.debug(ex.getMessage(), ex);
            // }
            return false;
        }
    }

    public final void verify(
            final String host, final X509Certificate cert) throws SSLException {
        final boolean ipv4 = AddressUtils.isIPv4Address(host);
        final boolean ipv6 = AddressUtils.isIPv6Address(host);
        final int subjectType = ipv4 || ipv6 ? IP_ADDRESS_TYPE : DNS_NAME_TYPE;
        final List<String> subjectAlts = extractSubjectAlts(cert,
                subjectType);
        final String[] cns = getCNs(cert);
        verify(host,
                cns != null ? cns : null,
                subjectAlts != null && !subjectAlts.isEmpty()
                        ? subjectAlts.toArray(new String[subjectAlts.size()]) : null);
    }

    public final void verify(final String host, final String[] cns,
            final String[] subjectAlts,
            final boolean strictWithSubDomains)
                    throws SSLException {

        final String cn = cns != null && cns.length > 0 ? cns[0] : null;
        final List<String> subjectAltList = subjectAlts != null && subjectAlts.length > 0
                ? Arrays.asList(subjectAlts) : null;

        final String normalizedHost = AddressUtils.isIPv6Address(host)
                ? normaliseAddress(host.toLowerCase(Locale.ROOT)) : host;

        if (subjectAltList != null) {
            for (String subjectAlt : subjectAltList) {
                final String normalizedAltSubject = AddressUtils.isIPv6Address(subjectAlt)
                        ? normaliseAddress(subjectAlt) : subjectAlt;
                if (matchIdentity(normalizedHost, normalizedAltSubject, strictWithSubDomains)) {
                    return;
                }
            }
            throw new SSLException("Certificate for <" + host + "> doesn't match any " +
                    "of the subject alternative names: " + subjectAltList);
        } else if (cn != null) {
            final String normalizedCN = AddressUtils.isIPv6Address(cn)
                    ? normaliseAddress(cn) : cn;
            if (matchIdentity(normalizedHost, normalizedCN, strictWithSubDomains)) {
                return;
            }
            throw new SSLException("Certificate for <" + host + "> doesn't match " +
                    "common name of the certificate subject: " + cn);
        } else {
            throw new SSLException("Certificate subject for <" + host + "> doesn't contain " +
                    "a common name and does not have alternative names");
        }
    }

    private static boolean matchIdentity(final String host, final String identity,
            final boolean strict) {
        if (host == null) {
            return false;
        }
        final String normalizedHost = host.toLowerCase(Locale.ROOT);
        final String normalizedIdentity = identity.toLowerCase(Locale.ROOT);
        // The CN better have at least two dots if it wants wildcard
        // action. It also can't be [*.co.uk] or [*.co.jp] or
        // [*.org.uk], etc...
        final String parts[] = normalizedIdentity.split("\\.");
        final boolean doWildcard = parts.length >= 3 && parts[0].endsWith("*") &&
                (!strict || validCountryWildcard(parts));
        if (doWildcard) {
            boolean match;
            final String firstpart = parts[0];
            if (firstpart.length() > 1) { // e.g. server*
                final String prefix = firstpart.substring(0, firstpart.length() - 1); // e.g. server
                final String suffix = normalizedIdentity.substring(firstpart.length()); // skip wildcard part from cn
                final String hostSuffix = normalizedHost.substring(prefix.length()); // skip wildcard part from normalizedHost
                match = normalizedHost.startsWith(prefix) && hostSuffix.endsWith(suffix);
            } else {
                match = normalizedHost.endsWith(normalizedIdentity.substring(1));
            }
            return match && (!strict || countDots(normalizedHost) == countDots(normalizedIdentity));
        } else {
            return normalizedHost.equals(normalizedIdentity);
        }
    }

    private static boolean validCountryWildcard(final String parts[]) {
        if (parts.length != 3 || parts[2].length() != 2) {
            return true; // it's not an attempt to wildcard a 2TLD within a country code
        }
        return Arrays.binarySearch(BAD_COUNTRY_2LDS, parts[1]) < 0;
    }

    public static boolean acceptableCountryWildcard(final String cn) {
        return validCountryWildcard(cn.split("\\."));
    }

    /**
     * Extracts the array of SubjectAlt DNS names from an X509Certificate.
     * Returns null if there aren't any.
     * <p>
     * Note: Java doesn't appear able to extract international characters from
     * the SubjectAlts. It can only extract international characters from the CN
     * field.
     * </p>
     * <p>
     * (Or maybe the version of OpenSSL I'm using to test isn't storing the
     * international characters correctly in the SubjectAlts?).
     * </p>
     *
     * @param cert X509Certificate
     * @return Array of SubjectALT DNS names stored in the certificate.
     */
    public static String[] getDNSSubjectAlts(final X509Certificate cert) {
        final List<String> subjectAlts = extractSubjectAlts(
                cert, DNS_NAME_TYPE);
        return subjectAlts != null && !subjectAlts.isEmpty()
                ? subjectAlts.toArray(new String[subjectAlts.size()]) : null;
    }

    /**
     * Counts the number of dots "." in a string.
     * 
     * @param s string to count dots from
     * @return number of dots
     */
    public static int countDots(final String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '.') {
                count++;
            }
        }
        return count;
    }

    public final void verify(
            final String host,
            final String[] cns,
            final String[] subjectAlts) throws SSLException {
        verify(host, cns, subjectAlts, false);
    }

    @Override
    public final String toString() {
        return "BROWSER_COMPATIBLE";
    }

    /*
     * 备注：由于DefaultHostnameVerifier中的代码引用不到，以下代码为org.apache.http.conn.ssl.
     * DefaultHostnameVerifier(4.5)中的，其中，获取SubjectAlternativeName用的是4.5中的方法，
     * 获取CN用的是4.0中的方法。
     */
    static List<String> extractSubjectAlts(final X509Certificate cert, final int subjectType) {
        Collection<List<?>> c = null;
        try {
            c = cert.getSubjectAlternativeNames();
        } catch (final CertificateParsingException ignore) {
        }
        List<String> subjectAltList = null;
        if (c != null) {
            for (final List<?> aC : c) {
                final List<?> list = aC;
                final int type = ((Integer) list.get(0)).intValue();
                if (type == subjectType) {
                    final String s = (String) list.get(1);
                    if (subjectAltList == null) {
                        subjectAltList = new ArrayList<String>();
                    }
                    subjectAltList.add(s);
                }
            }
        }
        return subjectAltList;
    }

    /*
     * Normalize IPv6 or DNS name.
     */
    static String normaliseAddress(final String hostname) {
        if (hostname == null) {
            return hostname;
        }
        try {
            final InetAddress inetAddress = InetAddress.getByName(hostname);
            return inetAddress.getHostAddress();
        } catch (final UnknownHostException unexpected) { // Should not happen,
                                                          // because we check
                                                          // for IPv6 address
                                                          // above
            return hostname;
        }
    }

    public static String[] getCNs(X509Certificate cert) {
        LinkedList<String> cnList = new LinkedList<String>();
        String subjectPrincipal = cert.getSubjectX500Principal().toString();
        StringTokenizer st = new StringTokenizer(subjectPrincipal, ",");
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            int x = tok.indexOf("CN=");
            if (x >= 0) {
                cnList.add(tok.substring(x + 3));
            }
        }
        if (!cnList.isEmpty()) {
            String[] cns = new String[cnList.size()];
            cnList.toArray(cns);
            return cns;
        } else {
            return null;
        }
    }

}
