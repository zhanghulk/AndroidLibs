package com.android.hulk.androidutils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.hulk.android.http.download.DownloadListener;
import com.hulk.android.http.download.RetrofitDownloader;
import com.hulk.android.http.image.ImageLoadCallback;
import com.hulk.android.http.image.ImageLoadHelper;
import com.hulk.android.http.utils.HttpFileUtils;
import com.hulk.android.log.Log;
import com.hulk.android.log.RuntimeLog;
import com.hulk.util.file.PrintUtil;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String[] PERMISSIONS = {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE};

    private static final String IMAGE_URL = "https://pics5.baidu.com/feed/b8389b504fc2d56263ab63951219b5e776c66c3c.jpeg?token=e3f2b3a4806bb1a38913457e6ecb160a&s=1ECF985E4CDD0CC80687F3F10300801E";
    private static final String IMAGE_URL2 = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fa3.att.hudong.com%2F65%2F38%2F16300534049378135355388981738.jpg&refer=http%3A%2F%2Fa3.att.hudong.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1617537464&t=f6e8544f19d6eb754aaa057f125e4373";
    private static final String IMAGE_URL3 = "https://gss0.baidu.com/70cFfyinKgQFm2e88IuM_a/forum/w%3d580/sign=9cdd7204e51190ef01fb92d7fe199df7/b13533fa828ba61eb13b6ac64334970a314e5905.jpg";

    Context mContext;
    ImageView img_view;
    TextView msg_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_main2);
        Log.w(TAG, "onCreate: ");
        requestPermissionsIfNot();

        img_view = findViewById(R.id.img_view);
        msg_tv = findViewById(R.id.msg_tv);

        Button download_file_view = findViewById(R.id.download_file_view);
        if (download_file_view != null) {
            download_file_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    test(mContext);
                }
            });
        }
        testLoadImage();
    }

    private void testLoadImage() {
        Button load_glide_view = findViewById(R.id.load_glide_view);
        if (load_glide_view != null) {
            load_glide_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageLoadHelper.loadByGlide(mContext, img_view, IMAGE_URL, R.mipmap.app_default_icon);
                }
            });
        }

        Button load_task_view = findViewById(R.id.load_task_view);
        if (load_task_view != null) {
            load_task_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageLoadHelper.loadByTask(mContext, img_view, IMAGE_URL2, R.mipmap.app_default_icon);
                }
            });
        }

        Button load_image_view = findViewById(R.id.load_image_view);
        if (load_image_view != null) {
            load_image_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageLoadHelper.loadImage(mContext, img_view, IMAGE_URL3, R.mipmap.app_default_icon, new ImageLoadCallback() {
                        @Override
                        public void onImageLoadSuccess(String url, ImageView view, Bitmap bitmap) {
                            Log.w(TAG, "onImageLoadSuccess: " + view + ", bitmap=" + bitmap);
                        }

                        @Override
                        public void onImageLoadFailed(String url, ImageView view) {
                            Log.w(TAG, "onImageLoadSuccess: " + view);
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.w(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(TAG, "onResume: ");
        RuntimeLog.flushAsync(TAG + ".onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        Log.w(TAG, "onDestroy: ");
        RuntimeLog.flushAsync(TAG + ".onDestroy");
        super.onDestroy();
    }

    protected void requestPermissionsIfNot() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "requestPermissions: " + Arrays.toString(PERMISSIONS));
                requestPermissions(PERMISSIONS, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.w(TAG, "onRequestPermissionsResult: requestCode=" + requestCode
                + ", permissions=" + Arrays.toString(permissions) + ", grantResults= " + grantResults);
    }

    protected void setMsg(String text, boolean append) {
        if (append) {
            msg_tv.append("\n" + text);
        } else {
            msg_tv.setText(text);
        }
    }

    protected void setMsg(String text) {
        setMsg(text, false);
    }

    protected void appendMsg(String text) {
        setMsg(text, true);
    }

    public void test(Context context, String url) {
        msg_tv.setTextColor(Color.BLACK);
        setMsg("Downloading from url: " + url);
        DownloadListener downloadListener = new DownloadListener() {
            @Override
            public void onStart(String remark) {
                Log.w(TAG, "DownloadListener.onStart: " + remark);
                appendMsg("onStart: " + remark);
            }

            @Override
            public void onProgress(int progress) {
                Log.w(TAG, "DownloadListener.onProgress: " + progress);
                appendMsg("onProgress: " + progress);
            }

            @Override
            public void onFinished(String url, String filePath) {
                Log.w(TAG, "DownloadListener.onFinished: " + url + ", filePath=" + filePath);
                msg_tv.append("onFinished: " + filePath);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, "onFailure: " + throwable);
                msg_tv.setTextColor(Color.RED);
                appendMsg("onFailure: " + throwable + "\n" + PrintUtil.formatStackTrace("", throwable));
            }
        };
        RetrofitDownloader downloader = new RetrofitDownloader(context);
        String filename = "test.apk";
        String filePath = Environment.getExternalStorageDirectory() + "/Hulk/file/" + filename;
        downloader.download(url, filePath, downloadListener);
    }

    public void test(Context context) {
        String url = "https://s.shouji.qihucdn.com/210307/1fa2b07cdca7a7f81f8c4eedff06499b/com.qihoo.appstore_300090095.apk?en=curpage%3D%26exp%3D1615781273%26from%3Dopenbox_channel_getUrl%26m2%3D%26ts%3D1615176473%26tok%3D30feb64608edefe4b78cbabe4a91d6f5%26v%3D%26f%3Dz.apk";
        test(context, url);
    }
}