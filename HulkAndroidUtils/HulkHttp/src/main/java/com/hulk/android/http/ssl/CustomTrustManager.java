
package com.hulk.android.http.ssl;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import com.hulk.android.log.Log;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.X509TrustManager;

/**
 * 自定义包含内置证书的TrustManager
 */
public class CustomTrustManager implements X509TrustManager {
    public static final String TAG = "CustomTrustManager";
    public static final String ACTION_CONFIRM_CERT = "com.hulk.intent.action.CONFIRM_CERT";
    public static final String KEY_USER_TRUST_CERT_OR_NOT = "user_trust_cert_or_not";

    /** 系统自带的证书管理器*/
    private X509TrustManager mDefaultTrustManager;
    /**内置证书和用户信任的自签名证书管理器*/
    private X509TrustManager mCustomTrustManager;
    
    /** 所有证书列表 */
    private X509Certificate[] mAcceptedIssuers;

    private Context mContext;
    
    private static Object mLockObject = new Object();
    
    public static Object mWaitUserOpertion = new Object();

    private static boolean isTrustOperation = false;

    /**
     * 是否需要验证服务器证书,默认需要
     */
    private boolean mServerCertVerifyEnabled = true;

    /**
     * 构造
     */
    public CustomTrustManager(Context context) throws KeyStoreException, NoSuchAlgorithmException, IOException {
        mContext = context;
        initTrustManager(false);
    }

    /**
     * 构造自定义证书信任管理器
     * @param context
     * @param serverCertVerifyEnabled 是否需要验证服务器证书
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     */
    public CustomTrustManager(Context context, boolean serverCertVerifyEnabled) throws KeyStoreException, NoSuchAlgorithmException, IOException {
        mContext = context;
        initTrustManager(false);
        mServerCertVerifyEnabled = serverCertVerifyEnabled;
    }

    /**
     * 设置是否需要验证服务器证书,默认需要
     * @param serverCertVerifyEnabled
     */
    public void setServerCertVerifyEnabled(boolean serverCertVerifyEnabled) {
        this.mServerCertVerifyEnabled = serverCertVerifyEnabled;
    }

    /**
     * 是否需要验证服务器证书,默认需要
     * @return
     */
    public boolean isServerCertVerifyEnabled() {
        return mServerCertVerifyEnabled;
    }
    
    private void initTrustManager(boolean update) throws KeyStoreException, NoSuchAlgorithmException, IOException {
        mDefaultTrustManager = SSLUtils.getDefaultManager();
        initCustomTrustManager(update);
        if (mDefaultTrustManager == null || mCustomTrustManager == null) {
            throw new IOException("Couldn't load X509TrustManager");
        }
        ArrayList<X509Certificate> acceptedIssuersList = new ArrayList<X509Certificate>();
        addToAccepted(acceptedIssuersList, mCustomTrustManager);
        addToAccepted(acceptedIssuersList, mDefaultTrustManager);
        mAcceptedIssuers = acceptedIssuersList.toArray(new X509Certificate[acceptedIssuersList.size()]);
    }

    /**
     * @param update 是否需要更新trustmanager，在用户手动信任自签名证书后更新
     */
    private void initCustomTrustManager(boolean update) {
        if (mCustomTrustManager == null || update) {
            mCustomTrustManager = SSLUtils.getLocalCustomManager(mContext);
        }
    }
    
    /**
     * 把证书添加到可接受列表中
     */
    private void addToAccepted(List<X509Certificate> destCertList, X509TrustManager srcTrustManager) {
        for (X509Certificate cert : srcTrustManager.getAcceptedIssuers()) {
            destCertList.add(cert);
        }
    }

    /**
     * 检查TrustSpace本地的自签名证书（包含TrustSpace内置的根证书和用户信任过的自签名证书）
     * @return true if server peer ax509certificate is trusted by local self-cert.
     */
    private boolean checkCustomCertTrusted(X509Certificate[] ax509certificate, String s) {
        try {
            mCustomTrustManager.checkServerTrusted(ax509certificate, s);
            return true;
        } catch (CertificateException e) {
            Log.e(TAG, "checkCustomCertification: " + e);
            return false;
        }
    }
    
