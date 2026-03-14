package com.alibaba.fastjson3;

import com.alibaba.fastjson3.modules.ObjectReaderModule;
import com.alibaba.fastjson3.modules.ObjectWriterModule;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for serialization/deserialization edge cases, writeAny branches,
 * TypeReference, Module SPI, and the JSON static API.
 */
class SerializationEdgeTest {
    // ==================== writeAny type dispatch branches ====================

    @Test
    void writeAnyString() {
        assertEquals("\"hello\"", JSON.toJSONString("hello"));
    }

    @Test
    void writeAnyInteger() {
        assertEquals("42", JSON.toJSONString(42));
    }

    @Test
    void writeAnyLong() {
        assertEquals("9999999999", JSON.toJSONString(9999999999L));
    }

    @Test
    void writeAnyBoolean() {
        assertEquals("true", JSON.toJSONString(true));
        assertEquals("false", JSON.toJSONString(false));
    }

    @Test
    void writeAnyDouble() {
        String json = JSON.toJSONString(3.14);
        assertTrue(json.contains("3.14"));
    }

    @Test
    void writeAnyFloat() {
        String json = JSON.toJSONString(1.5f);
        assertTrue(json.contains("1.5"));
    }

    @Test
    void writeAnyBigDecimal() {
        String json = JSON.toJSONString(new BigDecimal("123.456"));
        assertTrue(json.contains("123.456"));
    }

    @Test
    void writeAnyBigInteger() {
        String json = JSON.toJSONString(new BigInteger("99999999999999999999"));
        assertTrue(json.contains("99999999999999999999"));
    }

    @Test
    void writeAnyShort() {
        String json = JSON.toJSONString((short) 42);
        assertEquals("42", json);
    }

    @Test
    void writeAnyByte() {
        String json = JSON.toJSONString((byte) 7);
        assertEquals("7", json);
    }

    @Test
    void writeAnyNull() {
        assertEquals("null", JSON.toJSONString(null));
    }

    @Test
    void writeAnyEnum() {
        String json = JSON.toJSONString(Thread.State.RUNNABLE);
        assertEquals("\"RUNNABLE\"", json);
    }

