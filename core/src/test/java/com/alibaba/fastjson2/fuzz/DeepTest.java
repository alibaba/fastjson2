package com.alibaba.fastjson2.fuzz;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.JDKUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeepTest {
    @Test
    public void testArray() {
        StringBuilder buf = new StringBuilder();
        int count = 100000;
        for (int i = 0; i < count; i++) {
            buf.append('[');
        }
        for (int i = 0; i < count; i++) {
            buf.append(']');
        }

        String str = buf.toString();
        {
            JSONReader.Context context = JSONFactory.createReadContext();
            if (JDKUtils.JVM_VERSION == 8) {
                context.setMaxLevel(512);
            }
            assertThrows(JSONException.class, () -> JSON.parse(str, context));
        }

        JSONReader.Context context = JSONFactory.createReadContext();
        assertEquals(2048, context.getMaxLevel());
        context.setMaxLevel(1024);
        assertEquals(1024, context.getMaxLevel());
    }

    @Test
    public void testFlatArray() {
        StringBuilder buf = new StringBuilder();
        int count = 1000000;
        buf.append('[');
        for (int i = 0; i < count; i++) {
            if (i != 0) {
                buf.append(',');
            }
            buf.append("[]");
        }
        buf.append(']');
        Object parse = JSON.parse(buf.toString());
        assertNotNull(parse);
    }

    @Test
    public void testFlatObj() {
        StringBuilder buf = new StringBuilder();
        int count = 1000000;
        buf.append('[');
        for (int i = 0; i < count; i++) {
            if (i != 0) {
                buf.append(',');
            }
            buf.append("{}");
        }
        buf.append(']');
        Object parse = JSON.parse(buf.toString());
        assertNotNull(parse);
    }

    @Test
    public void testObj() {
        StringBuilder buf = new StringBuilder();
        int count = 100000;
        for (int i = 0; i < count; i++) {
            buf.append("{\"v\":");
        }
        buf.append("{}");
        for (int i = 0; i < count; i++) {
            buf.append('}');
        }

        String str = buf.toString();

        {
            JSONReader.Context context = JSONFactory.createReadContext();
            if (JDKUtils.JVM_VERSION == 8) {
                context.setMaxLevel(512);
            }
            assertThrows(JSONException.class, () -> JSON.parse(str, context));
        }
    }
}
