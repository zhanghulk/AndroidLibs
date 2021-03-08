package com.hulk.android.http.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.hulk.android.http.conn.HttpManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

/**
 * 图片加载帮助类
 * @author hulk
 */
public class ImageLoadHelper {
    private static final String TAG = "ImageLoadHelper";

    public static final String ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static String IMAGE_CACHE_DIR = ROOT_DIR + "/Hulk/image/cache/";

    /**
     * 默认缓存
     */
    public static LruCache<String, Bitmap> sCache = new LruCache<String, Bitmap>(20);

    public static Set<String> sDownloadingUrls = Collections.synchronizedSet(new HashSet<>());

    public static final Pattern ICON_PATH_PATTERN = Pattern
            .compile("(.*)(app_apk|banners)/(.*)");
    
    public static void loadByTask(Context context, ImageView imageView, String url, int defaultImgResId) {
        new DownloadImageTask(context, imageView, url, defaultImgResId, null).start();
    }

    public static void loadByTask(Context context, ImageView imageView, String url, int defaultImgResId, ImageLoadCallback callback) {
        new DownloadImageTask(context, imageView, url, defaultImgResId, callback).start();
    }

    public static void loadImage(Context context, ImageView imageView, String url, int defaultImgResId, ImageLoadCallback callback) {
        ImageLoader.with(context)
                .load(url)
                .defaultImgResId(defaultImgResId)
                .callback(callback)
                .into(imageView);
    }

    public static void loadByGlide(Context context, ImageView imageView, String url, int defaultImgResId) {
        // setup Glide request without the into() method
        DrawableRequestBuilder<String> thumbnailRequest = Glide.with( context ).load( url );
        Glide.with(context)
                .load(url)
                //图片加载出来前，显示的图片
                .placeholder(defaultImgResId)
                //图片加载失败后，显示的图片
                //这里的单位是px
                //.override(width,height)
                //或者使用 dontAnimate() 关闭动画
                //.crossFade()
                .thumbnail(thumbnailRequest)
                .error(defaultImgResId)
                .into(imageView);
    }

    public static String getCacheFilePath(String cacheFileName) {
        return IMAGE_CACHE_DIR + cacheFileName;
    }

    public static void setImageCacheDir(String imageCacheDir) {
        IMAGE_CACHE_DIR = imageCacheDir;
    }

    public static void ensureCacheDir() {
        File dir = new File(IMAGE_CACHE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static Bitmap getImageFromLocal(String cacheFileName) {
        String filePath = getCacheFilePath(cacheFileName);
        return getLocalImage(filePath);
    }

    public static Bitmap getLocalImage(String filePath) {
        final File f = new File(filePath);
        if (f.exists()) {
            try{
                Bitmap bp = BitmapFactory.decodeFile(filePath);
                return bp;
            }catch(OutOfMemoryError oom){
                oom.printStackTrace();
            }
        }
        return null;
    }

    public static Bitmap downloadBitmap(Context context, String url) {
        Bitmap bitmap = null;
        try {
            HttpsURLConnection conn = HttpManager.getHttpsConnection(url, true, false);
            conn.setConnectTimeout(0);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
            conn.disconnect();
        } catch (IOException e) {
            Log.e(TAG, "downloadBitmap failed: " + url, e);
        }
        return bitmap;
    }

    public static Bitmap downloadBitmap(Context context, String url, String filePath) throws IOException {
        Log.i(TAG, "downloadBitmap: url=" + url + ", filePath=" + filePath);
        Bitmap bitmap = null;
        HttpsURLConnection conn = HttpManager.getHttpsConnection(url, true, false);
        conn.setConnectTimeout(0);
        conn.connect();
        InputStream is = conn.getInputStream();
        bitmap = parseBitmap(is, filePath);
        Log.i(TAG, "downloadBitmap: " + bitmap);
        conn.disconnect();
        return bitmap;
    }

    public static Bitmap parseBitmap(InputStream inputStream, String filePath) throws IOException {
        if (inputStream == null) {
            Log.w(TAG, "parseBitmap: inputStream is null");
            return null;
        }
        BufferedOutputStream bos = null;
        try {
            Bitmap bitmap = decodeStream(inputStream);
            if (bitmap != null) {
                boolean saved = saveBitmap(inputStream, filePath, false);
                Log.w(TAG, "parseBitmap: saved=" + saved);
            } else {
                Log.w(TAG, "parseBitmap: decode stream failed.");
            }
            return bitmap;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
    }

    public static boolean saveBitmap(InputStream inputStream, String filePath, boolean closeInput) throws IOException {
        if (inputStream == null) {
            Log.w(TAG, "saveBitmap: inputStream is null");
            return false;
        }
        BufferedOutputStream bos = null;
        try {
            File file = new File(filePath);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                boolean mkdirs = parentFile.mkdirs();
                Log.w(TAG, "saveBitmap: Parent files mkdirs: " + mkdirs);
            }
            bos = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buffer = new byte[4096];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.flush();
            Log.w(TAG, "saveBitmap: Finished filePath: " + filePath);
            return true;
        } finally {
            if (closeInput) {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            if (bos != null) {
                bos.close();
            }
        }
    }

    public static Bitmap decodeStream(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        return bitmap;
    }

    /**
     * 加载本地图片 http://bbs.3gstdy.com
     *
     * @param url
     * @return
     */
    public static Bitmap getLocalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            Bitmap bmp = BitmapFactory.decodeStream(fis);
            fis.close();
            return bmp;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 控制台下发的iconUrl的路径样式为：https://111.206.67.92/icons/app_apk/4ec16a49ee409b88c45d//icon.png，
     * 该方法提取4ec16a49ee409b88c45dicon.png作为本地缓存文件名；
     * @param url
     * @return
     */
    public static String getFileName(String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        int lastIndex = url.indexOf('?');
        String fileName = lastIndex > 0 ? url.substring(0, lastIndex) : url;
        Matcher matcher = ICON_PATH_PATTERN.matcher(fileName);
        if (matcher.find()) {
            fileName = matcher.group(3);
        }
        if (fileName != null) {
            fileName = fileName.replace("/", "");
        }
        Log.d(TAG, "getLoacalFileName:" + fileName);
        return fileName;
    }
    
    public static String getLocalCacheFilePath(String url) {
        String fileName = getFileName(url);
        String filePath = IMAGE_CACHE_DIR + fileName;
        return filePath;
    }

    public static boolean checkDownloading(String url) {
        Set<String> urls =  ImageLoadHelper.sDownloadingUrls;
        if (urls == null) {
            return false;
        }
        if (urls.contains(url)) {
            Log.i(TAG, "isDownloading: downloading url: " + url);
            return true;
        } else {
            urls.add(url);
        }
        return false;
    }

    public static void removeDownloading(String url) {
        Set<String> urls =  ImageLoadHelper.sDownloadingUrls;
        if (urls == null) {
            return;
        }
        urls.remove(url);
    }
}
