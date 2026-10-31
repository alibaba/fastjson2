package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONScanner;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.support.hsf.HSFJSONUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue7765 {
    @Test
    public void testGetJSONArrayFromString() {
        String json = "{\"items\":\"[{\\\"name\\\":\\\"alpha\\\"},[1,2]]\"}";
        JSONArray items = JSON.parseObject(json).getJSONArray("items");

        Object[] elements = items.toArray();
        assertEquals(JSONObject.class, elements[0].getClass());
        assertEquals(JSONArray.class, elements[1].getClass());
        assertEquals(JSONObject.class, items.iterator().next().getClass());
    }

    @Test
    public void testGetJSONObjectFromString() {
        String json = "{\"item\":\"{\\\"inner\\\":{\\\"name\\\":\\\"alpha\\\"},\\\"list\\\":[1]}\"}";
        JSONObject item = JSON.parseObject(json).getJSONObject("item");

        for (Map.Entry<String, Object> entry : item.entrySet()) {
            Class<?> expected = "inner".equals(entry.getKey()) ? JSONObject.class : JSONArray.class;
            assertEquals(expected, entry.getValue().getClass());
        }
    }

    @Test
    public void testArrayGetJSONArrayFromString() {
        JSONArray array = JSON.parseArray("[\"[{\\\"name\\\":\\\"alpha\\\"}]\"]");

        assertEquals(JSONObject.class, array.getJSONArray(0).toArray()[0].getClass());
    }

    @Test
    public void testArrayGetJSONObjectFromString() {
        JSONArray array = JSON.parseArray("[\"{\\\"inner\\\":{\\\"name\\\":\\\"alpha\\\"}}\"]");

        assertEquals(JSONObject.class, array.getJSONObject(0).values().iterator().next().getClass());
    }

    @Test
    public void testGetJSONArrayFromEncodedStringCast() {
        // reproduces issue #7765: elements from string reparse must be castable to fastjson1 JSONObject
        String json = "{\"items\":\"[{\\\"name\\\":\\\"alpha\\\"},{\\\"name\\\":\\\"beta\\\"}]\"}";
        Object[] elements = JSON.parseObject(json).getJSONArray("items").toArray();

        JSONObject first = (JSONObject) elements[0];
        assertEquals("alpha", first.getString("name"));
        assertEquals(JSONObject.class, elements[1].getClass());
    }

    @Test
    public void testGetObjectByTypeReference() {
        JSONObject root = JSON.parseObject("{\"v\":{\"inner\":{\"name\":\"alpha\"},\"list\":[1]}}");

        Map<String, Object> value = root.getObject("v", new TypeReference<Map<String, Object>>() {});
        assertEquals(JSONObject.class, value.get("inner").getClass());
        assertEquals(JSONArray.class, value.get("list").getClass());
    }

    @Test
    public void testArrayGetObjectByBeanClass() {
        JSONArray array = JSON.parseArray("[{\"inner\":{\"name\":\"alpha\"},\"list\":[{\"name\":\"beta\"}]}]");

        Bean bean = array.getObject(0, Bean.class);
        assertEquals(JSONObject.class, bean.inner.getClass());
        assertEquals(JSONObject.class, bean.list.get(0).getClass());
    }

    @Test
    public void testJSONPathReadNestedContainerType() {
        Object data = JSONPath.read("{\"data\":{\"inner\":{\"name\":\"alpha\"}}}", "$.data", Object.class);
        assertEquals(JSONObject.class, data.getClass());
        assertEquals(JSONObject.class, ((JSONObject) data).get("inner").getClass());
    }

    @Test
    public void testDefaultJSONParserNestedContainerType() {
        Object parsed = new DefaultJSONParser("{\"inner\":{\"name\":\"alpha\"}}").parse();
        assertEquals(JSONObject.class, parsed.getClass());
        assertEquals(JSONObject.class, ((JSONObject) parsed).get("inner").getClass());
    }

    @Test
    public void testDefaultJSONParserWithConfigNestedContainerType() {
        Object parsed = new DefaultJSONParser("{\"inner\":{\"name\":\"alpha\"}}", ParserConfig.global).parse();
        assertEquals(JSONObject.class, parsed.getClass());
        assertEquals(JSONObject.class, ((JSONObject) parsed).get("inner").getClass());
    }

    @Test
    public void testJSONScannerNestedContainerType() {
        Object parsed = new JSONScanner("{\"inner\":{\"name\":\"alpha\"}}").getReader().read(Object.class);
        assertEquals(JSONObject.class, parsed.getClass());
        assertEquals(JSONObject.class, ((JSONObject) parsed).get("inner").getClass());
    }

    @Test
    public void testHSFJSONUtilsNestedContainerType() throws Exception {
        String json = "{"
                + "\"argsTypes\":[\"java.util.Map\"],"
                + "\"argsObjs\":[{\"inner\":{\"name\":\"alpha\"}}]"
                + "}";
        Object[] args = HSFJSONUtils.parseInvocationArguments(
                json,
                types -> {
                    try {
                        return Issue7765.class.getMethod("acceptMap", Map.class);
                    } catch (NoSuchMethodException e) {
                        throw new IllegalStateException(e);
                    }
                }
        );
        assertEquals(JSONObject.class, ((Map<?, ?>) args[0]).get("inner").getClass());
    }

    public static void acceptMap(Map map) {
    }

    public static class Bean {
        public Object inner;
        public List<Object> list;
    }
}
