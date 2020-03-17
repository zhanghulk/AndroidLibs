package com.hulk.byod.ccb.entity;

import com.hulk.byod.ccb.XmlJsonUtils;
import com.hulk.byod.ccb.xml.httpbody.HttpBodyBase;

/**
 * http请求返回值
 * Created by zhanghao on 2017/11/20.
 */

public class CCBHttpResponse {
    public String tranCode = null;//交易码
    /**网络请求错误码*/
    public int errorCode = -1;
    /**网络请求错误信息*/
    public String errorMsg = null;
    /**网络请求成功时，CCB返回xml报文信息*/
    public String xmlText = "";

    public CCBHttpResponse(String tranCode) {
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
        return "CCBHttpResponse{" +
                "tranCode='" + tranCode + '\'' +
                ", errorCode=" + errorCode +
                ", errorMsg='" + errorMsg + '\'' +
                ", xmlText='" + xmlText + '\'' +
                '}';
    }
}
