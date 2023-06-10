package com.alibaba.fastjson2.v1issues.issue_2700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2784 {
    @Test
    public void test_for_issue_4() {
        Model m = new Model();
        m.date = new Date();
        String str = JSON.toJSONString(m);
        assertEquals("{\"date\":"
                + m.date.getTime()
                + "}", str);

        Model m1 = JSON.parseObject(str, Model.class);
        assertEquals(m.date.getTime(),
                m1.date.getTime());
    }

    @Test
    public void test_for_issue_5() {
        Model m = new Model();
        m.date1 = new Date();
        String str = JSON.toJSONString(m);
        assertEquals("{\"date1\":"
                + (m.date1.getTime() / 1000)
                + "}", str);

        Model m1 = JSON.parseObject(str, Model.class);
        assertEquals(m.date1.getTime() / 1000,
                m1.date1.getTime() / 1000);
    }

    @Test
    public void test_for_issue_6() {
        Model m = new Model();
        m.date1 = new Date();
        String str = JSON.toJSONString(m);
        assertEquals("{\"date1\":"
                + (m.date1.getTime() / 1000)
                + "}", str);

        Model m1 = JSON.parseObject(str, Model.class);
        assertEquals(m.date1.getTime() / 1000,
                m1.date1.getTime() / 1000);
    }

    public static class Model {
        @JSONField(format = "millis")
        public Date date;

        @JSONField(format = "unixtime")
        public Date date1;
    }
}
