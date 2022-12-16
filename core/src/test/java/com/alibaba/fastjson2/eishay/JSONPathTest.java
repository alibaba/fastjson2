package com.alibaba.fastjson2.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JSONPathTest {
    JSONPath path = JSONPath.of(
            new String[]{
                    "$.media.bitrate",
                    "$.media.duration"
            },
            new Type[]{
                    Integer.class,
                    Long.class
            }
    );

    @Test
    public void test() {
        Object[] result = (Object[]) path.extract(ParserTest.str);
        assertEquals("[262144,18000000]", JSON.toJSONString(result));
    }

    @Test
    public void testMiss() {
        Object[] result = (Object[]) path.extract("{}");
        assertEquals("[null,null]", JSON.toJSONString((Object[]) path.extract("{}")));
    }

    @Test
    public void testMiss1() {
        Object[] result = (Object[]) path.extract("{\"media\":null}");
        assertEquals("[null,null]", JSON.toJSONString(result));
    }

    @Test
    public void testMiss2() {
        Object[] result = (Object[]) path.extract("null");
        assertEquals("[null,null]", JSON.toJSONString(result));
    }

    @Test
    public void testError() {
        assertThrows(JSONException.class, () -> path.extract("{"));
    }

    @Test
    public void testError1() {
        assertThrows(JSONException.class, () -> path.extract("{\"media\":{"));
    }
}
