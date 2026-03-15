package com.alibaba.fastjson2.support.vertx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Fastjson2 与 Jackson (DatabindCodec) 行为对齐测试
 */
public class CodecAlignmentTest {
    private static final DatabindCodec JACKSON_CODEC = new DatabindCodec();
    private static final Fastjson2Codec FASTJSON_CODEC = new Fastjson2Codec();

    private static ComplexBean testBean;
    private static Map<String, Object> testMap;
    private static String complexJsonString;
    private static Buffer complexJsonBuffer;

    @BeforeAll
    static void setUp() {
        // 注入 Jackson 的 JSR-310 时间模块
        ObjectMapper mapper = DatabindCodec.mapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 包含各种边缘 case 的复杂对象
        testBean = new ComplexBean();
        testBean.setId(1001L);
        testBean.setName("Vertx-Fastjson2-Test");
        testBean.setStatus(Status.ACTIVE); // 测试枚举 (Jackson 默认输出 name)
        testBean.setTimestamp(Instant.parse("2024-01-01T12:00:00Z")); // 测试时间 (Jackson 默认 ISO-8601)
        testBean.setPayload(Buffer.buffer("Hello Vert.x")); // 测试 Vert.x 特有类型 (Jackson 默认 Base64)
        testBean.setNullField(null); // 测试 Null 处理
        testBean.setTags(Arrays.asList("fastjson2", "jackson", "align"));

        // 用于反序列化的基础数据
        complexJsonString = JACKSON_CODEC.toString(testBean, false);
        complexJsonBuffer = JACKSON_CODEC.toBuffer(testBean, false);

        // fromValue 使用的 Map 数据
        testMap = new HashMap<>();
        testMap.put("id", 1001L);
        testMap.put("name", "Vertx-Fastjson2-Test");
        testMap.put("status", "ACTIVE");
    }

    // ==========================================
    // 1. 序列化 API 测试 (toString, toBuffer)
    // ==========================================

    @Test
    @DisplayName("测试 toString")
    void testToString() {
        String jacksonRes = JACKSON_CODEC.toString(testBean);
        String fastjsonRes = FASTJSON_CODEC.toString(testBean);

        assertEquals(jacksonRes, fastjsonRes, "toString 输出的 JSON 字符串必须完全一致");
    }

    @Test
    @DisplayName("测试 toBuffer")
    void testToBuffer() {
        Buffer jacksonRes = JACKSON_CODEC.toBuffer(testBean);
        Buffer fastjsonRes = FASTJSON_CODEC.toBuffer(testBean);

        assertEquals(jacksonRes.toString(), fastjsonRes.toString(), "toBuffer 输出的字节内容必须一致");
    }

    // ==========================================
    // 2. 字符串反序列化 API 测试 (fromString)
    // ==========================================

    @Test
    @DisplayName("测试 fromString(String)")
    void testFromStringToObject() {
        Object jacksonRes = JACKSON_CODEC.fromString(complexJsonString);
        Object fastjsonRes = FASTJSON_CODEC.fromString(complexJsonString);
        assertEquals(jacksonRes, fastjsonRes, "反序列化为泛型 Object 的结果必须一致");
    }

    @Test
    @DisplayName("测试 fromString(String, Class<T>)")
    void testFromStringToClass() {
        ComplexBean jacksonRes = JACKSON_CODEC.fromString(complexJsonString, ComplexBean.class);
        ComplexBean fastjsonRes = FASTJSON_CODEC.fromString(complexJsonString, ComplexBean.class);

        assertEquals(jacksonRes, fastjsonRes, "反序列化为指定 Class 的结果必须一致");
    }

    @Test
    @DisplayName("测试 fromString(String, TypeReference<T>)")
    void testFromStringToTypeReference() {
        String listJson = "[" + complexJsonString + "]";
        TypeReference<List<ComplexBean>> typeRef = new TypeReference<List<ComplexBean>>() {};

        List<ComplexBean> jacksonRes = JACKSON_CODEC.fromString(listJson, typeRef);
        List<ComplexBean> fastjsonRes = FASTJSON_CODEC.fromString(listJson, typeRef);

        assertEquals(jacksonRes, fastjsonRes, "反序列化为带泛型的 TypeReference 结果必须一致");
    }

