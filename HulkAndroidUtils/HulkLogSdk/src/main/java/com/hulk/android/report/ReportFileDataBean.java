package com.hulk.android.report;

import org.json.JSONObject;

public class ReportFileDataBean {
    public JSONObject fileJson;//文件内容信息的json
    public int jsonStrLength = 0;//json字符串长度
    public int contentLength = 0;//文件内容总长度

    public long getJsonStrLength() {
        if (jsonStrLength <= 0 && fileJson != null) {
            jsonStrLength = fileJson.toString().length();
        }
        return jsonStrLength;
    }

    @Override
    public String toString() {
        try {
            return fileJson.get("file_name") + ", contentLength= " + contentLength + ", jsonStrLength= " + jsonStrLength;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
