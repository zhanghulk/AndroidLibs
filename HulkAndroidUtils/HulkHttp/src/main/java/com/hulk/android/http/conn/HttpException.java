package com.hulk.android.http.conn;

/**
 * 通用网络请求异常
 * @author: zhanghao
 * @Time: 2021-03-05 17:15
 */
public class HttpException extends Exception {
    private int code = -1;
    private String url = "";

    public int getCode() {
        return code;
    }
    public int code() {
        return code;
    }

    public String getUrl() {
        return url;
    }

    public HttpException(int responseCode) {
        this.code = responseCode;
    }
    public HttpException(int responseCode, String url) {
        this.code = responseCode;
        this.url = url;
    }

    public HttpException(int responseCode, String url, String msg) {
        super(msg);
        this.code = responseCode;
        this.url = url;
    }

    @Override
    public String toString() {
        return "HttpException{" +
                "responseCode=" + code +
                ", url='" + url + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
