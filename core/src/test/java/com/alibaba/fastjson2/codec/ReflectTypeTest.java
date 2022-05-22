package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.JSONBDump;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ReflectTypeTest {
    @Test
    public void test_0() throws Exception {
        assertEquals("\"" + A.class.getName() + "\"", JSON.toJSONString(A.class));

        byte[] jsonbBytes = JSONB.toBytes(A.class);

        Exception error = null;
        try {
            JSONB.parseObject(jsonbBytes, Class.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);

        assertSame(A.class, JSONB.parseObject(jsonbBytes, Class.class, JSONReader.Feature.SupportClassForName));
    }

    @Test
    public void test_paramType() {
        ParameterizedTypeImpl paramType = new ParameterizedTypeImpl(new Type[]{String.class, String.class}, null, Map.class);
        String str = JSON.toJSONString(paramType);
        ParameterizedType paramType1 = JSON.parseObject(str, ParameterizedType.class, JSONReader.Feature.SupportClassForName);
        assertEquals(paramType, paramType1);
    }

    @Test
    public void test_paramType_jsonb() {
        ParameterizedTypeImpl paramType = new ParameterizedTypeImpl(new Type[]{String.class, String.class}, null, Map.class);
        byte[] bytes = JSONB.toBytes(paramType);

        JSONBDump.dump(bytes);

        ParameterizedType paramType1 = JSONB.parseObject(bytes, ParameterizedType.class, JSONReader.Feature.SupportClassForName);
        assertEquals(paramType, paramType1);
    }

    public static class A {
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
