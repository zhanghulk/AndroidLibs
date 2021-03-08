package com.hulk.android.http.ssl;

import android.text.TextUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * 不进行域名验证
 * @author: zhanghao
 * @Time: 2021-03-04 13:33
 */
public class NoneHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        //TODO 默认不验证域名,请自定义 HostnameVerifier 验证域名
        return true;
    }
}
