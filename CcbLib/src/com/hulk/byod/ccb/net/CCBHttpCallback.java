package com.hulk.byod.ccb.net;

import com.hulk.byod.ccb.entity.CCBHttpResponse;

/**
 * Created by zhanghao on 2017/11/23.
 */

public interface CCBHttpCallback {
    void onHttpResult(CCBHttpResponse response, Object obj);
}
