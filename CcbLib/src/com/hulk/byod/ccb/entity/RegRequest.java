package com.hulk.byod.ccb.entity;

import com.hulk.util.gson.GsonParser;

/**
 * XML节点REQUEST:
 <p>其中数据内容由json数组字符串组成，其中FREE_USER_NAME不为空且用户存在时，该终端免认证申请通过后，
 只有用该用户登录终端才认为是免认证终端，否则是普通终端；ORG_CODE可为空，为空时如FREE_USER_NAME不空，
 则使用用户所在的机构编码，如FREE_USER_NAME为空，使用IP所在机构编码，如都无法取得ORG_CODE，报错。
 * Created by zhanghao on 2017/11/29.
 */

public class RegRequest extends GsonParser {
    public String FREE_USER_NAME = "";//zhangsan
    public String FREE_USER_PASSWORD = "";//12345678
    public String MOBILE = "";//手机号码：13366667777
    public String HOSTNAME = ""; //手机终端的名称
    public String OPERATING_SYSTEM = "";
    public String MODELS = "";
    public String DESCRIBE = "";
    public String ORG_CODE = "";//为空

    public RegRequest(String free_user_name, String free_user_password, String mobile) {
        this.FREE_USER_NAME = free_user_name;
        this.FREE_USER_PASSWORD = free_user_password;
        this.MOBILE = mobile;
    }

    @Override
    public String toString() {
        return "RegRequest{" +
                "FREE_USER_NAME='" + FREE_USER_NAME + '\'' +
                ", FREE_USER_PASSWORD='" + FREE_USER_PASSWORD + '\'' +
                ", MOBILE='" + MOBILE + '\'' +
                ", HOSTNAME='" + HOSTNAME + '\'' +
                ", OPERATING_SYSTEM='" + OPERATING_SYSTEM + '\'' +
                ", MODELS='" + MODELS + '\'' +
                ", DESCRIBE='" + DESCRIBE + '\'' +
                ", ORG_CODE='" + ORG_CODE + '\'' +
                '}';
    }
}
