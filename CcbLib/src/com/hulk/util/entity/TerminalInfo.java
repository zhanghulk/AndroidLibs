package com.hulk.byod.parser.entity;

import com.hulk.util.gson.GsonParser;

/**
 * json串对应实体类 eg.
 * {
 "HOSTNAME":"test.hostname",
 "OPERATING_SYSTEM":"android 7",
 "MODELS":"Moto X Style",
 "DESCRIBE":"test",
 "ACCESS_ORG_CODE":"010200300",
 "USER_NAME":"monitor.zh",
 "ONLINE_STATUS":"2"
 }
 * Created by zhanghao on 2017/10/19.
 */

public class TerminalInfo extends GsonParser {
    //手机端名称
    public String HOSTNAME = "";

    //操作系统及版本号
    public String OPERATING_SYSTEM = "";

    //手机型号
    public String MODELS = "";

    public String DESCRIBE = "";

    //手机端可不填（PC客户端是根据IP检索IP规划表得出的机构编码，手机端因没有此表，所以无法获取）
    public String ACCESS_ORG_CODE = "";

    //登录用户名
    public String USER_NAME = "";

    //终端在线状态 1：终端网络在线（但用户未登录）2：用户在线（用户已登录）
    public String ONLINE_STATUS = "";
}
