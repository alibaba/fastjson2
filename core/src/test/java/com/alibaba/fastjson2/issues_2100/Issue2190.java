package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue2190 {
    @Test
    public void test() {
        String str = "{\"code\":0,\"errMsg\":\"\",\"Data\":{\"is_focus\":1,\"user_info\":{\"userid\":\"13814\",\"nickname\":\"没事改昵称\"}}}";
        JSONObject jsonObject = JSON.parseObject(str);
        assertSame(jsonObject, JSONPath.eval(jsonObject, "[?exists(@.Data.user_info.userid)]"));
        assertNull(JSONPath.eval(jsonObject, "[?exists(@.Data.user_info.userid1)]"));
        assertNull(JSONPath.eval(jsonObject, "[?exists(@.Data.user_info1.userid)]"));
        assertNull(JSONPath.eval(jsonObject, "[?exists(@.Data1.user_info.userid)]"));
    }
}
