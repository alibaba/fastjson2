package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriters;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Enum_0 {
    Type[] types = Type.class.getEnumConstants();

    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<VO> objectWriter
                    = creator.createObjectWriter(VO.class);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                VO vo = new VO();
                vo.setValue(Type.T0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[0]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                jsonWriter.config(JSONWriter.Feature.WriteEnumUsingToString);
                VO vo = new VO();
                vo.setValue(Type.T0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[\"T0\"]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                jsonWriter.config(JSONWriter.Feature.WriteEnumsUsingName);
                VO vo = new VO();
                vo.setValue(Type.T0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[\"T0\"]",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_feature() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<VO> objectWriter
                    = creator.createObjectWriter(VO.class);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                VO vo = new VO();
                vo.setValue(Type.T0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"value\":0}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                VO vo = new VO();
                vo.setValue(Type.T0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"value\":0}",
                        jsonWriter.toString());
            }

            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.config(JSONWriter.Feature.WriteEnumUsingToString);
                VO vo = new VO();
                vo.setValue(Type.T0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"value\":\"T0\"}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.config(JSONWriter.Feature.WriteEnumsUsingName);
                VO vo = new VO();
                vo.setValue(Type.T0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"value\":\"T0\"}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteEnumUsingToString);
                VO vo = new VO();
                vo.setValue(Type.T0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"value\":\"T0\"}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteEnumsUsingName);
                VO vo = new VO();
                vo.setValue(Type.T0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"value\":\"T0\"}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                jsonWriter.config(JSONWriter.Feature.WriteEnumUsingToString);
                VO vo = new VO();
                vo.setValue(Type.T0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"value\":\"T0\"}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                jsonWriter.config(JSONWriter.Feature.WriteEnumsUsingName);
                VO vo = new VO();
                vo.setValue(Type.T0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"value\":\"T0\"}",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_null() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            FieldWriter fieldWriter = creator.createFieldWriter("date", 0, null, Type.class, VO::getValue);
            ObjectWriter<VO> objectWriter
                    = creator.createObjectWriter(fieldWriter);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                VO vo = new VO();
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                VO vo = new VO();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                VO vo = new VO();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"date\":null}",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_null_field() throws Exception {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            FieldWriter fieldWriter = creator.createFieldWriter("date", 0, 0, null, V1.class.getField("value"));
            ObjectWriter<V1> objectWriter
                    = creator.createObjectWriter(fieldWriter);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                V1 vo = new V1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                V1 vo = new V1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                V1 vo = new V1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"date\":null}",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_null_2() throws Exception {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            FieldWriter fieldWriter = creator.createFieldWriter(VO.class, "date", 0, 0, null, VO.class.getMethod("getValue"));
            ObjectWriter<VO> objectWriter
                    = creator.createObjectWriter(fieldWriter);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                VO vo = new VO();
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                VO vo = new VO();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                VO vo = new VO();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"date\":null}",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_reflect() throws Exception {
        ObjectWriter<VO> ow = ObjectWriters.ofReflect(VO.class);
        ObjectReader<VO> oc = ObjectReaders.ofReflect(VO.class);

        testV0(ow, oc);
    }

    @Test
    public void test_lambda() throws Exception {
        ObjectWriter<VO> ow = ObjectWriters.objectWriter(VO.class);
        ObjectReader<VO> oc = ObjectReaders.of(VO.class);

        testV0(ow, oc);
    }

    @Test
    public void test_asm() throws Exception {
        ObjectWriter<VO> ow = TestUtils.WRITER_CREATOR.createObjectWriter(VO.class);
        ObjectReader<VO> oc = TestUtils.READER_CREATOR.createObjectReader(VO.class);

        testV0(ow, oc);
    }

    private void testV0(ObjectWriter<VO> ow, ObjectReader<VO> oc) {
        for (Type type : types) {
            VO vo = new VO();
            vo.value = type;
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            VO o = oc.readObject(jr, 0);
            assertEquals(vo.value, o.value);
        }
    }

    @Test
    public void test_field_reflect() throws Exception {
        ObjectWriter<V1> ow = ObjectWriters.ofReflect(V1.class);
        ObjectReader<V1> oc = ObjectReaders.ofReflect(V1.class);

        testV1(ow, oc);
    }

    private void testV1(ObjectWriter<V1> ow, ObjectReader<V1> oc) {
        for (Type type : types) {
            V1 vo = new V1();
            vo.value = type;
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            V1 o = oc.readObject(jr, 0);
            assertEquals(vo.value, o.value);
        }
    }

    @Test
    public void test_field_lambda() throws Exception {
        ObjectWriter<V1> ow = ObjectWriters.objectWriter(V1.class);
        ObjectReader<V1> oc = ObjectReaders.of(V1.class);

        testV1(ow, oc);
    }

    @Test
    public void test_field_asm() throws Exception {
        ObjectWriter<V1> ow = TestUtils.WRITER_CREATOR.createObjectWriter(V1.class);
        ObjectReader<V1> oc = TestUtils.READER_CREATOR.createObjectReader(V1.class);

        testV1(ow, oc);
    }

    @Test
    public void test_field_jsonb() throws Exception {
        ObjectWriter<V1> ow = TestUtils.WRITER_CREATOR.createObjectWriter(V1.class);
        ObjectReader<V1> oc = TestUtils.READER_CREATOR.createObjectReader(V1.class);

        for (Type type : types) {
            V1 vo = new V1();
            vo.value = type;
            JSONWriter w = JSONWriter.ofJSONB();
            ow.write(w, vo);

            byte[] jsonbBytes = w.getBytes();
            JSONReader jr = JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length);
            V1 o = oc.readObject(jr, 0);
            assertEquals(vo.value, o.value);
        }
    }

    @Test
    public void test_str() {
        for (Type id : types) {
            VO vo = new VO();
            vo.setValue(id);
            String str = JSON.toJSONString(vo);

            VO v1 = JSON.parseObject(str, VO.class);
            assertEquals(vo.getValue(), v1.getValue());
        }
    }

    @Test
    public void test_str_value() {
        for (Type id : types) {
            String str = JSON.toJSONString(id);
            Type id2 = JSON.parseObject(str, Type.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_str_ordinal_value() {
        for (Type id : types) {
            String str = JSON.toJSONString(id.ordinal());
            Type id2 = JSON.parseObject(str, Type.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_utf8() {
        for (Type id : types) {
            VO vo = new VO();
            vo.setValue(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            VO v1 = JSON.parseObject(utf8Bytes, VO.class);
            assertEquals(vo.getValue(), v1.getValue());
        }
    }

    @Test
    public void test_utf8_value() {
        for (Type id : types) {
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            Type id2 = JSON.parseObject(utf8Bytes, Type.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_utf8_orinal_value() {
        for (Type id : types) {
            byte[] utf8Bytes = JSON.toJSONBytes(id.ordinal());
            Type id2 = JSON.parseObject(utf8Bytes, Type.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_ascii() {
        for (Type id : types) {
            VO vo = new VO();
            vo.setValue(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            VO v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, VO.class);
            assertEquals(vo.getValue(), v1.getValue());
        }
    }

    @Test
    public void test_ascii_value() {
        for (Type id : types) {
            if (id == Type.十1) {
                continue;
            }
            byte[] utf8Bytes = JSON.toJSONBytes(id);
            Type id2 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Type.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_ascii_ordinal_value() {
        for (Type id : types) {
            byte[] utf8Bytes = JSON.toJSONBytes(id.ordinal());
            Type id2 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Type.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonb() {
        for (Type id : types) {
            VO vo = new VO();
            vo.setValue(id);
            byte[] jsonbBytes = JSONB.toBytes(vo);

            VO v1 = JSONB.parseObject(jsonbBytes, 0, jsonbBytes.length, VO.class);
            assertEquals(vo.getValue(), v1.getValue());
        }
    }

    @Test
    public void test_jsonb_symbol_table() {
        SymbolTable symbolTable = JSONB.symbolTable(
                "value"
        );

        for (Type id : types) {
            VO vo = new VO();
            vo.setValue(id);
            byte[] jsonbBytes = JSONB.toBytes(vo, symbolTable);

            VO v1 = JSONB.parseObject(jsonbBytes, 0, jsonbBytes.length, VO.class, symbolTable);
            assertEquals(vo.getValue(), v1.getValue());
        }
    }

    @Test
    public void test_jsonb_value() {
        for (Type id : types) {
            byte[] jsonbBytes = JSONB.toBytes(id);
            Type id2 = JSONB.parseObject(jsonbBytes, 0, jsonbBytes.length, Type.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonb_value_type() {
        for (Type id : types) {
            byte[] jsonbBytes = JSONB.toBytes(id);
            Type id2 = JSONB.parseObject(jsonbBytes, 0, jsonbBytes.length, (java.lang.reflect.Type) Type.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonb_value2() {
        for (Type id : types) {
            byte[] jsonbBytes = JSONB.toBytes(id.name(), StandardCharsets.UTF_16);
            Type id2 = JSONB.parseObject(jsonbBytes, 0, jsonbBytes.length, Type.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonb_value_utf8() {
        for (Type id : types) {
            byte[] jsonbBytes = JSONB.toBytes(id.name(), StandardCharsets.UTF_8);
            Type id2 = JSONB.parseObject(jsonbBytes, 0, jsonbBytes.length, Type.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonb_value_utf16BE() {
        for (Type id : types) {
            byte[] jsonbBytes = JSONB.toBytes(id.name(), StandardCharsets.UTF_16BE);
            Type id2 = JSONB.parseObject(jsonbBytes, 0, jsonbBytes.length, Type.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonb_value_utf16LE() {
        for (Type id : types) {
            byte[] jsonbBytes = JSONB.toBytes(id.name(), StandardCharsets.UTF_16LE);
            Type id2 = JSONB.parseObject(jsonbBytes, 0, jsonbBytes.length, Type.class);
            assertEquals(id, id2);
        }
    }

    @Test
    public void test_jsonb_ordinal_value() {
        for (Type id : types) {
            byte[] jsonbBytes = JSONB.toBytes(id.ordinal());
            Type id2 = JSONB.parseObject(jsonbBytes, 0, jsonbBytes.length, Type.class);
            assertEquals(id, id2);
        }
    }

    public static class VO {
        private Type value;

        public Type getValue() {
            return value;
        }

        public void setValue(Type value) {
            this.value = value;
        }
    }

    public static class V1 {
        public Type value;
    }

    public static enum Type {
        T0,
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
        十1
    }
}
