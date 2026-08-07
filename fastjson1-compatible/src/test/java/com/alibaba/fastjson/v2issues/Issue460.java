package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue460 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.date = new Date(1655097829796L);
        String str = JSON.toJSONString(bean);
        assertEquals("{\"date\":1655097829796}", str);

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        Bean bean2 = JSON.parseObject(str).toJavaObject(Bean.class);
        assertEquals(bean.date.getTime(), bean2.date.getTime());
    }

    public static class Bean {
        public Date date;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.date = new Date(1655097829796L);
        String str = JSON.toJSONString(bean);
        assertEquals("{\"date\":1655097829796}", str);

        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        Bean1 bean2 = JSON.parseObject(str).toJavaObject(Bean1.class);
        assertEquals(bean.date.getTime(), bean2.date.getTime());
    }

    public static class Bean1 {
        @JSONField(format = "millis")
        public Date date;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.date = new Date(1655097829796L);
        String str = JSON.toJSONString(bean);
        assertEquals("{\"date\":1655097829}", str);

        Bean2 bean1 = JSON.parseObject(str, Bean2.class);
        assertEquals(1655097829000L, bean1.date.getTime());
        Bean2 bean2 = JSON.parseObject(str).toJavaObject(Bean2.class);
        assertEquals(1655097829000L, bean2.date.getTime());
    }

    public static class Bean2 {
        @JSONField(format = "unixtime")
        public Date date;
    }
}