    // ==========================================
    // 3. Buffer 反序列化 API 测试 (fromBuffer)
    // ==========================================

    @Test
    @DisplayName("测试 fromBuffer(Buffer)")
    void testFromBufferToObject() {
        Object jacksonRes = JACKSON_CODEC.fromBuffer(complexJsonBuffer);
        Object fastjsonRes = FASTJSON_CODEC.fromBuffer(complexJsonBuffer);

        assertEquals(jacksonRes, fastjsonRes, "从 Buffer 反序列化为 Object 的结果必须一致");
    }

    @Test
    @DisplayName("测试 fromBuffer(Buffer, Class<T>)")
    void testFromBufferToClass() {
        ComplexBean jacksonRes = JACKSON_CODEC.fromBuffer(complexJsonBuffer, ComplexBean.class);
        ComplexBean fastjsonRes = FASTJSON_CODEC.fromBuffer(complexJsonBuffer, ComplexBean.class);

        assertEquals(jacksonRes, fastjsonRes, "从 Buffer 反序列化为 Class 的结果必须一致");
    }

    @Test
    @DisplayName("测试 fromBuffer(Buffer, TypeReference<T>)")
    void testFromBufferToTypeReference() {
        Buffer listBuffer = Buffer.buffer("[" + complexJsonString + "]");
        TypeReference<List<ComplexBean>> typeRef = new TypeReference<List<ComplexBean>>() {};

        List<ComplexBean> jacksonRes = JACKSON_CODEC.fromBuffer(listBuffer, typeRef);
        List<ComplexBean> fastjsonRes = FASTJSON_CODEC.fromBuffer(listBuffer, typeRef);

        assertEquals(jacksonRes, fastjsonRes, "从 Buffer 反序列化为 TypeReference 的结果必须一致");
    }

    // ==========================================
    // 4. 对象转换 API 测试 (fromValue)
    // ==========================================

    @Test
    @DisplayName("测试 fromValue(Object, Class<T>)")
    void testFromValueToClass() {
        ComplexBean jacksonRes = JACKSON_CODEC.fromValue(testMap, ComplexBean.class);
        ComplexBean fastjsonRes = FASTJSON_CODEC.fromValue(testMap, ComplexBean.class);

        assertEquals(jacksonRes, fastjsonRes);
    }

    @Test
    @DisplayName("测试 fromValue(Object, TypeReference<T>)")
    void testFromValueToTypeReference() {
        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};

        Map<String, Object> jacksonMap = JACKSON_CODEC.fromValue(testBean, typeRef);
        Map<String, Object> fastjsonMap = FASTJSON_CODEC.fromValue(testBean, typeRef);

        assertNotNull(jacksonMap);
        assertNotNull(fastjsonMap);

