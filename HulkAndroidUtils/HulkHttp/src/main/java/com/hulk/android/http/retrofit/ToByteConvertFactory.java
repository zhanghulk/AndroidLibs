package com.hulk.android.http.retrofit;

import android.util.Log;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * 字节数据零转换器
 * @author：ltc
 */
public class ToByteConvertFactory extends Converter.Factory{
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/octet-stream");
    private static final String TAG = "ToByteConvertFactory";
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        Log.v(TAG, "convert: Converter<?, RequestBody>000" +type+"    "+byte[].class);
        if("byte[]".equals(type+"")){
            return new Converter<ResponseBody, byte[]>() {
                @Override
                public byte[] convert(ResponseBody body) throws IOException {
                    Log.i(TAG, "responseBodyConverter.convert: contentLength=" + body.contentLength() + ", contentType=" + body.contentType());
                    return body.bytes();
                }
            };
        }
        return super.responseBodyConverter(type, annotations, retrofit);
    }
    
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        Log.i(TAG, "convert: " +type );
        if("byte[]".equals(type+"")){
            return new Converter<byte[], RequestBody>() {
                @Override
                public RequestBody convert(byte[] content) throws IOException {
                    Log.i(TAG, "requestBodyConverter.convert: content.length= " + content.length);
                    return RequestBody.create(MEDIA_TYPE, content);
                }
            };
        }
        return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
    }

    public static ToByteConvertFactory create() {
        return new ToByteConvertFactory();
    }
}