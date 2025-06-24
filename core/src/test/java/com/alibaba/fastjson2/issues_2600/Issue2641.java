package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2641 {
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void test() throws Exception {
        {
            Bean bean = new Bean(new LinkedHashSet<>());
            String jackson = objectMapper.writeValueAsString(bean);
            assertEquals(jackson, JSON.toJSONString(bean));
        }

        {
            LinkedHashSet<String> groups = new LinkedHashSet<>();
            groups.add("abc");
            Bean bean = new Bean(groups);
            String jackson = objectMapper.writeValueAsString(bean);
            assertEquals(jackson, JSON.toJSONString(bean));
        }
    }

    public static class Bean {
        private final Set<String> groups;

        public Bean(Set<String> groups) {
            this.groups = groups;
        }

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public Set<String> getGroups() {
            return groups;
        }
    }

    @Test
    public void test1() throws Exception {
        Bean1 bean = new Bean1();
        String jackson = objectMapper.writeValueAsString(bean);
        assertEquals(jackson, JSON.toJSONString(bean));
    }

    public static class Bean1 {
        private final Status status = Status.UP;

        @JsonUnwrapped
        public Status getStatus() {
            return this.status;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static final class Status {
        public static final Status UP = new Status("UP");
        private final String code;

        private final String description;

        public Status(String code) {
            this(code, "");
        }

        public Status(String code, String description) {
            this.code = code;
            this.description = description;
        }

        @JsonProperty("status")
        public String getCode() {
            return this.code;
        }

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String getDescription() {
            return this.description;
        }
    }

    public static final class Status1 {
        public static final Status1 UP = new Status1("UP");
        private final String code;

        private final String description;

        public Status1(String code) {
            this(code, "");
        }

        public Status1(String code, String description) {
            this.code = code;
            this.description = description;
        }

        @JSONField(name = "status")
        public String getCode() {
            return this.code;
        }

        @JSONField(serializeFeatures = JSONWriter.Feature.IgnoreEmpty)
        public String getDescription() {
            return this.description;
        }
    }

    public static final class Status2 {
        public static final Status2 UP = new Status2("UP");
        private final String code;

        public final String description;

        public Status2(String code) {
            this(code, "");
        }

        public Status2(String code, String description) {
            this.code = code;
            this.description = description;
        }

        @JSONField(name = "status")
        public String getCode() {
            return this.code;
        }
    }

    @Test
    public void testStatus() throws Exception {
        String jackson = objectMapper.writeValueAsString(Status.UP);
        assertEquals(jackson, JSON.toJSONString(Status.UP));
        assertEquals(jackson, JSON.toJSONString(Status1.UP));
        assertEquals("{\"description\":\"\",\"status\":\"UP\"}", JSON.toJSONString(Status2.UP));
        assertEquals(jackson, JSON.toJSONString(Status2.UP, JSONWriter.Feature.IgnoreEmpty));
    }

    @Test
    public void test2() throws Exception {
        Bean2 bean = new Bean2(null);
        String jackson = objectMapper.writeValueAsString(bean);
        assertEquals(jackson, JSON.toJSONString(bean));
    }

    public static class Bean2 {
        private String value;
        public Bean2(String value) {
            this.value = value;
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String getValue() {
            return value;
        }
    }

    @Test
    public void test3() throws Exception {
        final TestJsonIncludeDTO dto = new TestJsonIncludeDTO();
        final String jackson = objectMapper.writeValueAsString(dto);
        final String fastjson2 = JSON.toJSONString(dto);

        assertEquals(jackson, "{}");
        assertEquals(fastjson2, "{}"); // fails
    }

    static class TestJsonIncludeDTO {
        private final Map<String, String> map = new HashMap<>();

        private final List<String> list = new ArrayList<>();

        private final Set<String> set = new HashSet<>();

        private final String strEmpty = "";

        private final String strNull = null;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @JsonProperty
        public Map<String, String> getMap() {
            return this.map;
        }

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public List<String> getList() {
            return this.list;
        }

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public Set<String> getSet() {
            return this.set;
        }

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String getStrEmpty() {
            return this.strEmpty;
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String getStrNull() {
            return this.strNull;
        }
    }

    @Test
    void testJsonUnwrapped() throws Exception {
        final Bean3 testJsonUnwrapped = new Bean3();
        final String jackson = new ObjectMapper().writeValueAsString(testJsonUnwrapped);
        assertEquals(jackson, JSON.toJSONString(testJsonUnwrapped)); //Don't work
    }

    private static class Bean3 {
        private final Status status = Status.UP;

        @JsonUnwrapped
        public Status getStatus() {
            return this.status;
        }
    }

    @Test
    public void test4() throws Exception {
        // given
        final Bean4 dto = new Bean4();

        // when
        final String jackson = new ObjectMapper().writeValueAsString(dto);
        final String fastjson2 = JSON.toJSONString(dto);

        // then
        assertEquals(jackson, "{}");
        assertEquals(fastjson2, "{}"); // fails
    }

    public class Bean4 {
        private final Map<String, String> map = new HashMap<>();

        private final List<String> list = new ArrayList<>();

        private final Set<String> set = new HashSet<>();

        private final String strEmpty = "";

        private final String strNull = null;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @JsonProperty
        public Map<String, String> getMap() {
            return this.map;
        }

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public List<String> getList() {
            return this.list;
        }

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public Set<String> getSet() {
            return this.set;
        }

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String getStrEmpty() {
            return this.strEmpty;
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String getStrNull() {
            return this.strNull;
        }
    }
}
