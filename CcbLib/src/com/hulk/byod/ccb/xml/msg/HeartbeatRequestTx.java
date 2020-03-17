package com.hulk.byod.ccb.xml.msg;

import com.hulk.byod.ccb.entity.ActivityJsonArray;
import com.hulk.byod.ccb.entity.TerminalInfo;
import com.hulk.byod.ccb.xml.msg.base.ITxBody;
import com.hulk.byod.ccb.xml.msg.base.RequestTXHeader;
import com.hulk.byod.ccb.xml.msg.base.RequestTxBase;

/**
 * 建行上传报文字段为大写，使用大写变量名
 * Created by zhanghao on 2017/10/18.
 */

public class HeartbeatRequestTx extends RequestTxBase<HeartbeatRequestTx.TXBody> {

    public HeartbeatRequestTx(RequestTXHeader tx_header, TXBody tx_body) {
    	super(tx_header, tx_body);
    }
    
    @Override
    public String toString() {
        return "Tx{" +
                "TX_HEADER=" + TX_HEADER +
                ", TX_BODY=" + TX_BODY +
                '}';
    }
    
    public static class TXBody implements ITxBody {
        //json 对象字符串： 终端设备信息
        public String TERMINAL_INFO = "";
        //json 数组 字符串: 目前未规定要上报那些活动，由客户端自已决定，可以是登录、退出、执行某活动。通常是填写一些自前一心跳以来的自认为关键的活动
        public String ACTIVITY = "";

        /**
         * @param terminal_info_json  json 对象 字符串: 终端设备信息
         * @param activity_jsonarray  json 数组 字符串: 目前未规定要上报那些活动，由客户端自已决定，
         *                            可以是登录、退出、执行某活动。通常是填写一些自前一心跳以来的自认为关键的活动
         */
        public TXBody(String terminal_info_json, String activity_jsonarray) {
            this.TERMINAL_INFO = terminal_info_json;
            this.ACTIVITY = activity_jsonarray;
        }

        public TXBody(TerminalInfo info, ActivityJsonArray array) {
            this(info == null ? "" : info.toJson(), array == null ? "" : array.toJson());
        }

        @Override
        public String toString() {
            return "HeartReqTXBody{" +
                    "TERMINAL_INFO='" + TERMINAL_INFO + '\'' +
                    ", ACTIVITY='" + ACTIVITY + '\'' +
                    '}';
        }
    }
}
