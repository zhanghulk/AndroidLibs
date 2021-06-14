package com.hulk.android.http.content;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamTool {

    /**
     * 从流中读取数据
     *
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static byte[] read(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        ByteArrayOutputStream outStream = null;
        try {
            outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            outStream.flush();
            byte[] data = outStream.toByteArray();
            return data;
        } finally {
            inputStream.close();
            if (outStream != null) {
                outStream.close();
            }
        }

    }
}
