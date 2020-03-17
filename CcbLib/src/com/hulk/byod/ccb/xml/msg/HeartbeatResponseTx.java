package com.hulk.byod.ccb.xml.msg;

import android.text.TextUtils;

import com.hulk.util.gson.GsonParser;
import com.hulk.byod.ccb.entity.CCBPolicy;
import com.hulk.byod.ccb.xml.msg.base.ResponseTXHeader;
import com.hulk.byod.ccb.xml.msg.base.ResponseTxBase;

/**
 * 建行上传报文字段为大写，哥儿使用大写变量名
 * Created by zhanghao on 2017/10/18.
 */

public class HeartbeatResponseTx extends ResponseTxBase<String> {

    public HeartbeatResponseTx(ResponseTXHeader tx_header, String tx_body) {
    	super(tx_header, tx_body);
    }

    @Override
    public String toString() {
        return "HeartbeatResponseTx{" +
                "TX_HEADER=" + TX_HEADER +
                ", TX_BODY=" + TX_BODY +
                '}';
    }

    public TxBody getTxBody() {
        return parseTxBody(getTX_BODY());
    }

    public static TxBody parseTxBody(String tx_body_json) {
        TxBody txBody = null;
        if (!TextUtils.isEmpty(tx_body_json)) {
            txBody = TxBody.fromJson(tx_body_json, TxBody.class);
        } else {
            log("parse Heartbeat Response TxBody failed tx_body_json is null ! ");
        }
        return txBody;
    }

    public CCBPolicy getPolicy() {
        TxBody b = getTxBody();
        if (b != null && !TextUtils.isEmpty(b.POLICY_CODE)) {
            return new CCBPolicy(b.POLICY_CODE, b.POLICY_NAME, b.POLICY_BIND_TYPE);
        } else {
            log("Invalid CCBPolicy from TxBody: " + b);
            return null;
        }
    }

    public class TxBody extends GsonParser {
        private static final String TAG = "HeartbeatResponseTxBody";
        //true or false
        public String IS_FREE;
        public String POLICY_CODE;
        public String POLICY_NAME;
        public String POLICY_BIND_TYPE;
        public String USER_ID;
        public String USER_NAME;
        public String FULL_NAME;
        public String EMPE_ID;
        public String ORG_CODE;
        public String ORG_NAME;
        //心跳间隔时间，单位：秒
        public String HEARTBEAT_INTERVAL;

        /**
         * 是否为免认证注册
         * @return
         */
        public boolean isFree() {
            return Boolean.parseBoolean(IS_FREE);
        }

        @Override
        public String toString() {
            return "TxBody{" +
                    "IS_FREE='" + IS_FREE + '\'' +
                    ", POLICY_CODE='" + POLICY_CODE + '\'' +
                    ", POLICY_NAME='" + POLICY_NAME + '\'' +
                    ", POLICY_BIND_TYPE='" + POLICY_BIND_TYPE + '\'' +
                    ", USER_ID='" + USER_ID + '\'' +
                    ", USER_NAME='" + USER_NAME + '\'' +
                    ", FULL_NAME='" + FULL_NAME + '\'' +
                    ", EMPE_ID='" + EMPE_ID + '\'' +
                    ", ORG_CODE='" + ORG_CODE + '\'' +
                    ", ORG_NAME='" + ORG_NAME + '\'' +
                    ", HEARTBEAT_INTERVAL='" + HEARTBEAT_INTERVAL + '\'' +
                    '}';
        }
    }
}
