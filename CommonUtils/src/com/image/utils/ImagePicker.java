
package com.image.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.common.utils.CommonFileUtil;

/**
 * pick image from system
 * @author hao
 *
 */
public class ImagePicker {
	
	// REQUESTCODE
	public static final int PICK_FTOM_GALLERY_URI_REQUEST_CODE = 8010;
	public static final int PICK_FTOM_GALLERY_DATA_REQUEST_CODE = 8011;
	public static final int PICK_FTOM_CAMEAR_URI_REQUEST_CODE = 8020;
	public static final int PICK_FTOM_CAMEAR_DATA_REQUEST_CODE = 8021;
	public static final int CROP_IMAGE_FTOM_URI_REQUEST_CODE = 8030;
	public static final int CORP_IMAGE_FTOM_DATA_REQUEST_CODE = 8040;

	/**120kb*/
	public static final int IMAGE_MAX_LENGTH = 1024 * 120;
	public static final int IMAGE_MAX_OUTPUT_XY = 400;

	private static final String TAG = "ImagePicker";
	private static final String PREF_CORP_OUTPUT_XY = "pref_corp_output_xy";
	
	protected Activity mActivity;
	protected String mImagePath;
	protected String mImageName;// 不设置文件名就用日期, 避免重复覆盖文件
	protected File mTmpImagePath;//打开相机或者相册前创建的临时文件路径
	protected Uri mTmpImageUri;//打开相机或者相册前需要传入的uri
	protected CallBack mCallBack;

	protected int outputX = IMAGE_MAX_OUTPUT_XY;
	protected int outputY = IMAGE_MAX_OUTPUT_XY;
	protected int imageMaxLength = IMAGE_MAX_LENGTH;

	boolean isbig = true;

	//

	public ImagePicker(Activity activity, CallBack callBack) {
		mActivity = activity;
		mCallBack = callBack;
		outputX = getPrefCorpOutputXY(mActivity);
		outputY = outputX;
	}

	public void setImageName(String name) {
		mImageName = name;
	}

	public void setCorpSize(int with, int height) {
		outputX = with;
		outputY = height;
	}

	public void setCorpOutputX(int with) {
		outputX = with;
	}
	
	public void setCorpOutputY(int height) {
		outputY = height;
	}
	
	public void setImageMaxLength(int length) {
		this.imageMaxLength = length;
	}


	public int getImageMaxLength() {
		return imageMaxLength;
	}

	public void setBigImageMode(boolean isbig) {
		this.isbig = isbig;
	}

	/**
	 * 打开相册选择照片
	 */
	public void pickFromPhotos() {
		log("打开相册选择照片");

		// 方式1，可通过关联URI，截取大图片,,,执行代码会再选择后直接跳转到剪辑界面
		// mTmpImageUri = Uri.fromFile(createIconPathTemp());
		// Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		// intent.setType("image/*");
		// intent.putExtra("crop", "true");
		// intent.putExtra("aspectX", 1);
		// intent.putExtra("aspectY", 1);
		// intent.putExtra("outputX", 800);
		// intent.putExtra("outputY", 800);
		// intent.putExtra("scale", true);
		// intent.putExtra("return-data", false);
		// intent.putExtra(MediaStore.EXTRA_OUTPUT, mTmpImageUri);
		// intent.putExtra("outputFormat",
		// Bitmap.CompressFormat.PNG.toString());
		// intent.putExtra("noFaceDetection", true);
		// mActivity.startActivityForResult(intent,
		// PICK_FTOM_GALLERY_URI_REQUEST_CODE);

		// // 方式2,此方式不能截取过大的图片，可能会OOM
		mTmpImageUri = Uri.fromFile(createPathTemp());
		Intent intent = new Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		mActivity.startActivityForResult(intent,
				PICK_FTOM_GALLERY_DATA_REQUEST_CODE);

	}

