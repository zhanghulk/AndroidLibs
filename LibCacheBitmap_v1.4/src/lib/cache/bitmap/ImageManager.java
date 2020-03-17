package lib.cache.bitmap;

import lib.cache.bitmap.impl.BitmapManagerBase;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

public class ImageManager extends BitmapManagerBase {

	protected ImageManager(Context context) {
		super(context, CacheFactory.DEF_CACHE_SIZE, CacheFactory.DEF_THREAD_POOL_SIZE);
	}
	
	public Bitmap getdefaultBitmap() {
		return defaultBitmap;
	}

	public void setDefaultImg(int resId) {
		setDefaultBitmap(resId);
	}

	public void setDefaultImg(Bitmap defaultImg) {
		this.defaultBitmap = defaultImg;
	}

	public void loadImage(ImageView imageView, String url) {
		loadBitmap(context, imageView, url, FIGURE_NO_FLAG, defaultBitmap, false, null);
	}
	
	public void loadImage(ImageView imageView, String url, boolean isLoadPrompt) {
		loadBitmap(context, imageView, url, FIGURE_NO_FLAG, defaultBitmap, false, null);
	}
	
	public void loadImage(ImageView imageView, String url, Bitmap defaultBitmap) {
		loadBitmap(context, imageView, url, FIGURE_NO_FLAG, defaultBitmap, false, null);
	}
	
	public void loadImage(ImageView imageView, String url, int figureFlag, int defResId) {
		loadBitmap(context, imageView, url, figureFlag, getCacheResBmp(defResId), false, null);
	}
	
	public void loadBitmap(final ImageView imageView, final String url) {
		loadBitmap(context, imageView, url, FIGURE_NO_FLAG, defaultBitmap, false, null);
	}
	
	public void loadBitmapForExternal(final String url, final Handler handler) {
		Bitmap bitmap = getCacheBitmap(context, url);
		if (bitmap != null) {
			sendToTarget(handler, REQUEST_NET_RESULT_CODE, bitmap);
		} else {
			downlodNetBitmap(context, url, handler);
		}
	}
}
