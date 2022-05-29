package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.Boolean1;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class BooleanTest {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<Boolean1> objectWriter = creator.createObjectWriter(Boolean1.class);

            {
                Boolean1 vo = new Boolean1();
                vo.setV0000(true);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":true}", jsonWriter.toString());
            }
            {
                Boolean1 vo = new Boolean1();
                vo.setV0000(false);
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":false}", jsonWriter.toString());
            }
            {
                Boolean1 vo = new Boolean1();
                vo.setV0000(true);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[true]", jsonWriter.toString());
            }
            {
                Boolean1 vo = new Boolean1();
                vo.setV0000(false);
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[false]", jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_null_x() throws Exception {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            FieldWriter fieldWriter = creator.createFieldWriter(Boolean1.class, "v0000", 0, 0, null, Boolean1.class.getMethod("getV0000"));
            ObjectWriter<Boolean1> objectWriter
                    = creator.createObjectWriter(fieldWriter);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                Boolean1 vo = new Boolean1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                Boolean1 vo = new Boolean1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                Boolean1 vo = new Boolean1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":null}",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_true() {
        boolean value = true;
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(value);
        String str = jw.toString();
        assertEquals("true", str);

        assertEquals(value, JSONReader.of(str).readBoolValue());
    }

    @Test
    public void test_true_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(true);
        assertEquals(Boolean.TRUE,
                JSONB.parseObject(jsonbBytes, Boolean.class));
    }

    @Test
    public void test_null_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes((java.util.Map) null);
        assertNull(JSONB.parseObject(jsonbBytes, Boolean.class));
    }

    @Test
    public void test_null() {
        assertNull(
                JSON.parseObject("null", Boolean.class));
    }

    @Test
    public void test_true_num() {
        boolean value = true;
        JSONWriter jw = JSONWriter.of();
        jw.config(JSONWriter.Feature.WriteBooleanAsNumber);
        jw.writeAny(value);
        String str = jw.toString();
        assertEquals("1", str);

        assertEquals(value, JSONReader.of(str).readBoolValue());
    }

    @Test
    public void test_false() {
        boolean value = false;
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(value);
        String str = jw.toString();
        assertEquals("false", str);

        assertEquals(value, JSONReader.of(str).readBoolValue());
    }

    @Test
    public void test_false_num() {
        boolean value = false;
        JSONWriter jw = JSONWriter.of();
        jw.config(JSONWriter.Feature.WriteBooleanAsNumber);
        jw.writeAny(value);
        String str = jw.toString();
        assertEquals("0", str);

        assertEquals(value, JSONReader.of(str).readBoolValue());
    }

    @Test
    public void test_array_boolean() {
        boolean[] array = new boolean[]{true, false};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[true,false]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_dim2() {
        boolean[][] array = new boolean[][]{{true, false, true}, {false, true}, {true, false}};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[[true,false,true],[false,true],[true,false]]", str);

        boolean[][] array2 = JSONReader.of(str).read(array.getClass());
        assertEquals(array.length, array2.length);
        for (int i = 0; i < array.length; ++i) {
            assertTrue(
                    Arrays.equals(array[i], array2[i]));
        }
    }

    @Test
    public void test_array_Boolean() {
        Boolean[] array = new Boolean[]{true, false};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[true,false]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_boolean_num() {
        boolean[] array = new boolean[]{true, false};
        JSONWriter jw = JSONWriter.of();
        jw.config(JSONWriter.Feature.WriteBooleanAsNumber);
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[1,0]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_Boolean_num() {
        Boolean[] array = new Boolean[]{true, false};
        JSONWriter jw = JSONWriter.of();
        jw.config(JSONWriter.Feature.WriteBooleanAsNumber);
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[1,0]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }
}
