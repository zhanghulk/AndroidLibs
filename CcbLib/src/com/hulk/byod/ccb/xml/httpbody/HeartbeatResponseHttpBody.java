package com.hulk.byod.ccb.xml.httpbody;

import android.text.TextUtils;
import android.util.Log;

import com.hulk.byod.ccb.XmlJsonUtils;
import com.hulk.byod.ccb.xml.msg.HeartbeatResponseTx;
import com.hulk.byod.ccb.xml.msg.HeartbeatResponseTx.TxBody;
import com.hulk.byod.ccb.xml.msg.base.ResponseTXHeader;
import com.hulk.byod.ccb.entity.CCBPolicy;

/**
 * 建行心跳接口请求返回结果响应 (网络传输字符串需要进行加密)
 * TX_BODY节点为json字符串，需要进行json解析
 * <p>xml template refer to assets/ccb/TS1609031_RESP.xml
 * Created by zhanghao on 2017/10/17.
 */

public class HeartbeatResponseHttpBody extends HttpBodyBase<HeartbeatResponseTx> {

    public final static String TAG = "HeartbeatResponse";

    private TxBody mTxBody = null;
    private CCBPolicy mPolicy = null;

    public HeartbeatResponseHttpBody() {
        this(null, null);
    }

    /**
     *
     * @param tx_header
     * @param tx_body_json  TX_BODY节点为json字符串，需要进行json解析
     */
    public HeartbeatResponseHttpBody(ResponseTXHeader tx_header, String tx_body_json) {
        super(new HeartbeatResponseTx(tx_header, tx_body_json));
    }

    public boolean isSuccess() {
        return getTX().isSuccess();
    }

    @Override
    public HeartbeatResponseTx parseFrom(String xmlText) {
        HeartbeatResponseHttpBody httpBody = XmlJsonUtils.fromXml(xmlText, getClass(), null);
        return httpBody != null ? httpBody.getTX() : null;
    }

    @Override
    public void setTX(HeartbeatResponseTx tx) {
        TX = tx;
        if (tx != null) {
            mTxBody = tx.getTxBody();
            mPolicy = tx.getPolicy();
        } else {
            mTxBody = null;
            mPolicy = null;
        }
    }

    public TxBody getTxBody() {
        if (mTxBody == null) {
            mTxBody = getTX().getTxBody();
        }
        return mTxBody;
    }

    public CCBPolicy getPolicy() {
        if (mPolicy == null) {
            mPolicy = getTX().getPolicy();
        }
        return mPolicy;
    }

    /**
     * 建行服务器配置心跳时间,单位： 秒
     * @return
     */
    public String getIntervalStr() {
        return mTxBody != null ? mTxBody.HEARTBEAT_INTERVAL : null;
    }

    public long getIntervalMillis() {
        int intervalMillis = 0;
        String intervalStr = getIntervalStr();
        if (!TextUtils.isEmpty(intervalStr)) {
            try {
                int interval = Integer.parseInt(intervalStr);
                intervalMillis = interval * 1000;
                Log.w(TAG, "CCB Heartbeat intervalMillis= " + intervalMillis);
            } catch (Throwable th) {
                Log.e(TAG, "parse failed interval: " + intervalStr, th);
            }
        } else {
            Log.w(TAG, "Heartbeat interval is null !!");
        }
        return intervalMillis;
    }
}
