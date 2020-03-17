package com.hulk.byod.ccb.parsers;

import android.util.Log;

import com.hulk.byod.ccb.entity.CCBPolicy;
import com.hulk.byod.ccb.xml.httpbody.AuthResponseHttpBody;
import com.hulk.byod.ccb.xml.msg.AuthResponseTx;
import com.hulk.byod.ccb.xml.msg.base.ResponseTXHeader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * 组合认证返回结果解析器
 * Created by zhanghao on 2017/10/23.
 */

public class AuthResponseParser extends AbsXmlParser<AuthResponseHttpBody> {
    @Override
    public boolean parseTX(XmlPullParser parser, AuthResponseHttpBody httpBody) throws XmlPullParserException, IOException {
        ResponseTXHeader header = httpBody.getTX_HEADER();
        boolean headerParsed = parseTXHeader(parser, header);
        if (!headerParsed) {
            boolean bodyParsed = parseTXBody(parser, httpBody.getTX_BODY());
            if (!bodyParsed) {
                Log.i(TAG, "Not need to parse filed: " + parser.getName());
            }
        }
        return false;
    }

    public static String USER_ID = "USER_ID";
    public static String USER_NAME = "USER_NAME";
    public static String EMPEID = "EMPEID";
    public static String EFF_DAYS = "EFF_DAYS";
    public static String PRE_TM = "PRE_TM";
    public static String PRE_IP = "PRE_IP";

    public static String IS_PERMITTED = "IS_PERMITTED";
    public static String TICKET = "TICKET";
    public static String LOGINED = "LOGINED";
    public static String LATEST_VERSION = "LATEST_VERSION";
    public static String FULL_NAME = "FULL_NAME";
    public static String ORG_CODE = "ORG_CODE";

    public static String ORG_NAME = "ORG_NAME";
    public static String BRAN_ORG_CODE = "BRAN_ORG_CODE";
    public static String ANCESTOR_CODE = "ANCESTOR_CODE";
    public static String EXTENDS = "EXTENDS";
    public static String POLICIES = "POLICIES";
    public boolean parseTXBody(XmlPullParser parser, AuthResponseTx.TXBody txBody) throws XmlPullParserException, IOException {
        AuthResponseTx.Entry entry = txBody.getENTRY();
        String name = parser.getName();
        boolean entryParsed = parseEntry(parser, entry);
        if (!entryParsed) {
            if (EXTENDS.equals(name)) {
                return parseEntryExtends(parser, entry.EXTENDS);
            } else if (POLICIES.equals(name)) {
                return parseEntryPolicies(parser, entry.POLICIES);
            }
        }
        return false;
    }

    boolean parseEntry(XmlPullParser parser, AuthResponseTx.Entry entry) throws XmlPullParserException, IOException {
        String name = parser.getName();
        String value = nextText(parser);
        if (USER_ID.equals(name)) {
            entry.USER_ID = value;
        } else if (USER_NAME.equals(name)) {
            entry.USER_NAME = value;
        } else if (EMPEID.equals(name)) {
            entry.EMPEID = value;
        } else if (EFF_DAYS.equals(name)) {
            entry.EFF_DAYS = value;
        } else if (PRE_TM.equals(name)) {
            entry.PRE_TM = value;
        } else if (PRE_IP.equals(name)) {
            entry.PRE_IP = value;
        } else if (IS_PERMITTED.equals(name)) {
            entry.IS_PERMITTED = value;
        } else if (TICKET.equals(name)) {
            entry.TICKET = value;
        } else if (LOGINED.equals(name)) {
            entry.LOGINED = value;
        } else if (LATEST_VERSION.equals(name)) {
            entry.LATEST_VERSION = value;
        } else if (FULL_NAME.equals(name)) {
            entry.FULL_NAME = value;
        } else if (ORG_CODE.equals(name)) {
            entry.ORG_CODE = value;
        } else if (ORG_NAME.equals(name)) {
            entry.ORG_NAME = value;
        } else if (BRAN_ORG_CODE.equals(name)) {
            entry.BRAN_ORG_CODE = value;
        } else if (ANCESTOR_CODE.equals(name)) {
            entry.ANCESTOR_CODE = value;
        } else {
            return false;
        }
        return true;
    }

    public static String CODE = "CODE";
    public static String MSG = "MSG";
    public static String POLICY = "POLICY";
    private boolean parseEntryPolicies(XmlPullParser parser, AuthResponseTx.EntryPolicies policies) throws XmlPullParserException, IOException  {
        String name = parser.getName();
        String value = nextText(parser);
        if (CODE.equals(name)) {
            policies.CODE = value;
            return true;
        } else if (MSG.equals(name)) {
            policies.MSG = value;
            return true;
        } else if (POLICY.equals(name)) {
            return parsePolicy(parser, policies.POLICY);
        }
        return false;
    }

    public static String POLICY_CODE = "POLICY_CODE";
    public static String POLICY_NAME = "POLICY_NAME";
    public static String POLICY_BIND_TYPE = "POLICY_BIND_TYPE";
    private static boolean parsePolicy(XmlPullParser parser, CCBPolicy policy) throws XmlPullParserException, IOException {
        String name = parser.getName();
        String value = nextText(parser);
        if (POLICY_CODE.equals(name)) {
            policy.POLICY_CODE = value;
            return true;
        } else if (POLICY_NAME.equals(name)) {
            policy.POLICY_NAME = value;
            return true;
        } else if (POLICY_BIND_TYPE.equals(name)) {
            policy.POLICY_BIND_TYPE = value;
            return true;
        } else {
            return false;
        }
    }

    public static String EXTENDS_CODE = "CODE";
    public static String EXTENDS_MSG = "MSG";
    static String EXTEND = "EXTEND";
    public boolean parseEntryExtends(XmlPullParser parser, AuthResponseTx.EntryExtends entryExtends) throws XmlPullParserException, IOException {
        String name = parser.getName();
        String value = nextText(parser);
        if (EXTENDS_CODE.equals(name)) {
            entryExtends.CODE = value;
            return true;
        } else if (EXTENDS_MSG.equals(name)) {
            entryExtends.MSG = value;
            return true;
        } else if (EXTEND.equals(name)) {
            return parseExtend(parser, entryExtends.EXTEND);
        }
        return false;
    }

    public static String EXTEND_NAME = "EXTEND_NAME";
    public static String EXTEND_VALUE = "EXTEND_VALUE";
    private static boolean parseExtend(XmlPullParser parser, AuthResponseTx.EntryExtend entryExtend) throws XmlPullParserException, IOException {
        String name = parser.getName();
        String value = nextText(parser);
        if (EXTEND_NAME.equals(name)) {
            entryExtend.EXTEND_NAME = value;
            return true;
        } else if (EXTEND_VALUE.equals(name)) {
            entryExtend.EXTEND_VALUE = value;
            return true;
        }
        return false;
    }
}
