package com.hulk.android.http.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;

/**
 * 下载图片异步任务
 * @author: zhanghao
 * @Time: 2021-03-04 20:09
 */
class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private static final String TAG = "DownloadImageTask";

    ImageView mImageView;
    LruCache<String, Bitmap> mCache = ImageLoadHelper.sCache;
    Context mContext = null;
    String mUrl = null;
    String mCacheFileName = null;
    ImageLoadCallback mCallback = null;
    int defaultImgResId = 0;
    boolean needDownload = true;

    public DownloadImageTask(Context context, ImageView imageView, String url, int defaultImgResId) {
        this.mContext = context;
        this.mImageView = imageView;
        this.mUrl = url;
        this.defaultImgResId = defaultImgResId;
    }

    public DownloadImageTask(Context context, ImageView imageView, String url, int defaultImgResId, ImageLoadCallback callback) {
        this.mContext = context;
        this.mImageView = imageView;
        this.mUrl = url;
        this.defaultImgResId = defaultImgResId;
        this.mCallback = callback;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        String url = urls[0];
        Bitmap image = downloadImage(url);
        return image;
    }

    private Bitmap downloadImage(String url) {
        if (TextUtils.isEmpty(url)) {
            Log.w(TAG, "downloadImage: urlis empty.");
            return null;
        }
        try {
            String filePath = getCacheFilePath();
            ImageLoadHelper.ensureCacheDir();
            Bitmap bitmap = ImageLoadHelper.downloadBitmap(mContext, url, filePath);
            return bitmap;
        } catch (IOException e) {
            Log.e(TAG, "downloadImage: " + e + ", url: " + url, e);
        } catch (IllegalArgumentException e){
            Log.e(TAG, "downloadImage: " + e + ", url: " + url, e);
        } catch (OutOfMemoryError oom) {
            oom.printStackTrace();
            Log.e(TAG, "downloadImage: OutOfMemoryError: ", oom);
        } finally{
            removeDownloading();
        }
        Log.w(TAG, "downloadImage failed url: " + url);
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        Object tag = mImageView.getTag();
        String url = mUrl;
        if (tag instanceof String) {
            url = (String) tag;
        }
        if (result == null) {
            // 设置为默认图片
            Log.w(TAG, "onPostExecute: set default image for result is null");
            setImageResource(mImageView, defaultImgResId);
            onImageLoadFailed();
        } else {
            mCache.put(url, result);
            setImageBitmap(mImageView, result);
        }
    }

    private void setImageBitmap(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        onImageLoadSuccess(bitmap);
    }

    private void setImageResource(ImageView imageView, int resId) {
        if (resId > 0) {
            imageView.setImageResource(resId);
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

    protected boolean preload() {
        if (TextUtils.isEmpty(mUrl)){
            // 设置为默认图片
            Log.w(TAG, "preload: set default Image for url is empty.");
            setImageResource(mImageView, defaultImgResId);
            onImageLoadFailed();
            return true;
        }

        // 先从缓存中查找
        Bitmap bitmap = mCache.get(mUrl);
        if (bitmap != null) {
            //Log.i(TAG, "startRun: set cache image for url: " + mUrl);
            setImageBitmap(mImageView, bitmap);
            return true;
        }

        mCacheFileName = getLoacalFileName(mUrl);
        Bitmap bp = getImageFromLocal();
        if (bp != null) {
            Log.i(TAG, "preload: set local image file for: " + mCacheFileName);
            setImageBitmap(mImageView, bp);
            mCache.put(mUrl, bp);
            return true;
        }
        // 先设置为默认图片
        setImageResource(mImageView, defaultImgResId);
        return false;
    }

    public void start() {
        boolean preloaded = preload();
        if (preloaded) {
            Log.w(TAG, "start: preloaded.");
            return;
        }
        //to download
        if (needDownload) {
            Log.w(TAG, "start: start download task.");
            executeDownloadTask();
        } else {
            Log.w(TAG, "start: needDownload is false");
        }
    }

    /**
     * 开始网络下载图片
     */
    private void executeDownloadTask() {
        if (isDownloading()) {
            Log.i(TAG, "executeDownloadTask: Ignored downloading url: " + mUrl);
            return;
        }

        try {
            Log.i(TAG, "executeDownloadTask:  url: " + mUrl);
            this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mUrl);
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
            Log.e(TAG, "executeDownloadTask: Error" , e);
        }
    }

    private boolean isDownloading() {
        return ImageLoadHelper.checkDownloading(mUrl);
    }

    private void removeDownloading() {
        ImageLoadHelper.removeDownloading(mUrl);
    }

    public void setNeedDownload(boolean needDownload) {
        this.needDownload = needDownload;
    }

    public String getCacheFilePath() {
        return ImageLoadHelper.getCacheFilePath(mCacheFileName);
    }

    public Bitmap getImageFromLocal() {
        return ImageLoadHelper.getImageFromLocal(mCacheFileName);
    }

    public static String getLoacalFileName(String url) {
        return ImageLoadHelper.getFileName(url);
    }
}
