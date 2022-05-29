package com.alibaba.fastjson2.v1issues.issue_3200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.filter.NameFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3206 {
    @Test
    public void test_for_issue() throws Exception {
        VO vo = new VO();
        vo.date = new java.util.Date(1590819204293L);

        assertEquals(JSON.toJSONString(vo), "{\"date\":\"2020-05-30\"}");

        String str = JSON.toJSONString(vo, new NameFilter() {
            public String process(Object object, String name, Object value) {
                return name;
            }
        });
        assertEquals("{\"date\":\"2020-05-30\"}", str);
    }

    public static class VO {
        @JSONField(format = "yyyy-MM-dd")
        public java.util.Date date;
    }
}
