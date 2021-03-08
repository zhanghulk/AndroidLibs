package com.hulk.android.http.content;

/**
 * 输入流写入毁掉接口
 * @author: zhanghao
 * @Time: 2021-03-05 16:25
 */
public interface InputWriteCallback {
    /**
     * 写入进度
     * @param writtenLength 已经写入的字节数
     */
    void onInputWriteProgress(long writtenLength);

    /**
     * 文件写入结束
     * @param code 0表示成功
     * @param msg c错误信息
     * @param error 错误信息
     */
    void onInputWriteFinished(int code, String msg, Throwable error);
}
