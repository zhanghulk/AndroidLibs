package com.example.aidlservice;

import com.example.aidldemo.aidl.ServiceAidl;
import com.example.aidldemo.aidl.ServiceAidlCallback;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class MyAIDLService extends Service {
	private static final String TAG = "MyAIDLService";
	private ServiceAidlCallback callback;

	private void log(String str) {
		Log.i(TAG, "------ " + str + "------");
	}

	@Override
	public void onCreate() {
		log("service create");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    log("service onStartCommand action: " + intent.getAction() + ", startId=" + startId);
	    return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		log("service start id=" + startId);
	}

	@Override
	public IBinder onBind(Intent t) {
		String key = t.getExtras().getString("key");
		log("service on bind  " + key);
		return mBinder;
	}

	@Override
	public void onDestroy() {
		Log.w(TAG, "service on destroy");
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.w(TAG, "service on unbind");
		return super.onUnbind(intent);
	}

	public void onRebind(Intent intent) {
		log("service on rebind");
		super.onRebind(intent);
	}

	private final ServiceAidl.Stub mBinder = new ServiceAidl.Stub() {
		@Override
		public void invokCallBack() throws RemoteException {
			//execute the function in activity
			String action = " The callback called in ServiceAidl.Stub";
			callback.performAction(action);
		}

		/**
		 * execute in onServiceConnected, the callback come from MyAIDLActivity
		 */
		@Override
		public void registerTestCall(ServiceAidlCallback cb) throws RemoteException {
			callback = cb;
		}

	};
}