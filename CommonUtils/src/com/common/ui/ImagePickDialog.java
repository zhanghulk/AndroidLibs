package com.common.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.common.utils.R;
import com.image.utils.ImagePicker;

/**
 * pick image from system
 * 
 * @author hao
 * 
 */
public class ImagePickDialog extends ImagePicker{

	public ImagePickDialog(Activity activity, CallBack callBack) {
		super(activity, callBack);
	}

	private Button bottomOpenPhtots;
	private Button bottomOpenCamera;
	private Button bottomHideMenu;
	private View view;

	/**
	 * 选择菜单
	 */
	@SuppressWarnings("deprecation")
	public void showMenuWindow() {
		AlertDialog.Builder builder = new Builder(mActivity);
		final AlertDialog dialog = builder.create();
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
		view = View.inflate(mActivity, R.layout.bottom_dialog_layout, null);
		dialog.setView(view, 0, 0, 0, 0);
		bottomOpenPhtots = (Button) view
				.findViewById(R.id.df_myself_personalInfo_openPhtots);
		bottomOpenCamera = (Button) view
				.findViewById(R.id.df_myself_personalInfo_openCamera);
		bottomHideMenu = (Button) view
				.findViewById(R.id.df_myself_personalInfo_hideMenu);
		bottomOpenPhtots.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				pickFromPhotos();
			}
		});
		bottomOpenCamera.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				pickFromCamera();
			}
		});
		bottomHideMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
		Window window = dialog.getWindow();
		window.setGravity(Gravity.BOTTOM);
		window.setWindowAnimations(R.style.bottom_dialog);
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = 0.8f;
		WindowManager windowManager = mActivity.getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		lp.width = (int) (display.getWidth()); // 设置宽度
		window.setAttributes(lp);
	}
}
