package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2550 {
    @Test
    public void mutatedTest1() {
        Bean1 bean = new Bean1();
        bean.date = new Date(1655097829796L);
        String str = JSON.toJSON(bean).toString();
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
    public void mutatedTest2() {
        Bean2 bean = new Bean2();
        bean.date = new Date(1655097829796L);
        String str = JSON.toJSON(bean).toString();
        assertEquals("{\"date\":1655097829796}", str);

        Bean2 bean1 = JSON.parseObject(str, Bean2.class);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        Bean2 bean2 = JSON.parseObject(str).toJavaObject(Bean2.class);
        assertEquals(bean.date.getTime(), bean2.date.getTime());
    }

    public static class Bean2 {
        @JSONField(format = "millis")
        public Date date;
    }

    @Test
    public void mutatedTest1fj() {
        Bean1fj bean = new Bean1fj();
        bean.date = new Date(1655097829796L);
        String str = com.alibaba.fastjson.JSON.toJSON(bean).toString();
        assertEquals("{\"date\":1655097829796}", str);

        Bean1fj bean1 = com.alibaba.fastjson.JSON.parseObject(str, Bean1fj.class);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        Bean1fj bean2 = com.alibaba.fastjson.JSON.parseObject(str).toJavaObject(Bean1fj.class);
        assertEquals(bean.date.getTime(), bean2.date.getTime());
    }

    public static class Bean1fj {
        @com.alibaba.fastjson.annotation.JSONField(format = "millis")
        public Date date;
    }

    @Test
    public void mutatedTest2fj() {
        Bean2fj bean = new Bean2fj();
        bean.date = new Date(1655097829796L);
        String str = com.alibaba.fastjson.JSON.toJSON(bean).toString();
        assertEquals("{\"date\":1655097829796}", str);

        Bean2fj bean1 = com.alibaba.fastjson.JSON.parseObject(str, Bean2fj.class);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        Bean2fj bean2 = com.alibaba.fastjson.JSON.parseObject(str).toJavaObject(Bean2fj.class);
        assertEquals(bean.date.getTime(), bean2.date.getTime());
    }

    public static class Bean2fj {
        @com.alibaba.fastjson.annotation.JSONField(format = "millis")
        public Date date;
    }
}
