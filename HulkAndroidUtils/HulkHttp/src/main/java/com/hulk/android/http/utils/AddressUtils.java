package com.hulk.android.http.utils;

/**
 * IP套件
 * @author: zhanghao
 * @Time: 2021-03-04 18:53
 */
public class AddressUtils {

    /**
     * Address type.
     */
    static private final int UNKNOWN_ADDRESS_TYPE = 0;
    static private final int PHONE_NUMBER_ADDRESS_TYPE = 1;
    static private final int EMAIL_ADDRESS_TYPE = 2;
    static private final int IPV4_ADDRESS_TYPE = 3;
    static private final int IPV6_ADDRESS_TYPE = 4;

    /**
     * Address regular expression string.
     */
    static final String REGEXP_PHONE_NUMBER_ADDRESS_TYPE = "\\+?[0-9|\\.|\\-]+";
    static final String REGEXP_EMAIL_ADDRESS_TYPE = "[a-zA-Z| ]*\\<{0,1}[a-zA-Z| ]+@{1}" +
            "[a-zA-Z| ]+\\.{1}[a-zA-Z| ]+\\>{0,1}";
    static final String REGEXP_IPV6_ADDRESS_TYPE =
            "[a-fA-F]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}" +
                    "[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}\\:{1}" +
                    "[a-fA-F0-9]{4}\\:{1}[a-fA-F0-9]{4}";
    static final String REGEXP_IPV4_ADDRESS_TYPE = "[0-9]{1,3}\\.{1}[0-9]{1,3}\\.{1}" +
            "[0-9]{1,3}\\.{1}[0-9]{1,3}";

    /**
     * Check address type.
     *
     * @param address address string without the postfix stinng type,
     *        such as "/TYPE=PLMN", "/TYPE=IPv6" and "/TYPE=IPv4"
     * @return PDU_PHONE_NUMBER_ADDRESS_TYPE if it is phone number,
     *         PDU_EMAIL_ADDRESS_TYPE if it is email address,
     *         PDU_IPV4_ADDRESS_TYPE if it is ipv4 address,
     *         PDU_IPV6_ADDRESS_TYPE if it is ipv6 address,
     *         PDU_UNKNOWN_ADDRESS_TYPE if it is unknown.
     */
    public static int checkAddressType(String address) {
        /**
         * From OMA-TS-MMS-ENC-V1_3-20050927-C.pdf, section 8.
         * address = ( e-mail / device-address / alphanum-shortcode / num-shortcode)
         * e-mail = mailbox; to the definition of mailbox as described in
         * section 3.4 of [RFC2822], but excluding the
         * obsolete definitions as indicated by the "obs-" prefix.
         * device-address = ( global-phone-number "/TYPE=PLMN" )
         * / ( ipv4 "/TYPE=IPv4" ) / ( ipv6 "/TYPE=IPv6" )
         * / ( escaped-value "/TYPE=" address-type )
         *
         * global-phone-number = ["+"] 1*( DIGIT / written-sep )
         * written-sep =("-"/".")
         *
         * ipv4 = 1*3DIGIT 3( "." 1*3DIGIT ) ; IPv4 address value
         *
         * ipv6 = 4HEXDIG 7( ":" 4HEXDIG ) ; IPv6 address per RFC 2373
         */

        if (null == address) {
            return UNKNOWN_ADDRESS_TYPE;
        }

        if (address.matches(REGEXP_IPV4_ADDRESS_TYPE)) {
            // Ipv4 address.
            return IPV4_ADDRESS_TYPE;
        }else if (address.matches(REGEXP_PHONE_NUMBER_ADDRESS_TYPE)) {
            // Phone number.
            return PHONE_NUMBER_ADDRESS_TYPE;
        } else if (address.matches(REGEXP_EMAIL_ADDRESS_TYPE)) {
            // Email address.
            return EMAIL_ADDRESS_TYPE;
        } else if (address.matches(REGEXP_IPV6_ADDRESS_TYPE)) {
            // Ipv6 address.
            return IPV6_ADDRESS_TYPE;
        } else {
            // Unknown address.
            return UNKNOWN_ADDRESS_TYPE;
        }
    }

    public static boolean isIPv6Address(String address) {
        return checkAddressType(address) == IPV6_ADDRESS_TYPE;
    }

    public static boolean isIPv4Address(String address) {
        return checkAddressType(address) == IPV4_ADDRESS_TYPE;
    }

    public static boolean isPhoneNumber(String address) {
        return checkAddressType(address) == PHONE_NUMBER_ADDRESS_TYPE;
    }

    public static boolean isEmailAddress(String address) {
        return checkAddressType(address) == EMAIL_ADDRESS_TYPE;
    }
}
