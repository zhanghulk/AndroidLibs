package com.hulk.byod.parser;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.util.Log;

import com.hulk.byod.parser.xml.httpbody.AuthRequestHttpBody;
import com.hulk.byod.parser.xml.httpbody.HeartbeatRequestHttpBody;
import com.hulk.byod.parser.xml.httpbody.RegisterRequestHttpBody;
import com.hulk.util.common.NetworkInfoUploader;
import com.hulk.util.common.PackageUtils;
import com.hulk.byod.parser.entity.ActivityJsonArray;
import com.hulk.byod.parser.entity.RegRequest;
import com.hulk.byod.parser.entity.RegRequestJsonArray;
import com.hulk.byod.parser.entity.TerminalInfo;
import com.hulk.byod.parser.xml.msg.AuthRequestTx;
import com.hulk.byod.parser.xml.msg.base.RequestTXHeader;

/**
 * Created by zhanghao on 2017/10/17.
 */

public class HulkManager {

    public final static String TAG = "Hulk";

    //TRADE_CODE:
    public final static String AUTH_TRADE_CODE = "TS1604051"; //组合认证
    public final static String HEARTBEAT_TRADE_CODE = "TS1609031"; //心跳保持在线
    public final static String REGISTER_TRADE_CODE = "TS1609050"; //免认证注册

    /**
     * 组合认证http请求body
     * @param context
     * @return
     */
    public static AuthRequestHttpBody getAuthRequestHttpBody(Context context, String username, String password) {
        RequestTXHeader header = getRequestTXHeader(context, AUTH_TRADE_CODE);
        AuthRequestTx.Entry entry = getAuthReqHttpBodyEntry(username, password);
        AuthRequestHttpBody body = new AuthRequestHttpBody(header, entry);
        Log.i(TAG, "AuthHttpRequestBody: " + body);
        return body;
    }

    /**
     * 组合认证请求body
     * @return
     */
    private static AuthRequestTx.Entry getAuthReqHttpBodyEntry(String username, String password) {
        // TODO 值从何而来 ?? 需要服务端提供解释说明
        AuthRequestTx.Entry entry = new AuthRequestTx.Entry();
        entry.AUTH_TYPE = "15";
        entry.LOGIN_NAME = username;
        entry.SEC_VOUCHER = password;
        entry.IS_CREATE_TICKET = "true";
        entry.IS_GET_LOGINED_APP = "true";
        entry.SERVICE_POINT = "010200300";
        return entry;
    }

    /**
     * 心跳保持在线http请求body
     * @param context
     * @param onlineStatus 终端在线状态 1：终端网络在线（但用户未登录）2：用户在线（用户已登录）,通常为2
     * @return
     */
    public static HeartbeatRequestHttpBody getHeartbeatRequestHttpBody(Context context, String username, int onlineStatus) {
        RequestTXHeader header = HulkManager.getRequestTXHeader(context, HulkManager.HEARTBEAT_TRADE_CODE);
        TerminalInfo terminalInfo = getTerminalInfo(username, onlineStatus);
        ActivityJsonArray array = HulkLogManager.getInstance().readArray();
        HeartbeatRequestHttpBody body = new HeartbeatRequestHttpBody(header, terminalInfo, array);
        Log.i(TAG, "HeartbeatRequestHttpBody: " + body);
        return body;
    }

    /**
     * 免认证注册http请求body
     * @param context
     * @param username
     * @param password
     * @param mobile  手机号
     * @return
     */
    public static RegisterRequestHttpBody getRegisterRequestHttpBody(Context context, String username, String password, String mobile) {
        RequestTXHeader header = getRequestTXHeader(context, REGISTER_TRADE_CODE);
        String op_type = "FREE";//免认证
        RegRequestJsonArray requestArray = new RegRequestJsonArray();
        requestArray.add(getRegRequest(username, password, mobile));
        RegisterRequestHttpBody body = new RegisterRequestHttpBody(header, op_type, requestArray);
        Log.i(TAG, "RegisterRequestHttpBody: " + body);
        return body;
    }

    public static RegRequest getRegRequest(String username, String password, String mobile) {
        RegRequest request = new RegRequest(username, password, mobile);
        request.HOSTNAME = Build.MANUFACTURER + "[" + getIP() + "]";
        request.OPERATING_SYSTEM = Build.PRODUCT + " " + Build.VERSION.SDK + "[" + Build.VERSION.SDK_INT + "]";
        request.MODELS = Build.MODEL;
        request.DESCRIBE = "FREE register Hulk";
        return request;
    }

    public static RequestTXHeader getRequestTXHeader(Context context, String tradeCode) {
        RequestTXHeader header = new RequestTXHeader(tradeCode);
        header.CLIENT_VERSION = getClientVetionName(context);
        header.CLIENT_IP = getIP();
        header.NETWORK_CARD_MAC = getMAC();
        return header;
    }

    /**
     * 终端信息
     * @param username
     * @param onlineStatus 终端在线状态 1：终端网络在线（但用户未登录）2：用户在线（用户已登录）,通常为2
     * @return
     */
    public static TerminalInfo getTerminalInfo(String username, int onlineStatus) {
        TerminalInfo info = new TerminalInfo();
        info.USER_NAME = username;
        info.ONLINE_STATUS = onlineStatus + "";
        info.HOSTNAME = getHoshName();
        info.OPERATING_SYSTEM = getOperatingSystem();
        info.MODELS = getModels();
        return info;
    }

    public static String getHoshName() {
        return Build.MANUFACTURER + "[" + getIP() + "]";
    }

    public static String getOperatingSystem() {
        return "Android " + Build.VERSION.RELEASE + "[" + Build.VERSION.SDK_INT + "]";
    }

    public static String getModels() {
        return Build.MODEL;
    }

    public static String getIP() {
        return NetworkInfoUploader.getIPAddress(true);
    }

    public static String getMAC() {
        return NetworkInfoUploader.getMACAddress(/*"wlan0"*/null);
    }

    public static String getClientVetionName(Context context) {
        PackageInfo info = PackageUtils.getPackageInfoByPkgName(context, context.getPackageName());
        return info != null ? info.versionName : "";
    }

    public static int getClientVetionCode(Context context) {
        PackageInfo info = PackageUtils.getPackageInfoByPkgName(context, context.getPackageName());
        return info != null ? info.versionCode : 0;
    }
}
