package com.alibaba.fastjson3;

import com.alibaba.fastjson3.writer.ObjectWriterCreatorASM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectWriterASMTest {
    public static class SimpleBean {
        public int id = 123;
        public String name = "test";
        public long version = 99L;
        public double percent = 3.14;
        public boolean active = true;
        public float score = 1.5f;
    }

    @Test
    public void testSimpleBean() {
        ObjectWriter<SimpleBean> writer = ObjectWriterCreatorASM.createObjectWriter(SimpleBean.class);
        assertNotNull(writer);

        SimpleBean bean = new SimpleBean();
        try (JSONGenerator generator = JSONGenerator.of()) {
            writer.write(generator, bean, null, null, 0);
            String json = generator.toString();

            assertNotNull(json);
            assertTrue(json.contains("\"id\":123"), "json should contain id: " + json);
            assertTrue(json.contains("\"name\":\"test\""), "json should contain name: " + json);
            assertTrue(json.contains("\"version\":99"), "json should contain version: " + json);
            assertTrue(json.contains("\"active\":true"), "json should contain active: " + json);
        }
    }

    @Test
    public void testNullStringField() {
        ObjectWriter<SimpleBean> writer = ObjectWriterCreatorASM.createObjectWriter(SimpleBean.class);

        SimpleBean bean = new SimpleBean();
        bean.name = null;

        try (JSONGenerator generator = JSONGenerator.of()) {
            writer.write(generator, bean, null, null, 0);
            String json = generator.toString();

            assertNotNull(json);
            // null strings should be omitted by default
            assertFalse(json.contains("\"name\""), "null name should be omitted: " + json);
            assertTrue(json.contains("\"id\":123"), "json should still contain id: " + json);
        }
    }

    @Test
    public void testMatchesReflectionOutput() {
        SimpleBean bean = new SimpleBean();

        // Reflection writer
        String reflectionJson = JSON.toJSONString(bean);

        // ASM writer
        ObjectWriter<SimpleBean> asmWriter = ObjectWriterCreatorASM.createObjectWriter(SimpleBean.class);
        try (JSONGenerator generator = JSONGenerator.of()) {
            asmWriter.write(generator, bean, null, null, 0);
            String asmJson = generator.toString();

            // Both should produce valid JSON with the same fields
            assertNotNull(reflectionJson);
            assertNotNull(asmJson);

            // Parse both and compare values
            JSONObject refObj = JSON.parseObject(reflectionJson);
            JSONObject asmObj = JSON.parseObject(asmJson);

            assertEquals(refObj.getIntValue("id"), asmObj.getIntValue("id"));
            assertEquals(refObj.getString("name"), asmObj.getString("name"));
            assertEquals(refObj.getLongValue("version"), asmObj.getLongValue("version"));
            assertEquals(refObj.getBooleanValue("active"), asmObj.getBooleanValue("active"));
        }
    }

    public static class GetterBean {
        private int age = 25;
        private String city = "Shanghai";

        public int getAge() {
            return age;
        }

        public String getCity() {
            return city;
        }
    }

    @Test
    public void testGetterBean() {
        ObjectWriter<GetterBean> writer = ObjectWriterCreatorASM.createObjectWriter(GetterBean.class);
        assertNotNull(writer);

        GetterBean bean = new GetterBean();
        try (JSONGenerator generator = JSONGenerator.of()) {
            writer.write(generator, bean, null, null, 0);
            String json = generator.toString();

            assertTrue(json.contains("\"age\":25"), "json should contain age: " + json);
            assertTrue(json.contains("\"city\":\"Shanghai\""), "json should contain city: " + json);
        }
    }
}
