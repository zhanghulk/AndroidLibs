package com.hulk.byod.parser.xml.msg.base;

/**
 * TX根节点接口规范
 * Created by zhanghao on 2017/12/20.
 */

public interface ITx<H, B> {

    H getTX_HEADER();

    void setTX_HEADER(H tx_header);

    B getTX_BODY();

    void setTX_BODY(B tx_body);
}
