package com.hulk.android.http.download;

import android.os.Handler;
import android.os.Looper;

/**
 * 状态调度器,更不结果
 * @author: zhanghao
 * @Time: 2021-02-26 17:05
 */
public class ProgressDispatcher {
    Handler mH = new Handler(Looper.getMainLooper());

    int lastProgress = 0;
    long lastProgressTime = 0;
    DownloadListener listener;

    public ProgressDispatcher(DownloadListener listener) {
        this.listener = listener;
    }

    public void onStart(final String remark) {
        if (runningOnMainThread()) {
            listener.onStart(remark);
        } else {
            mH.post(new Runnable() {
                @Override
                public void run() {
                    listener.onStart(remark);
                }
            });
        }
    }

    public boolean onProgress(final int progress) {
        if (!updateProgressEnabled(progress)) {
            //控制更新频率
            return false;
        }
        if (runningOnMainThread()) {
            listener.onProgress(progress);
        } else {
            mH.post(new Runnable() {
                @Override
                public void run() {
                    listener.onProgress(progress);
                }
            });
        }
        return true;
    }

    /**
     * 1秒钟更新一次进度
     * @return
     */
    private boolean updateProgressEnabled(int progress) {
        if ((progress == 100)) {
            return true;
        }
        if ((progress - lastProgress) < 2) {
            return false;
        }
        if ((System.currentTimeMillis() - lastProgressTime) < 1000) {
            return false;
        }
        lastProgress = progress;
        lastProgressTime = System.currentTimeMillis();
        return true;
    }

    public void onFinished(String url, String filePath) {
        if (runningOnMainThread()) {
            listener.onFinished(url, filePath);
        } else {
            mH.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFinished(url, filePath);
                }
            });
        }
    }

    public void doFailure(final Throwable throwable) {
        if (runningOnMainThread()) {
            listener.onFailure(throwable);
        } else {
            mH.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFailure(throwable);
                }
            });
        }
    }

    private boolean runningOnMainThread() {
        return DownloadUtil.runningOnMainThread();
    }

    public int updateProgress(long downloadedLength, long contentLength) {
        int progress = DownloadUtil.computeProgress(downloadedLength, contentLength);
        onProgress(progress);
        return progress;
    }
}
