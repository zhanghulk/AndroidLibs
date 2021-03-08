package com.hulk.android.report;

import org.apache.http.conn.ConnectTimeoutException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * 公共数据格式
 *
 * @param <T>
 */
public class ReportResult<T> {
	public int errno;//错误码，0为成功
	public String errmsg; //对应的错误信息
	public String detail;
	public T data; //详情数据
	public Exception exception;

	public ReportResult() {
	}

	public ReportResult(int errno, String errmsg) {
		this.errno = errno;
		this.errmsg = errmsg;
	}

	public boolean isOk() {
		return errno == 0;
	}

	public boolean isNetError() {
		if (exception != null) {
			if (exception instanceof SocketTimeoutException
					|| exception instanceof ConnectException
					|| exception instanceof ConnectTimeoutException
					|| exception instanceof UnknownHostException)
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "ReportResult{" +
				"errno=" + errno +
				", errmsg='" + errmsg + '\'' +
				", detail='" + detail + '\'' +
				", data=" + data +
				'}';
	}
}
