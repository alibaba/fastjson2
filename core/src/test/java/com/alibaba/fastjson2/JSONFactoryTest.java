package com.alibaba.fastjson2;

import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JSONFactoryTest {
    @Test
    public void contextReaderCreator() {
        JSONFactory.setContextObjectReaderProvider(null);
        assertSame(JSONFactory.defaultObjectReaderProvider, JSONFactory.getDefaultObjectReaderProvider());
    }

    @Test
    public void contextJSONPathCompiler() {
        JSONFactory.setContextJSONPathCompiler(null);
        assertSame(JSONFactory.defaultJSONPathCompiler, JSONFactory.getDefaultJSONPathCompiler());
    }

    @Test
    public void test1() {
        JSONFactory.setUseJacksonAnnotation(false);
        JSONFactory.setUseGsonAnnotation(false);
        assertFalse(JSONFactory.isUseGsonAnnotation());
        assertFalse(JSONFactory.isUseJacksonAnnotation());
        JSONFactory.setUseJacksonAnnotation(true);
        JSONFactory.setUseGsonAnnotation(true);
        assertTrue(JSONFactory.isUseJacksonAnnotation());

        ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
        JSONWriter.Context context = JSONFactory.createWriteContext(provider);
        assertSame(provider, context.getProvider());
    }

    @Test
    public void test2() {
        disableRun(() -> {
            Bean bean = new Bean();
            bean.item = new Item();
            bean.item.id = 123;
            String str = JSON.toJSONString(bean);
            Bean bean1 = JSON.parseObject(str, Bean.class);
            assertEquals(bean.item.id, bean1.item.id);
        });
    }

    public static class Bean {
        public Item item;
    }

    public static class Item {
        public int id;
    }

    @Test
    public void test3() {
        disableRun(() -> {
            Bean3 bean = new Bean3();
            bean.items = new ArrayList<>();
            bean.items.add(new Item());
            bean.items.get(0).id = 123;
            String str = JSON.toJSONString(bean);
            Bean3 bean1 = JSON.parseObject(str, Bean3.class);
            assertEquals(bean.items.size(), bean1.items.size());
            assertEquals(bean.items.get(0).id, bean1.items.get(0).id);
        });
    }

    public static class Bean3 {
        public List<Item> items;
    }

    @Test
    public void test4() {
        disableRun(() -> {
            Bean4 bean = new Bean4();
            bean.items = new HashMap<>();
            bean.items.put("first", new Item());
            bean.items.get("first").id = 123;
            String str = JSON.toJSONString(bean);
            Bean4 bean1 = JSON.parseObject(str, Bean4.class);
            assertEquals(bean.items.size(), bean1.items.size());
            assertEquals(bean.items.get("first").id, bean1.items.get("first").id);
        });
    }

    public static class Bean4 {
        public Map<String, Item> items;
    }

    @Test
    public void test5() {
        disableRun(() -> {
            Bean5 bean = new Bean5();
            bean.i = 123;
            String str = JSON.toJSONString(bean);
            Bean5 bean1 = JSON.parseObject(str, Bean5.class);
            assertEquals(bean.i, bean1.i);
        });
    }

    public static class Bean5 {
        public int i;
    }

    @Test
    public void test6() {
        disableRun(() -> {
            Bean6 bean = new Bean6();
            bean.i = 123;
            String str = JSON.toJSONString(bean);
            Bean6 bean1 = JSON.parseObject(str, Bean6.class);
            assertEquals(bean.i, bean1.i);
        });
    }

    public static class Bean6 {
        public int a;
        public int b;
        public int c;
        public int d;
        public int e;
        public int f;
        public int g;
        public int h;
        public int i;
        public int j;
        public int k;
    }

    static void disableRun(Runnable r) {
        boolean disableArrayMapping = JSONFactory.isDisableArrayMapping();
        boolean disableJSONB = JSONFactory.isDisableJSONB();
        boolean disableAutoType = JSONFactory.isDisableAutoType();
        boolean disableReferenceDetect = JSONFactory.isDisableReferenceDetect();
        boolean disableSmartMatch = JSONFactory.isDisableSmartMatch();
        try {
            JSONFactory.setDisableArrayMapping(true);
            JSONFactory.setDisableJSONB(true);
            JSONFactory.setDisableAutoType(true);
            JSONFactory.setDisableReferenceDetect(true);
            JSONFactory.setDisableSmartMatch(true);

            r.run();
        } finally {
            JSONFactory.setDisableArrayMapping(disableArrayMapping);
            JSONFactory.setDisableJSONB(disableJSONB);
            JSONFactory.setDisableAutoType(disableAutoType);
            JSONFactory.setDisableReferenceDetect(disableReferenceDetect);
            JSONFactory.setDisableSmartMatch(disableSmartMatch);
        }
    }
}