        ComplexBean jacksonRes = JACKSON_CODEC.fromValue(jacksonMap, ComplexBean.class);
        ComplexBean fastjsonRes = FASTJSON_CODEC.fromValue(fastjsonMap, ComplexBean.class);
        assertEquals(jacksonRes, fastjsonRes);
    }

    enum Status { ACTIVE, INACTIVE }

    public static class ComplexBean {
        private Long id;
        private String name;
        private Status status;
        private Instant timestamp;
        private Buffer payload;
        private String nullField;
        private List<String> tags;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Status getStatus() { return status; }
        public void setStatus(Status status) { this.status = status; }
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
        public Buffer getPayload() { return payload; }
        public void setPayload(Buffer payload) { this.payload = payload; }
        public String getNullField() { return nullField; }
        public void setNullField(String nullField) { this.nullField = nullField; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ComplexBean that = (ComplexBean) o;
            return Objects.equals(id, that.id) &&
                    Objects.equals(name, that.name) &&
                    status == that.status &&
                    Objects.equals(timestamp, that.timestamp) &&
                    Objects.equals(payload != null ? payload.toString() : null,
                            that.payload != null ? that.payload.toString() : null) &&
                    Objects.equals(nullField, that.nullField) &&
                    Objects.equals(tags, that.tags);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, status, timestamp,
                    payload != null ? payload.toString() : null, nullField, tags);
        }
    }

    // ==========================================
    // 5. 时间与日期类型对齐测试 (JSR-310)
    // ==========================================

    @Test
    @DisplayName("测试 JSR-310 日期时间类型 - 序列化与反序列化对齐")
    void testDateTimeAlignment() {
        DateTimeBean dateBean = new DateTimeBean();
        // 具有代表性的时间（包含毫秒和时区）
        dateBean.setLocalDate(java.time.LocalDate.of(2024, 5, 20));
        dateBean.setLocalDateTime(java.time.LocalDateTime.of(2024, 5, 20, 15, 30, 45, 123000000)); // 123ms
        dateBean.setOffsetDateTime(java.time.OffsetDateTime.of(2024, 5, 20, 15, 30, 45, 123000000, java.time.ZoneOffset.ofHours(8)));
        dateBean.setZonedDateTime(java.time.ZonedDateTime.of(2024, 5, 20, 15, 30, 45, 123000000, java.time.ZoneId.of("Asia/Shanghai")));

        // 序列化对比
        String jacksonJson = JACKSON_CODEC.toString(dateBean, false);
        String fastjsonJson = FASTJSON_CODEC.toString(dateBean, false);
        assertEquals(jacksonJson, fastjsonJson, "时间与日期类型的序列化 JSON 字符串必须一致");

        // 反序列化对比
        DateTimeBean jacksonParsed = JACKSON_CODEC.fromString(jacksonJson, DateTimeBean.class);
        DateTimeBean fastjsonParsed = FASTJSON_CODEC.fromString(jacksonJson, DateTimeBean.class);
        assertEquals(jacksonParsed, fastjsonParsed, "时间与日期类型的反序列化结果必须一致");
    }

    // ==========================================
    // 6. 浮点数与高精度数字处理对齐测试
    // ==========================================

    @Test
    @DisplayName("测试浮点数与 BigDecimal - 强类型解析对齐")
    void testNumericAlignmentTyped() {
        NumericBean numericBean = new NumericBean();
        numericBean.setDoubleObj(99.998D);
        numericBean.setFloatObj(10.5F);
        numericBean.setBigDecimalObj(new java.math.BigDecimal("123456789.987654321"));
        numericBean.setPrimitiveDouble(3.1415926);
        numericBean.setPrimitiveFloat(2.718F);

        // 序列化对比
        String jacksonJson = JACKSON_CODEC.toString(numericBean, false);
        String fastjsonJson = FASTJSON_CODEC.toString(numericBean, false);
        assertEquals(jacksonJson, fastjsonJson, "浮点数的序列化 JSON 字符串必须一致");

        // 反序列化对比
        NumericBean jacksonParsed = JACKSON_CODEC.fromString(jacksonJson, NumericBean.class);
        NumericBean fastjsonParsed = FASTJSON_CODEC.fromString(jacksonJson, NumericBean.class);
        assertEquals(jacksonParsed, fastjsonParsed, "带强类型的浮点数反序列化结果必须一致");
    }

    @Test
    @DisplayName("测试浮点数无类型解析 (Untyped Parsing) - 防止 ClassCastException")
    void testNumericAlignmentUntyped() {
        String untypedJson = "{\"price\": 99.99, \"score\": 4.5, \"bigNum\": 1234567890123456789}";

        Object jacksonParsed = JACKSON_CODEC.fromString(untypedJson);
        Object fastjsonParsed = FASTJSON_CODEC.fromString(untypedJson);

        Map<?, ?> jacksonMap = ((JsonObject) jacksonParsed).getMap();
        Map<?, ?> fastjsonMap = ((JsonObject) fastjsonParsed).getMap();

        Object jacksonPrice = jacksonMap.get("price");
        Object fastjsonPrice = fastjsonMap.get("price");

        assertEquals(jacksonPrice.getClass(), fastjsonPrice.getClass(), "无类型解析时，小数的实际 Java 类型必须对齐");
        assertEquals(jacksonPrice, fastjsonPrice, "无类型小数解析的值必须相等");
        assertEquals(jacksonMap, fastjsonMap, "无类型反序列化的 Map 结构及内容必须完全一致");
    }

    /**
     * 用于测试 JSR-310 时间对齐的 Bean
     */
    public static class DateTimeBean {
        private java.time.LocalDate localDate;
        private java.time.LocalDateTime localDateTime;
        private java.time.OffsetDateTime offsetDateTime;
        private java.time.ZonedDateTime zonedDateTime;

        public java.time.LocalDate getLocalDate() { return localDate; }
        public void setLocalDate(java.time.LocalDate localDate) { this.localDate = localDate; }
        public java.time.LocalDateTime getLocalDateTime() { return localDateTime; }
        public void setLocalDateTime(java.time.LocalDateTime localDateTime) { this.localDateTime = localDateTime; }
        public java.time.OffsetDateTime getOffsetDateTime() { return offsetDateTime; }
        public void setOffsetDateTime(java.time.OffsetDateTime offsetDateTime) { this.offsetDateTime = offsetDateTime; }
        public java.time.ZonedDateTime getZonedDateTime() { return zonedDateTime; }
        public void setZonedDateTime(java.time.ZonedDateTime zonedDateTime) { this.zonedDateTime = zonedDateTime; }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DateTimeBean that = (DateTimeBean) o;
            return Objects.equals(localDate, that.localDate) &&
                    Objects.equals(localDateTime, that.localDateTime) &&
                    isSameInstant(offsetDateTime, that.offsetDateTime) &&
                    isSameInstant(zonedDateTime, that.zonedDateTime);
        }

        // 辅助方法：比较两个带时区时间是否代表同一时刻
        private boolean isSameInstant(Temporal a, Temporal b) {
            if (a == b) {
                return true;
            }
            if (a == null || b == null) {
                return false;
            }
            // 提取绝对时刻(Instant)进行比对
            Instant instantA = Instant.from(a);
            Instant instantB = Instant.from(b);
            return instantA.equals(instantB);
        }

        @Override
        public int hashCode() {
            return Objects.hash(localDate, localDateTime, offsetDateTime, zonedDateTime);
        }
    }

    public static class NumericBean {
        private Double doubleObj;
        private Float floatObj;
        private java.math.BigDecimal bigDecimalObj;
        private double primitiveDouble;
        private float primitiveFloat;

        public Double getDoubleObj() { return doubleObj; }
        public void setDoubleObj(Double doubleObj) { this.doubleObj = doubleObj; }
        public Float getFloatObj() { return floatObj; }
        public void setFloatObj(Float floatObj) { this.floatObj = floatObj; }
        public java.math.BigDecimal getBigDecimalObj() { return bigDecimalObj; }
        public void setBigDecimalObj(java.math.BigDecimal bigDecimalObj) { this.bigDecimalObj = bigDecimalObj; }
        public double getPrimitiveDouble() { return primitiveDouble; }
        public void setPrimitiveDouble(double primitiveDouble) { this.primitiveDouble = primitiveDouble; }
        public float getPrimitiveFloat() { return primitiveFloat; }
        public void setPrimitiveFloat(float primitiveFloat) { this.primitiveFloat = primitiveFloat; }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NumericBean that = (NumericBean) o;
            return Double.compare(that.primitiveDouble, primitiveDouble) == 0 &&
                    Float.compare(that.primitiveFloat, primitiveFloat) == 0 &&
                    Objects.equals(doubleObj, that.doubleObj) &&
                    Objects.equals(floatObj, that.floatObj) &&
                    Objects.equals(bigDecimalObj, that.bigDecimalObj);
        }

        @Override
        public int hashCode() {
            return Objects.hash(doubleObj, floatObj, bigDecimalObj, primitiveDouble, primitiveFloat);
        }
    }
}
