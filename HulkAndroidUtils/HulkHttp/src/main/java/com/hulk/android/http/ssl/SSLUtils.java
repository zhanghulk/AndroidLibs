package com.hulk.android.http.ssl;

import android.content.Context;
import com.hulk.android.log.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class SSLUtils {

    private static final String TAG = "SSLUtils" ;

    private static Context sContext;

    static HostnameVerifier sCustomHostnameVerifier = null;

    /**
     * 证书验证开关: https 和 mqtt 连接是否验证服务端证书
     */
    private static boolean sServerCertVerifyEnabled = true;

    public static void setContext(Context context) {
        SSLUtils.sContext = context;
    }

    /**
     * 设置是否需要验证服务器证书
     * @param serverCertVerifyEnabled
     */
    public static void setServerCertVerifyEnabled(boolean serverCertVerifyEnabled) {
        sServerCertVerifyEnabled = serverCertVerifyEnabled;
    }

    /**
     * 是否需要验证服务器证书
     * @return
     */
    public static boolean isServerCertVerifyEnabled() {
        return sServerCertVerifyEnabled;
    }

    public static HostnameVerifier getCustomHostnameVerifier() {
        if (sCustomHostnameVerifier == null) {
            sCustomHostnameVerifier = new NoneHostnameVerifier();
        }
        return sCustomHostnameVerifier;
    }

    /**
     * 默认信任所有的证书
     * <p>最好加上证书认证，主流App都有自己的证书
     * @return
     */
    public static SSLSocketFactory createSSLSocketFactory(X509TrustManager trustManager) {
        SSLSocketFactory sslSocketFactory = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "createSSLSocketFactory: NoSuchAlgorithmException", e);
        } catch (KeyManagementException e) {
            Log.e(TAG, "createSSLSocketFactory: KeyManagementException", e);
        } catch (Exception e) {
            Log.e(TAG, "createSSLSocketFactory: ", e);
        }
        return sslSocketFactory;
    }

    public static SSLSocketFactory createNoneSSLSocketFactory() {
        return createSSLSocketFactory(createTrustNoneManager());
    }

    public static SSLSocketFactory getSSLSocketFactory() {
        return getSSLSocketFactory(sContext);
    }

    /**
     * 信任指定的证书的工厂
     * @param context
     * @return
     */
    public static SSLSocketFactory getSSLSocketFactory(Context context) {
        if (context == null) {
            throw new NullPointerException("context == null, please call setContext() at first.");
        }
        try {
            CustomTrustManager manager = new CustomTrustManager(context);
            manager.setServerCertVerifyEnabled(isServerCertVerifyEnabled());
            return createSSLSocketFactory(manager);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "createSSLSocketFactory: NoSuchAlgorithmException", e);
        } catch (IOException e) {
            Log.e(TAG, "createSSLSocketFactory: IOException", e);
        } catch (Exception e) {
            Log.e(TAG, "createSSLSocketFactory: ", e);
        }
        Log.w(TAG, "getSSLSocketFactory: create None SSLSocketFactory");
        return createNoneSSLSocketFactory();
    }

    /**
     * 获取本地自定义证书
     * @param context
     * @return
     */
    public static X509TrustManager getLocalCustomManager(Context context) {
        if (context == null) {
            return null;
        }
        try {
            X509Certificate rootCert = generateCertFromAssets(context,"root_ca.crt");
            // 设置算法
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            // 设置类型
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            // 不加载外部证书
            keyStore.load(null);
            // 加载内置的根证书到keyStore中
            if (rootCert != null) {
                Log.i(TAG, "getLocalCustomManager: add root_ca.crt");
                keyStore.setCertificateEntry("root_ca", rootCert);
            }
            // 还可以添加其他证书
            X509Certificate rootCert2 = generateCertFromAssets(context,"root_ca2.crt");
            if (rootCert2 != null) {
                Log.i(TAG, "getLocalCustomManager: add root_ca2.crt");
                keyStore.setCertificateEntry("root_ca2", rootCert2);
            }
            factory.init(keyStore);
            return getFirstX509TrustManager(factory);
        } catch (CertificateException e) {
            Log.w(TAG, "getLocalCustomManager CertificateException: " + e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            Log.w(TAG, "getLocalCustomManager NoSuchAlgorithmException: " + e);
            e.printStackTrace();
        } catch (KeyStoreException e) {
            Log.w(TAG, "getLocalCustomManager KeyStoreException: " + e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.w(TAG, "getLocalCustomManager IOException: " + e);
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获得系统默认的 X509TrustManager
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     */
    public static X509TrustManager getDefaultManager() throws KeyStoreException, NoSuchAlgorithmException {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init((KeyStore) null);
        return getFirstX509TrustManager(factory);
    }


    /**
     * 获取Assets中的内置证书
     * @param context
     * @param filename
     * @return
     */
    private static X509Certificate generateCertFromAssets(Context context, String filename) {
        try {
            InputStream inputStream = context.getAssets().open(filename);
            // 生成X509证书
            X509Certificate cert = generateX509Cert(inputStream);
            return cert;
        } catch (CertificateException e) {
            Log.w(TAG, "generateCertFromAssets Failed: " + e + ", filename: " + filename);
            e.printStackTrace();
        } catch (IOException e) {
            Log.w(TAG, "generateCertFromAssets Failed: " + e + ", filename: " + filename);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取输入流中的内置X509证书
     * @param inputStream  可以使assets里面的输入流,或者FileInputStream
     * @return
     */
    private static X509Certificate generateX509Cert(InputStream inputStream) throws CertificateException {
        // X509方案
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        // 生成X509证书
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(inputStream);
        return cert;
    }

    public static X509TrustManager getFirstX509TrustManager(TrustManagerFactory trustManagerFactory) {
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        return null;
    }

    public static X509TrustManager createTrustNoneManager() {
        X509TrustManager tm = null;
        try {
            tm =   new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                    //do nothing，接受任意客户端证书
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {

                    throw new CertificateException("Local X509TrustManager Init Failed");

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
        } catch (Exception e) {
            Log.e(TAG, "createTrustNoneManager: ", e);
        }
        return tm;
    }

    /**
     * 获得KeyStore.
     * @param type 证书类型
     * @param keyStorePath
     *            密钥库路径
     * @param password
     *            密码
     * @return 密钥库
     * @throws Exception
     */
    public static KeyStore getKeyStore(String type, String password, String keyStorePath) throws Exception {
        // 实例化密钥库
        KeyStore ks = KeyStore.getInstance(type);
        // 获得密钥库文件流
        FileInputStream is = new FileInputStream(keyStorePath);
        // 加载密钥库
        ks.load(is, password.toCharArray());
        // 关闭密钥库文件流
        is.close();
        return ks;
    }

    /**
     * 获得 JKS KeyStore.
     * @param password
     * @param keyStorePath
     * @return
     */
    public static KeyStore getJksKeyStore(String password, String keyStorePath) throws Exception {
        return getKeyStore("JKS", password, keyStorePath);
    }

    /**
     * 获得SSLSocketFactory剩下文
     * @param password 密钥库密码
     * @param keyStore key 密钥库
     * @param trustStore 信任库
     *            密钥库路径
     * @return SSLContext
     * @throws Exception
     */
    public static SSLContext getSSLContext(String password, KeyStore keyStore, KeyStore trustStore) throws Exception {
        // 实例化密钥库
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        // 初始化密钥工厂
        keyManagerFactory.init(keyStore, password.toCharArray());

        // 实例化信任库
        TrustManagerFactory trustManagerFactory = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());

        // 初始化信任库
        trustManagerFactory.init(trustStore);
        // 实例化SSL上下文
        SSLContext ctx = SSLContext.getInstance("TLS");
        // 初始化SSL上下文
        ctx.init(keyManagerFactory.getKeyManagers(),
                trustManagerFactory.getTrustManagers(), null);
        // SSLContext, 可获得SSLSocketFactory
        return ctx;
    }

    /**
     * 获得SSLSocketFactory.
     * @param password
     *            密码
     * @param keyStorePath
     *            密钥库路径
     * @param trustStorePath
     *            信任库路径
     * @return SSLSocketFactory
     * @throws Exception
     */
    public static SSLContext getSSLContext(String password, String keyStorePath, String trustStorePath) throws Exception {
        // 获得密钥库
        KeyStore keyStore = getJksKeyStore(password, keyStorePath);
        // 获得信任库
        KeyStore trustStore = getJksKeyStore(password, trustStorePath);
        return getSSLContext(password, keyStore, trustStore);
    }

    public static SSLSocketFactory getSSLSocketFactory(String password, String keyStorePath, String trustStorePath) throws Exception {
        // 声明SSL上下文
        SSLContext sslContext = null;
        try {
            sslContext = getSSLContext(password, keyStorePath, trustStorePath);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            Log.e(TAG, "getSSLSocketFactory: ", e);
        }
        if (sslContext != null) {
            return sslContext.getSocketFactory();
        }
        return null;
    }

    /**
     * 初始化HttpsURLConnection.
     * @param password
     *            密码
     * @param keyStorePath
     *            密钥库路径
     * @param trustStorePath
     *            信任库路径
     * @throws Exception
     */
    public static void initHttpsURLConnection(String password, String keyStorePath, String trustStorePath) throws Exception {
        // 声明SSL上下文
        SSLContext sslContext = null;
        // 实例化主机名验证接口
        SSLSocketFactory factory = getSSLSocketFactory(password, keyStorePath, trustStorePath);
        if (factory != null) {
            HttpsURLConnection.setDefaultSSLSocketFactory(factory);
        }
        // 实例化主机名验证接口
        HostnameVerifier hnv = new NoneHostnameVerifier();
        HttpsURLConnection.setDefaultHostnameVerifier(hnv);
    }
}
