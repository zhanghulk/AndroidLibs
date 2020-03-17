package com.hulk.byod.ccb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;

public class UassEdgeClientDemo {
	private final static String UASS_EDGE_URL = "http://10.1.1.2:8201/uass-edge/";//终端安全接入服务器地址

    /**
     * Main
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
    	
    	/*
    	 * 1.心跳交易(如终端ID不存在，会根据请求内容自动进行终端注册)
    	 */
    	String tranCode = "TS1609031";
    	boolean reqValid = true;//请求报文是否有效
    	
    	//1.1心跳交易成功
    	String reqText = getPostData(reqValid,tranCode);//请求报文
    	String respText = doPost(tranCode,reqText);
    	writeRespData(reqValid,tranCode,respText);//响应内容写入文件
    	System.out.println("请求报文为:\n" + reqText);
        System.out.println("响应报文为:\n" + respText);
        
        //1.2心跳交易失败
        reqValid = false;//
    	reqText = getPostData(reqValid,tranCode);//请求报文
    	respText = doPost(tranCode,reqText);
    	writeRespData(reqValid,tranCode,respText);//响应内容写入文件
    	System.out.println("请求报文为:\n" + reqText);
        System.out.println("响应报文为:\n" + respText);
        
        /*
         * 2.免认证注册(如终端ID不存在，会根据请求内容自动进行终端注册)
         */
        //2.1免认证成功
   	    tranCode = "TS1609050";
    	reqValid = true;//请求报文是否有效
    	reqText = getPostData(reqValid,tranCode);//请求报文
    	respText = doPost(tranCode,reqText);
    	writeRespData(reqValid,tranCode,respText);//响应内容写入文件
    	System.out.println("请求报文为:\n" + reqText);
        System.out.println("响应报文为:\n" + respText);
        
        //免认证失败 
    	reqValid = false;//请求报文是否有效
    	reqText = getPostData(reqValid,tranCode);//请求报文
    	respText = doPost(tranCode,reqText);
    	writeRespData(reqValid,tranCode,respText);//响应内容写入文件
    	System.out.println("请求报文为:\n" + reqText);
        System.out.println("响应报文为:\n" + respText);
    }
    
    /**
     * Post Request
     * @return
     * @throws Exception
     */
    public static String doPost(String tranCode,String postData) throws Exception {
        String returnString = null;
    	
    	
        URL localURL = new URL(UASS_EDGE_URL);
        URLConnection connection = localURL.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection)connection;
        
        //设定公共请求头属性
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpURLConnection.setRequestProperty("Content-Length", String.valueOf(postData.length()));
        
        //设定终端安全接入服务器专有请求头属性
        httpURLConnection.setRequestProperty("TRANCODE", tranCode);//交易码，后台执行指定交易处理的编码，不同的编码使用不同的交易报文和业务处理逻辑
        httpURLConnection.setRequestProperty("TERMINALID", "TEST_88888888");//终端ID，由手机端程序生成的唯一标识码，每个手机端都不同
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
            outputStream.write(encrypt(postData.getBytes("UTF-8")));
            outputStream.flush();
            
            // outputStreamWriter = new OutputStreamWriter(outputStream);
            //outputStreamWriter.write(postData);
            //outputStreamWriter.flush();
            
            //响应是否正常
            if (httpURLConnection.getResponseCode() != 200) {
                throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }
            
            //取响应头属性
            Map headers = httpURLConnection.getHeaderFields();
            Set<String> keys = headers.keySet();
            for( String key : keys ){
                String val = httpURLConnection.getHeaderField(key);
                System.out.println(key+"    "+val);

                /*
                 * 如果需要，对响应头中的不同属性值进行特殊处理
                 * ...略
                 */
            }
            
            //取得响应XML报文结果
            inputStream = httpURLConnection.getInputStream();            
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);
            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
            
            returnString = new String(decrypt(resultBuffer.toString().getBytes("UTF-8")));
            
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

        return returnString;
    }

    /**
     * 根据交易码从文件中取得提交报文内容字符串
     * @param tranCode
     * @return
     */
    public static String getPostData(boolean reqValid,String tranCode){
    	String rtnObj = null;
    	
		if (tranCode != null && tranCode.length() > 0) {
			String filName = "";
			if(reqValid){
				filName = tranCode + "_REQ.xml";
			}else{
				filName = tranCode + "_REQ_ERROR.xml";
			}
			
			
			String reqFilePath = System.getProperty("user.dir") + File.separator +"bin" + File.separator + filName;
			System.out.println("读取的文件路径:" + reqFilePath);
			FileReader reader = null;
			try {
				File file = new File(reqFilePath);
				if (file.isFile() && file.exists()) { // 判断文件是否存在
					 reader = new FileReader(file);
			        int fileLen = (int)file.length();
			        char[] chars = new char[fileLen];
			        reader.read(chars);
			        rtnObj = String.valueOf(chars);
			        reader.close();
				} else {
					System.out.println("找不到指定的文件");
				}
			} catch (Exception e) {
				System.out.println("读取文件内容出错");
				e.printStackTrace();
			}finally{
				if(reader!=null){
					try{
						reader.close();
					}catch(IOException ioe){
						//出错不处理
					}
				}
			}
		}
    	
    	return rtnObj;
    }
    
    
    /**
     * 将响应信息写入文件中
     * @param tranCode
     * @param respData
     * @return
     */
    public static boolean writeRespData(boolean reqValid,String tranCode,String respData){
    	boolean rtnObj = false;
    	
		if (respData != null && respData.length() > 0) {
			String filName = "";
			if(reqValid){
				filName = tranCode + "_RESP.xml";
			}else{
				filName = tranCode + "_RESP_ERROR.xml";
			}			
			
			
			String respFilePath = System.getProperty("user.dir") + File.separator +"bin" + File.separator + filName;
			System.out.println("写入的文件路径:" + respFilePath);
			FileWriter writer = null;
			try {
				File file = new File(respFilePath);
					writer = new FileWriter(file);
					writer.write(respData);
					writer.close();
					rtnObj = true;
			} catch (Exception e) {
				System.out.println("写入文件内容出错");
				e.printStackTrace();
			}finally{
				if(writer!=null){
					try{
						writer.close();
					}catch(IOException ioe){
						//出错不处理
					}
				}
			}
		}
    	
    	return rtnObj;
    }
    
    /**
     * 字节流加密
     * @param plainData
     * @return
     */
    private static byte[] encrypt(byte[] plainData){
    	byte[] encryptData = null;
    	
    	//加密过程略
    	//...
    	encryptData = plainData;//
    	
    	//返加
    	return encryptData;
    }
 
    /**
     * 解密字节流
     * @param encryptData
     * @return
     */
    private static byte[] decrypt(byte[] encryptData){
    	byte[] plainData = null;
    	
    	//解密过程略
    	//...
    	plainData = encryptData;//
    	
    	//返加
    	return plainData;
    }
}
