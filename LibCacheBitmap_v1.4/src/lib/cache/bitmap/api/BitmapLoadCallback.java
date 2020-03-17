package lib.cache.bitmap.api;

import android.graphics.Bitmap;

public interface BitmapLoadCallback {
	public void onAsyncLoad(String url, Bitmap bitmap);
}
