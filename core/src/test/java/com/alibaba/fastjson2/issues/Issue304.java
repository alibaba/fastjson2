package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue304 {
    List<Bean> list;
    String result;

    @BeforeEach
    public void init() {
        Bean tt = new Bean();
        tt.setItem(null);
        tt.setName("testtt");

        List<Bean> list = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
            Bean t = new Bean();
            t.setItem(tt);
            t.setName("test");
            list.add(t);
        }

        JSONWriter.Feature[] features = {
                JSONWriter.Feature.ReferenceDetection,
                //JSONWriter.Feature.FieldBased
        };
        result = JSON.toJSONString(list, features);
    }

    @Test
    public void write() {
        assertEquals("[{\"item\":{\"name\":\"testtt\"},\"name\":\"test\"},{\"item\":{\"$ref\":\"$[0].item\"},\"name\":\"test\"},{\"item\":{\"$ref\":\"$[0].item\"},\"name\":\"test\"},{\"item\":{\"$ref\":\"$[0].item\"},\"name\":\"test\"}]", result);
    }

    @Test
    public void testRead() {
        List<Bean> list2 = JSON.parseArray(result, Bean.class);
        assertSame(list2.get(0).item, list2.get(1).item);
    }

    @Test
    public void readPrivate() {
        List<Bean1> list3 = JSON.parseArray(result, (Type) Bean1.class);
        assertSame(list3.get(0).item, list3.get(1).item);
    }

    @Test
    public void readUTF8Type() {
        List<Bean> list3 = JSON.parseArray(result.getBytes(StandardCharsets.UTF_8), (Type) Bean.class);
        assertSame(list3.get(0).item, list3.get(1).item);
    }

    @Test
    public void readUTF8() {
        List<Bean> list3 = JSON.parseArray(result.getBytes(StandardCharsets.UTF_8), Bean.class);
        assertSame(list3.get(0).item, list3.get(1).item);
    }

    @Test
    public void readStr() {
        JSONReader jsonReaderStr = TestUtils.createJSONReaderStr(result);
        Type type = new TypeReference<List<Bean>>() {
        }.getType();
        List<Bean> list4 = jsonReaderStr.read(type);
        jsonReaderStr.handleResolveTasks(list4);
        assertSame(list4.get(0).item, list4.get(1).item);
    }

    @Test
    public void test_jsonb() {
        Bean tt = new Bean();
        tt.setItem(null);
        tt.setName("testtt");

        List<Bean> list = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
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

    public static class Bean1 {
        private String name;
        private Bean1 item;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Bean1 getItem() {
            return item;
        }

        public void setItem(Bean1 item) {
            this.item = item;
        }
    }
}
