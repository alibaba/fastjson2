package com.alibaba.fastjson2.write;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ErrorOnNoneSerializableTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        assertEquals("{}", JSON.toJSONString(bean));
        assertThrows(
                JSONException.class,
                () -> JSON.toJSONString(bean, JSONWriter.Feature.ErrorOnNoneSerializable)
        );
        assertThrows(
                JSONException.class,
                () -> JSONB.toBytes(bean, JSONWriter.Feature.ErrorOnNoneSerializable)
        );
    }

    public static class Bean {
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        assertEquals("{}", JSON.toJSONString(bean));
        assertThrows(
                JSONException.class,
                () -> JSON.toJSONString(bean, JSONWriter.Feature.ErrorOnNoneSerializable)
        );
        assertThrows(
                JSONException.class,
                () -> JSONB.toBytes(bean, JSONWriter.Feature.ErrorOnNoneSerializable)
        );
        assertThrows(
                JSONException.class,
                () -> JSONB.toBytes(bean, new JSONWriter.Context(JSONWriter.Feature.ErrorOnNoneSerializable))
        );
    }

    private static class Bean1 {
        public Integer getId() {
            return null;
        }
    }
}
