package com.hulk.byod.ccb.xml.msg;

import com.hulk.byod.ccb.entity.CCBPolicy;
import com.hulk.byod.ccb.xml.msg.base.EntryNode;
import com.hulk.byod.ccb.xml.msg.base.ITxBody;
import com.hulk.byod.ccb.xml.msg.base.ResponseTXHeader;
import com.hulk.byod.ccb.xml.msg.base.ResponseTxBase;

/**
 * 建行上传报文字段为大写，哥儿使用大写变量名
 * Created by zhanghao on 2017/10/18.
 */

public class AuthResponseTx extends ResponseTxBase<AuthResponseTx.TXBody> {

    public AuthResponseTx(ResponseTXHeader tx_header, TXBody tx_body) {
    	super(tx_header, tx_body);
    }

    public static class TXBody extends EntryNode<Entry> implements ITxBody {
        public TXBody(Entry entry) {
            super(entry);
        }
    }

    public static class Entry {
        public String USER_ID = "";
        public String USER_NAME = "";
        public String EMPEID = "";
        public String EFF_DAYS = "";
        public String PRE_TM = "";
        public String PRE_IP = "";

        public String IS_PERMITTED = "";
        public String TICKET = "";
        public String LOGINED = "";
        public String LATEST_VERSION = "";
        public String FULL_NAME = "";
        public String ORG_CODE = "";

        public String ORG_NAME = "";
        public String BRAN_ORG_CODE = "";
        public String ANCESTOR_CODE = "";
        public EntryExtends EXTENDS = null;
        public EntryPolicies POLICIES = null;

        public Entry() {
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "USER_ID='" + USER_ID + '\'' +
                    ", USER_NAME='" + USER_NAME + '\'' +
                    ", EMPEID='" + EMPEID + '\'' +
                    ", EFF_DAYS='" + EFF_DAYS + '\'' +
                    ", PRE_TM='" + PRE_TM + '\'' +
                    ", PRE_IP='" + PRE_IP + '\'' +
                    ", IS_PERMITTED='" + IS_PERMITTED + '\'' +
                    ", TICKET='" + TICKET + '\'' +
                    ", LOGINED='" + LOGINED + '\'' +
                    ", LATEST_VERSION='" + LATEST_VERSION + '\'' +
                    ", FULL_NAME='" + FULL_NAME + '\'' +
                    ", ORG_CODE='" + ORG_CODE + '\'' +
                    ", ORG_NAME='" + ORG_NAME + '\'' +
                    ", BRAN_ORG_CODE='" + BRAN_ORG_CODE + '\'' +
                    ", ANCESTOR_CODE='" + ANCESTOR_CODE + '\'' +
                    ", EXTENDS=" + EXTENDS +
                    ", POLICIES=" + POLICIES +
                    '}';
        }
    }

    public static class EntryExtends {
        public String CODE = "";
        public String MSG = "";
        public EntryExtend EXTEND = null;

        @Override
        public String toString() {
            return "EntryExtends{" +
                    "CODE='" + CODE + '\'' +
                    ", MSG='" + MSG + '\'' +
                    ", EXTEND=" + EXTEND +
                    '}';
        }
    }

    public static class EntryExtend {
        public String EXTEND_NAME = "";
        public String EXTEND_VALUE = "";

        @Override
        public String toString() {
            return "EntryExtend{" +
                    "EXTEND_NAME='" + EXTEND_NAME + '\'' +
                    ", EXTEND_VALUE='" + EXTEND_VALUE + '\'' +
                    '}';
        }
    }

    public static class EntryPolicies {
        public String CODE = "";
        public String MSG = "";
        public CCBPolicy POLICY = null;

        @Override
        public String toString() {
            return "EntryPolicies{" +
                    "CODE='" + CODE + '\'' +
                    ", MSG='" + MSG + '\'' +
                    ", POLICY=" + POLICY +
                    '}';
        }
    }
}
