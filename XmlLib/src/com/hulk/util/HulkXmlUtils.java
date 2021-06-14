package com.hulk.byod.parser;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.hulk.util.file.TxtFileUtil;

import java.io.File;

/**
 * xml文件
 * Created by zhanghao on 2017/11/3.
 */

public class HulkXmlUtils {

    public static final String TAG = "HulkXmlUtils";

    public static final String ENCODING = "UTF-8";
    //心跳接口请求模板,使用次数多，作缓存
    private static String sHeartbeatReqTemplate = null;

    public static String getHeartbeatReqTemplate(Context context, boolean original) {
        if (sHeartbeatReqTemplate == null) {
            sHeartbeatReqTemplate = readRequestXmlText(context, HulkManager.HEARTBEAT_TRADE_CODE, original);
        }
        return sHeartbeatReqTemplate;
    }

    /**
     * 根据交易码从xml文件中读取提交请求httpBody报文内容字符串
     * @param context
     * @param tranCode
     * @return
     */
    public static String readRequestXmlText(Context context, String tranCode, boolean original){
        String fileName = getAssetReqXmlName(tranCode);
        return readAssetsXmlText(context, fileName, original);
    }

    public static String readResponseDemoXmlText(Context context, String tranCode, boolean original){
        String fileName = getAssetRespDemoXmlName(tranCode);
        return readAssetsXmlText(context, fileName, original);
    }

    /**
     * 从assets文件中取得提交报文内容字符串模板
     * @param fileName
     * @param original 是否保持xml模板文件的原样格式， 为false将去掉换行符及首尾空格
     * @return
     */
    public static String readAssetsXmlText(Context context, String fileName, boolean original){
        //去掉xml的换行符和首尾空格
        String xmlText = TxtFileUtil.readAssetsFile(context, fileName, original);
        Log.i(TAG, "Read assets file: " + fileName+ ", xml text content:\n" + xmlText);
        return xmlText;
    }

    public static String getAssetReqXmlName(String tranCode) {
        return "hulk/" + tranCode + "_REQ.xml";
    }

    public static String getAssetRespDemoXmlName(String tranCode) {
        return "hulk/" + tranCode + "_RESP.xml";
    }

    private static String getDir() {
        return Environment.getExternalStorageDirectory() + File.separator + "hulkXml" + File.separator;
    }

    /**
     * 将响应信息写入SD文件中,可以打开查看
     * @param tranCode
     * @param respText
     * @return
     */
    public static boolean writeRespData(String tranCode, String respText){
        String filName = tranCode + "_RESP.xml";
        String respFilePath = getDir() + filName;
        return writeXmlText(respText, respFilePath);
    }

    private static boolean writeXmlText(String xmlText, String filePath){
        boolean write =  TxtFileUtil.write(xmlText, filePath);
        Log.i(TAG, "writeXmlText: " + xmlText + ", write= " + write);
        if (!write) {
            Log.e(TAG, "writeXmlText failed to filePath: " + filePath);
        }
        return write;
    }
}
