package com.hulk.util.file;

import com.hulk.util.common.FileUtils;
import com.hulk.util.file.TxtFile;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

/**
 * 打印log信息到txt文件中. 日志文件名称根据timeMode分为两种模式：
 * <p>1. false：日期模式 yyyyMMdd.txt 默认模式， eg： 20180118.txt.
 * <p>2. true： 时间模式： yyyyMMdd_HHmmss.txt， eg： 20180118_080324.txt.
 * <p> 可以给日志文件夹前缀,eg: vpn, 日志文件为vpn-20180118.txt or vpn-20180118_080324.txt.
 * Created by zhanghao on 2018-1-17, modify 2020-02-19
 */

public class PrintLog {
	/**
     * log level
     */
    public enum LogLevel {
        V,
        D,
        I,
        W,
        E
    }
    
    public static String TAG = "PrintLog";
    /**默认每个文件最大5M  */
    public static final long FILE_LENGTH_LIMIT = 1024 * 1024 * 5;
    /**日志文件个数最大数量*/
    public static final int LOG_MAX_FILE_COUNT = 5;
    
    /**
     * 默认缓存大小 ，超过之后立刻写入，提高运行效率
     */
    public static final int DEFAULT_BUFFER_LENGTH = 1024 * 4;
    
    /**
     * 最大缓存大小 ，超过之后立刻写入，必须清空缓存，避免OOM
     */
    public static final int MAX_BUFFER_LENGTH = 1024 * 1024;
    
    public static final String FILE_EXTENSION = ".txt";
    
    /**locked标记的log文件不会被自动删除*/
    public static final String LOCKED_FLAG = "locked";
    
    File mDir;
    TxtFile mTxtFile;
  //是否为缓冲区模式，可以减少IO此时，自己控制缓冲区，可以提高效率
    boolean bufferMode = false;
    int bufferLength = DEFAULT_BUFFER_LENGTH;
    StringBuffer buffer;
    //是否每次新起一行
    boolean lineMode = true;
    
    //默认使用日期模式
    boolean fileNameTimeMode = false;
    String fileExtension = FILE_EXTENSION;
    String logPrefix;//log文件前缀
    String logFilename;//log文件名
    
    //limit max file number in dir, but 0 is not to limit
    int maxFileCount = LOG_MAX_FILE_COUNT;
    int logFileCount;//log文件数量
    String[] logFilenames;//单签目录下文件数组
    
    //限制文件大小,最近文件进行判断穿件文件
    long maxFileLength = FILE_LENGTH_LIMIT;

    public PrintLog(String dir) {
    	this.mDir = new File(dir);
    }

    public PrintLog(String dir, boolean bufferMode, int bufferLength) {
    	this.mDir = new File(dir);
        this.bufferMode = bufferMode;
        this.bufferLength = bufferLength;
    }
    
    public PrintLog(TxtFile file, boolean bufferMode, boolean lineMode) {
    	this.mTxtFile = file;
    	this.mDir = getParentDirFile();
        this.bufferMode = bufferMode;
        this.lineMode = lineMode;
    }
    
    public void setMaxFileCount(int maxFileCount) {
    	this.maxFileCount = maxFileCount;
    }
    
    public int getMaxFileCount() {
    	return this.maxFileCount;
    }
    
    public int getLogFileCount() {
    	return this.logFileCount;
    }
    
    public String[] getLogFilenames() {
    	return this.logFilenames;
    }
    
    public void setMaxFileLength(long maxFileLength) {
    	this.maxFileLength = maxFileLength;
    }
    
    public long getMaxFileLength() {
    	return this.maxFileLength;
    }
    
    public void setLogPrefix(String logPrefix) {
    	this.logPrefix = logPrefix;
    }
    
    public String getLogPrefix() {
    	return this.logPrefix;
    }
    
    public void setLogFilemane(String logFilename) {
    	this.logFilename = logFilename;
    }
    
    public void setCharsetName(String charsetName) {
    	if(mTxtFile != null) {
    		mTxtFile.setCharsetName(charsetName);
    	}
	}
    
    public void setFileExtension(String extension) {
    	fileExtension = extension;
	}
    
    public void setFileNameTimeMode(boolean timeMode) {
    	fileNameTimeMode = timeMode;
    }
    
