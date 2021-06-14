package com.hulk.android.http.image;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * 图片加载毁掉接口
 * @author: zhanghao
 * @Time: 2021-03-04 20:22
 */
public interface ImageLoadCallback {
    /**
     * 加载成功
     * @param url
     * @param view
     * @param bitmap
     */
    void onImageLoadSuccess(String url, ImageView view, Bitmap bitmap);

    /**
     * 加载失败
     * @param url
     * @param view
     */
    void onImageLoadFailed(String url,ImageView view);
}
