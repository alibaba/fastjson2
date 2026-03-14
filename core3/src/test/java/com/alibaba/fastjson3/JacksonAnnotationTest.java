package com.alibaba.fastjson3;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for optional Jackson annotation support.
 * Enabled via {@code ObjectMapper.builder().useJacksonAnnotation(true).build()}.
 */
public class JacksonAnnotationTest {
    static final ObjectMapper MAPPER = ObjectMapper.builder()
            .useJacksonAnnotation(true)
            .build();

    // ==================== @JsonProperty ====================

    public static class PropertyBean {
        @JsonProperty("user_name")
        private String userName;
        private int age;

        public PropertyBean() {
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    @Test
    public void testJsonProperty() {
        PropertyBean bean = new PropertyBean();
        bean.setUserName("test");
        bean.setAge(25);

        String json = MAPPER.writeValueAsString(bean);
        assertTrue(json.contains("\"user_name\""), json);
        assertTrue(json.contains("\"age\""), json);

        PropertyBean parsed = MAPPER.readValue("{\"user_name\":\"test\",\"age\":25}", PropertyBean.class);
        assertEquals("test", parsed.getUserName());
        assertEquals(25, parsed.getAge());
    }

    // ==================== @JsonIgnore ====================

    public static class IgnoreBean {
        private String name;
        @JsonIgnore
        private String password;

        public IgnoreBean() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Test
    public void testJsonIgnore() {
        IgnoreBean bean = new IgnoreBean();
        bean.setName("test");
        bean.setPassword("secret");

        String json = MAPPER.writeValueAsString(bean);
        assertTrue(json.contains("\"name\""), json);
        assertFalse(json.contains("password"), json);
    }

    // ==================== @JsonIgnoreProperties ====================

    @JsonIgnoreProperties({"password", "secret"})
    public static class IgnorePropertiesBean {
        private String name;
        private String password;
        private String secret;

        public IgnorePropertiesBean() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    @Test
    public void testJsonIgnoreProperties() {
        IgnorePropertiesBean bean = new IgnorePropertiesBean();
        bean.setName("test");
        bean.setPassword("pw");
        bean.setSecret("s");

        String json = MAPPER.writeValueAsString(bean);
        assertTrue(json.contains("\"name\""), json);
        assertFalse(json.contains("password"), json);
        assertFalse(json.contains("secret"), json);
    }

    // ==================== @JsonPropertyOrder ====================

    @JsonPropertyOrder({"z", "a", "m"})
    public static class PropertyOrderBean {
        private String a;
        private String m;
        private String z;

        public PropertyOrderBean() {
        }

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getM() {
            return m;
        }

        public void setM(String m) {
            this.m = m;
        }

        public String getZ() {
            return z;
        }

        public void setZ(String z) {
            this.z = z;
        }
    }

    @Test
    public void testJsonPropertyOrder() {
        PropertyOrderBean bean = new PropertyOrderBean();
        bean.setA("1");
        bean.setM("2");
        bean.setZ("3");

        String json = MAPPER.writeValueAsString(bean);
        int zIndex = json.indexOf("\"z\"");
        int aIndex = json.indexOf("\"a\"");
        int mIndex = json.indexOf("\"m\"");
        assertTrue(zIndex < aIndex, "z should come before a: " + json);
        assertTrue(aIndex < mIndex, "a should come before m: " + json);
    }

    // ==================== @JsonInclude ====================

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class IncludeBean {
        private String name;
        private String email;

        public IncludeBean() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    @Test
    public void testJsonIncludeNonNull() {
        IncludeBean bean = new IncludeBean();
        bean.setName("test");
        // email is null

        String json = MAPPER.writeValueAsString(bean);
        assertTrue(json.contains("\"name\""), json);
        assertFalse(json.contains("email"), json);
    }

    // ==================== @JsonAlias ====================

    public static class AliasBean {
        @JsonAlias({"user_name", "userName"})
        @JsonProperty("name")
        private String name;

        public AliasBean() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testJsonAlias() {
        AliasBean bean1 = MAPPER.readValue("{\"user_name\":\"test\"}", AliasBean.class);
        assertEquals("test", bean1.getName());

        AliasBean bean2 = MAPPER.readValue("{\"name\":\"test2\"}", AliasBean.class);
        assertEquals("test2", bean2.getName());
    }

    // ==================== @JsonProperty(access) ====================

    public static class AccessBean {
        private String name;
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        private String password;

        public AccessBean() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Test
    public void testJsonPropertyAccessWriteOnly() {
        // WRITE_ONLY means deserialize only, no serialize
        AccessBean bean = new AccessBean();
        bean.setName("test");
        bean.setPassword("secret");

        String json = MAPPER.writeValueAsString(bean);
        assertTrue(json.contains("\"name\""), json);
        assertFalse(json.contains("password"), json);

        // But deserialization should work
        AccessBean parsed = MAPPER.readValue("{\"name\":\"test\",\"password\":\"secret\"}", AccessBean.class);
        assertEquals("secret", parsed.getPassword());
    }

    // ==================== Priority: @JSONField > Jackson ====================

    public static class PriorityBean {
        @com.alibaba.fastjson3.annotation.JSONField(name = "fastjson_name")
        @JsonProperty("jackson_name")
        private String name;

        public PriorityBean() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testFastjsonAnnotationTakesPriority() {
        PriorityBean bean = new PriorityBean();
        bean.setName("test");

        String json = MAPPER.writeValueAsString(bean);
        // @JSONField should take precedence
        assertTrue(json.contains("\"fastjson_name\""), json);
        assertFalse(json.contains("jackson_name"), json);
    }

    // ==================== Disabled by default ====================

    @Test
    public void testDisabledByDefault() {
        // The shared mapper should NOT process Jackson annotations
        ObjectMapper defaultMapper = ObjectMapper.shared();

        PropertyBean bean = new PropertyBean();
        bean.setUserName("test");
        bean.setAge(25);

        String json = defaultMapper.writeValueAsString(bean);
        // Should use Java field names, not @JsonProperty names
        assertTrue(json.contains("\"userName\""), json);
        assertFalse(json.contains("\"user_name\""), json);
    }

    // ==================== @JsonProperty on field (not getter) ====================

    public static class FieldAnnotationBean {
        @JsonProperty("field_name")
        public String name;
        public int age;

        public FieldAnnotationBean() {
        }
    }

    @Test
    public void testJsonPropertyOnPublicField() {
        FieldAnnotationBean bean = new FieldAnnotationBean();
        bean.name = "test";
        bean.age = 30;

        String json = MAPPER.writeValueAsString(bean);
        assertTrue(json.contains("\"field_name\""), json);

        FieldAnnotationBean parsed = MAPPER.readValue("{\"field_name\":\"test\",\"age\":30}", FieldAnnotationBean.class);
        assertEquals("test", parsed.name);
        assertEquals(30, parsed.age);
    }

    // ==================== Roundtrip ====================

    @Test
    public void testRoundtrip() {
        PropertyBean original = new PropertyBean();
        original.setUserName("hello");
        original.setAge(42);

        String json = MAPPER.writeValueAsString(original);
        PropertyBean parsed = MAPPER.readValue(json, PropertyBean.class);

        assertEquals(original.getUserName(), parsed.getUserName());
        assertEquals(original.getAge(), parsed.getAge());
    }

    // ==================== @JsonIgnore on private field with getter ====================

    public static class IgnorePrivateFieldBean {
        private String visible;
        @JsonIgnore
        private String hidden;

        public IgnorePrivateFieldBean() {
        }

        public String getVisible() {
            return visible;
        }

        public void setVisible(String visible) {
            this.visible = visible;
        }

        public String getHidden() {
            return hidden;
        }

        public void setHidden(String hidden) {
            this.hidden = hidden;
        }
    }

    @Test
    public void testIgnoreOnPrivateFieldAffectsGetter() {
        IgnorePrivateFieldBean bean = new IgnorePrivateFieldBean();
        bean.setVisible("yes");
        bean.setHidden("no");

        String json = MAPPER.writeValueAsString(bean);
        assertTrue(json.contains("\"visible\""), json);
        assertFalse(json.contains("hidden"), json);
    }

    // ==================== rebuild() preserves flag ====================

    @Test
    public void testRebuildPreservesJacksonAnnotation() {
        ObjectMapper derived = MAPPER.rebuild()
                .enableWrite(WriteFeature.PrettyFormat)
                .build();

        PropertyBean bean = new PropertyBean();
        bean.setUserName("test");
        bean.setAge(25);

        String json = derived.writeValueAsString(bean);
        // Jackson annotation should still work after rebuild
        assertTrue(json.contains("\"user_name\""), json);
    }

    // ==================== @JsonProperty(index) → ordinal ====================

    public static class IndexBean {
        @JsonProperty(value = "b", index = 0)
        private String second;
        @JsonProperty(value = "a", index = 1)
        private String first;

        public IndexBean() {
        }

        public String getSecond() {
            return second;
        }

        public void setSecond(String second) {
            this.second = second;
        }

        public String getFirst() {
            return first;
        }

        public void setFirst(String first) {
            this.first = first;
        }
    }

    @Test
    public void testJsonPropertyIndex() {
        IndexBean bean = new IndexBean();
        bean.setFirst("1");
        bean.setSecond("2");

        String json = MAPPER.writeValueAsString(bean);
        int bIdx = json.indexOf("\"b\"");
        int aIdx = json.indexOf("\"a\"");
        assertTrue(bIdx < aIdx, "b (index=0) should come before a (index=1): " + json);
    }

    // ==================== @JsonInclude on field level ====================

    public static class FieldIncludeBean {
        private String name;
        @JsonInclude(Include.NON_NULL)
        private String optional;

        public FieldIncludeBean() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOptional() {
            return optional;
        }

        public void setOptional(String optional) {
            this.optional = optional;
        }
    }

    @Test
    public void testFieldLevelJsonInclude() {
        FieldIncludeBean bean = new FieldIncludeBean();
        bean.setName("test");
        // optional is null

        String json = MAPPER.writeValueAsString(bean);
        assertTrue(json.contains("\"name\""), json);
        assertFalse(json.contains("optional"), json);
    }

    // ==================== @JsonProperty(required) on reader ====================

    public static class RequiredBean {
        @JsonProperty(value = "id", required = true)
        public int id;
        public String name;

        public RequiredBean() {
        }
    }

    @Test
    public void testJsonPropertyRequired() {
        RequiredBean bean = MAPPER.readValue("{\"id\":42,\"name\":\"test\"}", RequiredBean.class);
        assertEquals(42, bean.id);
        assertEquals("test", bean.name);
    }
}
