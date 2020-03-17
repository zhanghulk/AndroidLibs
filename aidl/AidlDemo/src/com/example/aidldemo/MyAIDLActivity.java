package com.example.aidldemo;

import com.example.aidldemo.aidl.ServiceAidlCallback;
import com.example.aidldemo.aidl.ServiceAidl;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MyAIDLActivity extends Activity {
	private static final String TAG = "MyAIDLActivity";
    protected static final String ACTION_AIDL = "com.example.demo.action.MY_AIDL_SERVICE";
	private Button btnBind;
	private Button btnUnbind, btnStart, btnStop;
	private Button btnCallBack;
	TextView action_tv;

	private ServiceAidlCallback mCallback = new ServiceAidlCallback.Stub() {
		public void performAction(String action) throws RemoteException {
			Toast.makeText(MyAIDLActivity.this, "This toast is called from MyAIDLService: " + action, Toast.LENGTH_LONG).show();
			action_tv.setText(action);
			action_tv.setTextColor(Color.BLUE);
		}
	};

	ServiceAidl mService;
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = ServiceAidl.Stub.asInterface(service);
			Log.i(TAG, "onServiceConnected: " + mService.getClass().getName() + ", className:"  + mService);
			try {
				mService.registerTestCall(mCallback);
			} catch (RemoteException e) {

			}
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.w(TAG, "onServiceDisconnected className: " + className);
			mService = null;
		}
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_main);
		action_tv = (TextView) findViewById(R.id.action_tv);
		btnBind = (Button) findViewById(R.id.bind_service_btn);
		btnUnbind = (Button) findViewById(R.id.unbind_service_btn);
		btnStart = (Button) findViewById(R.id.start_service_btn);
		btnStop = (Button) findViewById(R.id.stop_service_btn);
		btnCallBack = (Button) findViewById(R.id.callback_toast_btn);
		btnBind.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Bundle args = new Bundle();
				args.putString("key", "This String come from MyAIDLActivity");
				Intent intent = new Intent(ACTION_AIDL);
				intent.putExtras(args);
				boolean bound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
				action_tv.setText("The service bound= " + bound);
				action_tv.setTextColor(bound ? Color.GREEN : Color.RED);
			}
		});
		
		btnUnbind.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    unbindService(mConnection);
                    action_tv.setText("The service is unbinded !! ");
                    action_tv.setTextColor(Color.RED);
                } catch (Exception e) {
                    e.printStackTrace();
                    String msg = e.getMessage();
                    Toast.makeText(MyAIDLActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
		
		btnCallBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mService == null) {
					Toast.makeText(MyAIDLActivity.this, "Maybe the Service is not binded !! ", Toast.LENGTH_SHORT).show();
					return;
				}
				try {
					mService.invokCallBack();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		btnStop.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ACTION_AIDL);
                stopService(intent);
                action_tv.setText("stopService");
            }
        });
        btnStart.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ACTION_AIDL);
                startService(intent);
                action_tv.setText("startService");
            }
        });
		findViewById(R.id.ssl_vpn_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                 startActivity(new Intent(getApplicationContext(), SslVpnActivity.class));
            }
        });
	}
}