package lib.cache.bitmap;

import lib.cache.bitmap.api.BitmapLoadCallback;
import lib.cache.bitmap.impl.BitmapManagerBase;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * usd for manage all icon or picture of drawable
 * 
 * @author Hulk
 * 
 */
public class IconManager extends BitmapManagerBase {

	public static final String TAG = "IconManager";
	public final static int REQUEST_NET_RESULT_CODE = 0x11;

	protected IconManager(Context context) {
		super(context, CacheFactory.DEF_CACHE_SIZE, CacheFactory.DEF_THREAD_POOL_SIZE);
	}

	public Bitmap getDefaultIcon() {
		return defaultBitmap;
	}

	public void setDefaultIcon(Bitmap defaultIcon) {
		this.defaultBitmap = defaultIcon;
	}

	public void setDefaultIcon(int resId) {
		setDefaultBitmap(resId);
	}

	public void loadIcon(ImageView imageView, String url) {
		loadBitmap(context, imageView, url, FIGURE_NO_FLAG, defaultBitmap, false, null);
	}
	
	public void loadIcon(final String url, BitmapLoadCallback callback) {
		loadBitmap(url, callback);
	}
	
	public void loadIcon(ImageView imageView, String url, int figureFlag) {
		loadBitmap(context, imageView, url, FIGURE_NO_FLAG, defaultBitmap, false, null);
	}
	
	public void loadIcon(ImageView imageView, String url, int figureFlag, int defResId) {
		loadBitmap(context, imageView, url, FIGURE_NO_FLAG, getBitmap(context, defResId), false, null);
	}
}
