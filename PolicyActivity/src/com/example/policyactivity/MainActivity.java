package com.example.policyactivity;

import java.text.NumberFormat;

import android.os.Bundle;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	DevicePolicyManager dpm;
	ComponentName admin;
	//KeyguardManager km;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		//km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		admin = new ComponentName(this, AdminReceiver.class);
		if(dpm.isAdminActive(admin)) {
			Log.i(TAG, "---------resetPassword=========================");
			dpm.resetPassword("", 0);
		} else {
			activeManager();
		}
		Button btn_removeActiveAdmin = (Button) findViewById(R.id.btn_removeActiveAdmin);
		btn_removeActiveAdmin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dpm.removeActiveAdmin(admin);
			}
		});
		
		findViewById(R.id.btn_removeActiveAdmin).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				activeManager();
			}
		});
		findViewById(R.id.btn_removeLockPwd).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				sesetPwd();
			}
		});
		if(!dpm.isAdminActive(admin)) {
			activeManager();
		} else {
			int quality = dpm.getPasswordQuality(admin);
			Log.i(TAG, "111  quality = " + Integer.toHexString(quality));
			dpm.setPasswordQuality(admin, DevicePolicyManager.PASSWORD_QUALITY_SOMETHING);
			Log.i(TAG, "2222 quality = " + Integer.toHexString(quality));
			setTimeout();
		}
	}
	
	private void sesetPwd() {
		if(dpm.isAdminActive(admin)) {
			Log.i(TAG, "---------resetPassword=========================");
			dpm.resetPassword("", 0);
		}
	}

	private void setNewPwd() {
		boolean isActivePasswordSufficient = dpm.isActivePasswordSufficient();
		if(!isActivePasswordSufficient) {
			Intent it = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
			startActivity(it);
		}
		Log.i(TAG, "isActivePasswordSufficient: " + isActivePasswordSufficient);
	}
	
	private void setTimeout() {
		//6 * 60 * 1000 :  1 minute
		//10 * 60 * 1000  5m
		//8 * 60 * 1000   2m
		//15 * 60 * 1000  10m
		//35 * 60 * 1000  30 m
		long timeout = 35 * 60 * 1000;
		dpm.setMaximumTimeToLock(admin, timeout);
		dpm.setPasswordExpirationTimeout(admin, 5 * 60 * 1000);
		Log.i(TAG, "MaximumTimeToLock= " + dpm.getMaximumTimeToLock(admin) + ",PasswordExpirationTimeout= " + dpm.getPasswordExpirationTimeout(admin));
	}

	/**
	 * 激活设备管理权限 成功执行激活时，DeviceAdminReceiver中的 onEnabled 会响应
	 */
	private void activeManager() {
		// 启动设备管理(隐式Intent) - 在AndroidManifest.xml中设定相应过滤器
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);

		// 权限列表
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);

		// 描述(additional explanation)
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
				"------ 其他描述 ------");

		startActivityForResult(intent, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
