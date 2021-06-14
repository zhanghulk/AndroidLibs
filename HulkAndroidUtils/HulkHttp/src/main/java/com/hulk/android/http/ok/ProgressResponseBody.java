package com.hulk.android.http.ok;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 下载进度响应
 * @author: zhanghao
 * @Time: 2021-02-24 21:58
 */
public class ProgressResponseBody extends ResponseBody {
    private final ResponseBody responseBody;
    private final ProgressListener progressListener;
    private BufferedSource bufferedSource;
    private Response response;

    public ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener){
        this.responseBody = responseBody;
        this.progressListener = progressListener;
        onPreExecute();
    }

    public ProgressResponseBody(Response response, ProgressListener progressListener){
        this.response = response;
        this.responseBody = response.body();
        this.progressListener = progressListener;
        onPreExecute();
    }

    private void onPreExecute() {
        if (progressListener != null) {
            progressListener.onPreExecute(contentLength(), response);
        }
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null){
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    /**
     * 回调下载精度函数
     * @param source
     * @return
     */
    private Source source(Source source){
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = 0;
                try {
                    bytesRead = super.read(sink,byteCount);
                    //不断统计当前下载好的数据
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    boolean done = bytesRead == -1;
                    //接口回调
                    if (progressListener != null) {
                        progressListener.update(totalBytesRead, done, response);
                    }
                } catch (IOException e) {
                    if (progressListener != null) {
                        progressListener.onError(e, response);
                    }
                    throw e;
                }
                return bytesRead;
            }
        };
    }
}
