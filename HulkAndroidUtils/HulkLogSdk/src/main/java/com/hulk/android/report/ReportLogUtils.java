package com.hulk.android.report;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.hulk.util.common.FileUtils;
import com.hulk.util.common.ZipUtil;
import com.hulk.android.log.LogUtil;
import com.hulk.android.log.RuntimeLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportLogUtils {
    public static final String TAG = "ReportLogUtils";

    //上报文件夹是否需要压缩文件夹
    public static final boolean REPORT_ZIP_MODE = true;
    public static final int FILE_LENGTH_M = 1024 * 1024;
    //此处春文件数据控制在6M以内:base64编码和组装json字符串都会增加数据亮,文件过大base64直接崩溃
    public static final int MAX_REPORT_FILE_LENGTH = FILE_LENGTH_M * 6;
    //是否在上报json文件名前增加用户名前缀，方便服务端查日志文件归属
    public static final boolean REPORT_USER_FILE_PREFIX_MODE = true;

    public static final long LARGE_FILE_MIN_LENGTH = FILE_LENGTH_M * 20;
    public static final int REPORT_DIR_FILE_MAX_COUNT = 10;

    //Dbug模式系统日志打印临时目录
    public static final String SYSTEM_LOGS_DIR = "/system_logs/";
    //上报日志临时目录
    public static final String REPORT_ZIP_DIR = "/report_zip/";
    static ExecutorService sExecutor = Executors.newCachedThreadPool();

    public static String sUsername = "HulkLog";

    public static void setUsername(String username) {
        ReportLogUtils.sUsername = username;
    }

    /**
     * 获取文件上报内容
     * @param file
     * @return
     */
    public static FileDataBean encodeFileReportData(File file) {
        if (file == null) {
            LogUtil.w(TAG, "encodeFileReportData: file is null");
            return null;
        }
        if (!file.exists()) {
            LogUtil.w(TAG, "encodeFileReportData: not existed  file: " + file);
            return null;
        }
        try {
            FileDataBean dataBean = new FileDataBean();
            dataBean.file_length = file.length();
            dataBean.file_name = file.getName();
            byte[] rawData = FileUtils.readFileData(file);
            if (rawData != null) {
                String b64Text = base64Encode(rawData);
                dataBean.content = b64Text;
                dataBean.content_length = b64Text.length();
                LogUtil.d(TAG, "Encoded file: " + file + ", dataBean= " + dataBean);
            } else {
                LogUtil.w(TAG, "read file data failed from file: " + file);
            }
            return dataBean;
        } catch (Exception e) {
            LogUtil.e(TAG, "encodeFileReportData failed: " + e + ", file= " + file, e);
        }
        return null;
    }

    /**
     * Base编码
     * @param rawData
     * @return
     */
    public static String base64Encode(byte[] rawData) {
        if (rawData == null) {
            LogUtil.w(TAG, "base64Encode: raw data is null");
            return null;
        }
        byte[] b64 = Base64.encode(rawData, Base64.NO_WRAP);
        String content = new String(b64);
        return content;
    }

    public static String readFileText(File file) {
        return FileUtils.readFileText(file);
    }

    public static List<FileDataBean> getFileDataList(List<File> files) {
        if (files == null || files.isEmpty()) {
            LogUtil.w(TAG, "getFileDataList: files is null or empty");
            return null;
        }
        List<FileDataBean> fileDataList = new ArrayList<>();
        for (File file: files) {
            FileDataBean bean = encodeFileReportData(file);
            if (bean != null) {
                LogUtil.d(TAG, "File data bean: " +  bean);
                fileDataList.add(bean);
            } else {
                LogUtil.w(TAG, "encodeFileReportData failed for file: " + file);
            }
        }
        return fileDataList;
    }

    public static ReportFileDataBean getFileReportData(File file) {
        FileDataBean bean = encodeFileReportData(file);
        if (bean != null) {
            ReportFileDataBean data = createReportFileData(bean);
            return data;
        }else {
            LogUtil.w(TAG, "getFileReportData failed for file: " + file);
        }
        return null;
    }

    /**
     * 对所有需要上报的文件，进行上报文件数据分组
     * 获取需要上报文件的JSON数组，每个json数组需要单独上报，避免超出服务器接口承载力(总数据不能与10M).
     * <p>每次上报一个JSONArray, 大小不能超过10M (还有其他字段，总共不能超过12M)
     * @param files
     * @param maxGroupLength 分组都每个元素所有文件的最大长度
     * @return
     */
    public static List<JSONArray> getReportFileDataGroups(List<File> files, int maxGroupLength) {
        if (files == null || files.isEmpty()) {
            LogUtil.w(TAG, "getReportFileDataGroups: files is null or empty");
            return null;
        }
        List<JSONArray> groups = new ArrayList<>();
        int fileCount = files.size();
        LogUtil.w(TAG, "getReportFileDataGroups: fileCount= " + fileCount + ", maxGroupLength= " + maxGroupLength);
        //每次上报一个JSONArray, 大小不能超过10M (还有其他字段，总共不能超过12M)
        JSONArray subGroup = new JSONArray();
        ReportFileDataBean first = getFileReportData(files.get(0));
        long subGroupLength = 0;
        if (first != null) {
            //首先加入第一个元素
            subGroup.put(first);
            subGroupLength = first.getJsonStrLength();
        }
        if (fileCount == 1) {
            groups.add(subGroup);
            LogUtil.w(TAG, "getReportFileDataGroups: Ended for Only one src file");
            return groups;
        }
        for (int i= 1; i < fileCount; i++) {
            File file = files.get(i);
            ReportFileDataBean data = getFileReportData(file);
            if (data == null) {
                LogUtil.w(TAG, "Ignored report data is null for file: " + file);
                continue;
            }
            long jsonLength = data.getJsonStrLength();
            LogUtil.i(TAG, "getReportFileDataGroups: jsonLength= " + jsonLength + ", subGroupLength= " + subGroupLength);
            if (jsonLength >= maxGroupLength || (subGroupLength + jsonLength) > maxGroupLength) {
                //总长度加上当前长度大于限额时，加入到集团，新起一个子集
                groups.add(subGroup);
                LogUtil.w(TAG, "Add subGroup to groups count: " + subGroup.length());
                subGroupLength = 0;//计数清0
                subGroup = new JSONArray();
            }

            //加入文件子元素到子数组中
            subGroupLength += jsonLength;
            subGroup.put(data.fileJson);

            if (i >= fileCount - 1) {
                //最后一个文件，直接加入大集团，分组完成
                groups.add(subGroup);
                LogUtil.w(TAG, "Finished group all file list, groups count: " + groups.size());
                break;
            }
        }
        return groups;
    }

    /**
     * 构建上报数据json数据,列表每一次上报其中一个元素
     * @param files 日志源文件列表
     * @return
     */
    public static List<String> buildReportFileDataGroups(List<File> files) {
        List<JSONArray> groups = getReportFileDataGroups(files, MAX_REPORT_FILE_LENGTH);
        return buildReportDataList(groups);
    }

    /**
     * 构建上报数据json数据,列表每一次上报其中一个元素
     * @param groups 上报日志数据分组
     * @return
     */
    public static List<String> buildReportDataList(List<JSONArray> groups) {
        if (groups == null || groups.isEmpty()) {
            LogUtil.w(TAG, "buildReportDataList: groups is null or empty: " +groups);
            return null;
        }
        List<String> list = new ArrayList<>();
        for (JSONArray group: groups) {
            String json = buildReportData(group);
            if (!TextUtils.isEmpty(json)) {
                list.add(json);
            } else {
                LogUtil.w(TAG, "buildReportDataList: built json is empty from group: " + group);
            }
        }
        LogUtil.w(TAG, "buildReportDataList: built list count: " + list.size());
        return list;
    }

    /**
     * 构建上报数据json数据,每一次上报的数据总量
     * 数据格式如下:
     * {"feedback":
     *  {
     *      "STACK_TRACE":"ok",
     *      "FILE_LIST":[
     *                      {"file_name":"main-20200221.txt","content":"fsdafhsdalfkasdnvkasvs"},
     *                      {"file_name":"sportal_vpn-20200221.txt","content":"dlfsjdfjkgdjhkdfjgndfgnldfkngsfdgd"}
     *                  ]
     *   }
     * }
     * STACK_TRACE这个必须有，其他随便起名字,STACK_TRACE 这个不能超过20KB  整个不超过10mb
     */
    public static String buildReportData(JSONArray group) {
        try {
            //构建上报数据
            JSONObject feedback = new JSONObject();
            //接口必须字段，此处常量ok
            feedback.put("STACK_TRACE", "ok");
            //日志文件数据列表，group为json数组
            feedback.put("file_list", group);

            JSONObject reportJson = new JSONObject();
            reportJson.put("feedback", feedback);
            return reportJson.toString();
        } catch (Exception e) {
            LogUtil.e(TAG, "buildReportData failed: " + e, e);
        }
        return "";
    }

    /**
     * 创建上报文件数据
     * @param bean
     * @return
     */
    public static ReportFileDataBean createReportFileData(FileDataBean bean) {
        if (bean == null) {
            LogUtil.e(TAG, "createReportFileData: the FileDataBean is null.");
            return null;
        }
        try {
            ReportFileDataBean reportbean = new ReportFileDataBean();
            JSONObject jsonObject = new JSONObject();
            String file_name = optimizeFilename(bean.file_name);
            jsonObject.put("file_name", file_name);
            jsonObject.put("content", bean.content);
            reportbean.fileJson = jsonObject;
            reportbean.jsonStrLength = jsonObject.toString().length();
            if (bean.content != null) {
                reportbean.contentLength = bean.content.length();
            }
            return reportbean;
        } catch (JSONException e) {
            LogUtil.e(TAG, "createReportFileData failed: " + bean, e);
        }
        return null;
    }

    /**
     * 优化上报文件明前面增加用户名，方便服务端查找文件，避免重复
     * @param filename
     * @return
     */
    public static String optimizeFilename(String filename) {
        String file_name = filename;
        if (REPORT_USER_FILE_PREFIX_MODE) {
            String username = sUsername;
            if (!TextUtils.isEmpty(username)) {
                file_name = username + "_" + filename;
            }
        }
        return android.os.Build.MODEL + "_" + file_name;
    }

    public static List<File> listAllFiles(Context context) {
        List<File> list = new ArrayList<>();
        String mainDir = RuntimeLog.getMainLogDir(context);
        List<File> mainFiles = listFiles(mainDir);
        if (mainFiles != null) {
            list.addAll(mainFiles);
        }
        String vpnDir = RuntimeLog.getVpnLogDir(context);
        List<File> vpnFiles = listFiles(vpnDir);
        if (vpnFiles != null) {
            list.addAll(vpnFiles);
        }
        String sysDir = RuntimeLog.getSysLogDir(context);
        List<File> sysFiles = listFiles(sysDir);
        if (sysFiles != null) {
            list.addAll(sysFiles);
        }

        //TODO 这个文件目录可能是空的，需要使用WrapFileUtils把加密的源文件解码到值了才有文件
        String wrapDir = RuntimeLog.getWrapReportLogDir(context);
        List<File> wrapFiles = listFiles(wrapDir);
        if (wrapFiles != null) {
            list.addAll(wrapFiles);
        }
        return list;
    }

    public static List<File> listFiles(String dir) {
        if (TextUtils.isEmpty(dir)) {
            LogUtil.w(TAG, "listFiles: dir is empty");
            return null;
        }
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            LogUtil.w(TAG, "listFiles: not existd dir: " + dir);
            return null;
        }
        File[] files = dirFile.listFiles(new LogFileFilter());
        if (files != null && files.length > 0) {
            LogUtil.i(TAG, "listFiles: files.length= " + files.length);
            return Arrays.asList(files);
        }
        LogUtil.w(TAG, "listFiles: Empty files: " + files);
        return null;
    }

    /**
     * 获取要上报的文件目录
     * @param context
     * @return
     */
    public static String[] getReportDirs(Context context) {
        String rootDir = RuntimeLog.getMainLogDir(context);
        String vpnDir = RuntimeLog.getVpnLogDir(context);
        String itsDir = RuntimeLog.getItsSdkLogDir(context);
        String sysDir = RuntimeLog.getSysLogDir(context);
        //上传任务中进行解码到临时目录中再上传
        String wrapDir = RuntimeLog.getWrapReportLogDir(context);
        String[] dirs = new String[]{rootDir, vpnDir, wrapDir, sysDir, itsDir};
        return dirs;
    }

    /**
     * 获取ZIP要锁上报临时目录
     * @param context
     * @return
     */
    public static String getReportZipDir(Context context) {
        return RuntimeLog.getTianjiLogDir(context) + REPORT_ZIP_DIR;
    }

    /**
     * 系统日志目录
     * @param context
     * @return
     */
    public static String getSystemLogDir(Context context) {
        return RuntimeLog.getTianjiLogDir(context) + SYSTEM_LOGS_DIR;
    }

    /**
     * 获取系统日志文件
     * @param context
     * @return
     */
    public static File[] getSystemLogFiles(Context context) {
        try {
            String dir = getSystemLogDir(context);
            File sysDir = new File(dir);
            final File[] files = sysDir.listFiles();
            if (files == null) {
                return new File[0];
            }
            Arrays.sort(files, new LastModifiedComparator());
            return files;
        } catch (Exception e) {
            Log.e(TAG, "getSystemLogFiles failed: " + e, e);
        }
        return null;
    }

    /**
     * 手动导出日志的文件，可以使用微信等等发送。
     * 位于日志主目录下
     * @param context
     * @return
     */
    public static String getManualExportLogFile(Context context) {
        return RuntimeLog.getTianjiLogDir(context) + "/tj-logs.zip";
    }

    /**
     * 删除指定扩展名的文件
     * @param files
     * @param ex
     * @return
     */
    public static int deleteFiles(List<File> files, String ex) {
        if (files == null) {
            return 0;
        }
        int count = 0;
        for (File file: files) {
            String name = file.getName();
            if (name != null && name.endsWith(ex)) {
                boolean deleted = file.delete();
                LogUtil.w(TAG, "Delete file: " + file + ", deleted= " + deleted);
                if (deleted) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 压缩文件名名字采用时间格式,避免重复,形如: main-yyyyMMdd_HHmmSSS.zip.
     * @param zipDir
     * @param filename
     * @return
     */
    public static File getZipFile(String zipDir, String filename) {
        String name = formatMillisecondZipName(filename);
        File destZipFile = new File(zipDir, name);
        File parentDir = destZipFile.getParentFile();
        if (parentDir.exists()) {
            //删除之前的压缩文件，避免重复过多,也避免下面压缩报错
            if (destZipFile.exists()) {
                boolean deleted = destZipFile.delete();
                if (!deleted) {
                    String name2 = formatMillisecondZipName(filename);
                    LogUtil.w(TAG, "Failed delete existed destZipFile: " + destZipFile + ", create new filename: " + name2);
                    destZipFile = new File(zipDir, name2);
                } else {
                    LogUtil.w(TAG, "Delete existed destZipFile: " + destZipFile);
                }
            }
        } else {
            boolean mkdirs = parentDir.mkdirs();
            LogUtil.w(TAG, "Create zip parentDir: " + parentDir + ", mkdirs= " + mkdirs);
        }
        return destZipFile;
    }

    /**
     * 遍历文件夹,提交所有上报文件.
     * <p>文件夹中的子文件夹江北过滤掉.如果子文件夹需要上报，请参数数组中直接传入子目录
     * @param context
     * @param dirs 需要上报的文件夹列表.
     * @param zipDir 压缩文件文件存放目录，如果子字段为空将不进行压缩.
     * @return 文件列表，如果是压缩模式，返回压缩文件的文件列表.
     * @throws IOException
     * @throws RuntimeException
     */
    public static List<File> renderReportDirs(Context context, String[] dirs, String zipDir)throws IOException, RuntimeException {
        if (dirs == null && dirs.length <= 0) {
            LogUtil.w(TAG, "renderReportDirs: Ignored for dirs is empty: " + dirs);
            return null;
        }
        LogUtil.w(TAG, "renderReportDirs: " + Arrays.toString(dirs) + ", zipDir= " + zipDir);
        List<File> reportFiles = new ArrayList<>();
        for (String dirPath: dirs) {
            File dir = new File(dirPath);
            if (!dir.exists()) {
                LogUtil.w(TAG, "Not existed dir: " + dir);
                continue;
            }
            LogUtil.w(TAG, "\n\n==================================renderReportDirs: " + dir);
            //只要文件，文件夹去掉
            File[] srcFiles = dir.listFiles(new LogFileFilter());
            if (srcFiles == null || srcFiles.length == 0) {
                LogUtil.w(TAG, "Invalid srcPaths: " + srcFiles + " in dir: " + dir);
                continue;
            }
            if (!TextUtils.isEmpty(zipDir)) {
                //压缩到上一级目录,名字采用时间格式,避免重复,形如: main-yyyyMMdd_HHmm.zip.
                File parentDir = new File(zipDir);
                if (!parentDir.exists()) {
                    boolean mkdirs = parentDir.mkdirs();
                    LogUtil.w(TAG, "Create zip parentDir: " + parentDir + ", mkdirs= " + mkdirs);
                }
                //把srcFiles中所有文件压缩到destZipFile文件中
                //如果文件过大需要分批压缩,避免一个压缩文件超过上报最大长度10M
                //策略:大于10M的原文件单独压缩，小文件集合压缩，但是总数不能超过10M
                List<File[]> groups = divideIntoGroups(srcFiles, MAX_REPORT_FILE_LENGTH);
                if (groups != null && !groups.isEmpty()) {
                    LogUtil.w(TAG, "renderReportDirs: divided Groups count= " + groups.size());
                    for (File[] group: groups) {
                        boolean zipped = false;
                        String zipName = dir.getName();
                        File destZipFile = getZipFile(zipDir, zipName);
                        try {
                            LogUtil.v(TAG, "Will zip files: " + Arrays.toString(group));
                            zipped = ZipUtil.zip(group, destZipFile);
                            LogUtil.w(TAG, "zipped file: " + destZipFile.getName() + ", length= " + destZipFile.length() + ", group: " + group);
                        } catch (Exception e) {
                            LogUtil.w(TAG, "renderReportDirs zip failed: " + e);
                        }
                        if (zipped) {
                            reportFiles.add(destZipFile);
                        } else {
                            LogUtil.w(TAG, "Add src files for failed zip file: " + Arrays.toString(srcFiles));
                            reportFiles.addAll(Arrays.asList(srcFiles));
                        }
                    }
                } else {
                    LogUtil.w(TAG, "renderReportDirs: divided groups is null, add src files to list");
                    if (srcFiles != null) {
                        reportFiles.addAll(Arrays.asList(srcFiles));
                    }
                }
            } else {
                LogUtil.i(TAG, "Add all src files: " + Arrays.toString(srcFiles));
                reportFiles.addAll(Arrays.asList(srcFiles));
            }
        }
        return reportFiles;
    }

    /**
     * 把源文件数组按照规定的大小进行分组
     * <p>策略:大于10M的原文件单独为一个元素，小文件集合为一个元素，其中文件总长度不能超过10M
     * @param srcFiles 源文件数组
     * @param maxGroupLength 分组都每个元素所有文件的最大长度
     * @return
     */
    public static List<File[]> divideIntoGroups(File[] srcFiles, int maxGroupLength) {
        if (srcFiles == null || srcFiles.length <= 0) {
            LogUtil.w(TAG, "divideIntoGroups: srcFiles is nullor empty");
            return null;
        }
        List<File[]> groups = new ArrayList<>();
        int fileCount = srcFiles.length;
        LogUtil.w(TAG, "divideIntoGroups: src files count= " + fileCount + ", maxGroupLength= " + maxGroupLength);
        List<File> subGroup = new ArrayList<>();
        //首先加入第一个元素
        File first = srcFiles[0];
        subGroup.add(first);
        long subGroupLength = first.length();
        if (fileCount == 1) {
            groups.add(subGroup.toArray(new File[subGroup.size()]));
            LogUtil.w(TAG, "divideIntoGroups: Ended for Only one src file");
            return groups;
        }
        for (int i = 1; i < fileCount; i++) {
            File file = srcFiles[i];
            //当前文件长度加到子分组总长度超过限制，就把子分组加入总分组，重新创建子分组
            long length = file.length();
            LogUtil.i(TAG, "divideIntoGroups file: " + file.getName() + ", length= " + length);
            if (length >= maxGroupLength || subGroupLength + length >= maxGroupLength) {
                groups.add(subGroup.toArray(new File[subGroup.size()]));
                LogUtil.w(TAG, "divideIntoGroups: Add subGroup groups for Exceed max sub group length limit, subGroup count: " + subGroup.size());
                subGroup = new ArrayList<>();
                subGroupLength = 0;
            }
            //加到字符组中
            subGroup.add(file);
            subGroupLength = subGroupLength + length;

            if (i >= fileCount - 1) {
                groups.add(subGroup.toArray(new File[subGroup.size()]));
                LogUtil.w(TAG, "divideIntoGroups： Finished groups count: " + groups.size());
                break;
            }
        }
        return groups;
    }

    /**
     * 导出log目下的文件
     * @param context
     * @param dirs
     * @param exportZipPath
     * @param isClearRawDirs
     * @param remark
     * @return
     * @throws IOException
     */
    public static boolean exportFilesAsZip(Context context, String[] dirs, String exportZipPath, boolean isClearRawDirs, String remark) throws IOException {
        if (dirs == null || dirs.length == 0) {
            LogUtil.w(TAG, "exportFilesAsZip: Ignored for dir is null or empty");
            return false;
        }
        LogUtil.w(TAG, "exportFilesAsZip: dirs= " + Arrays.toString(dirs) + " exportZipPath= " + exportZipPath + ", remark= " +remark);
        RuntimeLog.flush(TAG);//冲洗缓冲区
        File zipFile = new File(exportZipPath);
        if (zipFile.exists()) {
            boolean deleted =  zipFile.delete();
            LogUtil.w(TAG, "exportZipPath: delete exists: " + zipFile + ", deleted= " + deleted);
        }
        boolean exported = ZipUtil.zip(dirs, exportZipPath);
        LogUtil.w(TAG, "exportZipPath: exported= " + exported);
        if (isClearRawDirs && dirs != null) {
            for (String dir: dirs) {
                boolean cleared = RuntimeLog.clearDir(dir);
                LogUtil.w(TAG, "Cleared= " + cleared + ", dir= " + dir);
            }
        }
        return exported;
    }

    /**
     *  执行手动导出ri所有需要上报的文件到
     * @param context
     * @param isClearRawDirs
     * @param remark
     */
    public static boolean doManualExportLogFiles(Context context, boolean isClearRawDirs, String remark) {
        try {
            String[] dirs = ReportLogUtils.getReportDirs(context);
            String exportZipPath = ReportLogUtils.getManualExportLogFile(context);
            boolean exported = ReportLogUtils.exportFilesAsZip(context, dirs, exportZipPath, isClearRawDirs, remark);
            LogUtil.w(TAG, "doManualExportLogFiles: exported= " + exported);
            return exported;
        } catch (Throwable e) {
            LogUtil.e(TAG, "doManualExportLogFiles failed: " + e, e);
            return false;
        }
    }

    /**
     *  异步整理日志数据，主要是压缩和界面
     * @param context
     * @param isClearRawDirs 是否清空源文件的目录
     * @param remark
     */
    public static void exportLogFiles(final Context context, final boolean isClearRawDirs, final String remark) {
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean exported = doManualExportLogFiles(context, isClearRawDirs, remark);
                LogUtil.w(TAG, "exportLogFiles: exported= " + exported);
            }
        });
    }

    public static String formatZipName(String filename, String timeStr) {
        return filename + "-" + timeStr + ".zip";
    }

    /**
     * g格式化压缩文件时间后缀 eg： main-yyyyMMdd_HHmm.zip.
     * @return
     */
    public static String formatMinuteZipName(String filename) {
        String str = formatTimeMinuteStr(System.currentTimeMillis());
        return formatZipName(filename, str);
    }

    /**
     * 格式化压缩文件时间后缀 eg： main-yyyyMMdd_HHmmss.zip.
     * @return
     */
    public static String formatSecondZipName(String filename) {
        String str = formatTimeSecondStr(System.currentTimeMillis());
        return formatZipName(filename, str);
    }

    /**
     * 格式化压缩文件时间毫秒 eg： main-yyyyMMdd_HHmmss.SSS.zip.
     * @return
     */
    public static String formatMillisecondZipName(String filename) {
        String str = formatTimeMillisecondStr(System.currentTimeMillis());
        return formatZipName(filename, str);
    }

    /**
     * 格式化时间戳：yyyyMMdd_HHmm, 用于文件名
     * @param timeMillis
     * @return
     */
    public static String formatTimeMinuteStr(long timeMillis) {
        return formatTimeStr(timeMillis, "yyyyMMdd_HHmm");
    }

    /**
     * eg： main-yyyyMMdd_HHmmss.zip.
     * @param timeMillis
     * @return
     */
    public static String formatTimeSecondStr(long timeMillis) {
        return formatTimeStr(timeMillis, "yyyyMMdd_HHmmss");
    }

    /**
     * 格式化事假到毫秒:yyyyMMdd_HHmmss.SSS.zip.
     * @param timeMillis
     * @return
     */
    public static String formatTimeMillisecondStr(long timeMillis) {
        return formatTimeStr(timeMillis, "yyyyMMdd_HHmmss.SSS");
    }

    /**
     * 格式化时间戳：yyyyMMdd_HHmm
     * @param timeMillis
     * @param pattern  yyyyMMdd_HHmm or "yyyy-MM-dd HH:mm:ss.SSS"
     * @return
     */
    public static String formatTimeStr(long timeMillis, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.format(timeMillis);
        } catch (Exception e) {
            LogUtil.e(TAG, "formatTimeStr error: " + e + ", pattern= " + pattern + ", timeMillis= " + timeMillis, e);
        }
        return String.valueOf(timeMillis);
    }

    public static boolean clearDir(String dir) {
        try {
            if (!TextUtils.isEmpty(dir)) {
                File file = new File(dir);
                if (file.exists()) {
                    FileUtils.deleteDirectory(file);
                    LogUtil.w(TAG, "cleared dir: " + dir);
                    return true;
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "clearDir failed: " + e + ", dir: " + dir, e);
        }
        return false;
    }

    /**
     * 清空天机日志目录
     * @param context
     */
    public static void clearTianjiLogDirAsync(final Context context, final String remark) {
        LogFileManager.clearRuntimeLogsAsync(context, remark);
    }

    /**
     * 简化天机日志目录中老旧文件
     * @param context
     */
    public static void simplifySystemLogAsync(final Context context, final String remark) {
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String systemLogDir = getSystemLogDir(context);
                int delSysCount = LogFileManager.simplifyDirFiles(systemLogDir, LARGE_FILE_MIN_LENGTH, SysLogPrinter.SYS_LOGS_FILE_MAX_COUNT);
                LogUtil.w(TAG, "simplifyLogFiles: delSysCount= " + delSysCount);
            }
        });
    }

    /**
     * 简化天机日志目录中老旧文件
     * @param context
     */
    public static void simplifyLogFilesAsync(final Context context, final String remark) {
        simplifySystemLogAsync(context, remark);
        LogFileManager.simplifyLogFilesAsync(context, remark);
    }
}
