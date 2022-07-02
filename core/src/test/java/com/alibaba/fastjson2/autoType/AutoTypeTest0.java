package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.*;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AutoTypeTest0 {
    @Test
    public void test_0() throws Exception {
        String text = "{\"@type\":\"com.alibaba.fastjson2_vo.IntField1\",\"v0000\":123}";
        IntField1 model = (IntField1) JSON.parseObject(text, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(123, model.v0000);

        Object parse = JSON.parse(text);
        assertTrue(parse instanceof java.util.Map);
        IntField1 model2 = (IntField1) JSON.parse(text, JSONReader.Feature.SupportAutoType);
        assertEquals(123, model2.v0000);
    }

    @Test
    public void test_jsonb() throws Exception {
        Map map = new LinkedHashMap<>();
        map.put("@type", "com.alibaba.fastjson2_vo.IntField1");
        map.put("v0000", 123);
        byte[] jsonbBytes = JSONB.toBytes(map);

        System.out.println(JSONB.toJSONString(jsonbBytes));

        IntField1 model = (IntField1) JSONB.parseObject(jsonbBytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(123, model.v0000);
    }

    @Test
    public void test_jsonb_1() throws Exception {
        Map map = new LinkedHashMap<>();
        map.put("@type", "java.util.HashMap");
        map.put("v0000", 123);
        byte[] jsonbBytes = JSONB.toBytes(map);

        java.util.HashMap model = (java.util.HashMap) JSONB.parseObject(jsonbBytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(123, model.get("v0000"));
    }

    @Test
    public void test_jsonb_2() throws Exception {
        Map map = new LinkedHashMap<>();
        map.put("@type", "java.util.TreeMap");
        map.put("v0000", 123);
        byte[] jsonbBytes = JSONB.toBytes(map);

        java.util.TreeMap model = (java.util.TreeMap) JSONB.parseObject(jsonbBytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(123, model.get("v0000"));
    }

    @Test
    public void test_jsonb_3() throws Exception {
        Map map = new LinkedHashMap<>();
        map.put("@type", "java.util.TreeMap");
        map.put("v0000", 123);
        byte[] jsonbBytes = JSONB.toBytes(map);

        java.util.HashMap model = (java.util.HashMap) JSONB.parseObject(jsonbBytes, Map.class, JSONReader.Feature.SupportAutoType);
        assertEquals("java.util.TreeMap", model.get("@type"));
        assertEquals(123, model.get("v0000"));
    }

    @Test
    public void test_jsonb_4() throws Exception {
        Map map = new LinkedHashMap<>();
        map.put("@type", "java.util.TreeMap");
        map.put("v0000", 123);
        byte[] jsonbBytes = JSONB.toBytes(map);

        java.util.HashMap model = (java.util.HashMap) JSONB.parseObject(jsonbBytes, Map.class);
        assertEquals("java.util.TreeMap", model.get("@type"));
        assertEquals(123, model.get("v0000"));
    }

    @Test
    public void test_jsonb_5() throws Exception {
        SymbolTable symbolTable = JSONB.symbolTable("@type", "java.util.TreeMap", "v0000");

        Map map = new LinkedHashMap<>();
        map.put("@type", "java.util.TreeMap");
        map.put("v0000", 16);
        byte[] jsonbBytes = JSONB.toBytes(map, symbolTable);

        assertEquals(9, jsonbBytes.length);

        java.util.Map model = JSONB.parseObject(jsonbBytes, Map.class, symbolTable);
        assertEquals("java.util.TreeMap", model.get("@type"));
        assertEquals(16, model.get("v0000"));
    }

    @Test
    public void test_jsonb_6() throws Exception {
        SymbolTable symbolTable = JSONB.symbolTable("@type", "java.util.TreeMap", "v0000");

        Map map = new LinkedHashMap<>();
        map.put("@type", "java.util.TreeMap");
        map.put("v0000", 16);
        byte[] jsonbBytes = JSONB.toBytes(map, symbolTable);

        assertEquals(9, jsonbBytes.length);

        java.util.TreeMap model = JSONB.parseObject(jsonbBytes, Object.class, symbolTable, JSONReader.Feature.SupportAutoType);
        assertEquals(1, model.size());
        assertEquals(16, model.get("v0000"));
    }

    @Test
    public void test_jsonb_7() throws Exception {
        SymbolTable symbolTable = JSONB.symbolTable("@type", "java.util.TreeMap");

        Map map = new LinkedHashMap<>();
        map.put("@type", "java.util.TreeMap");
        for (int i = 0; i < 32; ++i) {
            map.put("v" + i, i);
        }

        byte[] jsonbBytes = JSONB.toBytes(map, symbolTable);

        java.util.TreeMap model = JSONB.parseObject(jsonbBytes, Object.class, symbolTable, JSONReader.Feature.SupportAutoType);
        assertEquals(32, model.size());
        for (int i = 0; i < 32; ++i) {
            assertEquals(Integer.valueOf(i), map.get("v" + i));
        }
    }

    @Test
    public void test_write_0() throws Exception {
        IntField1 m = new IntField1();
        m.v0000 = 123;

        String text = JSON.toJSONString(m, JSONWriter.Feature.WriteClassName);

        assertEquals(text, "{\"@type\":\"com.alibaba.fastjson2_vo.IntField1\",\"v0000\":123}");
        IntField1 model = (IntField1) JSON.parseObject(text, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(m.v0000, model.v0000);
    }

    @Test
    public void test_write_2() throws Exception {
        IntField1 m = new IntField1();
        m.v0000 = 123;

        String text = JSON.toJSONString(m, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);

        assertEquals(text, "{\"v0000\":123}");
    }

    @Test
    public void test_f1() {
        ObjectReaderCreator[] readerCreators = TestUtils.readerCreators();

        ObjectWriterCreator[] writerCreators = TestUtils.writerCreators();

        IntField1 a = new IntField1();
        a.v0000 = 123;

        String expected = "{\"@type\":\"com.alibaba.fastjson2_vo.IntField1\",\"v0000\":123}";

        for (ObjectWriterCreator writerCreator : writerCreators) {
            ObjectWriter objectWriter = writerCreator.createObjectWriter(IntField1.class);
            JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteClassName);
            jsonWriter.setRootObject(a);
            objectWriter.write(jsonWriter, a);
            String json = jsonWriter.toString();
            assertEquals(expected, json);
        }

        for (ObjectReaderCreator readerCreator : readerCreators) {
            ObjectReader<IntField1> objectReader = readerCreator.createObjectReader(IntField1.class);
            JSONReader jsonReader = JSONReader.of(expected);
            IntField1 a2 = objectReader.readObject(jsonReader);
            assertSame(a.v0000, a2.v0000);
        }
    }

    @Test
    public void test_f2() {
        ObjectReaderCreator[] readerCreators = TestUtils.readerCreators();

        ObjectWriterCreator[] writerCreators = TestUtils.writerCreators();

        IntField2 a = new IntField2();
        a.v0000 = 101;
        a.v0001 = 102;

        String expected = "{\"@type\":\"com.alibaba.fastjson2_vo.IntField2\",\"v0000\":101,\"v0001\":102}";

        for (ObjectWriterCreator writerCreator : writerCreators) {
            ObjectWriter objectWriter = writerCreator.createObjectWriter(IntField2.class);
            JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteClassName);
            jsonWriter.setRootObject(a);
            objectWriter.write(jsonWriter, a);
            String json = jsonWriter.toString();
            assertEquals(expected, json);
        }

        for (ObjectReaderCreator readerCreator : readerCreators) {
            ObjectReader<IntField2> objectReader = readerCreator.createObjectReader(IntField2.class);
            JSONReader jsonReader = JSONReader.of(expected);
            IntField2 a2 = objectReader.readObject(jsonReader);
            assertSame(a.v0000, a2.v0000);
            assertSame(a.v0001, a2.v0001);
        }
    }

    @Test
    public void test_f3() {
        ObjectReaderCreator[] readerCreators = TestUtils.readerCreators();

        ObjectWriterCreator[] writerCreators = TestUtils.writerCreators();

        IntField3 a = new IntField3();
        a.v0000 = 101;
        a.v0001 = 102;
        a.v0002 = 103;

        String expected = "{\"@type\":\"com.alibaba.fastjson2_vo.IntField3\",\"v0000\":101,\"v0001\":102,\"v0002\":103}";

        for (ObjectWriterCreator writerCreator : writerCreators) {
            ObjectWriter objectWriter = writerCreator.createObjectWriter(IntField3.class);
            JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteClassName);
            jsonWriter.setRootObject(a);
            objectWriter.write(jsonWriter, a);
            String json = jsonWriter.toString();
            assertEquals(expected, json);
        }

        for (ObjectReaderCreator readerCreator : readerCreators) {
            ObjectReader<IntField3> objectReader = readerCreator.createObjectReader(IntField3.class);
            JSONReader jsonReader = JSONReader.of(expected);
            IntField3 a2 = objectReader.readObject(jsonReader);
            assertSame(a.v0000, a2.v0000);
            assertSame(a.v0001, a2.v0001);
            assertSame(a.v0002, a2.v0002);
        }
    }

    @Test
    public void test_f4() {
        ObjectReaderCreator[] readerCreators = TestUtils.readerCreators();

        ObjectWriterCreator[] writerCreators = TestUtils.writerCreators();

        IntField4 a = new IntField4();
        a.v0000 = 101;
        a.v0001 = 102;
        a.v0002 = 103;
        a.v0003 = 104;

        String expected = "{\"@type\":\"com.alibaba.fastjson2_vo.IntField4\",\"v0000\":101,\"v0001\":102,\"v0002\":103,\"v0003\":104}";

        for (ObjectWriterCreator writerCreator : writerCreators) {
            ObjectWriter objectWriter = writerCreator.createObjectWriter(IntField4.class);
            JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteClassName);
            jsonWriter.setRootObject(a);
            objectWriter.write(jsonWriter, a);
            String json = jsonWriter.toString();
            assertEquals(expected, json);
        }

        for (ObjectReaderCreator readerCreator : readerCreators) {
            ObjectReader<IntField4> objectReader = readerCreator.createObjectReader(IntField4.class);
            JSONReader jsonReader = JSONReader.of(expected);
            IntField4 a2 = objectReader.readObject(jsonReader);
            assertSame(a.v0000, a2.v0000);
            assertSame(a.v0001, a2.v0001);
            assertSame(a.v0002, a2.v0002);
            assertSame(a.v0003, a2.v0003);
        }
    }

    @Test
    public void test_f5() {
        ObjectReaderCreator[] readerCreators = TestUtils.readerCreators();

        ObjectWriterCreator[] writerCreators = TestUtils.writerCreators();

        IntField5 a = new IntField5();
        a.v0000 = 101;
        a.v0001 = 102;
        a.v0002 = 103;
        a.v0003 = 104;
        a.v0004 = 105;

        String expected = "{\"@type\":\"com.alibaba.fastjson2_vo.IntField5\",\"v0000\":101,\"v0001\":102,\"v0002\":103,\"v0003\":104,\"v0004\":105}";

        for (ObjectWriterCreator writerCreator : writerCreators) {
            ObjectWriter objectWriter = writerCreator.createObjectWriter(IntField5.class);
            JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteClassName);
            jsonWriter.setRootObject(a);
            objectWriter.write(jsonWriter, a);
            String json = jsonWriter.toString();
            assertEquals(expected, json);
        }

        for (ObjectReaderCreator readerCreator : readerCreators) {
            ObjectReader<IntField5> objectReader = readerCreator.createObjectReader(IntField5.class);
            JSONReader jsonReader = JSONReader.of(expected);
            IntField5 a2 = objectReader.readObject(jsonReader);
            assertSame(a.v0000, a2.v0000);
            assertSame(a.v0001, a2.v0001);
            assertSame(a.v0002, a2.v0002);
            assertSame(a.v0003, a2.v0003);
            assertSame(a.v0004, a2.v0004);
        }
    }

    @Test
    public void test_f6() {
        ObjectReaderCreator[] readerCreators = TestUtils.readerCreators();

        ObjectWriterCreator[] writerCreators = TestUtils.writerCreators();

        IntField6 a = new IntField6();
        a.v0000 = 101;
        a.v0001 = 102;
        a.v0002 = 103;
        a.v0003 = 104;
        a.v0004 = 105;
        a.v0005 = 106;

        String expected = "{\"@type\":\"com.alibaba.fastjson2_vo.IntField6\",\"v0000\":101,\"v0001\":102,\"v0002\":103,\"v0003\":104,\"v0004\":105,\"v0005\":106}";

        for (ObjectWriterCreator writerCreator : writerCreators) {
            ObjectWriter objectWriter = writerCreator.createObjectWriter(IntField6.class);
            JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteClassName);
            jsonWriter.setRootObject(a);
            objectWriter.write(jsonWriter, a);
            String json = jsonWriter.toString();
            assertEquals(expected, json);
        }

        for (ObjectReaderCreator readerCreator : readerCreators) {
            ObjectReader<IntField6> objectReader = readerCreator.createObjectReader(IntField6.class);
            JSONReader jsonReader = JSONReader.of(expected);
            IntField6 a2 = objectReader.readObject(jsonReader);
            assertSame(a.v0000, a2.v0000);
            assertSame(a.v0001, a2.v0001);
            assertSame(a.v0002, a2.v0002);
            assertSame(a.v0003, a2.v0003);
            assertSame(a.v0004, a2.v0004);
            assertSame(a.v0005, a2.v0005);
        }
    }
}
