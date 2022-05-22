package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue304 {
    @Test
    public void test() {
        Bean tt = new Bean();
        tt.setItem(null);
        tt.setName("testtt");

        List<Bean> list = new LinkedList<>();
        for(int i = 0; i < 4; i++) {
            Bean t = new Bean();
            t.setItem(tt);
            t.setName("test");
            list.add(t);
        }

        JSONWriter.Feature[] features = {
                JSONWriter.Feature.ReferenceDetection,
                //JSONWriter.Feature.FieldBased
        };
        String result = JSON.toJSONString(list,features );
        assertEquals("[{\"item\":{\"name\":\"testtt\"},\"name\":\"test\"},{\"item\":{\"$ref\":\"$[0].item\"},\"name\":\"test\"},{\"item\":{\"$ref\":\"$[0].item\"},\"name\":\"test\"},{\"item\":{\"$ref\":\"$[0].item\"},\"name\":\"test\"}]", result);

        List<Bean> list2 = JSON.parseArray(result, Bean.class);
        assertSame(list2.get(0).item, list2.get(1).item);
    }

    @Test
    public void test_jsonb() {
        Bean tt = new Bean();
        tt.setItem(null);
        tt.setName("testtt");

        List<Bean> list = new LinkedList<>();
        for(int i = 0; i < 4; i++) {
            Bean t = new Bean();
            t.setItem(tt);
            t.setName("test");
            list.add(t);
        }

        JSONWriter.Feature[] features = {
                JSONWriter.Feature.ReferenceDetection,
                //JSONWriter.Feature.FieldBased
        };

        byte[] jsonbBytes = JSONB.toBytes(list, features);
        String result = JSONB.toJSONString(jsonbBytes);
        assertEquals("[\n" +
                "\t{\n" +
                "\t\t\"item\":{\n" +
                "\t\t\t\"name\":\"testtt\"\n" +
                "\t\t},\n" +
                "\t\t\"name\":\"test\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"item\":{\"$ref\":\"$[0].item\"},\n" +
                "\t\t\"name\":\"test\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"item\":{\"$ref\":\"#-1\"},\n" +
                "\t\t\"name\":\"test\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"item\":{\"$ref\":\"#-1\"},\n" +
                "\t\t\"name\":\"test\"\n" +
                "\t}\n" +
                "]", result);

        List<Bean> list2 = JSONB.parseArray(jsonbBytes, Bean.class);
        assertSame(list2.get(0).item, list2.get(1).item);
    }

    public static class Bean {
        private String name;
        private Bean item;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Bean getItem() {
            return item;
        }

        public void setItem(Bean item) {
            this.item = item;
        }
    }
}
