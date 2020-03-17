package com.hulk.byod.ccb.xml.msg;

import com.hulk.byod.ccb.xml.msg.base.EntryNode;
import com.hulk.byod.ccb.xml.msg.base.ITxBody;
import com.hulk.byod.ccb.xml.msg.base.RequestTXHeader;
import com.hulk.byod.ccb.xml.msg.base.RequestTxBase;

/**
 * 建行上传报文字段为大写，哥儿使用大写变量名
 * Created by zhanghao on 2017/10/18.
 */

public class AuthRequestTx extends RequestTxBase<AuthRequestTx.TXBody> {
    public AuthRequestTx(RequestTXHeader tx_header, TXBody tx_body) {
    	super(tx_header, tx_body);
    }

    @Override
    public String toString() {
        return "AuthRequestTx{" +
                "TX_HEADER=" + TX_HEADER +
                ", TX_BODY=" + TX_BODY +
                '}';
    }
    
    public static class TXBody extends EntryNode<Entry> implements ITxBody {
        public TXBody(Entry entry) {
            super(entry);
        }
    }
    
    public static class Entry {
        public String AUTH_TYPE = "15";
        public String LOGIN_NAME = "";//登录名,用户输入
        public String SEC_VOUCHER = "";//登录密码,用户输入
        public String IS_CREATE_TICKET = "true";
        public String IS_GET_LOGINED_APP = "true";
        public String SERVICE_POINT = "";//可为空

        public Entry() {
        }

        public Entry(String auth_type, String login_name, String sec_voucher,
                     String is_create_ticket, String is_get_logined_app, String service_point) {
            this.AUTH_TYPE = auth_type;
            this.LOGIN_NAME = login_name;
            this.SEC_VOUCHER = sec_voucher;
            this.IS_CREATE_TICKET = is_create_ticket;
            this.IS_GET_LOGINED_APP = is_get_logined_app;
            this.SERVICE_POINT = service_point;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "AUTH_TYPE='" + AUTH_TYPE + '\'' +
                    ", LOGIN_NAME='" + LOGIN_NAME + '\'' +
                    ", SEC_VOUCHER='" + SEC_VOUCHER + '\'' +
                    ", IS_CREATE_TICKET='" + IS_CREATE_TICKET + '\'' +
                    ", IS_GET_LOGINED_APP='" + IS_GET_LOGINED_APP + '\'' +
                    ", SERVICE_POINT='" + SERVICE_POINT + '\'' +
                    '}';
        }
    }
}
