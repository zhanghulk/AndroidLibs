package com.example.aidldemo.aidl;
import com.example.aidldemo.aidl.ISslVpnCallback;
interface ISslVpn {     
    boolean updateAppList(String sslVpnConfig);
    void registerCallback(ISslVpnCallback cb);
}  