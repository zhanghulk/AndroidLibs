package com.hulk.byod.parser.xml.msg;

import com.hulk.byod.parser.xml.msg.base.RequestTXHeader;
import com.hulk.byod.parser.xml.msg.base.RequestTxBase;

/**
 * 上传报文字段为大写，哥儿使用大写变量名
 * Created by zhanghao on 2017/10/18.
 */

public class RegisterRequestTx extends RequestTxBase<RegisterRequestTx.TXBody> {

    public RegisterRequestTx(RequestTXHeader tx_header, TXBody tx_body) {
        super(tx_header, tx_body);
    }

    @Override
    public String toString() {
        return "Tx{" +
                "TX_HEADER=" + TX_HEADER +
                ", TX_BODY=" + TX_BODY +
                '}';
    }
    
    public static class TXBody {
        public String OP_TYPE = "FREE";
        //RegRequest的json数组
        public String REQUEST = "";

        public TXBody(String op_type, String request_jsonArray) {
            this.OP_TYPE = op_type;
            this.REQUEST = request_jsonArray;
        }

        @Override
        public String toString() {
            return "TXBody{" +
                    "OP_TYPE='" + OP_TYPE + '\'' +
                    ", REQUEST='" + REQUEST + '\'' +
                    '}';
        }
    }
}
