package com.image.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation.AnimationListener;

public class BitmapUtils {
	
	private static final String TAG = "BitmapUtils";

	public static final int FIGURE_NO_FLAG = -1;
	public static final int FIGURE_ROUNDED_FLAG = 1;
	public static final int FIGURE_ROUND_RECT_FLAG = 2;
	public static final int FIGURE_OVAL_FLAG = 3;
	public static final int FIGURE_TRIANGLE_FLAG = 4;
	public static final int FIGURE_PENTAGON_FLAG = 5;
	public static final int FIGURE_DRAW_PATH_FLAG = 6;
	
	
	/**
	 *  compress bitmap into destination according to targetLength
	 * @param bitmap
	 * @param maxLength unit is byte
	 * @param format the one of Bitmap.CompressFormat.*
	 * @param dstFilePath the file path to save.
	 * @return
	 */
	public static Bitmap compress(Bitmap bitmap, int maxLength, String dstFilePath, Bitmap.CompressFormat format) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(format, 100, baos);
		int length = baos.toByteArray().length;
		if (length <= maxLength) {
			int writeLength = writeToFile(baos, dstFilePath);
			Log.i(TAG, "Not need to compress, writen Length= " + writeLength);
			return bitmap;
		}
		Log.i(TAG, "Starting Compress length= " + length + ", targetLength" + maxLength);
		int quality = 100;
		
