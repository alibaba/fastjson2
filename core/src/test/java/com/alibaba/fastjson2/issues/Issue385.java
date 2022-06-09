package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue385 {
    @Test
    public void test() {
        List<Enum> list = Arrays.asList(PropertyNamingStrategy.CamelCase, PropertyNamingStrategy.PascalCase);
        byte[] jsonbBytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName);

        List<Enum> list2 = (List<Enum>) JSONB.parse(jsonbBytes, JSONReader.Feature.SupportClassForName, JSONReader.Feature.SupportAutoType);
        assertEquals(list.size(), list2.size());
        assertEquals(list.get(0), list2.get(0));
        assertEquals(list.get(1), list2.get(1));
    }

    @Test
    public void test1() {
        List<PropertyNamingStrategy> list = Arrays.asList(PropertyNamingStrategy.CamelCase, PropertyNamingStrategy.PascalCase);
        Bean bean = new Bean();
        bean.setItems(list);
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);

        JSONBDump.dump(jsonbBytes);

        Bean bean1 = JSONB.parseObject(jsonbBytes, Bean.class, JSONReader.Feature.SupportClassForName, JSONReader.Feature.SupportAutoType);
        List<PropertyNamingStrategy> list2 = bean1.items;
        assertEquals(list.size(), list2.size());
        assertEquals(list.get(0), list2.get(0));
        assertEquals(list.get(1), list2.get(1));
    }

    public static class Bean {
        private List<PropertyNamingStrategy> items;

        public List<PropertyNamingStrategy> getItems() {
            return items;
        }

        public void setItems(List<PropertyNamingStrategy> items) {
            this.items = items;
        }
    }

    @Test
    public void test2() {
        List<Enum> list = Arrays.asList(PropertyNamingStrategy.CamelCase, PropertyNamingStrategy.PascalCase);
        Bean2 bean = new Bean2();
        bean.setItems(list);
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);

//        assertEquals("{\n" +
//                "\t\"@type\":\"com.alibaba.fastjson2.issues.Issue385$Bean2#0\",\n" +
//                "\t\"@value\":{\n" +
//                "\t\t\"items\":[\n" +
//                "\t\t\t{\n" +
//                "\t\t\t\t\"@type\":\"com.alibaba.fastjson2.annotation.NamingStrategy#1\",\n" +
//                "\t\t\t\t\"@value\":\"CamelCase\"\n" +
//                "\t\t\t},\n" +
//                "\t\t\t{\n" +
//                "\t\t\t\t\"@type\":\"#1\",\n" +
//                "\t\t\t\t\"@value\":\"PascalCase\"\n" +
//                "\t\t\t}\n" +
//                "\t\t]\n" +
//                "\t}\n" +
//                "}", new JSONBDump(jsonbBytes, true).toString());

        Bean2 bean1 = JSONB.parseObject(jsonbBytes, Bean2.class, JSONReader.Feature.SupportClassForName, JSONReader.Feature.SupportAutoType);
        List<Enum> list2 = bean1.items;
        assertEquals(list.size(), list2.size());
        assertEquals(list.get(0), list2.get(0));
        assertEquals(list.get(1), list2.get(1));
    }

    public static class Bean2 {
        private List<Enum> items;

        public List<Enum> getItems() {
            return items;
        }

        public void setItems(List<Enum> items) {
            this.items = items;
        }
    }
}
