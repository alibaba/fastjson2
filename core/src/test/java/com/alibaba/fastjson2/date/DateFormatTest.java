package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DateFormatTest {
    @Test
    public void test0() {
        String json = "{\"date\":\"2018-07-14\"}";
        Bean bean = JSON.parseObject(json, Bean.class);
        assertNotNull(bean.date);
        assertEquals(json, JSON.toJSONString(bean));

        Bean bean2 = JSON.parseObject(json).toJavaObject(Bean.class);
        assertEquals(bean.date, bean2.date);

        Bean bean3 = new Bean();
        JSONPath.set(bean3, "$.date", "2018-07-14");
        assertEquals(bean.date, bean3.date);
    }

    public static class Bean {
        @JSONField(format = "yyyy-MM-dd")
        private Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

    @Test
    public void test1() {
        String json = "{\"date\":\"2018-07-14\"}";
        Bean1 bean = JSON.parseObject(json, Bean1.class);
        assertNotNull(bean.date);
        assertEquals(json, JSON.toJSONString(bean));

        Bean1 bean2 = JSON.parseObject(json).toJavaObject(Bean1.class);
        assertEquals(bean.date, bean2.date);

        Bean1 bean3 = new Bean1();
        JSONPath.set(bean3, "$.date", "2018-07-14");
        assertEquals(bean.date, bean3.date);
    }

    public static class Bean1 {
        @JSONField(format = "yyyy-MM-dd")
        public Date date;
    }

    @Test
    public void testFormat0() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String json = JSON.toJSONString(format);
        assertEquals("\"yyyy-MM-dd\"", json);
        SimpleDateFormat format1 = JSON.parseObject(json, SimpleDateFormat.class);
        assertEquals(format, format1);
    }

    @Test
    public void testFormat2() {
        LocalDateTime ldt = LocalDateTime.of(2012, 1, 2, 12, 13, 14);
        assertEquals("\"2012-01-02\"", JSON.toJSONString(ldt, "yyyy-MM-dd"));
        assertEquals("\"12-13-14\"", JSON.toJSONString(ldt, "HH-mm-ss"));
        assertEquals("1325477594000", JSON.toJSONString(ldt, "millis"));
        assertEquals("1325477594", JSON.toJSONString(ldt, "unixtime"));
    }
}
