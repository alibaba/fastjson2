package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.Inclusion;
import com.alibaba.fastjson3.annotation.JSONField;
import com.alibaba.fastjson3.annotation.JSONType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Inclusion strategy (NON_NULL, NON_EMPTY, ALWAYS).
 */
class InclusionTest {
    private final ObjectMapper mapper = ObjectMapper.shared();

    // ==================== NON_EMPTY at class level ====================

    @Test
    void nonEmptyClassLevel_skipNullString() {
        NonEmptyBean bean = new NonEmptyBean();
        bean.name = null;
        bean.value = "hello";
        String json = mapper.writeValueAsString(bean);
        assertFalse(json.contains("\"name\""), json);
        assertTrue(json.contains("\"hello\""), json);
    }

    @Test
    void nonEmptyClassLevel_skipEmptyString() {
        NonEmptyBean bean = new NonEmptyBean();
        bean.name = "";
        bean.value = "test";
        String json = mapper.writeValueAsString(bean);
        assertFalse(json.contains("\"name\""), json);
        assertTrue(json.contains("\"test\""), json);
    }

    @Test
    void nonEmptyClassLevel_skipEmptyList() {
        NonEmptyBean bean = new NonEmptyBean();
        bean.name = "ok";
        bean.value = "val";
        bean.tags = new ArrayList<>();
        String json = mapper.writeValueAsString(bean);
        assertFalse(json.contains("\"tags\""), json);
        assertTrue(json.contains("\"ok\""), json);
    }

    @Test
    void nonEmptyClassLevel_keepNonEmptyList() {
        NonEmptyBean bean = new NonEmptyBean();
        bean.name = "ok";
        bean.value = "v";
        bean.tags = List.of("a", "b");
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"tags\""), json);
    }

    // ==================== NON_EMPTY at field level ====================

    @Test
    void nonEmptyFieldLevel() {
        FieldLevelBean bean = new FieldLevelBean();
        bean.required = "yes";
        bean.optional = "";  // should be skipped
        bean.normal = "";    // should NOT be skipped (default inclusion)
        String json = mapper.writeValueAsString(bean);
        assertFalse(json.contains("\"optional\""), json);
        assertTrue(json.contains("\"normal\""), json);
        assertTrue(json.contains("\"required\""), json);
    }

    // ==================== ALWAYS ====================

    @Test
    void alwaysInclusion_writeNulls() {
        AlwaysBean bean = new AlwaysBean();
        bean.name = null;
        bean.age = 0;
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"name\""), json);
        assertTrue(json.contains("null"), json);
    }

    // ==================== Test beans ====================

    @JSONType(inclusion = Inclusion.NON_EMPTY)
    public static class NonEmptyBean {
        public String name;
        public String value;
        public List<String> tags;
    }

    public static class FieldLevelBean {
        public String required;
        @JSONField(inclusion = Inclusion.NON_EMPTY)
        public String optional;
        public String normal;
    }

    @JSONType(inclusion = Inclusion.ALWAYS)
    public static class AlwaysBean {
        public String name;
        public int age;
    }
}
