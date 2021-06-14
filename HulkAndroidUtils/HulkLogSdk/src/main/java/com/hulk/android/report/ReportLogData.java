package com.hulk.android.report;

import android.content.Context;

import com.hulk.android.log.LogUtil;
import com.hulk.android.log.RuntimeLog;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 上报数据
 * @author: zhanghao
 * @Time: 2021-03-04 14:22
 */
public class ReportLogData {
    private static final String TAG = "ReportLogData";
    Context mContext;
    /**
     * 需要上报的文件目录
     */
    String[] mDirs;
    /**
     * 上报文件夹是否需要压缩文件夹
     */
    boolean mZipDirMode = ReportLogUtils.REPORT_ZIP_MODE;
    /**
     * 压缩上传时的临时文件夹,方便删除临时zip文件
     */
    String mTempZipDir;

    /**
     * 上报文件夹数组,默认压缩为zip文件上传
     * @param context
     * @param dirs
     */
    public ReportLogData(Context context, String[] dirs) {
        this.mContext = context;
        this.mDirs = dirs;
    }

    public void setZipMode(boolean zipMode) {
        this.mZipDirMode = zipMode;
    }

    private List<File> prepareFiles() {
        try {
            if (mZipDirMode) {
                mTempZipDir = ReportLogUtils.getReportZipDir(mContext);
                //清空临时文件夹
                RuntimeLog.clearDir(mTempZipDir);
            }
            //冲洗缓冲区日志
            RuntimeLog.flush(TAG);

            List<File> files = ReportLogUtils.renderReportDirs(mContext, mDirs, mTempZipDir);
            if (files != null && files.size() > 0) {
                LogUtil.w(TAG, "rendered Report files count: " + files.size());
            } else {
                LogUtil.w(TAG, "rendered Report files is empty.");
            }
            return files;
        } catch (Exception e) {
            String detail = RuntimeLog.mergeStackTrace("Failed renderReportDirs: " + Arrays.toString(mDirs), e);
            LogUtil.e(TAG, detail);
            return null;
        }
    }

    /**
     * 构建上报数据列表(元素为json字符串)
     * <p>TS接口为上报json数据, 每次上报一个元素,数据太多会失败
     * @return
     */
    public List<String> buildReportFileDataGroups() {
        List<File> files = prepareFiles();
        if (files == null || files.size() <= 0) {
            LogUtil.w(TAG, "buildReportFileDataGroups: invalid files: " + files);
            return null;
        }
        List<String> fileDataGroups = null;
        try {
            LogUtil.w(TAG, "Start to build report files count: " + files.size());
            //ZipUtil.zip(fileArray);//转化为文件夹数组,进行压缩
            fileDataGroups = ReportLogUtils.buildReportFileDataGroups(files);
        } catch (Exception e) {
            String detail = RuntimeLog.mergeStackTrace("Failed buildReportFileDataGroups: " + Arrays.toString(files.toArray()), e);
            LogUtil.e(TAG, detail);
        }
        if (fileDataGroups == null || fileDataGroups.isEmpty()) {
            LogUtil.w(TAG, "Built file Data Groups is null or empty:" +  Arrays.toString(files.toArray()));
        }
        return fileDataGroups;
    }

    public static ReportResult createResponse(int errno, String errmsg, String detail) {
        return createResponse(errno, errmsg, detail, null);
    }

    public static ReportResult createResponse(int errno, String errmsg, String detail, Exception ex) {
        ReportResult response = new ReportResult(errno, errmsg);
        response.detail = detail;
        response.exception = ex;
        return response;
    }

    public static ReportResult createErrorResponse(String errmsg, String detail) {
        return createResponse(-1, errmsg, detail);
    }

    public static ReportResult createCanceledResponse(String detail) {
        return createResponse(-1, "Task is canceled", detail);
    }
}
