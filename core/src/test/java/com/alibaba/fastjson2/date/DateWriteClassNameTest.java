package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateWriteClassNameTest {
    @Test
    public void test0() {
        long millis = 1324138987429L;
        Date date = new Date(millis);

        String str = JSON.toJSONString(date, JSONWriter.Feature.WriteClassName);
        assertEquals("new Date(1324138987429)", str);

        Date date1 = (Date) JSON.parse(str);
        assertEquals(date.getTime(), date1.getTime());
    }

    @Test
    public void testBean() {
        long millis = 1324138987429L;
        Date date = new Date(millis);

        Bean bean = new Bean();
        bean.date = date;

        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);
        assertEquals("{\"date\":\"2011-12-18 00:23:07.429\"}", str);

        Date date1 = (Date) JSON.parseObject(str, Bean.class).date;
        assertEquals(date.getTime(), date1.getTime());
    }

    public static class Bean {
        public Date date;
    }

    @Test
    public void testBean1() {
        long millis = 1324138987429L;
        Date date = new Date(millis);

        Bean1 bean = new Bean1();
        bean.date = date;

        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);
        assertEquals("{\"date\":new Date(1324138987429)}", str);

        Date date1 = (Date) JSON.parseObject(str, Bean1.class).date;
        assertEquals(date.getTime(), date1.getTime());
    }

    private static class Bean1 {
        public Object date;
    }

    @Test
    public void testBean2() {
        long millis = 1324138987429L;
        Date date = new Date(millis);

        Bean2 bean = new Bean2();
        bean.date = date;

        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);
        assertEquals("{\"date\":new Date(1324138987429)}", str);

        Date date1 = (Date) JSON.parseObject(str, Bean2.class).date;
        assertEquals(date.getTime(), date1.getTime());
    }

    private static class Bean2 {
        private Object date;

        public Object getDate() {
            return date;
        }

        public void setDate(Object date) {
            this.date = date;
        }
    }

    @Test
    public void testBean3() {
        long millis = 1324138987429L;
        Date date = new Date(millis);

        Bean3 bean = new Bean3();
        bean.date = date;

        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);
        assertEquals("{\"date\":new Date(1324138987429)}", str);

        Date date1 = (Date) JSON.parseObject(str, Bean3.class).date;
        assertEquals(date.getTime(), date1.getTime());

        Date date2 = (Date) JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Bean3.class).date;
        assertEquals(date.getTime(), date2.getTime());

        JSONReader jsonReader = TestUtils.createJSONReaderStr(str);
        Date date3 = (Date) jsonReader.read(Bean3.class).date;
        assertEquals(date.getTime(), date2.getTime());
    }

    public static class Bean3 {
        public Object date;
    }
}
