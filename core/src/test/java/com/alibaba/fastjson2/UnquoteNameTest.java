package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnquoteNameTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 101;
        bean.date = new Date(1684735229662L);
        bean.value = 123456789;
        bean.timeUnit = TimeUnit.MILLISECONDS;

        String str = JSON.toJSONString(bean, JSONWriter.Feature.UnquoteFieldName);
        assertEquals(
                "{date:\"2023-05-22 14:00:29.662\",id:101,timeUnit:\"MILLISECONDS\",value:123456789}",
                str
        );
    }

    public static class Bean {
        public int id;
        public String name;
        public long value;
        public TimeUnit timeUnit;
        public Date date;
    }
}