    public void setBufferMode(boolean bufferMode) {
    	this.bufferMode = bufferMode;
    	ensureBuffer();
    }
    
    public void setBufferLength(int bufferLength) {
    	this.bufferLength = bufferLength;
    }
    
    public void setLineMode(boolean lineMode) {
    	this.lineMode = lineMode;
    }

    /**
     * 打印log到文件中:
     * <p>文件超限制，重新创建一个文件
     * <p>文件数量过多，删除旧的文件
     * @param text 日志信息
     * @return 是否打印成功
     * @return
     */
    public boolean printLog(String text) throws Exception {
        return printLog(LogLevel.I, TAG, text);
    }

    /**
     * 打印log到文件中:
     * <p>文件超限制，重新创建一个文件
     * <p>文件数量过多，删除旧的文件
     * @param level 日志级别
     * @param tag  日志TAG
     * @param text 日志信息
     * @return 是否打印成功
     * @return
     */
    public boolean printLog(LogLevel level, String tag, String text){
        return printLog(level, tag, text, null, null);
    }
    
    /**
     * 打印log到文件中:
     * <p>文件超限制，重新创建一个文件
     * <p>文件数量过多，删除旧的文件
     * @param level 日志级别
     * @param tag  日志TAG
     * @param text 日志信息
     * @param threadInfo  线程信息，可包含进程号等等,建议格式pid/tid 如果为空默认:tName-tid(java不方便获取进程号)
     * @return 是否打印成功
     * @return
     */
    public boolean printLog(LogLevel level, String tag, String text, String threadInfo){
        return printLog(level, tag, text, threadInfo, null);
    }
    
    /**
     * 打印log到文件中:
     * <p>文件超限制，重新创建一个文件
     * <p>文件数量过多，删除旧的文件
     * @param level 日志级别
     * @param tag  日志TAG
     * @param text 日志信息
     * @param e 异常堆栈
     * @return 是否打印成功
     * @return
     */
    public boolean printLog(LogLevel level, String tag, String text, Throwable e){
        return printLog(level, tag, text, null, e);
    }
    
    /**
     * 打印log到文件中:
     * <p>文件超限制，重新创建一个文件
     * <p>文件数量过多，删除旧的文件
     * @param level 日志级别
     * @param tag  日志TAG
     * @param text 日志信息
     * @param threadInfo  线程信息，可包含进程号等等,建议格式pid/tid 如果为空默认:tName-tid(java不方便获取进程号)
     * @param e 异常堆栈
     * @return 是否打印成功
     * @return
     */
    public boolean printLog(LogLevel level, String tag, String text, String threadInfo, Throwable e){
        try {
            String levelStr = String.valueOf(level);
            printLog(levelStr, tag, text, threadInfo, e);
            return true;
        } catch (Exception ex) {
            PrintUtil.e(TAG, "printLog failed: " + ex, ex);
        }
        return false;
    }
    
    /**
     * 打印日志到文件中
     * <p>文件超限制，重新创建一个文件
     * <p>文件数量过多，删除旧的文件
     * @param level 日志级别
     * @param tag  日志TAG
     * @param text 日志信息
     * @param threadInfo  线程信息，可包含进程号等等,建议格式pid/tid 如果为空默认:tName-tid(java不方便获取进程号)
     * @return 是否打印成功
     * @throws Exception
     */
    public boolean printLog(String level, String tag, String text, String threadInfo) throws Exception {
    	return printLog(level, tag, text, threadInfo, null);
    }
    
    /**
     * 打印日志到文件中
     * <p>文件超限制，重新创建一个文件
     * <p>文件数量过多，删除旧的文件
     * @param level 日志级别
     * @param tag  日志TAG
     * @param text 日志信息
     * @param e 异常堆栈
     * @return 是否打印成功
     * @throws Exception
     */
    public boolean printLog(String level, String tag, String text, Throwable e) throws Exception {
    	String str = formatLogStr(level, tag, text, null, e);
    	return write(str);
    }
    
