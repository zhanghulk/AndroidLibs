package com.hulk.byod.parser.entity;

/**
 * http请求返回值
 * Created by zhanghao on 2017/11/20.
 */

public class HulkHttpResponse {
    public String tranCode = null;//交易码
    /**网络请求错误码*/
    public int errorCode = -1;
    /**网络请求错误信息*/
    public String errorMsg = null;
    /**网络请求成功时，Hulk返回xml报文信息*/
    public String xmlText = "";

    public HulkHttpResponse(String tranCode) {
        this.tranCode = tranCode;
    }

    public boolean isSuccess() {
        return errorCode == 0;
    }

    public String getErrorMsg() {
        return errorMsg + "[" + errorCode + "]";
    }

    @Override
    public String toString() {
        return "HulkHttpResponse{" +
                "tranCode='" + tranCode + '\'' +
                ", errorCode=" + errorCode +
                ", errorMsg='" + errorMsg + '\'' +
                ", xmlText='" + xmlText + '\'' +
                '}';
    }
}
