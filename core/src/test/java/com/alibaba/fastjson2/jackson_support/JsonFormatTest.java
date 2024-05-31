package com.alibaba.fastjson2.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Date;

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

    @Test
    public void test5() throws Exception {
        Bean5 bean = new Bean5();
        bean.time = new Date();

        ObjectMapper objectMapper = new ObjectMapper();
        String fastjson = JSON.toJSONString(bean);
        String jackson = objectMapper.writeValueAsString(bean);
        assertEquals(jackson, fastjson);

        Bean5 parsed = JSON.parseObject(fastjson, Bean5.class);
        assertEquals(bean.time, parsed.time);
    }

    @Data
    public static class Bean5 {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        private Date time;
    }

    @Test
    public void test6() throws Exception {
        Bean6 bean = new Bean6();
        bean.time = new Date();

        ObjectMapper objectMapper = new ObjectMapper();
        String fastjson = JSON.toJSONString(bean);
        String jackson = objectMapper.writeValueAsString(bean);
        assertEquals(jackson, fastjson);
//
//        Bean6 parsed0 = objectMapper.readValue(jackson, Bean6.class);
//        Bean6 parsed1 = JSON.parseObject(fastjson, Bean6.class);
//        assertEquals(parsed0.time.getTime(), parsed1.time.getTime());
    }

    @Data
    public static class Bean6 {
        @JsonFormat(pattern = "yyyy-MM-dd", locale = "zh_CN")
        private Date time;
    }
}