    /**
     * 打印日志到文件中
     * <p>文件超限制，重新创建一个文件
     * <p>文件数量过多，删除旧的文件
     * @param level 日志级别
     * @param tag  日志TAG
     * @param text 日志信息
     * @param threadInfo  线程信息，可包含进程号等等,建议格式pid/tid 如果为空默认:tName-tid(java不方便获取进程号)
     * @param e 异常堆栈
     * @return 是否打印成功
     * @throws Exception
     */
    public boolean printLog(String level, String tag, String text, String threadInfo, Throwable e) throws Exception {
    	String str = formatLogStr(level, tag, text, threadInfo, e);
    	return write(str);
    }
    
    /**
     * * 格式化日志信息
     * @param level 日志级别
     * @param tag  日志TAG
     * @param text 日志信息
     * @param e 异常堆栈
     * @param level
     * @param tag
     * @param text
     * @param e
     * @return
     */
    public String formatLogStr(LogLevel level, String tag, String text, Throwable e) {
    	return formatLogStr(String.valueOf(level), tag, text, null, e);
    }
    
    /**
     * 格式化日志信息
     * @param level 日志级别
     * @param tag  日志TAG
     * @param text 日志信息
     * @param threadInfo  线程信息，可包含进程号等等,建议格式pid/tid 如果为空默认:tName-tid(java不方便获取进程号)
     * @param e 异常堆栈
     * @return
     */
    public String formatLogStr(String level, String tag, String text, String threadInfo, Throwable e) {
    	String str = PrintUtil.formatLogStr(level, tag, text, threadInfo, e);
    	if (lineMode) {
    		return "\n" + str;
    	}
    	return str;
    }
    
    /**
     * 写入日志文件
     * @param str
     * @return
     * @throws Exception
     */
    public boolean write(String str) throws Exception {
        boolean written = write(str, true);
        return written;
    }
    
    /**
     * flush buffer and then append it
     * @param str
     * @return
     * @throws Exception
     */
    public PrintLog appendStr(String str) throws Exception {
    	appendToBuffer(str);
		return this;
    }
    
    /**
     * 追加日志（bufferMode）
     * @param level 日志级别
     * @param tag  日志TAG
     * @param text 日志信息
     * @param e 异常堆栈
     * @return
     * @throws Exception
     */
    public PrintLog appendLog(LogLevel level, String tag, String text) throws Exception {
		return appendLog(level, tag, text, null, null);
    }
    
    /**
     * 追加日志（bufferMode）
     * @param level 日志级别
     * @param tag  日志TAG
     * @param text 日志信息
     * @param threadInfo  线程信息，可包含进程号等等,建议格式pid/tid 如果为空默认:tName-tid(java不方便获取进程号)
     * @return
     * @throws Exception
     */
    public PrintLog appendLog(LogLevel level, String tag, String text, String threadInfo) throws Exception {
		return appendLog(level, tag, text, threadInfo, null);
    }
    
    /**
     * 追加日志（bufferMode）
     * @param level 日志级别
     * @param tag  日志TAG
     * @param text 日志信息
     * @param threadInfo  线程信息，可包含进程号等等,建议格式pid/tid 如果为空默认:tName-tid(java不方便获取进程号)
     * @param e 异常堆栈
     * @return
     * @throws Exception
     */
    public PrintLog appendLog(LogLevel level, String tag, String text, String threadInfo, Throwable e) throws Exception {
		return appendLog(String.valueOf(level), tag, text, threadInfo, e);
    }
    
    /**
     * 追加日志（bufferMode）
     * @param level 日志级别
     * @param tag  日志TAG
     * @param text 日志信息
     * @param e 异常堆栈
     * @return
     * @throws Exception
     */
    public PrintLog appendLog(String level, String tag, String text, Throwable e) throws Exception {
		return appendLog(level, tag, text, null, e);
    }
    
    /**
     * 追加日志（bufferMode）
     * @param level 日志级别
     * @param tag  日志TAG
     * @param text 日志信息
     * @param threadInfo  线程信息，可包含进程号等等,建议格式pid/tid 如果为空默认:tName-tid(java不方便获取进程号)
     * @param e 异常堆栈
     * @return
     * @throws Exception
     */
    public PrintLog appendLog(String level, String tag, String text, String threadInfo, Throwable e) throws Exception {
    	String str = formatLogStr(level, tag, text, threadInfo, null);
		return appendToBuffer(str);
    }
    
