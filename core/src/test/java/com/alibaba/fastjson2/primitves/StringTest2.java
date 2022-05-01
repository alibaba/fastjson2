package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringTest2 {
    @Test
    public void test_true() throws Throwable {
        String str = "true";
        byte[] jsonbBytes = JSONB.toBytes(str);
        JSONBDump.dump(jsonbBytes);
        assertEquals(str, JSONB.parse(jsonbBytes));
        assertEquals(str, JSONB.parseObject(jsonbBytes, String.class));
    }

    @Test
    public void test_false() throws Throwable {
        String str = "false";
        byte[] jsonbBytes = JSONB.toBytes(str);
        JSONBDump.dump(jsonbBytes);
        assertEquals(str, JSONB.parse(jsonbBytes));
        assertEquals(str, JSONB.parseObject(jsonbBytes, String.class));
    }

    @Test
    public void test_0() throws Throwable {
        String str = "0";
        byte[] jsonbBytes = JSONB.toBytes(str);
        JSONBDump.dump(jsonbBytes);
        assertEquals(str, JSONB.parse(jsonbBytes));
        assertEquals(str, JSONB.parseObject(jsonbBytes, String.class));
    }

    @Test
    public void test_1() throws Throwable {
        String str = "1";
        byte[] jsonbBytes = JSONB.toBytes(str);
        JSONBDump.dump(jsonbBytes);
        assertEquals(str, JSONB.parse(jsonbBytes));
        assertEquals(str, JSONB.parseObject(jsonbBytes, String.class));
    }

    @Test
    public void test_name() throws Throwable {
        String str = "name";
        byte[] jsonbBytes = JSONB.toBytes(str);
        JSONBDump.dump(jsonbBytes);
        assertEquals(str, JSONB.parse(jsonbBytes));
        assertEquals(str, JSONB.parseObject(jsonbBytes, String.class));
    }

    @Test
    public void test_bean() throws Throwable {
        Bean bean = new Bean();
        bean.id = 101;
        bean.name = "true";
        bean.type = "false";
        byte[] jsonbBytes = JSONB.toBytes(bean);
        JSONBDump.dump(jsonbBytes);

        Bean bean2 = JSONB.parseObject(jsonbBytes, Bean.class);
        assertEquals(bean.id, bean2.id);
        assertEquals(bean.name, bean2.name);
        assertEquals(bean.type, bean2.type);
    }

    public static class Bean {
        public int id;
        public String type;
        public String name;
    }
}
