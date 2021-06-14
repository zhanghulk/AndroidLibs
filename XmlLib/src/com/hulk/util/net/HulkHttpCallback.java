package com.hulk.byod.parser.net;

import com.hulk.byod.parser.entity.HulkHttpResponse;

/**
 * Created by zhanghao on 2017/11/23.
 */

public interface HulkHttpCallback {
    void onHttpResult(HulkHttpResponse response, Object obj);
}
