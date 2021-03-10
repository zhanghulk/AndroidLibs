package com.hulk.byod.parser.entity;

import java.util.ArrayList;

/**
 * [
 {
 "FREE_USER_NAME":" monitor.zh ",
 "HOSTNAME":"test.hostname",
 "OPERATING_SYSTEM":"test.macOS",
 "MODELS":"test.macbookpro",
 "DESCRIBE":"test",
 "ORG_CODE":"010200300",
 }
 ]
 * Created by zhanghao on 2017/10/19.
 */

public class RegRequestJsonArray extends JsonArrayBase<RegRequest> {

    public RegRequestJsonArray() {
        super(new ArrayList<RegRequest>());
    }
}
