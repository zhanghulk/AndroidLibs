package lib.cache.bitmap.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import lib.cache.bitmap.impl.BitmapBase;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation.AnimationListener;

public class ImageUtils extends BitmapBase {

	private static final String TAG = "ImageUtils";

	/**
	 * The cycle compression specify meet the specified target size(100kb)
	 * 
	 * @param bitmap
	 * @param target_size
	 *            unit is kb
	 * @return
	 */
	private static Bitmap compress(Bitmap bitmap, int target_size) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		int quality = 100;
		int length = baos.toByteArray().length / 1024;
		// The cycle compression specify meet the specified size
		while (length > target_size) {
			baos.reset();
			quality -= 10;
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
			length = baos.toByteArray().length / 1024;
		}
		if (!bitmap.isRecycled()) {
			bitmap.recycle();
		}
		byte[] data = baos.toByteArray();
		int cp_length = data.length;
		return BitmapFactory.decodeByteArray(data, 0, cp_length);
	}

	/**
	 * 
	 * @param srcPath
	 * @param width
	 * @param height
	 * @param maxSize
	 *            max size kb
	 * @return
	 */
	public static Bitmap getImage(String srcPath, int width, int height,
			int maxSize) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		// 此时返回bm为空,读取图片的宽高属性
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		int inSampleSize = 1;
		if (w > h && w > width) {
			inSampleSize = (int) (newOpts.outWidth / width);
		} else if (w < h && h > height) {
			inSampleSize = (int) (newOpts.outHeight / height);
		}
		if (inSampleSize <= 0)
			inSampleSize = 1;
		newOpts.inSampleSize = inSampleSize;
		// getSimpleSize(newOpts, width, height);
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		// 压缩好比例大小后再进行质量压缩
		return compress(bitmap, maxSize);
	}

	/**
	 * save bitmap 100%
	 * 
	 * @param bitmap
	 * @param destFilePath
	 * @return
	 * @throws IOException
	 */
	public static String saveImage(Bitmap bitmap, String destFilePath)
			throws IOException {
		return saveDestImage(bitmap, destFilePath, 100);
	}

	/**
	 * save bitmap 50%
	 * 
	 * @param bitmap
	 * @return save mUrl
	 * @throws IOException
	 */
	public static String saveImage75(Bitmap bitmap, String destFilePath)
			throws IOException {
		return saveDestImage(bitmap, destFilePath, 75);
	}

	/**
	 * save bitmap 50%
	 * 
	 * @param bitmap
	 * @param destFilePath
	 * @return
	 * @throws IOException
	 */
	public static String saveImage50(Bitmap bitmap, String destFilePath)
			throws IOException {
		return saveDestImage(bitmap, destFilePath, 50);
	}

	/**
	 * save bitmap 25%
	 * 
	 * @param bitmap
	 * @param destFilePath
	 * @return
	 * @throws IOException
	 */
	public static String saveImage25(Bitmap bitmap, String destFilePath)
			throws IOException {
		return saveDestImage(bitmap, destFilePath, 25);
	}

	/**
	 * save bitmap
	 * 
	 * @param bitmap
	 * @param quality
	 *            bitmap quality(1-100)
	 * @param destFileName
	 *            file name you want (jpg or png) in "/slim/iamge/destFileName"
	 * @return
	 * @throws IOException
	 */
	public static String saveImage(Bitmap bitmap, String filePath, int quality
			) throws IOException {
		return saveDestImage(bitmap, filePath, quality, true);
	}

	public static String saveImage(Bitmap bitmap, String destPath, int width,
			int height, int maxSize) throws IOException {
		String srcFile = saveImage(bitmap, destPath);
		Bitmap bmp = getImage(srcFile, width, height, maxSize);
		return saveDestImage(bmp, destPath, 100, false);
	}

	/**
	 * save src file to dest image file
	 * 
	 * @param srcFile
	 * @param quality
	 * @param destFile
	 * @return
	 * @throws IOException
	 */
	public static String saveDestImage(String srcFile, String destFile,
			int quality) throws IOException {
		return saveDestImage(getBitmap(srcFile), destFile, quality, true);
	}

	/**
	 * save bitmap
	 * 
	 * @param bitmap
	 * @param quality
	 *            bitmap quality(1-100)
	 * @param destFile
	 *            : file name you want (jpg or png)
	 * @return
	 * @throws IOException
	 */
	public static String saveDestImage(Bitmap bitmap, String destFile,
			int quality) throws IOException {
		return saveDestImage(bitmap, destFile, quality, false);
	}

	/**
	 * 
	 * @param bitmap
	 * @param destPath
	 * @param quality
	 *            quality(1-100)
	 * @param recycle
	 * @return
	 * @throws IOException
	 */
	public static String saveDestImage(Bitmap bitmap, String destPath,
			int quality, boolean recycle) throws IOException {
		if (TextUtils.isEmpty(destPath)) {
			throw new IOException("dest file path is null ");
		}
		Bitmap bmp = bitmap;
		File file = new File(destPath);
		// if parent of file is not existed, make its parent folder
		File parentFile = file.getParentFile();
		if (!parentFile.exists() && !parentFile.mkdirs()) {
			throw new IOException("mkdirs failed, destPath: " + destPath);
		}

		FileOutputStream out = new FileOutputStream(file);
		Bitmap.CompressFormat formate = Bitmap.CompressFormat.JPEG;
		if (file.getName().contains(".png")) {
			formate = Bitmap.CompressFormat.PNG;
		}
		String path = null;
		if (bmp.compress(formate, quality, out)) {
			out.flush();
			out.close();
			path = file.getPath();
		}
		if (recycle && bmp != null) {
			bmp.recycle();
		}
		return path;
	}

	public static void saveImageToMemo(Context context, String fileName,
			Bitmap bitmap) throws IOException {
		saveImageToMemo(context, fileName, bitmap, 100);
	}

	/**
	 * save image in context
	 * 
	 * @param context
	 * @param fileName
	 * @param bitmap
	 * @param quality
	 * @throws IOException
	 */
	public static void saveImageToMemo(Context context, String fileName,
			Bitmap bitmap, int quality) throws IOException {
		if (bitmap == null)
			return;
		FileOutputStream fos = context.openFileOutput(fileName,
				Context.MODE_PRIVATE);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, quality, stream);
		byte[] bytes = stream.toByteArray();
		fos.write(bytes);
		fos.close();
	}

	/**
	 * 转变图片成自定义分辨率
	 * 
	 * @param fromFile
	 * @param toFile
	 * @param width
	 * @param height
	 * @param quality
	 */
	public static void transImage(String fromFile, String toFile, int width,
			int height, int quality) {
		try {
			Bitmap bitmap = BitmapFactory.decodeFile(fromFile);
			int bitmapWidth = bitmap.getWidth();
			int bitmapHeight = bitmap.getHeight();
			// scaling proportion
			float scaleWidth = (float) width / bitmapWidth;
			float scaleHeight = (float) height / bitmapHeight;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			// resizeBitmap
			Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0,
					bitmapWidth, bitmapHeight, matrix, false);
			// save file
			File myCaptureFile = new File(toFile);
			FileOutputStream out = new FileOutputStream(myCaptureFile);
			if (resizeBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
				out.flush();
				out.close();
			}
			// Release memory resources
			if (!bitmap.isRecycled()) {
				bitmap.recycle();
			}
			if (!resizeBitmap.isRecycled()) {
				resizeBitmap.recycle();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @param mUrl
	 * @return
	 */
	public static Bitmap getBitmap(String pathName) {
		return BitmapFactory.decodeFile(pathName);
	}

	/**
	 * get resized (kb) bitmap
	 * 
	 * @param mUrl
	 * @param size
	 *            unit is kb
	 * @return
	 */
	public static Bitmap getBitmap(String path, int inSampleSize) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		try {
			Options op = new Options();
			op.inSampleSize = inSampleSize;
			return BitmapFactory.decodeFile(path, op);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param mUrl
	 * @param width  >1
	 * @param heigh >1
	 * @return
	 */
	public static Bitmap getBitmap(String path, int width, int heigh) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		try {
			Options op = new Options();
			op.inJustDecodeBounds = true;
			Bitmap bmp = BitmapFactory.decodeFile(path, op);
			int xScale = 1;
			int yScale = 1;
			if(width > 1) {
				xScale = op.outWidth / width;
			}
			if(heigh > 1) {
				yScale = op.outHeight / heigh;
			}
			op.inSampleSize = xScale > yScale ? xScale : yScale;
			op.inJustDecodeBounds = false;
			bmp = BitmapFactory.decodeFile(path, op);
			return bmp;
		} catch (Exception e) {
			Log.e(TAG, "e: " + e + ", path: " + path);
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap shotScreen(Activity activity) {
		View view = activity.getWindow().getDecorView();
		Display display = activity.getWindowManager().getDefaultDisplay();
		view.layout(0, 0, display.getWidth(), display.getHeight());
		view.setDrawingCacheEnabled(true);
		Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache());
		return bmp;
	}

	/**
	 * 获取View中的Bitmap JN
	 * */
	public static Bitmap shotViewScreen(Activity activity, ViewGroup vg) {
		try {
			vg.layout(0, 0, vg.getWidth(), vg.getHeight());
			vg.setDrawingCacheEnabled(true);
			Bitmap bmp = Bitmap.createBitmap(vg.getDrawingCache());
			return bmp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param activity
	 * @return
	 */
	public static Bitmap getRootViewBitmap(View view) {
		return shotViewBitmap(view.getRootView());
	}

	/**
	 * @param activity
	 * @return
	 */
	public static Bitmap shotViewBitmap(View v) {
		v.clearFocus();
		v.setPressed(false);
		Bitmap bmp = null;
		try {
			v.layout(0, 0, v.getWidth(), v.getHeight());
			v.setDrawingCacheEnabled(true);
			v.buildDrawingCache();
			bmp = Bitmap.createBitmap(v.getDrawingCache());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bmp;
	}

	/**
	 * convert view to integral bitmap and return it
	 * 
	 * @param view
	 *            : view or layout
	 * @return
	 */
	public static Bitmap convertBitmap(View view) {
		return convertViewToBitmap(view, view.getWidth(), view.getHeight());
	}

	/**
	 * convert measured view to integral bitmap and return it
	 * 
	 * @param view
	 *            : view or layout
	 * @return
	 */
	public static Bitmap convertMeasureBitmap(View view) {
		view.measure(MeasureSpec.makeMeasureSpec(view.getWidth(),
				MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
				view.getHeight(), MeasureSpec.AT_MOST));
		return convertViewToBitmap(view, view.getMeasuredWidth(),
				view.getMeasuredHeight());
	}

	/**
	 * convert view to bitmap according to with and height
	 * 
	 * @param view
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap convertViewToBitmap(View view, int width, int height) {
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		view.draw(new Canvas(bitmap));
		return bitmap;
	}

	/**
	 * 获得带倒影的图片方法
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
		final int reflectionGap = 4;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);
		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2,
				width, height / 2, matrix, false);
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				(height + height / 2), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bitmap, 0, 0, null);
		Paint deafalutPaint = new Paint();
		// Generated by Foxit PDF Creator © Foxit Software
		// http://www.foxitsoftware.com For evaluation only.
		canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);
		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
				bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
				0x00ffffff, TileMode.CLAMP);
		paint.setShader(shader);
		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);

		return bitmapWithReflection;
	}

	/**
	 * 获取圆形图片， 一较短的一边的1/2为半径
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap drawRoundedBitmap(Bitmap bitmap) {
		if (bitmap == null)
			return null;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPxy;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPxy = width / 2;
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
			roundPxy = height / 2;
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
			int color = 0xff424242;
			final Rect srcRect = new Rect((int) left, (int) top, (int) right,
					(int) bottom);
			final Rect dstRect = new Rect((int) dst_left, (int) dst_top,
					(int) dst_right, (int) dst_bottom);
			Paint paint = new Paint();
			paint.setColor(color);
			return drawRoundRectBitmap(bitmap, paint, srcRect, dstRect,
					roundPxy, roundPxy, Bitmap.Config.ARGB_8888);
		} catch (Exception e) {
			Log.e(TAG, "getRoundedBitmap Exception: " + e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获得圆角矩形图片的方法
	 * 
	 * @param bitmap
	 * @param roundPxy
	 *            xy方向的圆角半径 eg: 50, 80, 100 150...
	 * @return
	 */
	public static Bitmap drawRoundRectBitmap(Bitmap bitmap, float roundPxy) {
		final int color = 0xff424242;
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);
		return drawRoundRectBitmap(bitmap, paint, roundPxy, roundPxy);
	}

	public static Bitmap drawRoundRectBitmap(Bitmap bitmap, Paint paint,
			float roundPx, float roundPy) {
		Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		return drawRoundRectBitmap(bitmap, paint, rect, rect, roundPx, roundPy,
				Bitmap.Config.ARGB_8888);
	}

	public static Bitmap drawRoundRectBitmap(Bitmap bitmap, Paint paint,
			Rect srcRect, Rect dstRect, float roundPx, float roundPy,
			Bitmap.Config bmpConfig) {
		if (bmpConfig == null) {
			bmpConfig = Bitmap.Config.ARGB_8888;
		}
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), bmpConfig);
		Canvas canvas = new Canvas(output);
		paint.setAntiAlias(true);
		if (paint.getColor() <= 0) {
			paint.setColor(0xff424242);
		}
		canvas.drawARGB(0, 0, 0, 0);
		final RectF rectF = new RectF(dstRect);
		canvas.drawRoundRect(rectF, roundPx, roundPy, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, srcRect, dstRect, paint);
		return output;
	}

	public static Bitmap drawRoundRectBitmap(Bitmap bitmap, Rect srcRect,
			Rect dstRect, float roundPx, float roundPy, int paintColor,
			boolean isAntiAlias, Xfermode xfermode, Bitmap.Config bmpConfig) {
		if (bmpConfig == null) {
			bmpConfig = Bitmap.Config.ARGB_8888;
		}
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), bmpConfig);
		Paint paint = new Paint();
		Canvas canvas = new Canvas(output);
		paint.setAntiAlias(isAntiAlias);
		canvas.drawARGB(0, 0, 0, 0);
		if (paintColor <= 0) {
			paintColor = 0xff424242;
		}
		paint.setColor(paintColor);
		final RectF rectF = new RectF(dstRect);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		if (xfermode == null) {
			xfermode = new PorterDuffXfermode(Mode.SRC_IN);
		}
		paint.setXfermode(xfermode);
		canvas.drawBitmap(bitmap, srcRect, dstRect, paint);
		return output;
	}

	/**
	 * 获取椭圆形图片， 长宽边长为椭圆长短轴a,b
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap drawOvalBitmap(Bitmap bitmap) {
		if (bitmap == null)
			return null;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		left = 0;
		top = 0;
		right = width;
		bottom = height;
		width = height;
		dst_left = 0;
		dst_top = 0;
		dst_right = width;
		dst_bottom = height;
		final Rect srcRect = new Rect((int) left, (int) top, (int) right,
				(int) bottom);
		final Rect dstRect = new Rect((int) dst_left, (int) dst_top,
				(int) dst_right, (int) dst_bottom);
		RectF oval = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
		Paint paint = new Paint();
		paint.setColor(0xff424242);
		return drawOvalBitmap(bitmap, oval, srcRect, dstRect, paint, null);
	}

	public static Bitmap drawOvalBitmap(Bitmap bitmap, RectF oval,
			Rect srcRect, Rect dstRect, Paint paint, Bitmap.Config bmpConfig) {
		if (bmpConfig == null) {
			bmpConfig = Bitmap.Config.ARGB_8888;
		}
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), bmpConfig);
		Canvas canvas = new Canvas(output);

		canvas.drawARGB(0, 0, 0, 0);
		if (paint == null) {
			paint = new Paint();
			paint.setColor(0xff424242);
		}
		paint.setAntiAlias(true);
		canvas.drawOval(oval, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, srcRect, dstRect, paint);
		return output;
	}

	public static Bitmap drawPathBitmap(Bitmap bmp, int edgeNum, int edgeLength, int edgeColor) {
		Path path = new Path();
		// 绘制正无边形
		long tmpX, tmpY;
		Double tmpAngle = 0D;
		path.moveTo(edgeLength, edgeLength);// 此点为多边形的起点
		for (int i = 0; i <= 5; i++) {
			int deltaAngle = 360/edgeNum;
			tmpAngle = (i * deltaAngle + deltaAngle/2) * 2 * Math.PI / 360;
			tmpX = (long) (edgeLength + edgeLength * Math.sin(tmpAngle));
			tmpY = (long) (edgeLength + edgeLength * Math.cos(tmpAngle));
			path.lineTo(tmpX, tmpY);
		}
		return drawPathBitmapBase(bmp, path, edgeColor);
	}

	/**
	 * 
	 * @param bmp
	 * @param edgeLength eg: 200
	 * @param edgeColor eg: Color.RED
	 * @return
	 */
	public static Bitmap drawPentagonBitmap(Bitmap bmp, int edgeLength, int edgeColor) {
		Path path = new Path();
		// 绘制正无边形
		long tmpX, tmpY;
		Double tmpAngle = 0D;
		path.moveTo(edgeLength, edgeLength);// 此点为多边形的起点
		for (int i = 0; i <= 5; i++) {
			tmpAngle = (i * 72 + 36) * 2 * Math.PI / 360;
			tmpX = (long) (edgeLength + edgeLength * Math.sin(tmpAngle));
			tmpY = (long) (edgeLength + edgeLength * Math.cos(tmpAngle));
			path.lineTo(tmpX, tmpY);
		}
		return drawPathBitmapBase(bmp, path, Color.TRANSPARENT);
	}

	public static Bitmap drawPathBitmapBase(Bitmap bmp, Path path, int edgeColor) {
		// 根据源文件新建一个darwable对象
		Drawable imageDrawable = new BitmapDrawable(bmp);
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		// 新建一个新的输出图片
		Bitmap output = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		// 新建一个矩形
		RectF outerRect = new RectF(0, 0, width, height);
		path.close(); // 使这些点构成封闭的多边形
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		if(edgeColor <= 0) {
			edgeColor = Color.TRANSPARENT;
		}
		canvas.drawPath(path, paint);
		// 将源图片绘制到这个圆角矩形上
		// 产生一个红色的圆角矩形
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		imageDrawable.setBounds(0, 0, width, height);
		canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
		imageDrawable.draw(canvas);
		canvas.restore();
		return output;
	}

	/**
	 * 绘制三角形
	 * 
	 * @param bmp
	 * @param paint
	 *            eg: Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	 *            paint.setColor(Color.RED);
	 * @param firstPoint
	 * @param secondPoint
	 * @param thirdPoint
	 * @return
	 */
	public static Bitmap drawTriangleBitmap(Bitmap bmp, int edgeColor,
			float[] firstPoint, float[] secondPoint, float[] thirdPoint) {
		Path path = new Path();
		path.moveTo(firstPoint[0], firstPoint[1]);
		path.lineTo(secondPoint[0], secondPoint[1]);
		path.lineTo(thirdPoint[0], thirdPoint[1]);
		return drawPathBitmapBase(bmp, path, edgeColor);
	}
	
	public static Bitmap drawEquilateralTriangleBitmap(Bitmap bmp, int edgeColor) {
		Path path = new Path();
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		path.moveTo(0, 0);
		path.lineTo(0, height);
		path.lineTo(width, height / 2);
		return drawPathBitmapBase(bmp, path, edgeColor);
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

	public static Bitmap scaleBitmap(Bitmap bmp, int figureFlag, int width, int height) {
        int bmpWidth = bmp.getWidth();  
        int bmpHeight = bmp.getHeight();
        float scaleW = (float) (width / bmpWidth);
        float scaleH = (float) (height / bmpHeight);
        float scale = scaleW < scaleH ? scaleW : scaleH;
        Matrix matrix = new Matrix();  
        matrix.postScale(scale, scale);  
        Bitmap resizeBmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
		return resizeBmp;
	}

	/**
	 * 放大或者缩小的比例   
	 * @param bmp
	 * @param scale  1.5 图片放大比例   
	 * @return 产生ReSize之后的bmp对象   
	 * 
	 * Matrix matrix = new Matrix();  
        matrix.postScale(widthScale, heightScale);  
        Bitmap resizeBmp = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight, matrix, true);
	 */
	public static Bitmap scaleBitmap(Bitmap bmp, float scale) {  
        int bmpWidth = bmp.getWidth();  
        int bmpHeight = bmp.getHeight();  
        Matrix matrix = new Matrix();  
        matrix.postScale(scale, scale);  
        Bitmap resizeBmp = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight, matrix, true);
		return resizeBmp;
	}

	/**
	 * 放大或者缩小的比例
	 * 
	 * @param bmp
	 * @param ratio
	 *            1.5 图片放大比例
	 * @return 产生ReSize之后的bmp对象
	 * 
	 *         Matrix matrix = new Matrix(); matrix.postScale(widthScale,
	 *         heightScale); Bitmap resizeBmp = Bitmap.createBitmap(bmp, 0, 0,
	 *         bmpWidth, bmpHeight, matrix, true);
	 */
	public static Bitmap scaleEtRatioBitmap(Bitmap bmp, float ratio) {
		if(bmp == null) throw new IllegalArgumentException("Bitmap is null, scale= " + ratio);
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(ratio, ratio);
		Bitmap resizeBmp = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight, matrix, true);
		return resizeBmp;
	}

	/**
	 * Matrix matrix = new Matrix(); matrix.postScale(widthScale, heightScale);
	 * Bitmap resizeBmp = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight,
	 * matrix, true);
	 * 
	 * @param bmp
	 * @param widthScale
	 * @param heightScale
	 * @return 产生ReSize之后的bmp对象
	 */
	public static Bitmap scaleBitmap(Bitmap bmp, float widthScale,
			float heightScale) {
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(widthScale, heightScale);
		Bitmap resizeBmp = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight,
				matrix, true);
		return resizeBmp;
	}

	public static CompressFormat getImageFormat(String fileName) {
		String exName = getExtensionName(fileName);
		CompressFormat format = CompressFormat.JPEG;
		if("jpg".equalsIgnoreCase(exName) || "jpeg".equalsIgnoreCase(exName)) {
			format = CompressFormat.JPEG;
		} else if("png".equalsIgnoreCase(exName)) {
			format = CompressFormat.PNG;
		} else if("webp".equalsIgnoreCase(exName) && (Build.VERSION.SDK_INT >= 14)) {
			//format = CompressFormat.JPEG;
		}
		return format;
	}
	
	/**
	 * Java文件操作 获取文件扩展名
	 * @param fileNameOrPathOrUrl
	 * @return
	 */
	public static String getExtensionName(String fileNameOrPathOrUrl) {
		if (TextUtils.isEmpty(fileNameOrPathOrUrl)) {
			return null;
		}
		final String fileName = fileNameOrPathOrUrl;
		if ((fileName != null) && (fileName.length() > 0)) {
			int dot = fileName.lastIndexOf('.');
			if ((dot > -1) && (dot < (fileName.length() - 1))) {
				return fileName.substring(dot + 1);
			}
		}
		return fileName;
	}
}