    /**
     * 显示对话框，询问用户是否信任此自签名证书
     */
    private void showCertConfirmActivity(X509Certificate cert) throws ActivityNotFoundException {
        try {
            Log.w(TAG, "show CertConfirmActivity");
            Intent intent = new Intent(ACTION_CONFIRM_CERT);
            intent.putExtra("certificate", cert);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "showCertConfirmActivity: ", e);
        }
    }
    
    protected void checkServerTrustedByCustomCert(X509Certificate[] certs, String authType) throws CertificateException {
        Log.w(TAG, "Start checkServerTrustedByCustomCert");
        // 优先检查TrustSpace本地内置和保存的自签名证书
        try {
            if (!checkCustomCertTrusted(certs, authType)) {
                Log.w(TAG, "checkServerTrustedByCustomCert: check custom cert failed, check default cert.");
                checkDefaultCertTrusted(certs, authType);
            }
        } catch (CertificateException certException) {
            Log.w(TAG, "checkServerTrustedByCustomCert failed: " + certException);
            // 都检查不通过，则提示用户去信任此证书
            handleCertificateException(certs, certException);
        }
    }

    protected boolean checkDefaultCertTrusted(X509Certificate[] certs,  String authType) throws CertificateException {
        try {
            // 本地证书检查失败，检查系统自带的证书
            Log.w(TAG, "checking defaultTrustManager");
            mDefaultTrustManager.checkServerTrusted(certs, authType);
            return true;
        } catch (CertificateException certException) {
            Log.w(TAG, "checkDefaultCertTrusted certException: " + certException);
            // 都检查不通过，则提示用户去信任此证书
            throw certException;
        }
    }

    protected void handleCertificateException(X509Certificate[] certs, CertificateException certException) throws CertificateException {
        try {
            showCertConfirmActivity(certs[0]);
            Log.w(TAG, "waiting user make their selection, just waiting.....");
            synchronized (mWaitUserOpertion) {
                mWaitUserOpertion.wait(2000);
            }
            boolean isTrustOperation = isTrustOperation(mContext);
            Log.d(TAG, "user select trust operation as : " + isTrustOperation);
            if (isTrustOperation) {
                try {
                    Log.w(TAG, "reInit TrustManager");
                    // 用户信任自签名证书后，重新初始化trustmanager
                    initTrustManager(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "user not trust, just throw exeption");
                throw certException;
            }
        } catch (InterruptedException ex) {
            Log.d(TAG, "this should not be happen, but ... " + ex);
            ex.printStackTrace();
            throw certException;
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            throw certException;
        } finally {
            removeTrustOperation(mContext);
        }
    }
    
    @Override
    public synchronized void checkServerTrusted(X509Certificate[] certs, String authType)
            throws CertificateException {
        if (!isServerCertVerifyEnabled()) {
            int certCount = certs != null ? certs.length : -1;
            Log.w(TAG, "checkServerTrusted: ignored for disabled");
            return;
        }
        Log.i(TAG, "checkServerTrusted: certs=" + certs + ", authType=" + authType);
        synchronized (mLockObject) {
            try {
                checkServerTrustedByCustomCert(certs, authType);
            } catch (CertificateException e) {
                Log.w(TAG, "checkServerTrusted: " + e);
                throw e;
            }
        }
    }

    /**
     * 返回所有证书列表
     * @return
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return mAcceptedIssuers;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        // TODO Auto-generated method stub
    }

    public static boolean isTrustOperation(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_USER_TRUST_CERT_OR_NOT, false);
        return isTrustOperation;
    }

    public static void setTrustOperation(Context context, boolean trusted) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(KEY_USER_TRUST_CERT_OR_NOT, trusted).apply();
    }

    public static void removeTrustOperation(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(KEY_USER_TRUST_CERT_OR_NOT).apply();
    }
}
