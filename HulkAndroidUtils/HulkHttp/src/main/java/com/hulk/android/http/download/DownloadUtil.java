package com.hulk.android.http.download;

import android.os.Looper;
import android.util.Log;

import com.hulk.android.http.content.FileHelper;
import com.hulk.android.http.content.InputWriteCallback;
import com.hulk.android.http.utils.UrlParser;
import com.hulk.android.http.ok.OkHttpManager;
import com.hulk.android.http.ok.ProgressListener;
import com.hulk.android.http.retrofit.RetrofitManager;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;

/**
 * 调度器
 * @author: zhanghao
 * @Time: 2021-02-26 15:31
 */
public class DownloadUtil {

    private static final String TAG = "DownloadUtil";

    static Map<String, Long> sContentLengths = new Hashtable<>();

    public static DownloadApiService createDownloadApiService(String baseUrl, OkHttpClient client) {
        return RetrofitManager.createRetrofit(baseUrl, client).create(DownloadApiService.class);
    }

    /**
     * 开始下载
     *  @param url
     * @param filePath
     */
    public static void download(final String url, final String filePath, DownloadApiService service) {
        Log.i(TAG, "download: url=" + url + ", filePath=" + filePath);
        Observable<ResponseBody> observable = service.download(url);
        // subscribeOn()改变调用它之前代码的线程
        // observeOn()改变调用它之后代码的线程
        observable.subscribeOn(Schedulers.io())
                //.unsubscribeOn(Schedulers.io())
                .map(new Function<ResponseBody, InputStream>() {
                    @Override
                    public InputStream apply(ResponseBody body) throws Exception {
                        Log.i(TAG, "map.apply responseBody: " + body);
                        long contentLength = body.contentLength();
                        sContentLengths.put(url, contentLength);
                        return body.byteStream();
                    }
                })
                // 用于计算任务
                .observeOn(Schedulers.computation())
                .doOnNext(new Consumer<InputStream>() {
                    @Override
                    public void accept(InputStream inputStream) throws Exception {
                        long contentLength = sContentLengths.get(url);
                        Log.i(TAG, "doOnNext.call inputStream: " + contentLength);
                        FileHelper.writeFile(inputStream, filePath);
                    }
                })
                //.observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    /**
     * 开始下载
     * 使用ProgressListener更新下载进度状态
     *  @param url
     * @param filePath
     */
    public static void download(@NonNull String url, final String filePath, final ProgressListener listener) {
        Log.i(TAG, "download: url=" + url + ", filePath=" + filePath);
        OkHttpClient client = OkHttpManager.getBrowserDownloadClient(listener);
        String baseUrl = UrlParser.getBaseUrl(url);
        DownloadApiService service = createDownloadApiService(baseUrl, client);
        download(url, filePath, service);
    }

    /**
     * 开始下载
     * 使用ProgressListener更新下载进度状态
     *  @param url
     * @param filePath
     */
    public static void download(@NonNull String url, final String filePath, final DownloadListener downloadListener) {
        Log.i(TAG, "download: url=" + url + ", filePath=" + filePath);
        DefaultProgressListener listener = new DefaultProgressListener(url, filePath, downloadListener);
        listener.setDownloadObserverMode(false);
        download(url, filePath, listener);
    }

    public static int computeProgress(long downloadedLength, long contentLength) {
        int progress = 0;
        if (contentLength > 0) {
            progress = (int) (downloadedLength * 100 / contentLength);
        }
        return progress;
    }

    public static boolean runningOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
