package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MapTest {
    @Test
    public void test_read() throws Exception {
        String str = "{\"properties\":{\"prop1\":0.0}}";
        VO vo = JSON.parseObject(str, VO.class);
        assertEquals("0.0", vo.getProperties().get("prop1"));
    }

    @Test
    public void test_write() {
        VO vo = new VO();
        Map map = Collections.singletonMap("prop1", new BigDecimal("0.0"));
        vo.setProperties(map);

        String str = "{\"properties\":{\"prop1\":0.0}}";
        assertEquals(str, JSON.toJSONString(vo));
    }

    @Test
    public void test_write_jsonb_array_mapping() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("id", 104), JSONWriter.Feature.BeanToArray);
        JSONObject object = JSONB.parseObject(jsonbBytes);
        assertEquals(104, object.get("id"));

        assertNull(JSONB.parseObject(JSONB.toBytes(null)));
    }

    @Test
    public void test_write_jsonb_array_mapping_1() {
        assertEquals(
                0,
                JSONB.parseObject(
                        JSONB.toBytes(
                                new JSONObject().fluentPut("id", null)
                        )
                ).size()
        );

        assertEquals(
                0,
                JSONB.parseObject(
                        JSONB.toBytes(
                                new JSONObject().fluentPut("id", null),
                                JSONWriter.Feature.BeanToArray
                        )
                ).size()
        );

        assertEquals(
                1,
                JSONB.parseObject(
                        JSONB.toBytes(
                                new JSONObject().fluentPut("id", null),
                                JSONWriter.Feature.WriteNulls
                        )
                ).size()
        );

        assertEquals(
                1,
                JSONB.parseObject(
                        JSONB.toBytes(
                                new JSONObject().fluentPut("id", null),
                                JSONWriter.Feature.WriteNulls, JSONWriter.Feature.BeanToArray
                        )
                ).size()
        );

        JSONObject object = new JSONObject();
        object
                .fluentPut("id_0", 100)
                .fluentPut("id_1", 101)
                .fluentPut("id_2", 102)
                .fluentPut("id_3", 103)
                .fluentPut("id_4", 104)
                .fluentPut("id_5", 105)
                .fluentPut("id_6", 106)
                .fluentPut("id_7", 107)
                .fluentPut("id_8", 108)
                .fluentPut("id_9", 109)
                .fluentPut("id_10", 110)
                .fluentPut("id_11", 111)
                .fluentPut("id_12", 112)
                .fluentPut("id_13", 113)
                .fluentPut("id_14", 114)
                .fluentPut("id_15", 115)
                .fluentPut("id_16", 116)
                .fluentPut("id_17", 117)
                .fluentPut("id_18", 118)
                .fluentPut("id_19", 119);

        assertEquals(
                20,
                JSONB.parseObject(
                        JSONB.toBytes(
                                object,
                                JSONWriter.Feature.WriteNulls, JSONWriter.Feature.BeanToArray
                        )
                ).size()
        );
    }

    @Test
    public void test_reference_jsonb() {
        JSONObject object = new JSONObject();
        object.put("root", object);

        byte[] jsonBytes = JSONB.toBytes(object, JSONWriter.Feature.ReferenceDetection);
        JSONObject parsed = JSONB.parseObject(jsonBytes);
        assertSame(parsed, parsed.get("root"));
    }

    @Test
    public void test_reference_jsonb_2() {
        JSONObject object = new JSONObject();
        object.put("root", new JSONArray().fluentAdd(object));

        byte[] jsonBytes = JSONB.toBytes(object, JSONWriter.Feature.ReferenceDetection);
        JSONObject parsed = JSONB.parseObject(jsonBytes);
        assertSame(parsed, parsed.getJSONArray("root").get(0));
    }

    @Test
    public void test_reference_jsonb_3() {
        JSONObject object = new JSONObject();
        object.put("child", new JSONObject().fluentPut("root", object));

        byte[] jsonBytes = JSONB.toBytes(object, JSONWriter.Feature.ReferenceDetection);
        JSONObject parsed = JSONB.parseObject(jsonBytes);
        assertSame(parsed, parsed.getJSONObject("child").get("root"));
    }

    @Test
    public void test_reference_jsonb_4() {
        JSONObject object = new JSONObject();
        JSONObject child1 = new JSONObject();
        object.put("child1", child1);
        object.put("child2", child1);

        byte[] jsonBytes = JSONB.toBytes(object, JSONWriter.Feature.ReferenceDetection);
        JSONObject parsed = JSONB.parseObject(jsonBytes);
        assertSame(parsed.getJSONObject("child1"), parsed.getJSONObject("child2"));
    }

    public static class VO {
        private Map<String, String> properties = new LinkedHashMap<String, String>();

        public Map<String, String> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }
    }

    @Test
    public void test_specialKey() {
        String key = "中国®";
        Map map = Collections.singletonMap(key, 1);

        String str = JSON.toJSONString(map);
        assertEquals(1, JSON.parseObject(str).get(key));

        byte[] utf8Bytes = JSON.toJSONBytes(map);
        assertEquals(1, JSON.parseObject(utf8Bytes).get(key));
    }

    @Test
    public void test_specialKey_2() {
        String key = "\\\r\n中国®";
        Map map = Collections.singletonMap(key, 1);

        String str = JSON.toJSONString(map);
        assertEquals(1, JSON.parseObject(str).get(key));

        byte[] utf8Bytes = JSON.toJSONBytes(map);
        assertEquals(1,
                JSON.parseObject(utf8Bytes)
                        .get(key));
    }

    @Test
    public void test_writeNulls_jsonb() {
        com.alibaba.fastjson.JSONObject object = new com.alibaba.fastjson.JSONObject();
        object.put("id", null);
        byte[] bytes = JSONB.toBytes(object, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.WriteClassName);

        com.alibaba.fastjson.JSONObject object2 = (com.alibaba.fastjson.JSONObject) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(object.size(), object2.size());
        assertTrue(object2.containsKey("id"));
    }

    @Test
    public void test_writeNulls_jsonb2() {
        List list = new ArrayList();
        list.add(new com.alibaba.fastjson.JSONObject().fluentPut("id", 101).fluentPut("name", "DataWorks"));
        list.add(new com.alibaba.fastjson.JSONObject().fluentPut("id", null).fluentPut("name", null));

        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.WriteClassName);

        ArrayList list2 = (ArrayList) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(list.size(), list2.size());
        assertEquals(list.get(0).getClass(), list2.get(0).getClass());
    }

    @Test
    public void getObjectClass() {
        assertEquals(
                Map.class,
                JSONFactory
                        .getDefaultObjectReaderProvider()
                        .getObjectReader(Map.class)
                        .getObjectClass()
        );
        assertEquals(
                Map.class,
                JSONFactory
                        .getDefaultObjectReaderProvider()
                        .getObjectReader(
                                new TypeReference<Map<String, String>>(){}.getType()
                        )
                        .getObjectClass()
        );
        assertNull(
                JSONFactory
                        .getDefaultObjectReaderProvider()
                        .getObjectReader(Map.class)
                        .getBuildFunction()
        );
    }
}
