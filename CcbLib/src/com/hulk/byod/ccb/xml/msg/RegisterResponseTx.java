package com.hulk.byod.ccb.xml.msg;

import com.hulk.byod.ccb.xml.msg.base.ResponseTXHeader;
import com.hulk.byod.ccb.xml.msg.base.ResponseTxBase;

/**
 * 建行上传报文字段为大写，哥儿使用大写变量名
 * TX_BODY为字符串的响应xml节点TX
 * Created by zhanghao on 2017/10/18.
 */

public class RegisterResponseTx extends ResponseTxBase<String> {

    public RegisterResponseTx(ResponseTXHeader tx_header, String tx_body) {
        super(tx_header, tx_body);
    }
}
