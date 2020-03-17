
package com.difflib;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.util.Log;

public class DiffDataUtils {

    private final static String TAG = "DiffDataUtils";

    /**
     * Write bytes data to file
     * @param data to dave data bytes
     * @param filePath
     * @author zhanghao@126.cn
     * @since 2016-4-11
     * @return true if successfully, or false.
     */
    public static boolean writeToFile(byte[] data, String filePath) {
        if (data == null || data.length == 0) {
            Log.w(TAG, "writeToFile the data array is null or Empty !! ");
            return false;
        }
        File file = new File(filePath);
        OutputStream outputStream = null;
        FileOutputStream fos = null;
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            fos = new FileOutputStream(file);
            outputStream = new BufferedOutputStream(fos);
            outputStream.write(data);
            Log.i(TAG, "writen data count= " + data.length + ", to file: " + filePath);
            return true;
        } catch (FileNotFoundException e) {
            Log.e(TAG , "writeToFile FileNotFoundException: " + e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "writeToFile IOException: " + e + ", filePath " + filePath);
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return false;
    }
    
    /**
     * un-gzip data bytes at first and write data to file.
     * @param gzipedData to be decode 
     * @param filePath
     * @author zhanghao@126.cn
     * @since 2016-4-11
     * @return true if successfully, or false.
     * @throws IOException 
     */
    public static boolean writeGzipDataToFile(byte[] gzipedData, String filePath) throws IOException {
        String text = decodeGzipData(gzipedData);
        if (text == null || text.isEmpty()) {
            Log.w(TAG, "writeGzipDataToFile the data array is null or Empty !! ");
        }
        return writeToFile(text.getBytes(), filePath);
    }

    /**
     * write text lines to file.
     * @param textLines
     * @param filePath
     * @author zhanghao@126.cn
     * @since 2016-4-11
     */
    public static boolean writeToFile(List<String> textLines, String filePath) {
        if (textLines == null || textLines.isEmpty()) {
            Log.w(TAG, "writeToFile the textLines is Empty !! ");
            return false;
        }
        StringBuffer buf = new StringBuffer();
        for (String line : textLines) {
            buf.append(line).append("\n");
        }
        return writeToFile(buf.toString().getBytes(), filePath);
    }

    public static String readFileText(String filePath) throws FileNotFoundException {
        StringBuffer buf = new StringBuffer();
        List<String> textLines = readFileLines(filePath);
        if (textLines != null) {
            for (String line : textLines) {
                buf.append(line).append("\n");
            }
        }
        return buf.toString();
    }

