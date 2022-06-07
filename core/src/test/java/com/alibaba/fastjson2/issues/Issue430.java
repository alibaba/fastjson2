package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue430 {
    @Test
    public void test() {
        JSONObject jsonObject = JSON.parseObject("{\n" +
                "  \"value\": \"zhinan\",\n" +
                "  \"label\": \"指南\",\n" +
                "  \"children\": [\n" +
                "    {\n" +
                "      \"value\": \"shejiyuanze\",\n" +
                "      \"label\": \"设计原则\",\n" +
                "      \"children\": []\n" +
                "    }\n" +
                "  ]\n" +
                "}\n");

        assertEquals("{\n" +
                "\t\"value\":\"zhinan\",\n" +
                "\t\"label\":\"指南\",\n" +
                "\t\"children\":[\n" +
                "\t\t{\n" +
                "\t\t\t\"value\":\"shejiyuanze\",\n" +
                "\t\t\t\"label\":\"设计原则\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}", jsonObject.toString(JSONWriter.Feature.NotWriteEmptyArray, JSONWriter.Feature.PrettyFormat));
    }

    @Test
    public void test1() {
        JSONObject object = JSONObject.of("values", new int[0]);
        assertEquals("{\"values\":[]}", object.toString());
        assertEquals("{}", object.toString(JSONWriter.Feature.NotWriteEmptyArray));
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.values = new ArrayList<>();
        assertEquals("{\"values\":[]}", JSON.toJSONString(bean));
        assertEquals("{}", JSON.toJSONString(bean, JSONWriter.Feature.NotWriteEmptyArray));
    }

    private static class Bean2 {
        private List<String> values;

        public List<String> getValues() {
            return values;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.values = new ArrayList<>();
        assertEquals("{\"values\":[]}", JSON.toJSONString(bean));
        assertEquals("{}", JSON.toJSONString(bean, JSONWriter.Feature.NotWriteEmptyArray));
    }

    public static class Bean3 {
        private List<String> values;

        public List<String> getValues() {
            return values;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }
    }

    @Test
    public void test4() {
        Bean4 bean = new Bean4();
        bean.values = new ArrayList<>();
        assertEquals("{\"values\":[]}", JSON.toJSONString(bean));
        assertEquals("{}", JSON.toJSONString(bean, JSONWriter.Feature.NotWriteEmptyArray));
    }

    public static class Bean4 {
        public List<Integer> values;
    }

    @Test
    public void test5() {
        Bean5 bean = new Bean5();
        bean.values = new Integer[0];
        assertEquals("{\"values\":[]}", JSON.toJSONString(bean));
        assertEquals("{}", JSON.toJSONString(bean, JSONWriter.Feature.NotWriteEmptyArray));
        assertEquals("{}", JSON.toJSONString(new Bean5()));
    }

    public static class Bean5 {
        public Integer[] values;
    }
}
