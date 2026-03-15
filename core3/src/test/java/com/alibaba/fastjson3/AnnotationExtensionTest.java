package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONCreator;
import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for extended annotation capabilities:
 * - @JSONField(format=) for date/time formatting
 * - @JSONCreator(parameterNames=) for constructor parameter mapping
 * - @JSONField(value=true) for single-value serialization
 * - @JSONField(serializeUsing=/deserializeUsing=) for custom codecs
 */
public class AnnotationExtensionTest {
    static final ObjectMapper MAPPER = ObjectMapper.shared();

    // ==================== @JSONField(format=) ====================

    public static class DateBean {
        @JSONField(format = "yyyy/MM/dd")
        private LocalDate birthday;
        private String name;

        public DateBean() {
        }

        public LocalDate getBirthday() {
            return birthday;
        }

        public void setBirthday(LocalDate birthday) {
            this.birthday = birthday;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testFormatSerialize() {
        DateBean bean = new DateBean();
        bean.setName("test");
        bean.setBirthday(LocalDate.of(2024, 3, 15));

        String json = MAPPER.writeValueAsString(bean);
        assertTrue(json.contains("\"2024/03/15\""), json);
    }

    @Test
    public void testFormatDeserialize() {
        DateBean bean = MAPPER.readValue(
                "{\"name\":\"test\",\"birthday\":\"2024/03/15\"}", DateBean.class);
        assertEquals(LocalDate.of(2024, 3, 15), bean.getBirthday());
    }

    @Test
    public void testFormatRoundtrip() {
        DateBean original = new DateBean();
        original.setName("test");
        original.setBirthday(LocalDate.of(2000, 1, 1));

        String json = MAPPER.writeValueAsString(original);
        DateBean parsed = MAPPER.readValue(json, DateBean.class);
        assertEquals(original.getBirthday(), parsed.getBirthday());
    }

    // Format with LocalDateTime
    public static class DateTimeBean {
        @JSONField(format = "yyyy-MM-dd HH:mm")
        private LocalDateTime time;

        public DateTimeBean() {
        }

        public LocalDateTime getTime() {
            return time;
        }

        public void setTime(LocalDateTime time) {
            this.time = time;
        }
    }

    @Test
    public void testFormatLocalDateTime() {
        DateTimeBean bean = new DateTimeBean();
        bean.setTime(LocalDateTime.of(2024, 3, 15, 14, 30));

        String json = MAPPER.writeValueAsString(bean);
        assertTrue(json.contains("\"2024-03-15 14:30\""), json);

        DateTimeBean parsed = MAPPER.readValue(json, DateTimeBean.class);
        assertEquals(bean.getTime(), parsed.getTime());
    }

    // ==================== @JSONCreator(parameterNames=) ====================

    public static class ParamNamesBean {
        private final String firstName;
        private final int age;

        @JSONCreator(parameterNames = {"first_name", "age"})
        public ParamNamesBean(String firstName, int age) {
            this.firstName = firstName;
            this.age = age;
        }

        public String getFirstName() {
            return firstName;
        }

        public int getAge() {
            return age;
        }
    }

    @Test
    public void testCreatorParameterNames() {
        ParamNamesBean bean = MAPPER.readValue(
                "{\"first_name\":\"Alice\",\"age\":30}", ParamNamesBean.class);
        assertNotNull(bean);
        assertEquals("Alice", bean.getFirstName());
        assertEquals(30, bean.getAge());
    }

    // ==================== @JSONField(value=true) ====================

    public enum StatusEnum {
        ACTIVE("active"),
        INACTIVE("inactive");

        private final String code;

        StatusEnum(String code) {
            this.code = code;
        }

        @JSONField(value = true)
        public String getCode() {
            return code;
        }
    }

    @Test
    public void testValueSerializeEnum() {
        String json = MAPPER.writeValueAsString(StatusEnum.ACTIVE);
        assertEquals("\"active\"", json);
    }

    // Value on a regular class
    public static class ValueWrapper {
        private final int data;

        public ValueWrapper(int data) {
            this.data = data;
        }

        @JSONField(value = true)
        public int getData() {
            return data;
        }
    }

    @Test
    public void testValueSerializeClass() {
        String json = MAPPER.writeValueAsString(new ValueWrapper(42));
        assertEquals("42", json);
    }

    // ==================== @JSONField(serializeUsing=) ====================

    public static class MaskedWriter implements ObjectWriter<String> {
        @Override
        public void write(JSONGenerator generator, Object object, Object fieldName,
                          java.lang.reflect.Type fieldType, long features) {
            if (object == null) {
                generator.writeNull();
            } else {
                String s = (String) object;
                generator.writeString("***" + s.substring(Math.max(0, s.length() - 4)));
            }
        }
    }

    public static class UserWithMask {
        private String name;
        @JSONField(serializeUsing = MaskedWriter.class)
        private String phone;

        public UserWithMask() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    @Test
    public void testSerializeUsing() {
        UserWithMask bean = new UserWithMask();
        bean.setName("Alice");
        bean.setPhone("13812345678");

        String json = MAPPER.writeValueAsString(bean);
        assertTrue(json.contains("\"Alice\""), json);
        assertTrue(json.contains("\"***5678\""), json);
        assertFalse(json.contains("13812345678"), json);
    }

    // ==================== @JSONField(deserializeUsing=) ====================

    public static class TrimReader implements ObjectReader<String> {
        @Override
        public String readObject(JSONParser parser, java.lang.reflect.Type fieldType,
                                 Object fieldName, long features) {
            String str = parser.readString();
            return str != null ? str.trim() : null;
        }
    }

    public static class TrimBean {
        @JSONField(deserializeUsing = TrimReader.class)
        private String value;

        public TrimBean() {
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Test
    public void testDeserializeUsing() {
        TrimBean bean = MAPPER.readValue("{\"value\":\"  hello  \"}", TrimBean.class);
        assertEquals("hello", bean.getValue());
    }

    // ==================== Combined: format + Jackson compat ====================

    @Test
    public void testFormatWithJacksonAnnotation() {
        ObjectMapper jacksonMapper = ObjectMapper.builder()
                .useJacksonAnnotation(true)
                .build();

        // Jackson @JsonFormat maps to format
        // (This test verifies the pipeline works end-to-end with Jackson compat enabled)
        DateBean bean = new DateBean();
        bean.setBirthday(LocalDate.of(2024, 6, 1));

        String json = jacksonMapper.writeValueAsString(bean);
        assertTrue(json.contains("\"2024/06/01\""), json);
    }

    // ==================== No format = default behavior ====================

    public static class NoFormatDateBean {
        private LocalDate date;

        public NoFormatDateBean() {
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }

    @Test
    public void testNoFormatUsesDefault() {
        NoFormatDateBean bean = new NoFormatDateBean();
        bean.setDate(LocalDate.of(2024, 3, 15));

        String json = MAPPER.writeValueAsString(bean);
        // Default format should NOT use custom pattern
        assertTrue(json.contains("2024-03-15"), json);
    }
}
