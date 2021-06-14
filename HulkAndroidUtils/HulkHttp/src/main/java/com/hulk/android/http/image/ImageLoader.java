package com.hulk.android.http.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.hulk.android.http.download.DownloadListener;
import com.hulk.android.http.download.DownloadUtil;
import com.hulk.android.http.download.RetrofitDownloader;

/**
 * 图片加载器
 * @author: zhanghao
 * @Time: 2021-03-04 20:09
 */
public class ImageLoader {
    private static final String TAG = "ImageLoader";

    private static class ImageLoaderHolder {
        private static ImageLoader imageLoader = new ImageLoader();

        public static ImageLoader get() {
            return imageLoader;
        }
    }

    private Context mContext = null;
    private LruCache<String, Bitmap> mCache = ImageLoadHelper.sCache;
    private ImageView mImageView;
    private String mUrl = null;
    private String mCacheFileName = null;
    private ImageLoadCallback mCallback = null;
    private int defaultImgResId = 0;
    private int errorImgResId = 0;
    private boolean needDownload = true;

    private RetrofitDownloader mDownloader;
    private String mFilename;

    private ImageLoader() {
    }

    public static ImageLoader with(Context context) {
        ImageLoader loader = ImageLoaderHolder.get();
        loader.init(context);
        return loader;
    }

    private void init(Context context) {
        this.mContext = context;
    }

    public ImageLoader load(String url) {
        this.mUrl = url;
        return this;
    }

    public ImageLoader filePath(String filename) {
        this.mFilename = filename;
        return this;
    }

    public ImageLoader defaultImgResId(int defaultImgResId) {
        this.defaultImgResId = defaultImgResId;
        return this;
    }

    public ImageLoader errorImgResId(int errorImgResId) {
        this.errorImgResId = errorImgResId;
        return this;
    }

    public ImageLoader callback(ImageLoadCallback callback) {
        this.mCallback = callback;
        return this;
    }

    public ImageLoader cache(LruCache<String, Bitmap> cache) {
        this.mCache = cache;
        return this;
    }

    private ImageLoader needDownload(boolean needDownload) {
        this.needDownload = needDownload;
        return this;
    }

    public void into(ImageView imageView) {
        this.mImageView = imageView;
        boolean preloaded = preload();
        if (preloaded) {
            //Log.w(TAG, "into: preloaded.");
            return;
        }
        //download();
        download2();
    }

    private boolean canDownload() {
        if (!needDownload) {
            Log.w(TAG, "canDownload: Not need download.");
            return false;
        }
        if (isDownloading()) {
            Log.i(TAG, "canDownload: Downloading: " + mUrl);
            return false;
        }
        return true;
    }

    private void download() {
        if (!canDownload()) {
            Log.w(TAG, "download: Can not download.");
            return;
        }
        ImageLoadHelper.ensureCacheDir();
        String filePath = getFilePath();
        Log.i(TAG, "download: mUrl=" + mUrl + ", filePath=" + filePath);
        DownloadUtil.download(mUrl, filePath, new DownloadImpl());
    }

    private void download2() {
        if (!canDownload()) {
            Log.w(TAG, "download2: Not need download.");
            return;
        }
        if (mDownloader == null) {
            mDownloader = new RetrofitDownloader(mContext);
        }
        ImageLoadHelper.ensureCacheDir();
        String filePath = getFilePath();
        Log.i(TAG, "download2: mUrl=" + mUrl + ", filePath=" + filePath);
        mDownloader.download(mUrl, filePath, new DownloadImpl());
    }

    private boolean preload() {
        String url = mUrl;
        if (TextUtils.isEmpty(url)){
            // 设置为默认图片
            Log.w(TAG, "preload: set default Image for url is empty.");
            setImageResource(errorImgResId);
            onImageLoadFailed();
            return true;
        }

        // 先从缓存中查找
        Bitmap img = getCache(url);
        if (img != null) {
            setImageBitmap(img);
            return true;
        }

        Bitmap bp = getImageFromLocal();
        if (bp != null) {
            setImageBitmap(bp);
            putCache(mUrl, bp);
            return true;
        }
        // 先设置为默认图片
        setImageResource(defaultImgResId);
        return false;
    }

    private void postDownLoad(String url, String filePath) {
        if (mImageView == null) {
            Log.i(TAG, "postDownLoad: mImageView is null");
            return;
        }
        Object tag = mImageView.getTag();
        if (tag instanceof String) {
            url = (String) tag;
        }
        Bitmap image = getLocalImage(filePath);
        if (image == null) {
            // 设置为默认图片
            Log.w(TAG, "onPostExecute: set default image for result is null");
            setImageResource(errorImgResId);
            onImageLoadFailed();
        } else {
            putCache(url, image);
            setImageBitmap( image);
        }
    }

    private void putCache(String url, Bitmap image) {
        if (TextUtils.isEmpty(url) || image == null) {
            return;
        }
        if (mCache != null) {
            mCache.put(url, image);
        }
    }

    private Bitmap getCache(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        if (mCache == null) {
            return null;
        }
        Bitmap image = mCache.get(url);
        return image;
    }

    private void setImageBitmap(Bitmap bitmap) {
        if (bitmap != null && mImageView != null) {
            mImageView.setImageBitmap(bitmap);
        }
        onImageLoadSuccess(bitmap);
    }

    private void setImageResource(int resId) {
        if (resId > 0 && mImageView != null) {
            mImageView.setImageResource(resId);
        }
    }

    private void onImageLoadSuccess(Bitmap bitmap) {
        if (mCallback != null){
            mCallback.onImageLoadSuccess(mUrl, mImageView, bitmap);
        }
    }

    private void onImageLoadFailed() {
        if (mCallback != null){
            mCallback.onImageLoadFailed(mUrl, mImageView);
        }
    }

    private boolean isDownloading() {
        return ImageLoadHelper.checkDownloading(mUrl);
    }

    private void removeDownloading() {
        ImageLoadHelper.removeDownloading(mUrl);
    }

    private Bitmap getImageFromLocal() {
        String filename = getFilename();
        return ImageLoadHelper.getImageFromLocal(filename);
    }

    private Bitmap getLocalImage(String filePath) {
        return ImageLoadHelper.getLocalImage(filePath);
    }

    private String getFilename() {
        if (TextUtils.isEmpty(mFilename)) {
            mFilename = ImageLoadHelper.getFileName(mUrl);
        }
        return mFilename;
    }

    private String getFilePath() {
        String filename = getFilename();
        return ImageLoadHelper.getCacheFilePath(filename);
    }

    private class DownloadImpl implements DownloadListener {
        @Override
        public void onStart(String remark) {
            Log.w(TAG, "onStart: " + remark);
        }

        @Override
        public void onProgress(int progress) {
            Log.v(TAG, "onProgress: " + progress);
        }

        @Override
        public void onFinished(String url, String filePath) {
            Log.w(TAG, "onFinished: " + url + ",filePath: " + filePath);
            postDownLoad(url, filePath);
        }

        @Override
        public void onFailure(Throwable throwable) {
            Log.e(TAG, "onFailure: " + throwable);
            setImageResource(errorImgResId);
        }
    }
}
