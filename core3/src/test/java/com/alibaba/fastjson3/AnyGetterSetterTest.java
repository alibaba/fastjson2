package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AnyGetterSetterTest {
    static final ObjectMapper MAPPER = ObjectMapper.shared();

    // ==================== @JSONField(anyGetter=true) ====================

    public static class AnyGetterBean {
        private String name;
        private final Map<String, Object> extra = new LinkedHashMap<>();

        public AnyGetterBean() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @JSONField(anyGetter = true)
        public Map<String, Object> getExtra() {
            return extra;
        }
    }

    @Test
    public void testAnyGetterSerialize() {
        AnyGetterBean bean = new AnyGetterBean();
        bean.setName("test");
        bean.extra.put("dynamicKey", "dynamicValue");
        bean.extra.put("count", 42);

        String json = MAPPER.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"test\""), json);
        assertTrue(json.contains("\"dynamicKey\":\"dynamicValue\""), json);
        assertTrue(json.contains("\"count\":42"), json);
    }

    @Test
    public void testAnyGetterEmpty() {
        AnyGetterBean bean = new AnyGetterBean();
        bean.setName("test");
        // extra is empty

        String json = MAPPER.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"test\""), json);
        // Should not crash, just no extra fields
    }

    // ==================== @JSONField(anySetter=true) ====================

    public static class AnySetterBean {
        private String name;
        private Map<String, Object> extra;

        public AnySetterBean() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @JSONField(anySetter = true)
        public void putExtra(String key, Object value) {
            if (extra == null) {
                extra = new LinkedHashMap<>();
            }
            extra.put(key, value);
        }

        public Map<String, Object> getExtra() {
            return extra;
        }
    }

    @Test
    public void testAnySetterDeserialize() {
        AnySetterBean bean = MAPPER.readValue(
                "{\"name\":\"test\",\"unknown1\":\"val1\",\"unknown2\":42}", AnySetterBean.class);
        assertNotNull(bean);
        assertEquals("test", bean.getName());
        assertEquals("val1", bean.getExtra().get("unknown1"));
        assertEquals(42, bean.getExtra().get("unknown2"));
    }

    @Test
    public void testAnySetterFromBytes() {
        byte[] json = "{\"name\":\"test\",\"extra_key\":\"extra_val\"}"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        AnySetterBean bean = MAPPER.readValue(json, AnySetterBean.class);
        assertNotNull(bean);
        assertEquals("test", bean.getName());
        assertEquals("extra_val", bean.getExtra().get("extra_key"));
    }

    // ==================== Combined anyGetter + anySetter ====================

    public static class DynamicBean {
        private String id;
        private Map<String, Object> properties;

        public DynamicBean() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @JSONField(anyGetter = true)
        public Map<String, Object> getProperties() {
            return properties;
        }

        @JSONField(anySetter = true)
        public void setProperty(String key, Object value) {
            if (properties == null) {
                properties = new LinkedHashMap<>();
            }
            properties.put(key, value);
        }
    }

    @Test
    public void testRoundtripDynamicProperties() {
        DynamicBean original = new DynamicBean();
        original.setId("123");
        original.setProperty("color", "red");
        original.setProperty("size", 42);

        String json = MAPPER.writeValueAsString(original);
        assertTrue(json.contains("\"id\":\"123\""), json);
        assertTrue(json.contains("\"color\":\"red\""), json);

        DynamicBean parsed = MAPPER.readValue(json, DynamicBean.class);
        assertEquals("123", parsed.getId());
        assertEquals("red", parsed.getProperties().get("color"));
        assertEquals(42, parsed.getProperties().get("size"));
    }

    // ==================== anySetter takes priority over ErrorOnUnknownProperties ====================

    @Test
    public void testAnySetterOverridesErrorOnUnknown() {
        ObjectMapper strictMapper = ObjectMapper.builder()
                .enableRead(ReadFeature.ErrorOnUnknownProperties)
                .build();

        // Without anySetter, unknown props would throw
        // With anySetter, unknown props are routed to anySetter
        AnySetterBean bean = strictMapper.readValue(
                "{\"name\":\"test\",\"unknown\":\"val\"}", AnySetterBean.class);
        assertEquals("val", bean.getExtra().get("unknown"));
    }
}
