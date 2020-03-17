package com.hulk.byod.ccb.entity;

import java.util.HashSet;

/**
 * json 数组 字符串:
 * 目前未规定要上报那些活动，由客户端自已决定，可以是登录、退出、执行某活动。通常是填写一些自前一心跳以来的自认为关键的活动
 * <p>
 [
 {
 "ACT_NAME":"用户登录",
 "ACT_DESC":"chh2.vu用户登录系统",
 "ACT_DATE":"2016-09-10 08:08:08",
 },
 {
 "ACT_NAME":"下载任务",
 "ACT_DESC":"下载任务列表:task001,task002",
 "ACT_DATE":"2016-09-10 18:08:08",
 }
 ]

 * Created by zhanghao on 2017/10/19.
 */

public class ActivityJsonArray extends JsonArrayBase<ActivityLog> {

    public ActivityJsonArray() {
        super(new HashSet<ActivityLog>());
    }
}
