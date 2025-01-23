package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

public class JSONBTest6 {
    @Test
    public void test() throws Exception {
        Bean bean = new Bean();
        bean.id = 1001;

        JSONWriter jsonWriter = JSONWriter.ofJSONB(JSONFactory.createWriteContext(), null);
        assertNull(jsonWriter.symbolTable);
        jsonWriter.writeAny(bean);
        int size = jsonWriter.size();
        byte[] bytes = jsonWriter.getBytes();
        assertEquals(size, bytes.length);

        {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            Bean bean1 = JSONB.parseObject(in, bytes.length, Bean.class, JSONReader.Feature.SupportArrayToBean);
            assertEquals(bean.id, bean1.id);
        }
        {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            Bean bean1 = JSONB.parseObject(in, bytes.length, Bean.class, JSONFactory.createReadContext(jsonWriter.symbolTable, JSONReader.Feature.SupportArrayToBean));
            assertEquals(bean.id, bean1.id);
        }

        {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            assertThrows(IllegalArgumentException.class, () -> JSONB.parseObject(in, bytes.length + 1, Bean.class, JSONReader.Feature.SupportArrayToBean));
        }
        {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            assertThrows(IllegalArgumentException.class, () -> JSONB.parseObject(in, bytes.length + 1, Bean.class, JSONReader.Feature.SupportArrayToBean));
        }
    }

    @Test
    public void test1() throws Exception {
        Bean bean = new Bean();
        bean.id = 1001;

        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        Bean1 bean1 = JSONB.parseObject(jsonbBytes, (Type) Bean1.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.IgnoreAutoTypeNotMatch);
        assertEquals(bean.id, bean1.id);
    }

    @Test
    public void test2() throws Exception {
        Bean bean = new Bean();
        bean.id = 1001;

        byte[] jsonbBytes = JSONB.toBytes(bean, new JSONWriter.Context(JSONWriter.Feature.WriteClassName));
        Bean1 bean1 = JSONB.parseObject(jsonbBytes, (Type) Bean1.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.IgnoreAutoTypeNotMatch);
        assertEquals(bean.id, bean1.id);
    }

    @Test
    public void test3() throws Exception {
        Bean bean = new Bean();
        bean.id = 1001;

        byte[] jsonbBytes = JSONB.toBytes(bean, new JSONWriter.Context(JSONWriter.Feature.WriteClassName));
        Bean1 bean1 = JSONB.parseObject(new ByteArrayInputStream(jsonbBytes), Bean1.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.IgnoreAutoTypeNotMatch);
        assertEquals(bean.id, bean1.id);
    }

    @Test
    public void test4() throws Exception {
        Bean bean = new Bean();
        bean.id = 1001;

        byte[] jsonbBytes = JSONB.toBytes(bean, new JSONWriter.Context(JSONWriter.Feature.WriteClassName));
        Bean1 bean1 = JSONB.parseObject(new ByteArrayInputStream(jsonbBytes), (Type) Bean1.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.IgnoreAutoTypeNotMatch);
        assertEquals(bean.id, bean1.id);

        Bean1 bean2 = JSONB.copy(bean1);
        assertEquals(bean1.id, bean2.id);
    }

    public static class Bean {
        public int id;
    }

    public static class Bean1 {
        public int id;
    }

    @Test
    public void test5() {
        JSONWriterJSONB w = (JSONWriterJSONB) JSONWriter.ofJSONB();
        w.writeRaw(BC_DECIMAL);
        w.writeInt32(1);
        w.writeRaw(BC_BIGINT_LONG);
        w.writeInt64(1234);

        byte[] bytes = w.getBytes();

        assertEquals("123.4", JSONB.toJSONString(bytes));
    }

    @Test
    public void bigint() {
        int[] values = new int[] {1, 12, 123, 1234, 12345, 123456, 1234567, 12345678, 123456789, 1234567890};
        for (int value : values) {
            JSONWriterJSONB w = (JSONWriterJSONB) JSONWriter.ofJSONB();
            w.writeRaw(BC_BIGINT_LONG);
            w.writeInt32(value);

            assertEquals(JSON.toJSONString(value), JSONB.toJSONString(w.getBytes()));
        }
    }

    @Test
    public void bigint1() {
        byte[] values = new byte[] {1, 12, 123};
        for (byte value : values) {
            JSONWriterJSONB w = (JSONWriterJSONB) JSONWriter.ofJSONB();
            w.writeRaw(BC_BIGINT_LONG);
            w.writeInt8(value);

            assertEquals(JSON.toJSONString(value), JSONB.toJSONString(w.getBytes()));
        }
    }

    @Test
    public void bigint2() {
        short[] values = new short[] {1, 12, 123, 1234, 12345};
        for (short value : values) {
            JSONWriterJSONB w = (JSONWriterJSONB) JSONWriter.ofJSONB();
            w.writeRaw(BC_BIGINT_LONG);
            w.writeInt16(value);

            assertEquals(JSON.toJSONString(value), JSONB.toJSONString(w.getBytes()));
        }
    }

    @Test
    public void length() {
        int[] values = new int[] {1, 12, 123, 1234, 12345, 123456, 1234567, 12345678, 123456789, 1234567890};
        for (int value : values) {
            JSONWriterJSONB w = (JSONWriterJSONB) JSONWriter.ofJSONB();
            w.writeInt32(value);
            w.writeInt32(value);

            JSONBDump dump = new JSONBDump(w.getBytes(), true);

            assertEquals(
                    value,
                    dump.readLength());
        }
    }

    @Test
    public void lengthError() {
        JSONWriterJSONB w = (JSONWriterJSONB) JSONWriter.ofJSONB();
        w.writeInt32(1);
        w.writeRaw(BC_NULL);

        JSONBDump dump = new JSONBDump(w.getBytes(), true);

        assertThrows(
                JSONException.class,
                dump::readLength);
    }
}
