package com.alibaba.fastjson.issue_3000;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3049 {
    @Test
    public void test_for_issue() throws Exception {
        String json1 = "{\"date\":\"2019-11-1 21:45:12\"}";
        MyObject myObject1 = JSON.parseObject(json1, MyObject.class);
        String str2 = JSON.toJSONStringWithDateFormat(myObject1, "yyyy-MM-dd HH:mm:ss");
        assertEquals("{\"date\":\"2019-11-01 21:45:12\"}", str2);
    }

    public static class MyObject {
        public java.util.Date date;
    }
}
