package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JSONObjectTest2 {
    @Test
    public void testSqlDate() {
        assertNotNull(JSONObject
                .of("date", "0000-00-00")
                .getObject("date", Date.class));

        assertNotNull(JSONObject
                .of("date", "0000-00-00")
                .getObject("date", Timestamp.class));

        assertNotNull(JSONObject
                .of("date", "0000-00-00")
                .getObject("date", Time.class));
    }

    @Test
    public void testSqlDate1() {
        assertNotNull(JSONObject
                .of("date", "0000-00-00 00:00:00")
                .getObject("date", Date.class));

        assertNotNull(JSONObject
                .of("date", "0000-00-00 00:00:00")
                .getObject("date", Timestamp.class));

        assertNotNull(JSONObject
                .of("date", "0000-00-00 00:00:00")
                .getObject("date", Time.class));
    }

    @Test
    public void testSqlDate2() {
        assertNotNull(JSONObject
                .of("date", "0000-00-00 00:00:00")
                .toJavaObject(new TypeReference<Bean<Date>>() {
                }.getType()));

        assertNotNull(JSONObject
                .of("date", "0000-00-00 00:00:00")
                .toJavaObject(new TypeReference<Bean<Timestamp>>() {
                }.getType()));

        assertNotNull(JSONObject
                .of("date", "0000-00-00 00:00:00")
                .toJavaObject(new TypeReference<Bean<Time>>() {
                }.getType()));
    }

    @Test
    public void testSqlDate3() {
        String str = "{\"date\":\"0000-00-00 00:00:00\"}";
        {
            Bean<Date> bean = JSON.parseObject(str, new TypeReference<Bean<Date>>() {
            }.getType());
            assertNotNull(bean.date);
        }
        {
            Bean<Date> bean = JSON.parseObject(str, new TypeReference<Bean<Timestamp>>() {
            }.getType());
            assertNotNull(bean.date);
        }
        {
            Bean<Date> bean = JSON.parseObject(str, new TypeReference<Bean<Time>>() {
            }.getType());
            assertNotNull(bean.date);
        }
    }

    @Test
    public void testSqlDate4() {
        byte[] str = "{\"date\":\"0000-00-00 00:00:00\"}".getBytes(StandardCharsets.UTF_8);
        {
            Bean<Date> bean = JSON.parseObject(str, new TypeReference<Bean<Date>>() {
            }.getType());
            assertNotNull(bean.date);
        }
        {
            Bean<Date> bean = JSON.parseObject(str, new TypeReference<Bean<Timestamp>>() {
            }.getType());
            assertNotNull(bean.date);
        }
        {
            Bean<Date> bean = JSON.parseObject(str, new TypeReference<Bean<Time>>() {
            }.getType());
            assertNotNull(bean.date);
        }
    }

    @Test
    public void testInterface() {
        if (TestUtils.GRAALVM || TestUtils.ANDROID) {
            return;
        }

        BeanInterface bean =
                JSONObject
                        .of("id", 123)
                        .toJavaObject(BeanInterface.class);
        assertEquals(123, bean.getId());
    }

    public interface BeanInterface {
        int getId();

        void setId(String id);
    }

    public static class Bean<T> {
        public T date;
    }
}
