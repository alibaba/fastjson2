package com.alibaba.fastjson2.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonFormatTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.time = LocalTime.of(12, 13, 14);
        String str = JSON.toJSONString(bean);
        assertEquals("{\"time\":\"121314\"}", str);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.time, bean1.time);
    }

    public static class Bean {
        @JsonFormat(pattern = "HHmmss")
        public LocalTime time;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.time = LocalTime.of(12, 13, 14);
        String str = JSON.toJSONString(bean);
        assertEquals("{\"time\":\"121314\"}", str);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.time, bean1.time);
    }

    public static class Bean1 {
        private LocalTime time;

        @JsonFormat(pattern = "HHmmss")
        public LocalTime getTime() {
            return time;
        }

        @JsonFormat(pattern = "HHmmss")
        public void setTime(LocalTime time) {
            this.time = time;
        }
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.time = LocalTime.of(12, 13, 14);
        String str = JSON.toJSONString(bean);
        assertEquals("{\"time\":\"121314\"}", str);
        Bean3 bean1 = JSON.parseObject(str, Bean3.class);
        assertEquals(bean.time, bean1.time);
    }

    @Test
    public void test3F() {
        Bean3 bean = new Bean3();
        bean.time = LocalTime.of(12, 13, 14);
        String str = JSON.toJSONString(bean, JSONWriter.Feature.FieldBased);
        assertEquals("{\"time\":\"121314\"}", str);
        Bean3 bean1 = JSON.parseObject(str, Bean3.class, JSONReader.Feature.FieldBased);
        assertEquals(bean.time, bean1.time);
    }

    @JsonFormat(pattern = "HHmmss")
    public static class Bean3 {
        public LocalTime time;
    }

    @Test
    public void test4() {
        Bean4 bean = new Bean4();
        bean.time = LocalTime.of(12, 13, 14);
        String str = JSON.toJSONString(bean);
        assertEquals("{\"time\":\"121314\"}", str);
        Bean4 bean1 = JSON.parseObject(str, Bean4.class);
        assertEquals(bean.time, bean1.time);
    }

    @JsonFormat(pattern = "HHmmss")
    public static class Bean4 {
        private LocalTime time;

        public LocalTime getTime() {
            return time;
        }

        public void setTime(LocalTime time) {
            this.time = time;
        }
    }
}