    /**
     * 追加日志
     * <p>如果超过缓存大小，立刻写入文件
     * @param str
     * @return
     * @throws Exception
     */
    public PrintLog appendToBuffer(String str) throws Exception {
    	synchronized (PrintLog.class) {
    		ensureBuffer();
    		buffer.append(str);
    		flushBuffer();
    		return this;
    	}
    }
    
    /**
     * Flush buffer if current buffer length extend max length
     * @return The length of written log string. If failed to write file , return -1.
     */
    public int flushBuffer() {
    	if(isBufferEmpty()) {
    		//Ignored invalid buffer
    		return 0;
    	}
    	if(isBufferExceeding()) {
    		try {
    			int len = doFlush();
    			return len;
    		} catch (Throwable e) {
    			PrintUtil.e(TAG, "flushBuffer failed: " + e, e);
    		}
    	}
    	if(isBufferMaxExceeding()) {
			//Force clear buffer avoid to OOM
			int len = clearBuffer();
			PrintUtil.w(TAG, "##flush: Force clear buffer avoid to OOM， length: " + len);
		}
    	return 0;
    }
    
    /**
     * Flush buffer and return written log string length.
     * @return flush writen result.
     * @throws Exception
     */
    public boolean flush() throws Exception {
    	int flushed = doFlush();
    	return flushed > 0;
    }
    
    /**
     * Flush buffer and return written log string length.
     * @return The length of written log string. If failed to write file , return -1.
     * @throws Exception
     */
    public int doFlush() throws Exception {
    	synchronized (PrintLog.class) {
    		if(isBufferEmpty()) {
        		//Ignored invalid buffer
        		return 0;
        	}
    		String str = buffer.toString();
        	if(str == null || str.equals("")) {
        		return 0;
        	}
        	// write text and clear buffer
        	boolean written = writeToFile(str, true);
        	if(!written) {
        		PrintUtil.w(TAG, "##doFlush: Failed to write buffer to file");
         		return -1;
        	}
        	int len = clearBuffer();
        	PrintUtil.i(TAG, "doFlush text len= " + len);
            return len;
    	}
    }
    
    /**
     * 缓存是否超过长度限制
     * @return
     */
    public boolean isBufferExceeding() {
    	if(isBufferEmpty()) {
    		//Ignored invalid buffer
    		return false;
    	}
    	int length = buffer.length();
    	if(length > bufferLength) {
    		return true;
    	}
    	if(isBufferMaxExceeding()) {
    		//超最大限制必须写入
    		return true;
    	}
    	return false;
    }
    
    /**
     * 缓存是否超过最大长度限制
     * <p> 超最大限制必须写入，或者清除缓存避免OOM
     * @return
     */
    public boolean isBufferMaxExceeding() {
    	if(isBufferEmpty()) {
    		//Ignored invalid buffer
    		return false;
    	}
    	int length = buffer.length();
    	if(length > MAX_BUFFER_LENGTH) {
    		//超最大限制必须写入，或者清除缓存避免OOM
    		return true;
    	}
    	return false;
    }
    
    /**
     * 清空缓存数据
     * @return cleared string length
     */
    public int clearBuffer() {
    	if(isBufferEmpty()) {
    		//Ignored invalid buffer
    		return 0;
    	}
    	int len = buffer.length();
    	//delete包前不包后
		//buffer.delete(0, len);
    	//setLength效率不delete高
		buffer.setLength(0);
        return len;
    }
    
