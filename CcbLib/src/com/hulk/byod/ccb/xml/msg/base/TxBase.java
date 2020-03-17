package com.hulk.byod.ccb.xml.msg.base;

import android.util.Log;

/**
 * Created by zhanghao on 18-1-16.
 */

public abstract class TxBase<H extends TxHeaderBase, B> implements ITx<H, B> {
    protected static final String TAG = "Tx";

    //msg tx common body node
    protected B TX_BODY = null;

    public B getTX_BODY() {
        return TX_BODY;
    }

    public void setTX_BODY(B tx_body) {
        TX_BODY = tx_body;
    }

    public static void log(String text) {
        Log.w(TAG, text + "");
    }
}
