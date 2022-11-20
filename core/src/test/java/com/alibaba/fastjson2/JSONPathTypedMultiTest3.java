package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static junit.framework.Assert.assertNull;
import static net.sf.json.test.JSONAssert.assertEquals;
import static net.sf.json.test.JSONAssert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JSONPathTypedMultiTest3 {
    @Test
    public void test() {
        String str = "[101,\"DataWorks\"]";
        JSONPath jsonPath = JSONPath.of(
                new String[]{"$[0]", "$[1]"},
                new Type[]{Long.class, String.class}
        );
        Object[] values = (Object[]) jsonPath.extract(str);
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals(101L, values[0]);
        assertEquals("DataWorks", values[1]);
    }

    @Test
    public void test1() {
        String str = "[101,\"DataWorks\"]";

        JSONPath jsonPath = JSONPath.of(
                new String[]{"$[1]", "$[0]"},
                new Type[]{String.class, Long.class}
        );
        Object[] values = (Object[]) jsonPath.extract(str);
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals(101L, values[1]);
        assertEquals("DataWorks", values[0]);

        assertNull(jsonPath.extract("null"));
        assertThrows(JSONException.class, () -> jsonPath.extract("12"));
    }

    @Test
    public void testEval1() {
        JSONArray array = JSONArray.of(101, "DataWorks");
        JSONPath jsonPath = JSONPath.of(
                new String[]{"$[1]", "$[0]"},
                new Type[]{String.class, Long.class}
        );
        Object[] values = (Object[]) jsonPath.eval(array);
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals(101L, values[1]);
        assertEquals("DataWorks", values[0]);
    }

    @Test
    public void testJSONB() {
        JSONArray array = JSONArray.of(101, "DataWorks");
        JSONPath jsonPath = JSONPath.of(
                new String[]{"$[1]", "$[0]"},
                new Type[]{String.class, Long.class}
        );

        byte[] jsonbBytes = array.toJSONBBytes();
        Object[] values = (Object[]) jsonPath.extract(JSONReader.ofJSONB(jsonbBytes));
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals(101L, values[1]);
        assertEquals("DataWorks", values[0]);
    }

    @Test
    public void test2() {
        JSONPath jsonPath = JSONPath.of(
                new String[]{"$.values[0]", "$.values[1]"},
                new Type[]{Long.class, String.class}
        );

        String str = "{\"values\":[101,\"DataWorks\"]}";
        Object[] values = (Object[]) jsonPath.extract(str);
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals(101L, values[0]);
        assertEquals("DataWorks", values[1]);

        assertNull(jsonPath.extract("null"));
        assertNull(jsonPath.extract("{\"values\":null}"));
        assertNull(jsonPath.extract("{\"id\":123,\"values\":null}"));
        assertNull(jsonPath.extract(JSONReader.ofJSONB(JSONB.toBytes(null))));
        assertNull(
                jsonPath.extract(
                        JSONReader.ofJSONB(
                                JSONObject.of("values", null).toJSONBBytes(JSONWriter.Feature.WriteNulls)
                        )
                )
        );
    }

    @Test
    public void test3() {
        JSONPath jsonPath = JSONPath.of(
                new String[]{"$[0][0]", "$[0][1]"},
                new Type[]{Long.class, String.class}
        );

        String str = "[[101,\"DataWorks\"]]";
        Object[] values = (Object[]) jsonPath.extract(str);
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals(101L, values[0]);
        assertEquals("DataWorks", values[1]);

        assertNull(jsonPath.extract("[null]"));
    }

    @Test
    public void test4() {
        JSONPath jsonPath = JSONPath.of(
                new String[]{"$[1][0]", "$[1][1]"},
                new Type[]{Long.class, String.class}
        );

        String str = "[null,[101,\"DataWorks\"]]";
        Object[] values = (Object[]) jsonPath.extract(str);
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals(101L, values[0]);
        assertEquals("DataWorks", values[1]);

        assertNull(jsonPath.extract("[1,null]"));
    }

    @Test
    public void test5() {
        JSONPath jsonPath = JSONPath.of(
                new String[]{"$[1][2][1]", "$[1][2][2]"},
                new Type[]{Long.class, String.class}
        );

        String str = "[null,[null,null,[null,101,\"DataWorks\"]]]";
        Object[] values = (Object[]) jsonPath.extract(str);
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals(101L, values[0]);
        assertEquals("DataWorks", values[1]);
    }
}
