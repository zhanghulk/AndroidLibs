package com.hulk.android.http.conn;

/**
 * Http请求结果 (对应服务器返回的json数据)
 * <p> json:
 *  {"code":0,"msg":"Success","data":"This is response data"}
 *  此处是举例返回数据, 具体的返回数据还更具真实数据确定
 * @author: zhanghao
 * @Time: 2021-03-04 17:35
 */
public class HttpResult<T> {
    public int code = -1;
    public String msg;
    public String detail;
    public Throwable error;
    public T data = null;

    //TODO 此处是举例返回数据, 具体的返回数据还更具真实数据确定

    public HttpResult() {
    }

    public HttpResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public HttpResult(int code, String msg, String detail, Throwable error) {
        this.code = code;
        this.msg = msg;
        this.detail = detail;
        this.error = error;
    }

    public void setData(T data) {
        this.data = data;
    }
}