    /**
     * 缓存是否为空
     * @return
     */
    public boolean isBufferEmpty() {
    	if(buffer == null || buffer.length() <= 0) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * 写入文本
     * @param str
     * @param append
     * @return
     * @throws Exception 
     */
    private boolean write(String text, boolean append) throws Exception {
    	if(bufferMode) {
        	appendToBuffer(text);
        	return true;
    	}
    	return writeToFile(text, append);
    }
    
    /**
     * 写入文本
     * @param str
     * @param append
     * @return
     * @throws Exception 
     */
    private boolean writeToFile(String text, boolean append) throws Exception {
    	synchronized (PrintLog.class) {
    		renderFiles();
    		//确保日志问价存在
    		ensureLogFile();
        	//文件数量过多，删除旧的文件
    		deleteOldFiles();
    		boolean written = mTxtFile.write(text, append);
            return written;
    	}
    }
    
    public int getFileCount() {
    	String[] paths = null;
    	if(mDir != null) {
    		paths = mDir.list();
    	}
    	return paths != null ? paths.length : 0;
    }
    
    /**
     * 提取最早和最近创建的文件
     * @param files
     * @param remark
     * @return
     */
    
    private void resetFiles(String reason) {
    	PrintUtil.i(TAG, "reason: " + reason + " >> Reset mLatestFile and mOldestFile as null !! ");
    }

    private void renderFiles() {
    	logFileCount = 0;
    	if(mDir != null && mDir.exists()) {
    		//列出dir下的文件列表，仅文件名不含签名路径,如果需要文件请使用dir.listFIles();
    		logFilenames = mDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                	if(dir != null && name != null) {
                		//刨开忽略文件和文件夹
                		File file = new File(dir, name);
                		if(file.isDirectory()) {
                			return false;
                		}
                		if(name.contains(LOCKED_FLAG)) {
                			return false;
                		}
                	}
                    return true;
                }
            });
    		if(logFilenames != null) {
    			logFileCount = logFilenames.length;
    		}
    		//PrintUtil.i(TAG, "renderFiles： logFileCount= " + logFileCount);
    	} else {
    		PrintUtil.i(TAG, "renderFiles： Not existed dir: " + mDir);
    	}
    }
    
    /**
     * 如果文件个数超限制，删除最早的一个
     * @return
     */
    private int deleteOldFiles() {
    	if(maxFileCount > 0 && logFileCount > maxFileCount) {
    		int deletedCount = FileUtils.deleteOldFiles(mDir, maxFileCount, LOCKED_FLAG);
    		PrintUtil.w(TAG, "deleteOldFiles： count= " + deletedCount + ", maxFileCount= " + maxFileCount);
    		return deletedCount;
    	}
    	return 0;
    }
    
    /**
     * 当前文件大小超过限额之后,把文件名改成时间格式,避免重复.
     * @return
     */
    private boolean renameExceedLengthFile() {
    	if(maxFileLength > 0) {
    		if(mTxtFile != null && mTxtFile.exists()) {
    			long length = mTxtFile.length();
    			if(length > maxFileLength) {
    				String filename = TxtFileUtil.createCurrentFileName(fileExtension, true);
    				File dest = new File(mDir, filename);
    				boolean renamed = renameTo(dest);
    				if(renamed) {
    					PrintUtil.i(TAG, "renamed exceed length File: " + mTxtFile + " to " + dest
    							+ ", length= " + length + ", maxFileLength= " + maxFileLength);
    				} else {
    					PrintUtil.w(TAG, "rename exceed length File failed: " + mTxtFile + " to " + dest
    							+ ", length= " + length + ", maxFileLength= " + maxFileLength);
    				}
    				return renamed;
    			}
    		}
    	}
    	return false;
    }
    
    public boolean renameTo(String destFilePath) {
    	return renameTo(new File(destFilePath));
    }
    
    public boolean renameTo(File dest) {
    	if(mTxtFile != null) {
    		if(mTxtFile.exists()) {
    			boolean renamed = mTxtFile.renameTo(dest);
    			if(!renamed) {
    				PrintUtil.w(TAG, "rename failed: " + mTxtFile + " to " + dest);
    			}
    			return renamed;
    		} else {
    			PrintUtil.w(TAG, "renameTo: Not existed file: " + mTxtFile);
    		}
    	}
    	return false;
    }
    
    /**
     * 锁定当前的日志文件(在文件加上"locked"标识，不会自动删除)
     * @return
     */
    public boolean lockFile() {
    	if(mTxtFile != null) {
    		String path = getLockFailedPath();
    		boolean locked = renameTo(path);
    		PrintUtil.w(TAG, "lockFile: locked= " + locked + ", new path= " + path);
    		return locked;
    	}
		return false;
    }
    
    public String getLockFailedPath() {
    	if(mTxtFile != null) {
    		return TxtFileUtil.addFilenamePrefix(mTxtFile.getFilePath(), LOCKED_FLAG);
    	}
    	return null;
    }
    
    /**
     * 创建日志文本文件对象: yyyyMMdd.txt or yyyyMMdd_HHmmss.txt
     * @return
     * @throws IOException 
     */
    public TxtFile createNewFile(boolean timeMode) {
    	try {
    		String dir = getDirPath();
        	ensureFileExtension();
            String filePath = TxtFileUtil.createFilePath(dir, logPrefix, fileExtension, timeMode);
        	TxtFile logFile = new TxtFile(filePath);
        	boolean created = true;
        	if(!logFile.exists()) {
        		created = logFile.createNewFile();
        		PrintUtil.w(TAG, "create log new file: " + logFile + "" + created);
        	} else {
        		PrintUtil.w(TAG, "Existed log file: " + logFile);
        	}
        	return logFile;
    	} catch (Exception e) {
    		PrintUtil.e(TAG, "createNewFile error: " + e, e);
    	}
    	return null;
    }
    
    public String getDirPath() {
    	return mDir.getAbsolutePath();
    }
    
    public String getFilePath() {
    	String filePath = "";
    	try {
    		if(mTxtFile != null) {
    			filePath = mTxtFile.getFilePath();
    		}
    	} catch (IllegalArgumentException e) {
    		PrintUtil.e(TAG, "getFilePath error: " + e, e);
    	}
    	return filePath;
    }
    
    /**
     * 优先使用上一个没有存满的文件, 否则重新创建一个
     * @return
     * @throws IOException 
     */
    public TxtFile createLogFile() {
    	TxtFile file = null;
    	if(fileNameTimeMode) {
    		file = createNewFile(true);
    	} else {
    		file = createNewFile(false);
    	}
    	return file;
    }
    
    /**
     * 确保日志w文件可用
     * @throws IOException
     */
    public void ensureLogFile() throws Exception {
    	if(mDir == null) {
    		if(mTxtFile != null) {
    			//用于自定义mTxtFile文件路径的情况(很少用)
        		mDir = mTxtFile.getParentFile();
        	} else {
        		PrintUtil.e(TAG, "The dir is null");
        		throw new NullPointerException("The dir is null");
        	}
    	}
    	if(!mDir.exists()) {
    		boolean mkDir = mDir.mkdirs();
    		if(mkDir) {
    			PrintUtil.i(TAG, "created dir: " + mDir);
    		} else {
    			PrintUtil.i(TAG, "Failed create dir: " + mDir);
    			throw new IOException("Failed create dir: " + mDir);
    		}
    	}
    	if(mTxtFile == null) {
    		if(logFilename != null && !logFilename.equals("")) {
        		//用户自定义的文件名
        		File file= new File(mDir, logFilename);
        		mTxtFile = new TxtFile(file);
        	} else {
        		//自动创建文件名,安札当前日期时间创建
        		mTxtFile = createLogFile();
        	}    		
    	}
    	//若果按照日期命名的文件超过了最大长度，就使用时间秒来命名，避免同一个文件过大
    	if(mTxtFile != null && mTxtFile.exists()) {
    		long length = mTxtFile.length();
			if(length > maxFileLength || length > FILE_LENGTH_LIMIT) {
				mTxtFile = createNewFile(true);
			}
    	}
    }
    
    public File getParentDirFile() {
    	if(mTxtFile != null) {
    		return mTxtFile.getParentFile();
    	}
    	return null;
    }
    
    public TxtFile getTxtFile() {
    	return mTxtFile;
    }
    
    /**
     * 创建以日期命名的文件
     * @return
     * @throws IOException 
     */
    public TxtFile createDateLogFile() {
    	TxtFile file = createNewFile(false);
    	return file;
    }
    
    private void ensureFileExtension() {
    	if(fileExtension == null || fileExtension.equals("")) {
    		fileExtension = FILE_EXTENSION;
    	}
    }
    
    private void ensureBuffer() {
    	if(buffer == null) {
    		this.buffer = new StringBuffer();
    	}
    }
    
    @Override
    public String toString() {
		return "PrintLog[mDir= " + mDir + ", logPrefix= " + logPrefix + ", bufferMode= " + bufferMode
				+ ", logFileCount= " + logFileCount+ ", maxFileCount= " + maxFileCount + ", mTxtFile= "+ mTxtFile + "]";
    	
    }
}
