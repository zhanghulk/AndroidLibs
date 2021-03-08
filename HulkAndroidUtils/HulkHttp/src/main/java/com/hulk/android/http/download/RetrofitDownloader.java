package com.hulk.android.http.download;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.hulk.android.http.content.FileHelper;
import com.hulk.android.log.Log;


import com.hulk.android.http.utils.HttpFileUtils;
import com.hulk.android.http.ok.OkHttpManager;
import com.hulk.android.http.ok.ProgressListener;
import com.hulk.android.http.retrofit.RetrofitManager;

import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * retrofit下载器
 * @author: zhanghao
 * @Time: 2021-02-25 21:06
 */
public class RetrofitDownloader {
    private static final String TAG = "RetrofitDownloader";

    Context context;
    String baseUrl;

    Retrofit retrofit;
    DownloadApiService downloadService;
    /**
     * 浏览器模式,此模式 Ok http client 下不执行SSL证书等等验证,直接下载
     */
    boolean browserMode = true;
    long contentLength = 0;

    /**
     * 全局精度监听器
     */
    ProgressListener mProgressListener;

    public RetrofitDownloader(Context context) {
        this.context = context;
        init();
    }

    public RetrofitDownloader(Context context, ProgressListener progressListener) {
        this.context = context;
        this.mProgressListener = progressListener;
        init();
    }

    public RetrofitDownloader(Context context, String baseUrl, boolean browserMode, ProgressListener progressListener) {
        this.context = context;
        this.baseUrl = baseUrl;
        this.browserMode = browserMode;
        this.mProgressListener = progressListener;
        init();
    }

    public RetrofitDownloader(Context context, Retrofit retrofit) {
        this.context = context;
        this.retrofit = retrofit;
    }

    private OkHttpClient getOkHttpClient() {
        return getOkHttpClient(mProgressListener);
    }

    private OkHttpClient getOkHttpClient(ProgressListener listener) {
        if (listener == null) {
            Log.w(TAG, "getOkHttpClient: progress listener is null");
        }
        if (browserMode) {
            return OkHttpManager.getBrowserDownloadClient(listener);
        }
        return OkHttpManager.getDownloadSslClient(listener);
    }

    public void init(OkHttpClient okHttpClient) {
        downloadService = createDownloadApiService(okHttpClient);
    }

    public DownloadApiService createDownloadApiService(OkHttpClient client) {
        String baseUrl = getAvailableBaseUrl();
        return RetrofitManager.createRetrofit(baseUrl, client).create(DownloadApiService.class);
    }

    public String getAvailableBaseUrl() {
        if (TextUtils.isEmpty(baseUrl)) {
            //baseUrl = ByodApiManager.getBaseUrl(context);
            baseUrl = "https://www.baidu.com";
        }
        return baseUrl;
    }

    private void init() {
        init(getOkHttpClient());
    }

    private void init(ProgressListener progressListener) {
        init(getOkHttpClient(progressListener));
    }

    private void ensureRetrofit() {
        if (retrofit == null) {
            init();
        }
    }

    /**
     * 下载
     * <p>使用observer实现精度结果监听,更为常用先进
     * @param url
     * @param filePath
     * @param observer
     */
    public void download(@NonNull String url, final String filePath, final DownloadObserver observer) {
        Log.i(TAG, "download2: url=" + url + ", filePath=" + filePath + ", observer=" + observer);
        //通过Observable发起请求
        ensureRetrofit();
        Observable<ResponseBody> observable = downloadService.download(url);
        observable
                //指定网络请求在io后台线程中进行
                .subscribeOn(Schedulers.io())
                //指定doOnNext的操作在io后台线程进行
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<ResponseBody>() {
                    //doOnNext里的方法执行完毕，observer里的onNext、onError等方法才会执行。
                    @Override
                    public void accept(ResponseBody body) throws Exception {
                        //下载文件，通过body.byteStream()可以得到输入流, 保存到本地
                        InputStream inputStream = body.byteStream();
                        long contentLength = body.contentLength();
                        Log.i(TAG, "doOnNext.accept contentLength: " + contentLength);
                        FileDownloadImpl impl = new FileDownloadImpl(url, filePath, contentLength, observer.downloadListener);
                        FileHelper.writeDownloadFile(inputStream, filePath, impl);
                    }
                })
                //指定observer回调在UI主线程中进行
                .observeOn(AndroidSchedulers.mainThread())
                //发起请求，请求的结果先回调到doOnNext进行处理，再回调到observer中
                .subscribe(observer);
    }

    public void download(@NonNull String url, final String filePath, final DownloadListener downloadListener) {
        Log.i(TAG, "download2: url=" + url + ", filePath=" + filePath);
        download(url, filePath, new DownloadObserver(url, filePath, downloadListener));
    }

    public static void test(Context context, String url) {
        DownloadListener downloadListener = new DownloadListener() {
            @Override
            public void onStart(String remark) {
                Log.w(TAG, "DownloadListener.onStart: " + remark);
            }

            @Override
            public void onProgress(int progress) {
                Log.w(TAG, "DownloadListener.onProgress: " + progress);
            }

            @Override
            public void onFinished(String url, String filePath) {
                Log.w(TAG, "DownloadListener.onFinished: " + url + ", filePath=" + filePath);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, "onFailure: " + throwable);
            }
        };
        RetrofitDownloader downloader = new RetrofitDownloader(context);
        String filename = HttpFileUtils.getFileName(url);
        String filePath = Environment.getExternalStorageDirectory() + "/Hulk/file/" + filename;
        downloader.download(url, filePath, downloadListener);
    }

    public static void test(Context context) {
        String url = "https://s.shouji.qihucdn.com/210307/1fa2b07cdca7a7f81f8c4eedff06499b/com.qihoo.appstore_300090095.apk?en=curpage%3D%26exp%3D1615781273%26from%3Dopenbox_channel_getUrl%26m2%3D%26ts%3D1615176473%26tok%3D30feb64608edefe4b78cbabe4a91d6f5%26v%3D%26f%3Dz.apk";
        test(context, url);
    }
}
