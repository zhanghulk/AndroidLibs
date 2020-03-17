package com.difflib;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JMD5Util {

	private static final String ALGORIGTHM_MD5 = "MD5";
	private static final int CACHE_SIZE = 2048;
	
	public static String encode(String string) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(string.getBytes());
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			return buf.toString();
			// if (type) {
			// return buf.toString(); // 32
			// } else {
			// return buf.toString().substring(8, 24);// 16
			// }
		} catch (Exception e) {
			return null;
		}
	}

	public static String generateFileMD5(String filePath) throws Exception {
		String md5 = "";
		File file = new File(filePath);
		if (file.exists()) {
			MessageDigest messageDigest = getMD5();
			InputStream in = new FileInputStream(file);
			byte[] cache = new byte[CACHE_SIZE];
			int nRead = 0;
			while ((nRead = in.read(cache)) != -1) {
				messageDigest.update(cache, 0, nRead);
			}
			in.close();
			byte data[] = messageDigest.digest();
			md5 = byteArrayToHexString(data);
		}
		return md5;
	}
	
	public static String generateFileMD5(InputStream in) throws Exception {
		String md5 = "";
		MessageDigest messageDigest = getMD5();
		byte[] cache = new byte[CACHE_SIZE];
		int nRead = 0;
		while ((nRead = in.read(cache)) != -1) {
			messageDigest.update(cache, 0, nRead);
		}
		in.close();
		byte data[] = messageDigest.digest();
		md5 = byteArrayToHexString(data);
		return md5;
	}
	
	public static String getMd5HexString(byte[] data) {
	    String hexStr = null;
	    if (data != null) {
	        hexStr = byteArrayToHexString(data);
	    }
	    return hexStr;
	}
	
	/**
     * <p>
     * MD5摘要字节数组转换为16进制字符串
     * </p>
     * 
     * @param data MD5摘要
     * @return
     */
    private static String byteArrayToHexString(byte[] data) {
        // 用来将字节转换成 16 进制表示的字符
        char hexDigits[] = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' 
        };
        // 每个字节用 16 进制表示的话，使用两个字符，所以表示成 16 进制需要 32 个字符
        char arr[] = new char[16 * 2];
        int k = 0; // 表示转换结果中对应的字符位置
        // 从第一个字节开始，对 MD5 的每一个字节转换成 16 进制字符的转换
        for (int i = 0; i < 16; i++) {
            byte b = data[i]; // 取第 i 个字节
            // 取字节中高 4 位的数字转换, >>>为逻辑右移，将符号位一起右移
            arr[k++] = hexDigits[b >>> 4 & 0xf];
            // 取字节中低 4 位的数字转换
            arr[k++] = hexDigits[b & 0xf];
        }
        // 换后的结果转换为字符串
        return new String(arr);
    }
    
    /**
     * <p>
     * 获取MD5实例
     * </p>
     * 
     * @return
     * @throws NoSuchAlgorithmException 
     */
    private static MessageDigest getMD5() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(ALGORIGTHM_MD5);
    }
    

}
