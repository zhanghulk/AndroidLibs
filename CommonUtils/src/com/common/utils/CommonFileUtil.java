package com.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.os.Environment;

public class CommonFileUtil {
	public static final String SDCARD_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath();
	public static final String IMG_PATH = SDCARD_PATH + "/Quyou/imgView/";
	public static final String IMAGES_PATH = SDCARD_PATH + "/Quyou/images/";

	public static String getImgPath() {
		File file = new File(IMG_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
		return IMG_PATH;
	}

	public static String getImgTmpPath() {
		File file = new File(IMG_PATH + "tmp/");
		if (!file.exists()) {
			file.mkdirs();
		}
		return file.getAbsolutePath();
	}

	public static String getImageDir() {
        return getDir(IMAGES_PATH);
    }

	public static String getDir(String dir) {
        if (makeDir(dir)) {
            return dir;
        }
        return SDCARD_PATH;
    }

	public static boolean makeDir(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }
	
	/**
	 * 获取文件名 格式为：20140718_221839.jpg
	 * @return
	 */
	public static String getFileName(){
		return DateTimeUtil.formatNowTimeText() + ".jpg";
	}

	public static int getFileSize(File file){
		int byteSize = 0;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			byteSize = fis.available();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return byteSize;
	}
}
