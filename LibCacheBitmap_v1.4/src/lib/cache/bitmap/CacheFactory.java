package lib.cache.bitmap;

import android.content.Context;

import lib.cache.bitmap.api.BitmapCacheAPI;
import lib.cache.bitmap.api.CacheAPI;
import lib.cache.bitmap.entity.CacheBase;
import lib.cache.bitmap.impl.BitmapCache;
import lib.cache.bitmap.impl.CacheImpl;
import lib.cache.bitmap.utils.CacheUtils;

/**
 * bitmap factory,must init by application context
 * @author hao
 *
 */
public class CacheFactory {

	public static final int TYPE_GENERIC = -1;
	public static final int TYPE_BITMAP = 0;
	
	public static final int DEF_CACHE_SIZE = 8;
	public static final int DEF_THREAD_POOL_SIZE = 4;

	static IconManager iconManager;
	static ImageManager imageManager;
	static Context context;
	
	private static boolean debug = false;
	private static boolean autoClearOldCache = true;

	public static boolean isDebug() {
		return debug;
	}

	public static void setDebug(boolean debug) {
		CacheFactory.debug = debug;
	}

	public static boolean isAutoClearOldCache() {
		return autoClearOldCache;
	}

	public static void setAutoClearOldCache(boolean autoClearOldCache) {
		CacheFactory.autoClearOldCache = autoClearOldCache;
	}

	/**
	 * * create Bitmap GenericC api.
	 * 
	 * @param capacity
	 *            new a cache which capacity is 10
	 * @param capacity
	 * @return
	 */
	public static CacheAPI<CacheBase> createGenericCacheAPI(int capacity) {
		CacheImpl<CacheBase> impl = new CacheImpl<CacheBase>();
		if (capacity > 0) {
			impl.setCapacity(capacity);
		}
		return impl;
	}

	/**
	 * create Bitmap Cache pai.
	 * 
	 * @param capacity
	 *            new a cache which capacity is 10
	 * @return
	 */
	public static BitmapCacheAPI createBitmapCacheAPI(int capacity) {
		if (capacity > 0) {
			return new BitmapCache(capacity);
		} else {
			return new BitmapCache();
		}
	}
	
	public static IconManager createIconManager(Context context) {
		return new IconManager(context);
	}

	public static ImageManager createImageManager(Context context) {
		return new ImageManager(context);
	}

	public static void init(Context context) {
		CacheFactory.context = context;
		iconManager = new IconManager(CacheFactory.context);
		imageManager = new ImageManager(CacheFactory.context);
	}

	public static void recycle() {
		if(iconManager != null) {
			iconManager.clearCache();
			iconManager = null;
		}
		if(imageManager != null) {
			imageManager.clearCache();
			imageManager = null;
		}
	}

	public Context getContext() {
		return context;
	}

	public static IconManager getIconManager() {
		if(iconManager == null) {
			iconManager = new IconManager(CacheFactory.context);
		}
		return iconManager;
	}

	public static ImageManager getImageManager() {
		if(imageManager == null) {
			imageManager = new ImageManager(CacheFactory.context);
		}
		return imageManager;
	}
}
