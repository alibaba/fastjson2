package com.alibaba.fastjson2.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JSONPathTest1 {
    JSONPath path = JSONPath.of(
            new String[]{
                    "$.root.media.bitrate",
                    "$.root.media.duration"
            },
            new Type[]{
                    Integer.class,
                    Long.class
            }
    );

    @Test
    public void test() {
        String str = JSONObject.of(
                "root", JSON.parseObject(ParserTest.str)
        ).toString();
        Object[] result = (Object[]) path.extract(str);
        assertEquals("[262144,18000000]", JSON.toJSONString(result));
    }

    @Test
    public void testMiss() {
        Object[] result = (Object[]) path.extract("{}");
        assertEquals("[null,null]", JSON.toJSONString(result));
    }

    @Test
    public void testMiss1() {
        Object[] result = (Object[]) path.extract("null");
        assertEquals("[null,null]", JSON.toJSONString(result));
    }

    @Test
    public void testMiss2() {
        Object[] result = (Object[]) path.extract("{\"id\":123}");
        assertEquals("[null,null]", JSON.toJSONString(result));
    }

    @Test
    public void testMiss3() {
        Object[] result = (Object[]) path.extract("{\"root\":{}}");
        assertEquals("[null,null]", JSON.toJSONString(result));
    }

    @Test
    public void testMiss4() {
        Object[] result = (Object[]) path.extract("{\"root\":{\"media\":null}}");
        assertEquals("[null,null]", JSON.toJSONString(result));
    }

    @Test
    public void testMiss5() {
        Object[] result = (Object[]) path.extract("{\"root\":{\"media\":{}}}");
        assertEquals("[null,null]", JSON.toJSONString(result));
    }

    @Test
    public void testError() {
        assertThrows(JSONException.class, () -> path.extract("{"));
        assertThrows(JSONException.class, () -> path.extract("{\"root\":"));
        assertThrows(JSONException.class, () -> path.extract("{\"root\":{"));
        assertThrows(JSONException.class, () -> path.extract("{\"root\":{\"media\":"));
        assertThrows(JSONException.class, () -> path.extract("{\"root\":{\"media\":{"));
        assertThrows(JSONException.class, () -> path.extract("{\"root\":{\"media\":123"));
    }
}
