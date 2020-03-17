package lib.cache.bitmap.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import lib.cache.bitmap.utils.ImageUtils;
import lib.cache.bitmap.utils.NetUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.PorterDuff.Mode;
import android.text.TextUtils;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation.AnimationListener;

public class BitmapBase {
	
	private static final String TAG = "BitmapBase";

	public static final int FIGURE_NO_FLAG = -1;
	public static final int FIGURE_ROUNDED_FLAG = 1;
	public static final int FIGURE_ROUND_RECT_FLAG = 2;
	public static final int FIGURE_OVAL_FLAG = 3;
	public static final int FIGURE_TRIANGLE_FLAG = 4;
	public static final int FIGURE_PENTAGON_FLAG = 5;
	public static final int FIGURE_DRAW_PATH_FLAG = 6;
	
	public Bitmap downloadBitmap(Context context, String url) {
		if(TextUtils.isEmpty(url)) return null;
		Bitmap bitmap = null;
		try {
			bitmap = NetUtils.getNetBitmap(context, url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (bitmap == null) {
			Log.e(TAG, "downloadBitmap: bitmap is null url= " + url);
		}
		return bitmap;
	}
	
	public Bitmap loadLocalBitmap(String filePath) {
		if(TextUtils.isEmpty(filePath)) return null;
		try {
			return ImageUtils.getBitmap(filePath, 0, 0);
		} catch (Exception e) {
			Log.w(TAG, "load failed filePath: " + filePath + ", e: " + e);
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getBitmap(Context context, int resId) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			options.inPurgeable = true;
			options.inInputShareable = true;
			InputStream is = context.getResources().openRawResource(resId);
			Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
			return bitmap;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap drawFigureBitmap(Bitmap input, int figureFlag) {
		if (input == null)
			return null;
		Bitmap output = null;
		final Bitmap bitmap = input;
		switch (figureFlag) {
		case FIGURE_ROUNDED_FLAG:
			output = ImageUtils.drawRoundedBitmap(bitmap);
			break;
		case FIGURE_ROUND_RECT_FLAG:
			output = ImageUtils.drawRoundRectBitmap(bitmap, 80);
			break;
		case FIGURE_OVAL_FLAG:
			output = ImageUtils.drawOvalBitmap(bitmap);
			break;
		case FIGURE_TRIANGLE_FLAG:
			output = ImageUtils.drawEquilateralTriangleBitmap(bitmap, 0);
			break;
		case FIGURE_PENTAGON_FLAG:
			int min = bitmap.getWidth() > bitmap.getHeight() ? bitmap.getHeight() : bitmap.getWidth();
			output = ImageUtils.drawPentagonBitmap(bitmap, min / 2, 0);
			break;
		case FIGURE_DRAW_PATH_FLAG:
			int min1 = bitmap.getWidth() > bitmap.getHeight() ? bitmap.getHeight() : bitmap.getWidth();
			output = ImageUtils.drawPathBitmap(bitmap, 6, min1 / 2, 0);
			break;

		default:
			Log.w(TAG, "drawFigureBitmap unknown flag: " + figureFlag);
			output = input;
			break;
		}
		return output;
	}

	public static Bitmap getRoundedBitmap(Bitmap bitmap) {
		if (bitmap == null) return null;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}
		try {
			Bitmap output = Bitmap
					.createBitmap(width, height, Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			final int color = 0xff424242;
			final Paint paint = new Paint();
			final Rect src = new Rect((int) left, (int) top, (int) right,
					(int) bottom);
			final Rect dst = new Rect((int) dst_left, (int) dst_top,
					(int) dst_right, (int) dst_bottom);
			final RectF rectF = new RectF(dst);
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			canvas.drawBitmap(bitmap, src, dst, paint);
			return output;
		} catch (Exception e) {
			Log.e(TAG, "getRoundedBitmap Exception: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	public static Bitmap decodeBitmap(Bitmap bitmap, Options newOpts) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		Bitmap bmp = BitmapFactory.decodeStream(isBm, null, newOpts);
		return bmp;
	}
	
	public Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
		if(bitmap == null) return null;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.reset();
		float scaleWidht = ((float) w / (float) width);
		float scaleHeight = ((float) h / (float) height);
		matrix.postScale(scaleWidht, scaleHeight);
		Bitmap newbmp = null;
		int count = 0;
		try {
			newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
			count++;
		} catch (OutOfMemoryError e) {
	        while(newbmp == null && count < 2) {
	            newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,matrix, true);
	        }
	    }
		return newbmp;
	}
	
	/**
	 * change image size: width and height
	 * 
	 * @param options
	 * @return
	 */
	private static int getSimpleSize(Options options, int width, int height) {
		int widthRatio = (int) FloatMath.ceil(options.outWidth / (float) width);
		int heightRatio = (int) FloatMath.ceil(options.outHeight
				/ (float) height);
		int inSampleSize = 1;
		if (widthRatio > 1 && heightRatio > 1) {
			if (widthRatio > heightRatio) {
				inSampleSize = widthRatio;
			} else {
				inSampleSize = heightRatio;
			}
		}
		return inSampleSize;
	}

	public static Bitmap getResizeBitmap(String srcPath, int width, int height) {
		try {
			BitmapFactory.Options newOpts = new BitmapFactory.Options();
			// start parse image only width and height, set
			// options.inJustDecodeBounds to true
			newOpts.inJustDecodeBounds = true;
			Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
			newOpts.inJustDecodeBounds = false;
			newOpts.inSampleSize = getSimpleSize(newOpts, width, height);
			// restart parse image all info, set options.inJustDecodeBounds as
			// false
			bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
			return bitmap;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;
		Bitmap bmp = decodeBitmap(bitmap, newOpts);
		newOpts.inJustDecodeBounds = false;
		newOpts.inSampleSize = getSimpleSize(newOpts, width, height);
		bmp = decodeBitmap(bitmap, newOpts);
		return bmp;
	}

	public static void setAlphaAnimation(View v, int time,
			AnimationListener listener) {
		View view = v;
		if (view != null) {
			AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
			alpha.setDuration(time);
			if (listener != null) {
				alpha.setAnimationListener(listener);
			}
			view.startAnimation(alpha);
			view = null;
		}
	}
}
