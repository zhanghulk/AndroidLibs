package com.example.aidldemo.aidl;

interface ISslVpnCallback {     
    void applyAppListSslVpnCompeleted(int resultCode, String remark);
    void reportAppSslVpnError(int errorCode, String remark);
}  