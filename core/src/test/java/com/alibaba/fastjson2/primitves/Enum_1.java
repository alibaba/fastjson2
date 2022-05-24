package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Enum_1 {
    @Test
    public void test0() {
        assertEquals("\"HOURS\"", JSON.toJSONString(TimeUnit.HOURS));

        Bean bean = new Bean();
        bean.unit = TimeUnit.DAYS;

        assertEquals("{\"unit\":\"" + bean.unit.name() + "\"}", JSON.toJSONString(bean, JSONWriter.Feature.WriteEnumsUsingName));
        assertEquals("{\"unit\":" + bean.unit.ordinal() + "}", JSON.toJSONString(bean));

        assertEquals(TimeUnit.DAYS, JSON.parseObject("{\"unit\":6}", Bean.class).unit);
        assertEquals(TimeUnit.DAYS, JSON.parseObject("{\"unit\":6}").to(Bean.class).unit);
        assertEquals(TimeUnit.DAYS, JSON.parseObject("{\"unit\":\"DAYS\"}", Bean.class).unit);
        assertEquals(TimeUnit.DAYS, JSON.parseObject("{\"unit\":\"DAYS\"}").to(Bean.class).unit);

        assertEquals(
                TimeUnit.DAYS,
                JSONArray
                        .of("DAYS")
                        .getObject(0, TimeUnit.class));
        assertEquals(
                TimeUnit.DAYS,
                JSONArray
                        .of(6)
                        .getObject(0, TimeUnit.class));
    }

    public static class Bean {
        public TimeUnit unit;
    }
}
