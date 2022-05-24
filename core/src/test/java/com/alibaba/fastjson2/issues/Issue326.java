package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue326 {
    @Test
    public void test() {
        JSONObject obj = new JSONObject();
        obj.put("issue", "3");
        obj.put("company", "aaa");
        obj.put("signer", "bbbb");
        obj.put("title", "cccc");
        obj.put("overview", "ddddd");
        obj.put("details", new String[]{"eeee", "ffff", "eeee"});
        obj.put("datetime", "2022-05-24 14:50:32");
        obj.put("onduty", "gggg");
        obj.put("review", "hhhh");
        String x = JSON.toJSONString(obj);
        JSONObject jsonObject = JSON.parseObject(x);
        String[] details = jsonObject.getObject("details", String[].class);
        assertEquals("[eeee, ffff, eeee]", Arrays.toString(details));
    }
}
