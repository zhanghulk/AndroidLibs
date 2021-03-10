package com.hulk.byod.parser.net;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hulk.util.common.Base64Decoder;
import com.hulk.util.common.TerminelManager;
import com.hulk.byod.parser.HulkEncryptUtils;
import com.hulk.byod.parser.HulkXmlUtils;
import com.hulk.byod.parser.entity.HulkHttpResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhanghao on 2017/10/24.
 */

public class HulkHttpUtils {

    protected final static String TAG = "HulkHttpUtils";
    protected final static String ENCODING_CHARSET = "UTF-8";

    protected final static boolean TEST_MODE = true;//本地测试模式，直接返回demo数据

    /**
     * 远程调试返回本地demo数据
     * @param context
     * @return
     */
    public static HulkHttpResponse testHttpRequest(Context context, String tranCode, String postText) {
        Log.i(TAG, "testHttpRequest tranCode: " + tranCode + ", postText: " + postText);
        HulkHttpResponse response = new HulkHttpResponse(tranCode);
        response.errorCode = 0;
        response.xmlText = HulkXmlUtils.readResponseDemoXmlText(context, tranCode, false);
        Log.i(TAG, "testHttpRequest response: " + response);
        return response;
    }

    /**
     * 网络请求（含加密/解密）
     * @param context
     * @return
     */
    public static HulkHttpResponse startHttpRequest(Context context, String tranCode, String postText) {
        Log.i(TAG, "startHttpRequest tranCode: " + tranCode + ", postText: " + postText);
        HulkHttpResponse response = new HulkHttpResponse(tranCode);
        byte[] encData = HulkEncryptUtils.encryptText(postText);
        if (encData == null) {
            Log.w(TAG, "startHttpRequest canceled: post text encrypted data is null !! ");
            return response;
        }
        try {
            String resultEncText = doPost(context, tranCode, encData);
            if (!TextUtils.isEmpty(resultEncText)) {
                String decodedText = Base64Decoder.decode(resultEncText);
                if (!TextUtils.isEmpty(decodedText)) {
                    String clearText = HulkEncryptUtils.decryptText(decodedText);
                    response.xmlText = clearText;
                    if (!TextUtils.isEmpty(clearText)) {
                        HulkXmlUtils.writeRespData(tranCode, clearText);
                    } else {
                        Log.w(TAG, "startHttpRequest Decrypt failed for text: " + decodedText);
                    }
                } else {
                    Log.w(TAG, "startHttpRequest Base64 decode failed for text: " + resultEncText);
                }
            } else {
                Log.w(TAG, "startHttpRequest failed for post result text is null !! ");
            }
        } catch (Exception e) {
            Log.e(TAG, "startHttpRequest Exception: " + e, e);
        } finally {
            Log.i(TAG, "startHttpRequest response: " + response);
            return response;
        }
    }

    /**
     * Post http Request
     * @param context
     * @param tranCode 交易码
     * @param postData  post byte 数组
     * @return
     * @throws Exception
     */
    public static String doPost(Context context, String tranCode, byte[] postData) throws Exception {
        if (postData == null) {
            Log.w(TAG, "doPost canceled for postData is null !! ");
            throw new IllegalArgumentException("doPost canceled for postData is null");
        }
        String url = "https://www.baidu.com/aa";
        URL localURL = new URL(url);
        URLConnection connection = localURL.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection)connection;

        int length = 0;
        if (postData != null) {
            length = postData.length;
        }

        //设定公共请求头属性
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpURLConnection.setRequestProperty("Content-Length", String.valueOf(length));

        //设定终端安全接入服务器专有请求头属性
        httpURLConnection.setRequestProperty("TRANCODE", tranCode);//交易码，后台执行指定交易处理的编码，不同的编码使用不同的交易报文和业务处理逻辑
        httpURLConnection.setRequestProperty("TERMINALID", TerminelManager.getTerminelID(context));//终端ID，由手机端程序生成的唯一标识码，每个手机端都不同
        httpURLConnection.setRequestProperty("FROM_AREA", "1");//来源区域 1.内网 2.互联网,此标识通过探测行方提供的指定的网址是否可达来填写
        httpURLConnection.setRequestProperty("CALLER", "MT");//接口调用者 MT:手机终端 FT:固定终端, 手机端固定为MT
        httpURLConnection.setRequestProperty("ENCRYPT_FLAG", "1");//加密标识 0:不加密 1:加密，手机端固定上使用1；

        OutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;

        try {
            outputStream = httpURLConnection.getOutputStream();
            if (postData != null) {
                outputStream.write(postData);
            } else {
                Log.w(TAG, "postData is null !! ");
            }
            outputStream.flush();

            //can write char[] data by OutputStreamWriter
            //outputStreamWriter = new OutputStreamWriter(outputStream);
            //outputStreamWriter.write(postData);
            //outputStreamWriter.flush();

            //响应是否正常
            if (httpURLConnection.getResponseCode() != 200) {
                throw new Exception("HTTP POST Request is failed, Response code is " + httpURLConnection.getResponseCode());
            }

            //取响应头属性
            //如果需要，对响应头中的不同属性值进行特殊处理
            Map headers = httpURLConnection.getHeaderFields();
            if (headers != null) {
                Set<String> keys = headers.keySet();
                for(String key : keys){
                    String val = httpURLConnection.getHeaderField(key);
                    Log.i(TAG, "Header key: " + key + ", value: " + val);
                }
            }

            //取得响应XML报文结果
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);
            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
            byte[] resultData = resultBuffer.toString().getBytes(ENCODING_CHARSET);
            Log.w(TAG, "doPost failed: result text data is null !! ");
            return new String(resultData);
        } finally {

            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }

            if (reader != null) {
                reader.close();
            }

            if (inputStreamReader != null) {
                inputStreamReader.close();
            }

            if (inputStream != null) {
                inputStream.close();
            }

        }
    }
}
