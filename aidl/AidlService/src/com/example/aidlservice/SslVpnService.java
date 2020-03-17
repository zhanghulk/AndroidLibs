package com.example.aidlservice;

import com.example.aidldemo.aidl.ISslVpn;
import com.example.aidldemo.aidl.ISslVpnCallback;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class SslVpnService extends Service {
	private static final String TAG = "SslVpnService";
	ISslVpnCallback mCallback;
	static int appListVersionCode = 0;

	private void log(String str) {
		Log.d(TAG, "------ " + str + "------");
	}

	@Override
	public void onCreate() {
		log("service create");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		log("service start id=" + startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		log("on bind action: " + intent.getAction());
		return mBinder;
	}

	@Override
	public void onDestroy() {
		log("service on destroy");
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		log("service on unbind");
		return super.onUnbind(intent);
	}

	public void onRebind(Intent intent) {
		log("service on rebind");
		super.onRebind(intent);
	}

	private final ISslVpn.Stub mBinder = new ISslVpn.Stub() {
        @Override
        public boolean updateAppList(String sslVpnConfig) throws RemoteException {
            log("sslVpnConfig: " + sslVpnConfig);
            appListVersionCode++;
            if(appListVersionCode % 3 == 0) {
                mCallback.reportAppSslVpnError(appListVersionCode, "Received error sslVpnConfig: " + sslVpnConfig);
            } else {
                mCallback.applyAppListSslVpnCompeleted(appListVersionCode, "Received sslVpnConfig: " + sslVpnConfig);
            }
            return true;
        }

        @Override
        public void registerCallback(ISslVpnCallback cb) throws RemoteException {
            mCallback = cb;
        }
    };
}