package com.example.aidldemo;

import com.example.aidldemo.aidl.ISslVpn;
import com.example.aidldemo.aidl.ISslVpnCallback;
import com.example.aidldemo.aidl.ServiceAidl;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;


public class SslVpnActivity extends Activity {

    private static final String TAG = "SslVpnActivity";
    //The action to start sslvpn service 
    private static final String ACTION_SSL_VPN = "com.example.demo.action.SSL_VPN";
    TextView mMsgTv = null;
    ISslVpn mSslVpnService;
    private ServiceConnection mConnection = new ServiceConnection() {
        private ISslVpnCallback.Stub mSslVpnCallback = new ISslVpnCallback.Stub() {
            
            @Override
            public void reportAppSslVpnError(int errorCode, String remark) throws RemoteException {
                CharSequence text = "errorCode:" + errorCode + ", remark: " + remark;
                mMsgTv.setTextColor(Color.RED);
                mMsgTv.setText(text);
            }
            
            @Override
            public void applyAppListSslVpnCompeleted(int resultCode, String remark) throws RemoteException {
                CharSequence text = "resultCode:" + resultCode + ", remark: " + remark;
                mMsgTv.setTextColor(Color.BLACK);
                mMsgTv.setText(text);
            }
        };

        public void onServiceConnected(ComponentName className, IBinder service) {
            mSslVpnService = ISslVpn.Stub.asInterface(service);
            log("Connected service : " + mSslVpnService.getClass().getSimpleName());
            if(mSslVpnService != null) {
                try {
                    mSslVpnService.registerCallback(mSslVpnCallback);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            log("Disconnected service");
            toast("Disconnected service");
            mSslVpnService = null;
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ssl_vpn);
        mMsgTv = (TextView) findViewById(R.id.msg_tv); 
        Intent service = new Intent(ACTION_SSL_VPN);
        bindService(service , mConnection, Context.BIND_AUTO_CREATE);
        findViewById(R.id.send_ssl_vpn_config_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSslVpnService != null) {
                    String sslVpnConfig = "th app list json string !! ";
                    try {
                        mSslVpnService.updateAppList(sslVpnConfig);
                    } catch (RemoteException e) {
                        toast("updateAppList RemoteException: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    toast("The ssl vpn service is Disconnected ");
                }
            }
        });
    }

    private void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
    private void log(String string) {
        Log.i(TAG, string);
    }
}
