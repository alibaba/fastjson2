package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2435 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"date\":\"2014-07-14 12:13\"}", Bean.class);
        assertNotNull(bean.date);
        assertEquals("{\"date\":\"2014-07-14 12:13:00\"}", JSON.toJSONString(bean));
    }

    public static class Bean {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public Date date;
    }
}