		if (format == null) {
			format = Bitmap.CompressFormat.JPEG;
		}
		// The cycle compression specify meet the specified size
		while (length > maxLength) {
			baos.reset();
			quality -= 10;
			
			bitmap.compress(format, quality, baos);
			length = baos.toByteArray().length;
		}
		int count = writeToFile(baos, dstFilePath);
		Bitmap destBmp = BitmapFactory.decodeFile(dstFilePath);
		Log.i(TAG, "compress count= " + count + ", quality=" + quality + ", dstFilePath=" + dstFilePath);
		return destBmp;
	}

	private static int writeToFile(ByteArrayOutputStream baos, String filePath) {
		try {  
            File file = new File(filePath);
			FileOutputStream fos = new FileOutputStream(file);  
            fos.write(baos.toByteArray());  
            fos.flush();  
            fos.close(); 
            return baos.size();
        } catch (Exception e) {  
            e.printStackTrace();  
        }
		return 0; 
	}

	/**
	 * scale width and height, and then compress quality.
	 * @param srcPath
	 * @param dstWidth
	 * @param dstHeight
	 * @param maxLength max size kb
	 * @param format the one of Bitmap.CompressFormat.*
	 * @return
	 */
	public static Bitmap compress(Bitmap src, int dstWidth, int dstHeight,
			int maxLength, String filePath, Bitmap.CompressFormat format) {
		Bitmap bitmap = src;
		if (dstWidth != src.getWidth() || dstHeight != src.getHeight()) {
			bitmap = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
			src.recycle();
		}
		return compress(bitmap, maxLength, filePath, format);
	}

	public static Bitmap compress(String srcPath, String dstFilePath,
			int dstWidth, int dstHeight, int maxLength) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;//只读边,不读内容
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		int scale = 1;
		if (w > h && w > dstWidth) {
			scale = (int) (newOpts.outWidth / dstWidth);
		} else if (w < h && h > dstHeight) {
			scale = (int) (newOpts.outHeight / dstHeight);
		}
		if (scale <= 0)
			scale = 1;
		newOpts.inSampleSize = scale;//设置采样率
		
		newOpts.inPreferredConfig = Config.ARGB_8888;//该模式是默认的,可不设
		newOpts.inPurgeable = true;// 同时设置才会有效
		newOpts.inInputShareable = true;//。当系统内存不够时候图片自动被回收
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return compress(bitmap, maxLength, dstFilePath, getCompressFormat(dstFilePath));
	}

	/**
	 * set Bitmap.Config as RGB_565 and return a new bitmap for saving system resource if needed.
	 */
	public static Bitmap changeBitmapConfigAsRGB565(Bitmap bitmap) {
		Bitmap.Config config = Bitmap.Config.RGB_565;
		Bitmap temp = bitmap.copy(config, false);
		if(temp != null){
			bitmap.recycle();
		}
		return temp;
	}

	/**
	 * change Bitmap Config and return a new bitmap.
	 * @param bitmap
	 * @param config
	 * @return
	 */
	public static Bitmap changeBitmapConfig(Bitmap bitmap, Bitmap.Config config) {
		Bitmap temp = bitmap.copy(config, false);
		if(temp != null){
			bitmap.recycle();
		}
		return temp;
	}

	/**
	 * load local image according to file path or uri
	 * @param filePathOrUri
	 * @return
	 */
	public static Bitmap loadLocalBitmap(String filePathOrUri) {
		if(TextUtils.isEmpty(filePathOrUri)) return null;
		String filePath = null;
		if(filePathOrUri.startsWith("content://")) {
		    filePath = Uri.parse(filePathOrUri).getPath();
        } else {
            filePath = filePathOrUri;
        }
		try {
		    //get bitmap with width and height.
			return getBitmap(filePath, 0, 0);
		} catch (Exception e) {
			Log.w(TAG, "load failed filePath: " + filePathOrUri + ", e: " + e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get bitmap size according to Bitmap.CompressFormat.*
	 * @param bitmap
	 * @param format one of Bitmap.CompressFormat.JPEG, 
	 * Bitmap.CompressFormat.PNG, 
	 * Bitmap.CompressFormat.WEBP, 
	 * default JPEG if null.
	 * @return
	 */
	public static int getBitmapBytes(Bitmap bitmap, Bitmap.CompressFormat format){
		if(bitmap == null) return 0;
		int bitmapSize = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if (format == null) {
			format = Bitmap.CompressFormat.JPEG;
		}
		try {
			if (bitmap.compress(format, 100, out)) {
				out.flush();
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		bitmapSize = out.size();
		return bitmapSize;
	}

	/**
	 * save Bitmap to destFilePath
	 * @param bitmap
	 * @param destFilePath
	 * @param quality
	 * @param format one of Bitmap.CompressFormat.JPEG, 
	 * Bitmap.CompressFormat.PNG, 
	 * Bitmap.CompressFormat.WEBP, 
	 * default JPEG if null.
	 * @param recycle
	 * @return
	 * @throws IOException
	 */
	public static boolean saveBitmap(Bitmap bitmap, String destFilePath,
			int quality, Bitmap.CompressFormat format, boolean recycle) throws IOException {
		if (TextUtils.isEmpty(destFilePath)) {
			throw new IOException("dest file path is null ");
		}
		Bitmap bmp = bitmap;
		File file = new File(destFilePath);
		// if parent of file is not existed, make its parent folder
		File parentFile = file.getParentFile();
		if (!parentFile.exists() && !parentFile.mkdirs()) {
			throw new IOException("mkdirs parentFile failed: " + parentFile);
		}
		FileOutputStream out = new FileOutputStream(file);
		if (format == null) {
			format = getCompressFormat(destFilePath);
		}
		boolean success = false;
		if (bmp.compress(format, quality, out)) {
			out.flush();
			out.close();
			success = true;
		}
		if (recycle && bmp != null) {
			bmp.recycle();
		}
		return success;
	}

	public static Bitmap.CompressFormat getCompressFormat(String fileNameOrPath) {
		Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
		if (fileNameOrPath.endsWith(".png") || fileNameOrPath.endsWith(".PNG")) {
			format = Bitmap.CompressFormat.PNG;
		} else if (fileNameOrPath.endsWith(".webp")) {
			format = Bitmap.CompressFormat.PNG;
		}
		return format;
	}

	public static Bitmap getBitmap(Context context, int resId) {
		try {
			return BitmapFactory.decodeResource(context.getResources(), resId);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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

	public static Bitmap drawFigureBitmap(Bitmap input, int figureFlag) {
		if (input == null)
			return null;
		Bitmap output = null;
		final Bitmap bitmap = input;
		switch (figureFlag) {
		case FIGURE_ROUNDED_FLAG:
			output = drawRoundedBitmap(bitmap);
			break;
		case FIGURE_ROUND_RECT_FLAG:
			output = drawRoundRectBitmap(bitmap, 80);
			break;
		case FIGURE_OVAL_FLAG:
			output = drawOvalBitmap(bitmap);
			break;
		case FIGURE_TRIANGLE_FLAG:
			output = drawEquilateralTriangleBitmap(bitmap, 0);
			break;
		case FIGURE_PENTAGON_FLAG:
			int min = bitmap.getWidth() > bitmap.getHeight() ? bitmap.getHeight() : bitmap.getWidth();
			output = drawPentagonBitmap(bitmap, min / 2, 0);
			break;
		case FIGURE_DRAW_PATH_FLAG:
			int min1 = bitmap.getWidth() > bitmap.getHeight() ? bitmap.getHeight() : bitmap.getWidth();
			output = drawPathBitmap(bitmap, 6, min1 / 2, 0);
			break;

		default:
			Log.w(TAG, "drawFigureBitmap unknown flag: " + figureFlag);
			output = input;
			break;
		}
		return output;
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

    public static Bitmap drawRoundRectBitmap(Bitmap bitmap) {
        return drawRoundRectBitmap(bitmap, 80);
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

    /**
     * 获得圆角矩形图片的方法
     * 
     * @param bitmap
     * @param roundPx x方向的圆角半径 eg: 50, 80, 100 150...
     * * @param roundPy y方向的圆角半径 eg: 50, 80, 100 150...
     * @return
     */
    public static Bitmap drawRoundRectBitmap(Bitmap bitmap, float roundPx, float roundPy) {
    	if (bitmap == null) {
    		throw new IllegalArgumentException("drawRoundRectBitmap: the bitmap must not be null !!");
		}
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final int color = 0xff424242;
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        return drawRoundRectBitmap(bitmap, paint, rect, rect, roundPx, roundPy,
                Bitmap.Config.ARGB_8888);
    }

    public static Bitmap drawRoundRectBitmap(Bitmap bitmap, Paint paint,
            float roundPx, float roundPy) {
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        return drawRoundRectBitmap(bitmap, paint, rect, rect, roundPx, roundPy,
                Bitmap.Config.ARGB_8888);
    }

    /**
     * 获得圆角矩形图片的方法
     * @param bitmap
     * @param paint
     * @param srcRect eg : Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
     * @param dstRect eg : Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
     * @param roundPx x方向的圆角半径 eg: 50, 80, 100 150...
     * @param roundPy y方向的圆角半径 eg: 50, 80, 100 150...
     * @param bmpConfig
     * @return
     */
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

    /**
     * 获得圆角矩形图片的方法
     * @param bitmap
     * @param srcRect  eg : Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
     * @param dstRect  eg : Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
     * @param roundPx x方向的圆角半径 eg: 50, 80, 100 150...
     * @param roundPy y方向的圆角半径 eg: 50, 80, 100 150...
     * @param paintColor
     * @param isAntiAlias
     * @param xfermode
     * @param bmpConfig
     * @return
     */
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
    
    /**
     * 获取等边三角形图片.
     * @param bmp
     * @param edgeColor
     * @return
     */
    public static Bitmap drawEquilateralTriangleBitmap(Bitmap bmp, int edgeColor) {
        Path path = new Path();
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        path.moveTo(0, 0);
        path.lineTo(0, height);
        path.lineTo(width, height / 2);
        return drawPathBitmapBase(bmp, path, edgeColor);
    }

    /**
     * 
     * @param filePath
     * @param roundPxy  X/Y方向圆角半径
     * @return
     */
    public static Bitmap getRoundedRectBitmap(String filePath, float roundPxy) {
        Bitmap bitmap = getBitmap(filePath);
        return drawRoundRectBitmap(bitmap, roundPxy);
    }

    /**
     * 
     * @param filePath
     * @param roundPxy  X/Y方向圆角半径
     * @return
     */
    public static Bitmap getRoundedRectBitmap(String filePath, float roundPx, float roundPy) {
        Bitmap bitmap = getBitmap(filePath);
        return drawRoundRectBitmap(bitmap, roundPx, roundPy);
    }

	/**
	 * Get rounded bitmap
	 * @param bitmap
	 * @return
	 */
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
	
	public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
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
			BitmapFactory.Options opt = new BitmapFactory.Options();
			// start parse image only width and height, set
			// options.inJustDecodeBounds to true
			opt.inJustDecodeBounds = true;
			Bitmap bitmap = BitmapFactory.decodeFile(srcPath, opt);
			opt.inJustDecodeBounds = false;
			opt.inSampleSize = getSimpleSize(opt, width, height);
			// restart parse image all info, set options.inJustDecodeBounds as
			// false
			bitmap = BitmapFactory.decodeFile(srcPath, opt);
			return bitmap;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getScaledBitmap(Bitmap src, int dstWidth, int dstHeight) {
		return Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
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