    public static String readFileTextNoException(String filePath) {
        StringBuffer buf = new StringBuffer();
        List<String> textLines = null;
        try {
            textLines = readFileLines(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (textLines != null) {
            for (String line : textLines) {
                buf.append(line).append("\n");
            }
        }
        return buf.toString();
    }

    public static List<String> readFileLines(String filePath) throws FileNotFoundException {
        FileInputStream fis = null;
        BufferedReader reader = null;
        List<String> lines = new ArrayList<String>();
        try {
            fis = new FileInputStream(new File(filePath));
            reader = new BufferedReader(new InputStreamReader(fis));
            String readedLine;
            while ((readedLine = reader.readLine()) != null) {
                lines.add(readedLine);
            }
        } catch (FileNotFoundException e1) {
            Log.e(TAG, "readFileLines: " + e1 + ", filePath " + filePath);
            throw e1;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "readFileLines IOException: " + e + ", filePath " + filePath);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return lines;
    }

    public static String readFileTextFromAssets(Context context, String fileName) throws FileNotFoundException {
        InputStream ais = null;
        StringBuffer sb = new StringBuffer();
        try {
            ais = context.getAssets().open(fileName);

            byte[] buffer = new byte[1024 * 4];
            int readed;
            int count = 0;
            while ((readed = ais.read(buffer)) != -1) {
                count += readed;
                String string = new String(buffer);
                sb.append(string);
            }
            Log.i(TAG, "readFileTextFromAssets byte count= " + count + " from: " + fileName);
        } catch (FileNotFoundException e1) {
            Log.e(TAG, "readFileTextFromAssets: " + e1 + ", fileName " + fileName);
            throw e1;
        } catch (Exception e) {
            Log.i(TAG, "readFileTextFromAssets Exception: " + e);
            e.printStackTrace();
        } finally {
            if (ais != null) {
                try {
                    ais.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public static List<String> readFileLinesFromAssets(Context context, String fileName) {
        InputStream ais = null;
        BufferedReader reader = null;
        List<String> lines = new ArrayList<String>();
        int lineCount = 0;
        try {
            ais = context.getAssets().open(fileName);
            reader = new BufferedReader(new InputStreamReader(ais));
            String readedLine;
            while ((readedLine = reader.readLine()) != null) {
                lines.add(readedLine);
            }
            Log.i(TAG, "readFileLinesFromAssets lineCount= " + lineCount + " from: " + fileName);
        } catch (Exception e) {
            Log.i(TAG, "readFileLinesFromAssets Exception: " + e);
            e.printStackTrace();
        } finally {
            try {
                if (ais != null) {
                    ais.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return lines;
    }

    /**
     * decode bytes data of text as text
     * @param data the bytes data of text
     * @author zhanghao@126.cn
     * @throws IOException 
     * @since 2016-4-11
     */
    public static String decodeData(byte[] data) throws IOException {
        return decodeData(data, false);
    }

    /**
     * decode bytes data of text as text
     * @param data the bytes data of text
     * @param isGzipedData true if the bytes data is gziped
     * @author zhanghao@126.cn
     * @throws IOException 
     * @since 2016-4-11
     */
    public static String decodeData(byte[] data, boolean isGzipedData) throws IOException {
        StringBuffer buf = new StringBuffer();
        List<String> lines = decodeDataLines(data, isGzipedData);
        if (lines != null) {
            for (String line : lines) {
                buf.append(line).append("\n");
            }
        }
        return buf.toString();
    }

    /**
     * decode bytes data of text as text
     * @param data the bytes data of text
     * @author zhanghao@126.cn
     * @throws IOException 
     * @since 2016-4-11
     */
    public static List<String> decodeDataLines(byte[] data) throws IOException {
        return decodeDataLines(data, false);
    }

    /**
     * decode bytes data of text as text
     * @param data the bytes data of text
     * @param isGzipedData true if the bytes data is gziped
     * @author zhanghao@126.cn
     * @throws IOException 
     * @since 2016-4-11
     */
    public static List<String> decodeDataLines(byte[] data, boolean isGzipedData) throws IOException {
        List<String> lines = new ArrayList<String>();
        if (data == null) {
            return lines;
        }
        InputStream input = null;
        BufferedReader reader = null;
        try {
            if (isGzipedData) {
                input = new GZIPInputStream(new ByteArrayInputStream(data));
            } else {
                input = new DataInputStream(new ByteArrayInputStream(data));
            }
            reader = new BufferedReader(new InputStreamReader(input));
            String readed;
            while ((readed = reader.readLine()) != null) {
                lines.add(readed);
            }
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return lines;
    }

    public static String decodeGzipBytes(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(new ByteArrayInputStream(bytes))));
            String readed;
            while ((readed = reader.readLine()) != null) {
                sb.append(readed).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            sb.setLength(0);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e2) {
                    // TODO: handle exception
                }
            }
        }
        return sb.toString();
    }

    public static List<String> decodeGzipDataLines(byte[] gzipedData) throws IOException {
        return decodeDataLines(gzipedData, true);
    }

    /**
     * decode 
     * @param gzipedData the gziped bytes data
     * @author zhanghao@126.cn
     * @throws IOException 
     * @since 2016-4-11
     */
    public static String decodeGzipData(byte[] gzipedData) throws IOException {
        return decodeData(gzipedData, true);
    }
    
    /**
     * copy file from assets to data dir.
     * @param context
     * @param fileName  file name in assets
     * @param dstDir  to copy destination dir
     * @return saved file path, null if failed. return error message if exception.
     */
    public static String copyAssetsFileToDir(Context context, String fileName, String destDir) {
        Exception ex = null;
        try {
            InputStream ais = context.getAssets().open(fileName);

            File dstDir = new File(destDir);
            if (!dstDir.exists())
                dstDir.mkdirs();
            File dstFile = new File(dstDir, fileName);
            if (dstFile.exists()) {
                dstFile.delete();
            }
            FileOutputStream fos = new FileOutputStream(dstFile);

            byte[] buffer = new byte[1024 * 2];
            int readed;
            int count = 0;

            try {
                while ((readed = ais.read(buffer)) != -1) {
                    fos.write(buffer, 0, readed);
                    count += readed;
                }
                Log.i(TAG, "copyAssetsFile count= " + count + ", to file: " + dstFile);
            } catch (Exception e) {
                e.printStackTrace();
                ex = e;
                Log.i(TAG, "copyAssetsFile write Exception: " + e);
            } finally {
                ais.close();
                fos.close();
            }

            return dstFile.toString();

        } catch (Exception e) {
            e.printStackTrace();
            ex = e;
            Log.i(TAG, "copyAssetsFile Exception: " + e);
        }
        return ex != null ? ex.toString() : null;
    }

    public static boolean copyFile(String srcFilePath, String dstFilePath) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            int byteread = 0;
            File oldfile = new File(srcFilePath);
            if (oldfile.exists()) {
                oldfile.delete();
                fis = new FileInputStream(srcFilePath); 
                File outFile = new File(dstFilePath);
                outFile.createNewFile();
                outFile.setReadable(true);
                outFile.setWritable(true);
                fos = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024 * 4];
                while ((byteread = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteread);
                }
                fos.flush();
                fis.close();
                fos.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
/*    public static boolean copyUrlFile(Context context, String fromFileUrl, String toFileUrl) {
        try {
            FileInputStream fosfrom = context.openFileInput(fromFileUrl);
            FileOutputStream fosto = context.openFileOutput(toFileUrl, Context.MODE_PRIVATE);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c); 
            }
            // 关闭数据流
            fosfrom.close();
            fosto.close();
        } catch (Exception e) {
            return false;

        }
        return true;
    }*/
}
