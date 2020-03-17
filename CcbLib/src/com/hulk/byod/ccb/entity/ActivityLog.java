package com.hulk.byod.ccb.entity;

import com.hulk.util.gson.GsonParser;

/**
 * Created by zhanghao on 2017/11/24.
 */

public class ActivityLog extends GsonParser {
    public String ACT_NAME = "";
    public String ACT_DESC = "";
    public String ACT_TYPE = "";//活动类型: 1表示违规事件, 默认0
    public String ACT_DATE = "";

    public ActivityLog(String act_name, String act_desc, String act_type) {
        this.ACT_NAME = act_name;
        this.ACT_DESC = act_desc;
        this.ACT_TYPE = act_type;
    }

    @Override
    public String toString() {
        return "ActivityLog{" +
                "ACT_NAME='" + ACT_NAME + '\'' +
                ", ACT_DESC='" + ACT_DESC + '\'' +
                ", ACT_TYPE='" + ACT_TYPE + '\'' +
                ", ACT_DATE='" + ACT_DATE + '\'' +
                '}';
    }
}
