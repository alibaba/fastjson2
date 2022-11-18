package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnwrappedTest {
    @Test
    public void test() {
        ExtendableBean bean = new ExtendableBean();
        bean.name = "My bean";
        bean.properties.put("attr1", "val1");
        String str = JSON.toJSONString(bean);
        assertEquals("{\"name\":\"My bean\",\"attr1\":\"val1\"}", str);
    }

    public static class ExtendableBean {
        public String name;
        private Map<String, String> properties = new HashMap<>();

        public ExtendableBean() {
        }

        @JSONField(unwrapped = true)
        public Map<String, String> getProperties() {
            return properties;
        }
    }

    @Test
    public void test1() {
        ExtendableBean1 bean = new ExtendableBean1();
        bean.name = "My bean";
        bean.properties.put("attr1", "val1");
        String str = JSON.toJSONString(bean);
        assertEquals("{\"name\":\"My bean\",\"attr1\":\"val1\"}", str);

        ExtendableBean1 bean2 = JSON.parseObject(str, ExtendableBean1.class);
        assertEquals(bean.name, bean2.name);
        assertEquals(bean.properties, bean2.properties);

        assertEquals(bean.properties, JSON.parseObject(str, ExtendableBean1.class, JSONReader.Feature.SupportSmartMatch).properties);
    }

    private static class ExtendableBean1 {
        public String name;

        @JSONField(unwrapped = true)
        public final Map<String, String> properties = new HashMap<>();
    }

    @Test
    public void test1Public() {
        ExtendableBean1Public bean = new ExtendableBean1Public();
        bean.name = "My bean";
        bean.properties.put("attr1", "val1");
        String str = JSON.toJSONString(bean);
        assertEquals("{\"name\":\"My bean\",\"attr1\":\"val1\"}", str);

        ExtendableBean1Public bean2 = JSON.parseObject(str, ExtendableBean1Public.class);
        assertEquals(bean.name, bean2.name);
        assertEquals(bean.properties, bean2.properties);
    }

    public static class ExtendableBean1Public {
        public String name;

        @JSONField(unwrapped = true)
        public final Map<String, String> properties = new HashMap<>();
    }

    @Test
    public void test2() {
        ExtendableBean2 bean = new ExtendableBean2();
        bean.name = "My bean";
        bean.properties.put("attr1", "val1");
        String str = JSON.toJSONString(bean);
        assertEquals("{\"name\":\"My bean\",\"attr1\":\"val1\"}", str);

        ExtendableBean2 bean2 = JSON.parseObject(str, ExtendableBean2.class);
        assertEquals(bean.name, bean2.name);
        assertEquals(bean.properties, bean2.properties);

        assertEquals(bean.properties, JSON.parseObject(str, ExtendableBean2.class, JSONReader.Feature.SupportSmartMatch).properties);
    }

    private static class ExtendableBean2 {
        public String name;

        private final Map<String, String> properties = new HashMap<>();

        @JSONField(unwrapped = true)
        public Map<String, String> getProperties() {
            return properties;
        }
    }

    @Test
    public void test3() {
        String str = "{\"name\":\"My bean\",\"attr1\":\"val1\"}";

        ExtendableBean3 bean2 = JSON.parseObject(str, ExtendableBean3.class);
        assertEquals("My bean", bean2.name);
        assertEquals("val1", bean2.properties.get("attr1"));

        assertEquals(bean2.properties, JSON.parseObject(str, ExtendableBean3.class, JSONReader.Feature.SupportSmartMatch).properties);
    }

    @Test
    public void test4() {
        String str = "{\"name\":\"My bean\",\"attr1\":\"val1\"}";

        ExtendableBean3 bean2 = JSON.parseObject(str)
                .toJavaObject(ExtendableBean3.class);
        assertEquals("My bean", bean2.name);
        assertEquals("val1", bean2.properties.get("attr1"));

        assertEquals(bean2.properties, JSON.parseObject(str, ExtendableBean3.class, JSONReader.Feature.SupportSmartMatch).properties);
    }

    private static class ExtendableBean3 {
        private String name;

        private final Map<String, String> properties = new HashMap<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @JSONField(unwrapped = true)
        public void setProperty(String name, String value) {
            properties.put(name, value);
        }
    }

    @Test
    public void testF1() {
        String str = "{\"attr1\":\"val1\"}";

        ExtendableBeanF1 bean = JSON.parseObject(str, ExtendableBeanF1.class);
        assertEquals("val1", bean.properties.get("attr1"));

        assertEquals("val1", JSON.parseObject(str, ExtendableBeanF1.class, JSONReader.Feature.SupportSmartMatch).properties.get("attr1"));
    }

    private static class ExtendableBeanF1 {
        private final Map<String, String> properties = new HashMap<>();

        @JSONField(unwrapped = true)
        public void setProperty(String name, String value) {
            properties.put(name, value);
        }
    }

    @Test
    public void testF2() {
        String str = "{\"attr1\":\"val1\"}";

        ExtendableBeanF2 bean = JSON.parseObject(str, ExtendableBeanF2.class);
        assertEquals("val1", bean.properties.get("attr1"));

        assertEquals("val1", JSON.parseObject(str, ExtendableBeanF2.class, JSONReader.Feature.SupportSmartMatch).properties.get("attr1"));
    }

    private static class ExtendableBeanF2 {
        public int f1;
        private final Map<String, String> properties = new HashMap<>();

        @JSONField(unwrapped = true)
        public void setProperty(String name, String value) {
            properties.put(name, value);
        }
    }

    @Test
    public void testF3() {
        String str = "{\"attr1\":\"val1\"}";

        ExtendableBeanF3 bean = JSON.parseObject(str, ExtendableBeanF3.class);
        assertEquals("val1", bean.properties.get("attr1"));

        assertEquals("val1", JSON.parseObject(str, ExtendableBeanF3.class, JSONReader.Feature.SupportSmartMatch).properties.get("attr1"));
    }

    private static class ExtendableBeanF3 {
        public int f1;
        public int f2;
        private final Map<String, String> properties = new HashMap<>();

        @JSONField(unwrapped = true)
        public void setProperty(String name, String value) {
            properties.put(name, value);
        }
    }

    @Test
    public void testF4() {
        String str = "{\"attr1\":\"val1\"}";

        ExtendableBeanF4 bean = JSON.parseObject(str, ExtendableBeanF4.class);
        assertEquals("val1", bean.properties.get("attr1"));

        assertEquals("val1", JSON.parseObject(str, ExtendableBeanF4.class, JSONReader.Feature.SupportSmartMatch).properties.get("attr1"));
    }

    private static class ExtendableBeanF4 {
        public int f1;
        public int f2;
        public int f3;
        private final Map<String, String> properties = new HashMap<>();

        @JSONField(unwrapped = true)
        public void setProperty(String name, String value) {
            properties.put(name, value);
        }
    }

    @Test
    public void testF5() {
        String str = "{\"attr1\":\"val1\"}";

        ExtendableBeanF5 bean = JSON.parseObject(str, ExtendableBeanF5.class);
        assertEquals("val1", bean.properties.get("attr1"));

        assertEquals("val1", JSON.parseObject(str, ExtendableBeanF5.class, JSONReader.Feature.SupportSmartMatch).properties.get("attr1"));
    }

    private static class ExtendableBeanF5 {
        public int f1;
        public int f2;
        public int f3;
        public int f4;
        private final Map<String, String> properties = new HashMap<>();

        @JSONField(unwrapped = true)
        public void setProperty(String name, String value) {
            properties.put(name, value);
        }
    }

    @Test
    public void testF6() {
        String str = "{\"attr1\":\"val1\"}";

        ExtendableBeanF6 bean = JSON.parseObject(str, ExtendableBeanF6.class);
        assertEquals("val1", bean.properties.get("attr1"));

        assertEquals("val1", JSON.parseObject(str, ExtendableBeanF6.class, JSONReader.Feature.SupportSmartMatch).properties.get("attr1"));
    }

    private static class ExtendableBeanF6 {
        public int f1;
        public int f2;
        public int f3;
        public int f4;
        public int f5;
        private final Map<String, String> properties = new HashMap<>();

        @JSONField(unwrapped = true)
        public void setProperty(String name, String value) {
            properties.put(name, value);
        }
    }

    @Test
    public void testF7() {
        String str = "{\"attr1\":\"val1\"}";

        ExtendableBeanF7 bean = JSON.parseObject(str, ExtendableBeanF7.class);
        assertEquals("val1", bean.properties.get("attr1"));

        assertEquals("val1", JSON.parseObject(str, ExtendableBeanF7.class, JSONReader.Feature.SupportSmartMatch).properties.get("attr1"));
    }

    private static class ExtendableBeanF7 {
        public int f1;
        public int f2;
        public int f3;
        public int f4;
        public int f5;
        public int f6;
        private final Map<String, String> properties = new HashMap<>();

        @JSONField(unwrapped = true)
        public void setProperty(String name, String value) {
            properties.put(name, value);
        }
    }

    @Test
    public void testF7Public() {
        String str = "{\"attr1\":\"val1\"}";

        ExtendableBeanF7P bean = JSON.parseObject(str, ExtendableBeanF7P.class);
        assertEquals("val1", bean.properties.get("attr1"));

        assertEquals("val1", JSON.parseObject(str, ExtendableBeanF7P.class, JSONReader.Feature.SupportSmartMatch).properties.get("attr1"));
    }

    public static class ExtendableBeanF7P {
        public int f1;
        public int f2;
        public int f3;
        public int f4;
        public int f5;
        public int f6;
        private final Map<String, String> properties = new HashMap<>();

        @JSONField(unwrapped = true)
        public void setProperty(String name, String value) {
            properties.put(name, value);
        }
    }
}
