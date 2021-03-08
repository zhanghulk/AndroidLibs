package com.hulk.android.report;

/**
 * 上传文件(文本文件或者压缩文件文件)数据对象,通常为压缩文件
 */
public class FileDataBean {
    /**
     * 文件名
     */
    public String file_name;
    /**
     * 文件内容长度
     */
    public long file_length = 0;
    /**
     * 文件内容的base64编码
     */
    public String content;
    /**
     * 文件内容base64编码后的长度
     */
    public long content_length;

    @Override
    public String toString() {
        return "FileDataBean{" +
                "file_name='" + file_name + '\'' +
                ", file_length=" + file_length +
                ", content_length=" + content_length +
                //", content='" + content + '\'' +
                '}';
    }
}
