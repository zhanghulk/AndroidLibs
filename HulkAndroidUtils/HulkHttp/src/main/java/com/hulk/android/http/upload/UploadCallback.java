package com.hulk.android.http.upload;

import com.hulk.android.http.conn.HttpResult;

/**
 * 上传回调接口
 * @author: zhanghao
 * @Time: 2021-03-04 18:25
 */
public interface UploadCallback {
    /**
     * 上传灰调函数
     * @param result
     */
    void onUpload(HttpResult result);
}
