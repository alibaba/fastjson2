package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Issue3918 {
    @Test
    public void testUnquotedKeyWithExistingField() {
        String body = "{\"a\":{1:1}}";
        A result = JSON.parseObject(body, A.class);
        assertNotNull(result);
        assertNotNull(result.getA());
        assertEquals(1, result.getA().get(1));
    }

    @Test
    public void testUnquotedKeyWithNonExistingField() {
        String bodyB = "{\"b\":{1:1}}";
        A result = JSON.parseObject(bodyB, A.class);
        assertNotNull(result);
        assertTrue(result.getA().isEmpty());
    }

    @Test
    public void testUnquotedKeyInDirectMap() {
        String json = "{1:1, 2:2}";
        Map<Integer, Integer> map = JSON.parseObject(json, Map.class);
        assertNotNull(map);
        assertEquals(1, map.get(1));
        assertEquals(2, map.get(2));
    }

    @Test
    public void testMixedFieldsWithUnquotedKey() {
        String json = "{\"a\":{1:100, 2:200}, \"b\":{3:300}, \"c\":{4:400}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertNotNull(result.getA());
        assertEquals(2, result.getA().size());
        assertEquals(100, result.getA().get(1));
        assertEquals(200, result.getA().get(2));
    }

    @Test
    public void testNestedUnknownFieldsWithUnquotedKey() {
        String json = "{\"a\":{1:1}, \"unknown\":{\"nested\":{1:2, 2:3}}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertEquals(1, result.getA().get(1));
    }

    @Test
    public void testUnquotedKeyWithNegativeNumber() {
        String json = "{\"b\":{-1:100, -2:200}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertTrue(result.getA().isEmpty());
    }

    @Test
    public void testUnquotedKeyWithDecimal() {
        String json = "{\"b\":{1.5:100, 2.5:200}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertTrue(result.getA().isEmpty());
    }

    @Test
    public void testUnquotedKeyWithScientificNotation() {
        String json = "{\"b\":{1e2:100, 2E-1:200}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertTrue(result.getA().isEmpty());
    }

    @Test
    public void testUnquotedKeyWithPositiveSign() {
        String json = "{\"b\":{+1:100, +2:200}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertTrue(result.getA().isEmpty());
    }

    @Test
    public void testUnquotedKeyWithSpaces() {
        String json = "{\"b\":{ 1 : 100 , 2 : 200 }}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertTrue(result.getA().isEmpty());
    }

    @Test
    public void testUnquotedKeyWithExponent() {
        String json = "{\"b\":{1e+5:100, 2E+3:200, 3e-2:300}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertTrue(result.getA().isEmpty());
    }

    @Test
    public void testMultipleUnknownFieldsWithUnquotedKeys() {
        String json = "{\"b\":{1:1}, \"c\":{2:2}, \"d\":{3:3}, \"a\":{10:100}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertEquals(1, result.getA().size());
        assertEquals(100, result.getA().get(10));
    }

    @Test
    public void testDeeplyNestedUnknownFields() {
        String json = "{\"unknown1\":{\"unknown2\":{\"unknown3\":{1:1, 2:2}}}, \"a\":{5:50}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertEquals(1, result.getA().size());
        assertEquals(50, result.getA().get(5));
    }

    @Test
    public void testUnquotedKeyWithZero() {
        String json = "{\"b\":{0:100, -0:200}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertTrue(result.getA().isEmpty());
    }

    @Test
    public void testUnquotedKeyWithLargeNumber() {
        String json = "{\"b\":{999999999:100, -999999999:200}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertTrue(result.getA().isEmpty());
    }

    @Test
    public void testUnquotedKeyWithDecimalVariations() {
        String json = "{\"b\":{0.5:1, 5.0:3}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertTrue(result.getA().isEmpty());
    }

    @Test
    public void testUnquotedKeyMixedWithQuotedKey() {
        String json = "{\"b\":{1:100, \"name\":\"value\", 2:200}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertTrue(result.getA().isEmpty());
    }

    @Test
    public void testUnquotedKeyInArray() {
        String json = "{\"b\":[{1:100}, {2:200}]}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertTrue(result.getA().isEmpty());
    }

    @Test
    public void testUnquotedKeyWithComplexValue() {
        String json = "{\"b\":{1:{\"nested\":true}, 2:[1,2,3], 3:\"string\"}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertTrue(result.getA().isEmpty());
    }

    @Test
    public void testUTF16Encoding() {
        String json = "{\"测试\":{1:1}, \"a\":{2:20}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertEquals(1, result.getA().size());
        assertEquals(20, result.getA().get(2));
    }

    @Test
    public void testEmptyObjectWithUnquotedKey() {
        String json = "{\"b\":{1:1, 2:2, 3:3}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertTrue(result.getA().isEmpty());
    }

    @Test
    public void testUnquotedKeyAtEnd() {
        String json = "{\"a\":{1:10}, \"b\":{2:20}}";
        A result = JSON.parseObject(json, A.class);
        assertNotNull(result);
        assertEquals(1, result.getA().size());
        assertEquals(10, result.getA().get(1));
    }

    static class A {
        private Map<Integer, Integer> a = new HashMap<>();

        public Map<Integer, Integer> getA() {
            return a;
        }

        public void setA(Map<Integer, Integer> a) {
            this.a = a;
        }
    }
}
