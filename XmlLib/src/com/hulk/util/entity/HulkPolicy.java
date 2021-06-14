package com.hulk.byod.parser.entity;

import android.text.TextUtils;

import com.hulk.util.gson.GsonParser;

/**
 *上传报文字段为大写，哥儿使用大写变量名
 * Created by zhanghao on 2017/11/23.
 */

public class HulkPolicy extends GsonParser {
    public String POLICY_CODE = "";
    public String POLICY_NAME = "";
    /**
     * 策略获取维度类型
     TERMINAL:终端维度
     USER:用户维度
     USER_TYPE:用户类型维度
     IP:IP分组维度
     ORG:机构维度
     */
    public String POLICY_BIND_TYPE = "USER";
    //状态：1表示已经上传执行更新成功，其他只已在扩展，默认0
    public int STATUS = 0;

    public HulkPolicy(String policyCode) {
        this.POLICY_CODE = policyCode;
    }

    public HulkPolicy(String policyCode, String policyName) {
        this.POLICY_CODE = policyCode;
        this.POLICY_NAME = policyName;
    }

    public HulkPolicy(String policyCode, String policyName, String policyBindType) {
        this.POLICY_CODE = policyCode;
        this.POLICY_NAME = policyName;
        this.POLICY_BIND_TYPE = policyBindType;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HulkPolicy)) {
            return false;
        }
        if (o == null) {
            return false;
        }
        HulkPolicy p = (HulkPolicy) o;
        if (POLICY_CODE != null && POLICY_CODE.equals(p.POLICY_CODE)
                && POLICY_NAME != null && POLICY_NAME.equals(p.POLICY_NAME)
                && POLICY_BIND_TYPE != null && POLICY_BIND_TYPE.equals(p.POLICY_BIND_TYPE)) {
            return true;
        }
        return super.equals(o);
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(POLICY_CODE);
    }

    /**
     * 是否已上传成功1表示已经上传执行更新成功
     * @return
     */
    public boolean isExecuted() {
        return STATUS == 1;
    }

    public void setStatus(int status) {
        this.STATUS = status;
    }

    @Override
    public String toString() {
        return "HulkPolicy{" +
                "POLICY_CODE='" + POLICY_CODE + '\'' +
                ", POLICY_NAME='" + POLICY_NAME + '\'' +
                ", POLICY_BIND_TYPE='" + POLICY_BIND_TYPE + '\'' +
                ", STATUS='" + STATUS + '\'' +
                '}';
    }
}