	/**
	 * 打开照相机拍照
	 */
	public void pickFromCamera() {
		log("打开照相机拍照");
		mTmpImageUri = Uri.fromFile(createPathTemp());
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mTmpImageUri);
		mActivity.startActivityForResult(intent,
				PICK_FTOM_CAMEAR_URI_REQUEST_CODE);
	}

	private File createPathTemp() {
		mTmpImagePath = new File(CommonFileUtil.getImgTmpPath(), CommonFileUtil.getFileName());
		try {
			mTmpImagePath.createNewFile();
			log("mTmpImagePath 创建成功");
			return mTmpImagePath;
		} catch (IOException e) {
			log("头像文件创建失败 " + e.toString());
		}
		return null;
	}

	// 删除头像临时文件
	private void deleteTempIconPath() {
		if (mTmpImagePath != null && mTmpImagePath.exists()) {
			mTmpImagePath.delete();
			log("mTmpImagePath 删除成功");
			mTmpImageUri = null;
		} else {
			log("mTmpImagePath 不存在");
		}
	}

	/**
	 * 裁剪图片，数据直接绑定到URI中，适合大图
	 */
	public void cropImageByURI(Uri uri, Uri outUri, int requestCode) {
		Log.d("getphoto_cropImageUriByURi", uri.toString());
		log("裁剪图片");
		Intent intent = null;
		intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);// 设置截图宽高的比例
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", outputX); // 设置宽高
		intent.putExtra("outputY", outputY);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true);
		mActivity.startActivityForResult(intent, requestCode);
	}

	/**
	 * 裁剪图片2，数据从Data中返回,适合小图,不在使用本方式
	 */
	@Deprecated
	public void cropImageUriByData(Uri uri) {
		Log.d("getphoto_cropImageUriByData", uri.toString());
		log("裁剪图片2");
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1); // 设置截图宽高的比例
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", outputX); // 设置宽高
		intent.putExtra("outputY", outputY);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true);
		mActivity.startActivityForResult(intent,
				CORP_IMAGE_FTOM_DATA_REQUEST_CODE);
	}

	/***
	 * 此方法需要在生成本对象是activity的onActivityResult生命周期内调用
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		log("requestCode= " + requestCode + ", resultCode= " + resultCode + ", data = " + data);
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case PICK_FTOM_CAMEAR_URI_REQUEST_CODE:// 来源相机--第一次返回未裁剪的图，调用裁剪，cropImageUri获取大图，cropImageUri2获取小图
			log("使用url获取图片");
			if (mTmpImageUri != null) {
				cropImageByURI(mTmpImageUri, mTmpImageUri, CROP_IMAGE_FTOM_URI_REQUEST_CODE);
			}
			break;
		case PICK_FTOM_CAMEAR_DATA_REQUEST_CODE:
			if (data != null) {
				cropImageUriByData(data.getData());
			}
			break;
		case PICK_FTOM_GALLERY_DATA_REQUEST_CODE:
			log("有相册直接获取剪切后的图片");

			if (isbig) {
				// mTmpImageUri = data.getData();
				cropImageByURI(data.getData(), mTmpImageUri, CROP_IMAGE_FTOM_URI_REQUEST_CODE);
			} else {
				cropImageUriByData(data.getData());
			}

			break;
		case CROP_IMAGE_FTOM_URI_REQUEST_CODE:// 剪辑后
			log("剪辑后，使用url获取图片并设置");
			if (mTmpImageUri != null) {
				handleUriBitmap(mTmpImageUri);
			}
			break;
		case CORP_IMAGE_FTOM_DATA_REQUEST_CODE:// 已经裁剪好的图片
			log("剪辑后，使用intent--DATA获取图片并设置");
			if (data != null) {
				log("从相册选择的图片" + data.toString());
				handleImageResult(data);
			}
			break;
		case PICK_FTOM_GALLERY_URI_REQUEST_CODE:

			if (mTmpImageUri != null) {
				if (data != null) {
					log("从相册选择的图片" + mTmpImageUri.toString());
					handleUriBitmap(mTmpImageUri);
				}
			}
			break;

		}
	}

	private void handleUriBitmap(Uri imageUri) {// 文件的路径
		if (imageUri == null) {
			return;
		}
		Bitmap bitmap = decodeUriAsBitmap(imageUri);
		if (bitmap != null) {
			Bitmap dstBmp = handleBitmap(bitmap);
			mCallBack.onReturnBitmap(true, dstBmp, imageUri, mImagePath);
		}
	}

	/**
	 * 保存裁剪之后的图片数据
	 * 
	 * @param picdata
	 */
	private void handleImageResult(Intent picdata) {
		Bundle extras = picdata.getExtras();
		if (extras != null) {
			Bitmap src = extras.getParcelable("data");
			Bitmap dstBmp = handleBitmap(src);
			Uri uri = Uri.fromFile(new File(mImagePath));
			mCallBack.onReturnBitmap(true, dstBmp, uri, mImagePath);
		}
	}

	private Bitmap decodeUriAsBitmap(Uri uri) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(mActivity.getContentResolver()
					.openInputStream(uri));
			log("解析Bitmap图片成功");
		} catch (FileNotFoundException e) {
			log("找不到文件 " + e.toString());
		}
		return bitmap;
	}

	/**
	 * 对获取的 处理获取的图片进行压缩保存，删除缓存图片 
	 * 
	 * @param drawable
	 * @param bm
	 */
	private Bitmap handleBitmap(Bitmap bmp) {
		log("缓存图标");
		if (bmp == null) {
			return null;
		}
		mImagePath = getImageFilePath(mImageName);
		Bitmap bm = ImageUtils.changeBitmapConfigAsRGB565(bmp);
		Bitmap dstBmp = bm;
		//save image:
		try {
			int fileSize = CommonFileUtil.getFileSize(mTmpImagePath);
			int quality = 100;
			int bmpLength = ImageUtils.getBitmapBytes(bm, null);
			if (bmpLength > imageMaxLength) {
				//quality = (100 * imageMaxLength) / bmpLength;
				Log.i(TAG, "The bitmap is bigger than max length, length = " + bmpLength
						+ ", max Length=" + imageMaxLength);
			}
			dstBmp = ImageUtils.compress(bm, imageMaxLength, mImagePath, Bitmap.CompressFormat.JPEG);
			//boolean success = ImageUtils.saveBitmap(dstBmp, mImagePath, quality, Bitmap.CompressFormat.JPEG, false);
			//if (success) {
				fileSize = CommonFileUtil.getFileSize(new File(mImagePath));
				//dstBmp = BitmapFactory.decodeFile(mImagePath);
			///}
			
			Log.i(TAG, "Save bitmap success:" + ", quality= " + quality
					+ ", length=" + fileSize / 1024 + "kb" + "\n mTmpImagePath: " + mTmpImagePath + ", mImagePath: " + mImagePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		deleteTempIconPath();
		return dstBmp;
	}

	/**
	 * 没有传文件名, 就用日期, 避免重复覆盖文件
	 * 
	 * @param imageName
	 * @return
	 */
	private String getImageFilePath(String imageName) {
		if (TextUtils.isEmpty(imageName)) {
			imageName = CommonFileUtil.getFileName();
		}
		String imgPath = CommonFileUtil.getImageDir();
		File file = new File(imgPath, imageName);
		return file.getAbsolutePath();
	}

	public static int getPrefCorpOutputXY(Context c) {
		return PreferenceManager.getDefaultSharedPreferences(c).getInt(PREF_CORP_OUTPUT_XY, IMAGE_MAX_OUTPUT_XY);
	}

	public static void setPrefCorpOutputXY(Context c, int outputXY) {
		PreferenceManager.getDefaultSharedPreferences(c).edit().putInt(PREF_CORP_OUTPUT_XY, outputXY).apply();;
	}
	
	private void log(String msg) {
		Log.i(TAG, msg);
	}

	public static interface CallBack {
		// void onUploadRetrun(boolean success, String uri);

		/**
		 * 如果success为ture则表明返回截图成功，false则bitmap为空
		 * 
		 * @param isSuccess
		 * @param bitmap
		 */
		void onReturnBitmap(boolean isSuccess, Bitmap bitmap, Uri uri, String filePath);
	}
}