    @Test
    void writeAnyMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", "two");
        String json = JSON.toJSONString(map);
        assertTrue(json.contains("\"a\""));
        assertTrue(json.contains("\"b\""));
        assertTrue(json.contains("\"two\""));
    }

    @Test
    void writeAnyCollection() {
        List<Object> list = List.of(1, "two", true);
        String json = JSON.toJSONString(list);
        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
        assertTrue(json.contains("1"));
        assertTrue(json.contains("\"two\""));
        assertTrue(json.contains("true"));
    }

    @Test
    void writeAnyObjectArray() {
        Object[] arr = {1, "two", false};
        String json = JSON.toJSONString(arr);
        assertTrue(json.startsWith("["));
        assertTrue(json.contains("\"two\""));
    }

    @Test
    void writeAnyJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("x", 1);
        String json = JSON.toJSONString(obj);
        assertTrue(json.contains("\"x\""));
    }

    @Test
    void writeAnyJSONArray() {
        JSONArray arr = new JSONArray();
        arr.add(1);
        arr.add("two");
        String json = JSON.toJSONString(arr);
        assertTrue(json.startsWith("["));
    }

    // ==================== Nested map / collection ====================

    @Test
    void writeNestedMapAndList() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("items", List.of(1, 2, 3));
        map.put("meta", Map.of("count", 3));
        String json = JSON.toJSONString(map);
        assertTrue(json.contains("\"items\""));
        assertTrue(json.contains("[1,2,3]"));
    }

    // ==================== TypeReference ====================

    @Test
    void typeReferenceCapturesGenericType() {
        TypeReference<List<String>> ref = new TypeReference<List<String>>() {
        };
        Type type = ref.getType();
        assertNotNull(type);
        assertTrue(type.getTypeName().contains("List"));
        assertTrue(type.getTypeName().contains("String"));
    }

    @Test
    void typeReferenceRawThrows() {
        assertThrows(JSONException.class, () -> {
            // Using raw type triggers the error branch
            @SuppressWarnings({"rawtypes", "unused"})
            TypeReference ref = new TypeReference() {
            };
        });
    }

    // ==================== JSON static API coverage ====================

    @Test
    void parseNullAndEmpty() {
        assertNull(JSON.parse(null));
        assertNull(JSON.parse(""));
        assertNull(JSON.parseObject((String) null, SerializationEdgeTest.class));
        assertNull(JSON.parseObject("", SerializationEdgeTest.class));
    }

    @Test
    void parseWithReadFeature() {
        Object result = JSON.parse("123.456", ReadFeature.UseBigDecimalForDoubles);
        assertInstanceOf(BigDecimal.class, result);
    }

    @Test
    void toJSONStringWithWriteFeature() {
        // WriteBigDecimalAsPlain is implemented
        String json = JSON.toJSONString(
                new BigDecimal("1E+2"), WriteFeature.WriteBigDecimalAsPlain
        );
        assertTrue(json.contains("100"), json);
    }

    @Test
    void toJSONBytesNull() {
        byte[] bytes = JSON.toJSONBytes(null);
        assertEquals("null", new String(bytes));
    }

    @Test
    void toJSONBytesWithFeature() {
        byte[] bytes = JSON.toJSONBytes(
                new BigDecimal("1E+2"), WriteFeature.WriteBigDecimalAsPlain
        );
        assertTrue(new String(bytes).contains("100"));
    }

    @Test
    void isValidVariousCases() {
        assertTrue(JSON.isValid("{\"a\":1}"));
        assertTrue(JSON.isValid("[1,2,3]"));
        assertTrue(JSON.isValid("\"hello\""));
        assertTrue(JSON.isValid("42"));
        assertTrue(JSON.isValid("true"));
        assertTrue(JSON.isValid("null"));
        assertFalse(JSON.isValid(""));
        assertFalse(JSON.isValid((String) null));
        assertFalse(JSON.isValid("{invalid"));
    }

    @Test
    void isValidBytes() {
        assertTrue(JSON.isValid("{\"a\":1}".getBytes()));
        assertFalse(JSON.isValid((byte[]) null));
        assertFalse(JSON.isValid(new byte[0]));
        assertFalse(JSON.isValid("{bad".getBytes()));
    }

    @Test
    void jsonObjectFactory() {
        JSONObject empty = JSON.object();
        assertTrue(empty.isEmpty());

        JSONObject one = JSON.object("key", "val");
        assertEquals("val", one.getString("key"));
    }

    @Test
    void jsonArrayFactory() {
        JSONArray empty = JSON.array();
        assertTrue(empty.isEmpty());

        JSONArray items = JSON.array(1, "two", true);
        assertEquals(3, items.size());
    }

    @Test
    void toJSONBytesWithPojo() {
        SimpleBean bean = new SimpleBean();
        bean.name = "test";
        bean.value = 42;
        byte[] bytes = JSON.toJSONBytes(bean);
        String json = new String(bytes);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"test\""));
    }

    // ==================== Feature bitmask ====================

    @Test
    void readFeatureBitmask() {
        long mask = ReadFeature.of(
                ReadFeature.UseBigDecimalForDoubles,
                ReadFeature.ErrorOnUnknownProperties
        );
        assertTrue((mask & ReadFeature.UseBigDecimalForDoubles.mask) != 0);
        assertTrue((mask & ReadFeature.ErrorOnUnknownProperties.mask) != 0);
        assertEquals(0, mask & ReadFeature.AllowSingleQuotes.mask);
    }

    @Test
    void writeFeatureBitmask() {
        long mask = WriteFeature.of(WriteFeature.WriteNulls, WriteFeature.WriteBigDecimalAsPlain);
        assertTrue((mask & WriteFeature.WriteNulls.mask) != 0);
        assertTrue((mask & WriteFeature.WriteBigDecimalAsPlain.mask) != 0);
        assertEquals(0, mask & WriteFeature.PrettyFormat.mask);
    }

    // ==================== ObjectMapper API ====================

    @Test
    void mapperReadValueByType() {
        ObjectMapper mapper = ObjectMapper.shared();
        String json = "{\"name\":\"test\",\"value\":42}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertEquals("test", bean.name);
        assertEquals(42, bean.value);
    }

    @Test
    void mapperReadValueFromBytes() {
        ObjectMapper mapper = ObjectMapper.shared();
        byte[] bytes = "{\"name\":\"test\",\"value\":42}".getBytes();
        SimpleBean bean = mapper.readValue(bytes, SimpleBean.class);
        assertEquals("test", bean.name);
    }

    @Test
    void mapperReadObjectAndArray() {
        ObjectMapper mapper = ObjectMapper.shared();
        JSONObject obj = mapper.readObject("{\"a\":1}");
        assertEquals(1, obj.getIntValue("a"));

        JSONArray arr = mapper.readArray("[1,2]");
        assertEquals(2, arr.size());
    }

    @Test
    void mapperReadObjectFromBytes() {
        ObjectMapper mapper = ObjectMapper.shared();
        JSONObject obj = mapper.readObject("{\"a\":1}".getBytes());
        assertEquals(1, obj.getIntValue("a"));
    }

    @Test
    void mapperReadList() {
        ObjectMapper mapper = ObjectMapper.shared();
        List<SimpleBean> list = mapper.readList(
                "[{\"name\":\"a\",\"value\":1},{\"name\":\"b\",\"value\":2}]",
                SimpleBean.class
        );
        assertEquals(2, list.size());
        assertEquals("a", list.get(0).name);
        assertEquals("b", list.get(1).name);
    }

    @Test
    void mapperRebuild() {
        ObjectMapper base = ObjectMapper.builder()
                .enableWrite(WriteFeature.WriteNulls)
                .build();
        ObjectMapper derived = base.rebuild()
                .enableRead(ReadFeature.ErrorOnUnknownProperties)
                .build();
        assertTrue(derived.isWriteEnabled(WriteFeature.WriteNulls));
        assertTrue(derived.isReadEnabled(ReadFeature.ErrorOnUnknownProperties));
    }

    @Test
    void mapperSharedIsSingleton() {
        assertSame(ObjectMapper.shared(), ObjectMapper.shared());
    }

    // ==================== ValueReader / ValueWriter ====================

    @Test
    void valueReaderBasic() {
        ObjectMapper mapper = ObjectMapper.shared();
        ObjectMapper.ValueReader<SimpleBean> reader = mapper.readerFor(SimpleBean.class);
        SimpleBean bean = reader.readValue("{\"name\":\"test\",\"value\":1}");
        assertEquals("test", bean.name);
    }

    @Test
    void valueWriterBasic() {
        ObjectMapper mapper = ObjectMapper.shared();
        ObjectMapper.ValueWriter writer = mapper.writer();
        SimpleBean bean = new SimpleBean();
        bean.name = "test";
        bean.value = 1;
        String json = writer.writeValueAsString(bean);
        assertTrue(json.contains("\"test\""));

        byte[] bytes = writer.writeValueAsBytes(bean);
        assertTrue(new String(bytes).contains("\"test\""));
    }

    @Test
    void valueWriterWithNulls() {
        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.WriteNulls)
                .build();
        ObjectMapper.ValueWriter writer = mapper.writer();
        SimpleBean bean = new SimpleBean();
        bean.name = null;
        bean.value = 0;
        String json = writer.writeValueAsString(bean);
        assertTrue(json.contains("null"));
    }

    // ==================== Module SPI ====================

    @Test
    void customReaderModule() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addReaderModule(new ObjectReaderModule() {
                    @Override
                    public ObjectReader<?> getObjectReader(Type type) {
                        if (type == CustomPoint.class) {
                            return (parser, fieldType, fieldName, features) -> {
                                JSONObject obj = parser.readObject();
                                CustomPoint p = new CustomPoint();
                                p.x = obj.getIntValue("x");
                                p.y = obj.getIntValue("y");
                                return p;
                            };
                        }
                        return null;
                    }
                })
                .build();

        CustomPoint point = mapper.readValue("{\"x\":10,\"y\":20}", CustomPoint.class);
        assertEquals(10, point.x);
        assertEquals(20, point.y);
    }

    @Test
    void customWriterModule() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addWriterModule(new ObjectWriterModule() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public ObjectWriter<?> getObjectWriter(Type type, Class<?> rawType) {
                        if (rawType == CustomPoint.class) {
                            return (ObjectWriter<CustomPoint>) (gen, obj, fn, ft, features) -> {
                                CustomPoint p = (CustomPoint) obj;
                                gen.startObject();
                                gen.writeName("x");
                                gen.writeInt32(p.x);
                                gen.writeName("y");
                                gen.writeInt32(p.y);
                                gen.endObject();
                            };
                        }
                        return null;
                    }
                })
                .build();

        CustomPoint p = new CustomPoint();
        p.x = 5;
        p.y = 10;
        String json = mapper.writeValueAsString(p);
        assertTrue(json.contains("\"x\":5"));
        assertTrue(json.contains("\"y\":10"));
    }

    @Test
    void customReaderAndWriterRegistration() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addReader(CustomPoint.class, (parser, fieldType, fieldName, features) -> {
                    JSONObject obj = parser.readObject();
                    CustomPoint p = new CustomPoint();
                    p.x = obj.getIntValue("x");
                    p.y = obj.getIntValue("y");
                    return p;
                })
                .addWriter(CustomPoint.class, (gen, obj, fn, ft, features) -> {
                    gen.startObject();
                    gen.writeName("x");
                    gen.writeInt32(((CustomPoint) obj).x);
                    gen.writeName("y");
                    gen.writeInt32(((CustomPoint) obj).y);
                    gen.endObject();
                })
                .build();

        CustomPoint p = new CustomPoint();
        p.x = 3;
        p.y = 4;
        String json = mapper.writeValueAsString(p);
        CustomPoint parsed = mapper.readValue(json, CustomPoint.class);
        assertEquals(3, parsed.x);
        assertEquals(4, parsed.y);
    }

    // ==================== Boxed primitive fields ====================

    public static class BoxedBean {
        public Integer intVal;
        public Long longVal;
        public Double doubleVal;
        public Boolean boolVal;
    }

    @Test
    void boxedPrimitivesRoundTrip() {
        String json = "{\"intVal\":10,\"longVal\":20,\"doubleVal\":3.14,\"boolVal\":true}";
        BoxedBean bean = JSON.parseObject(json, BoxedBean.class);
        assertEquals(10, bean.intVal);
        assertEquals(20L, bean.longVal);
        assertEquals(3.14, bean.doubleVal);
        assertTrue(bean.boolVal);

        String out = JSON.toJSONString(bean);
        assertTrue(out.contains("10"));
        assertTrue(out.contains("20"));
        assertTrue(out.contains("3.14"));
        assertTrue(out.contains("true"));
    }

    @Test
    void boxedPrimitivesNullOmitted() {
        BoxedBean bean = new BoxedBean();
        String json = JSON.toJSONString(bean);
        // Null boxed fields should be omitted by default
        assertFalse(json.contains("intVal"));
    }

    @Test
    void boxedPrimitivesNullWithWriteNulls() {
        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.WriteNulls)
                .build();
        BoxedBean bean = new BoxedBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("null"));
    }

    // ==================== Numeric type conversion (FieldReader.convertValue) ====================

    public static class NumericBean {
        public short shortVal;
        public byte byteVal;
        public float floatVal;
    }

    @Test
    void numericNarrowingConversion() {
        // JSON numbers parsed as Integer/Long, but field expects short/byte/float
        String json = "{\"shortVal\":100,\"byteVal\":7,\"floatVal\":1.5}";
        NumericBean bean = JSON.parseObject(json, NumericBean.class);
        assertEquals((short) 100, bean.shortVal);
        assertEquals((byte) 7, bean.byteVal);
        assertEquals(1.5f, bean.floatVal, 0.001f);
    }

    // ==================== Helper classes ====================

    public static class SimpleBean {
        public String name;
        public int value;
    }

    public static class CustomPoint {
        public int x;
        public int y;
    }

    // ==================== NaN / Infinity ====================

    @Test
    void nanSerializesToNull() {
        String json = JSON.toJSONString(Double.NaN);
        assertEquals("null", json);
    }

    @Test
    void positiveInfinitySerializesToNull() {
        String json = JSON.toJSONString(Double.POSITIVE_INFINITY);
        assertEquals("null", json);
    }

    @Test
    void floatNanSerializesToNull() {
        String json = JSON.toJSONString(Float.NaN);
        assertEquals("null", json);
    }

    @Test
    void nanInBeanSerializesToNull() {
        NumericBean bean = new NumericBean();
        bean.floatVal = Float.NaN;
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("null"), json);
    }

    // ==================== Nesting Depth ====================

    @Test
    void deepNestingParseThrows() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            sb.append("{\"a\":");
        }
        sb.append("1");
        for (int i = 0; i < 600; i++) {
            sb.append('}');
        }
        assertThrows(JSONException.class, () -> JSON.parse(sb.toString()));
    }

    @Test
    void deepArrayNestingParseThrows() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            sb.append('[');
        }
        sb.append("1");
        for (int i = 0; i < 600; i++) {
            sb.append(']');
        }
        assertThrows(JSONException.class, () -> JSON.parse(sb.toString()));
    }

    @Test
    void moderateNestingParseSucceeds() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("{\"a\":");
        }
        sb.append("1");
        for (int i = 0; i < 100; i++) {
            sb.append('}');
        }
        Object result = JSON.parse(sb.toString());
        assertNotNull(result);
    }

    // ==================== Int Overflow ====================

    @Test
    void largeNumberParsedAsLong() {
        String json = "3000000000"; // > Integer.MAX_VALUE
        Object val = JSON.parse(json);
        assertInstanceOf(Long.class, val);
        assertEquals(3000000000L, val);
    }

    @Test
    void veryLargeNumberParsedAsBigInteger() {
        String json = "99999999999999999999"; // > Long.MAX_VALUE
        Object val = JSON.parse(json);
        assertInstanceOf(java.math.BigInteger.class, val);
    }

    // ==================== Surrogate Pairs ====================

    @Test
    void emojiRoundTrip() {
        String emoji = "Hello \uD83D\uDE00 World"; // 😀
        String json = JSON.toJSONString(emoji);
        String parsed = (String) JSON.parse(json);
        assertEquals(emoji, parsed);
    }

    @Test
    void emojiInBeanRoundTrip() {
        SimpleBean bean = new SimpleBean();
        bean.name = "test \uD83C\uDF89"; // 🎉
        String json = JSON.toJSONString(bean);
        SimpleBean back = JSON.parseObject(json, SimpleBean.class);
        assertEquals(bean.name, back.name);
    }

    @Test
    void emojiInBytesRoundTrip() {
        String emoji = "\uD83D\uDE80\uD83C\uDF1F"; // 🚀🌟
        byte[] bytes = JSON.toJSONBytes(emoji);
        Object parsed = JSON.parseObject(bytes, String.class);
        assertEquals(emoji, parsed);
    }

    // ==================== Non-ASCII Field Names ====================

    public static class ChineseFieldBean {
        @com.alibaba.fastjson3.annotation.JSONField(name = "名字")
        public String name;

        @com.alibaba.fastjson3.annotation.JSONField(name = "年龄")
        public int age;

        public ChineseFieldBean() {
        }
    }

    @Test
    void nonAsciiFieldNameRoundTrip() {
        ChineseFieldBean bean = new ChineseFieldBean();
        bean.name = "张三";
        bean.age = 30;
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("名字"), json);
        assertTrue(json.contains("张三"), json);

        ChineseFieldBean back = JSON.parseObject(json, ChineseFieldBean.class);
        assertEquals("张三", back.name);
        assertEquals(30, back.age);
    }

    @Test
    void nonAsciiFieldNameFromBytes() {
        byte[] json = "{\"名字\":\"李四\",\"年龄\":25}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ChineseFieldBean back = JSON.parseObject(json, ChineseFieldBean.class);
        assertEquals("李四", back.name);
        assertEquals(25, back.age);
    }
}
