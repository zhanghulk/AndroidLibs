package com.example.aidldemo.aidl;

import com.example.aidldemo.aidl.ServiceAidlCallback;
interface ServiceAidl {  
    void registerTestCall(ServiceAidlCallback cb);  
    void invokCallBack();  
}