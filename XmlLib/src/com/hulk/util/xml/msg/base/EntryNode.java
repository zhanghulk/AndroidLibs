package com.hulk.byod.parser.xml.msg.base;

/**
 * 组合认证的请求/响应 TX_BODY
 * Created by zhanghao on 2017/10/18.
 */

public class EntryNode<E> {
    public E ENTRY = null;

    public EntryNode(E ENTRY) {
        this.ENTRY = ENTRY;
    }

    public E getENTRY() {
        return ENTRY;
    }

    @Override
    public String toString() {
        return "EntryNode{" +
                "ENTRY=" + ENTRY +
                '}';
    }
}
