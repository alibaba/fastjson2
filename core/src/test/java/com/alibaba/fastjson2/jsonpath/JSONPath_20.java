package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

public class JSONPath_20 {
    @Test
    public void testMultiPathHasSame() {
        JSONPath jsonPath = JSONPath.of(new String[]{"$.c1", "$.c1"}, new Type[]{Long.class, String.class});
        Object extract = jsonPath.extract("{\"c1\":123}");

        Object[] dataArray = (Object[]) extract;
        assertNotNull(dataArray[0]);
        assertNotNull(dataArray[1]);
    }

    @Test
    public void testMultiPathHasSame1() {
        JSONPath jsonPath = JSONPath.of(new String[]{"$.a.c1", "$.a.c1"}, new Type[]{Long.class, String.class});
        Object extract = jsonPath.extract("{\"a\":{\"c1\":123}}");

        Object[] dataArray = (Object[]) extract;
        assertNotNull(dataArray[0]);
        assertNotNull(dataArray[1]);
    }

    @Test
    public void testMultiPathHasSame2() {
        JSONPath jsonPath = JSONPath.of(new String[]{"$.a.b.c1", "$.a.b.c1"}, new Type[]{Long.class, String.class});
        Object extract = jsonPath.extract("{\"a\":{\"b\":{\"c1\":123}}}");

        Object[] dataArray = (Object[]) extract;
        assertNotNull(dataArray[0]);
        assertNotNull(dataArray[1]);
    }

    @Test
    public void testMultiPathHasSame3() {
        JSONPath jsonPath = JSONPath.of(new String[]{"$.a.b.c.c1", "$.a.b.c.c1"}, new Type[]{Long.class, String.class});
        Object extract = jsonPath.extract("{\"a\":{\"b\":{\"c\":{\"c1\":123}}}}");

        Object[] dataArray = (Object[]) extract;
        assertNotNull(dataArray[0]);
        assertNotNull(dataArray[1]);
    }

    @Test
    public void testMultiPathHasSame12() {
        JSONPath jsonPath = JSONPath.of(new String[]{"$[0]", "$[0]"}, new Type[]{Long.class, String.class});
        Object extract = jsonPath.extract("[123]");

        Object[] dataArray = (Object[]) extract;
        assertNotNull(dataArray[0]);
        assertNotNull(dataArray[1]);
    }
}
