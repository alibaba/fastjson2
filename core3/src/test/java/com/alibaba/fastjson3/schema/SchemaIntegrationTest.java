package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONSchemaValidException;
import com.alibaba.fastjson3.annotation.JSONField;
import com.alibaba.fastjson3.annotation.JSONType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaIntegrationTest {
    // ==================== @JSONField(schema=) Integration ====================

    public static class User {
        @JSONField(schema = "{\"minLength\": 1, \"maxLength\": 50}")
        public String name;

        @JSONField(schema = "{\"minimum\": 0, \"maximum\": 150}")
        public int age;

        @JSONField(schema = "{\"format\": \"email\"}")
        public String email;
    }

    @Test
    public void testSchemaValidationOnParse() {
        // Valid input
        User user = JSON.parseObject("{\"name\":\"John\", \"age\":30, \"email\":\"john@example.com\"}", User.class);
        assertEquals("John", user.name);
        assertEquals(30, user.age);
        assertEquals("john@example.com", user.email);
    }

    @Test
    public void testSchemaValidationRejectsInvalidAge() {
        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{\"name\":\"John\", \"age\":-1}", User.class));
    }

    @Test
    public void testSchemaValidationRejectsEmptyName() {
        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{\"name\":\"\", \"age\":30}", User.class));
    }

    @Test
    public void testSchemaValidationRejectsInvalidEmail() {
        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{\"name\":\"John\", \"age\":30, \"email\":\"bad\"}", User.class));
    }

    @Test
    public void testSchemaValidationRejectsAgeTooHigh() {
        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{\"name\":\"John\", \"age\":200}", User.class));
    }

    // ==================== Schema with different field types ====================

    public static class Product {
        @JSONField(schema = "{\"type\": \"string\", \"pattern\": \"^[A-Z]{3}-[0-9]{4}$\"}")
        public String sku;

        @JSONField(schema = "{\"minimum\": 0.01}")
        public double price;

        @JSONField(schema = "{\"minimum\": 0}")
        public long stock;

        public String description;
    }

    @Test
    public void testProductValidation() {
        Product p = JSON.parseObject("{\"sku\":\"ABC-1234\", \"price\":9.99, \"stock\":100, \"description\":\"test\"}", Product.class);
        assertEquals("ABC-1234", p.sku);
        assertEquals(9.99, p.price, 0.001);
        assertEquals(100, p.stock);
    }

    @Test
    public void testProductRejectsInvalidSku() {
        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{\"sku\":\"invalid\", \"price\":9.99, \"stock\":100}", Product.class));
    }

    @Test
    public void testProductRejectsNegativePrice() {
        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{\"sku\":\"ABC-1234\", \"price\":-1, \"stock\":100}", Product.class));
    }

    @Test
    public void testProductRejectsNegativeStock() {
        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{\"sku\":\"ABC-1234\", \"price\":9.99, \"stock\":-1}", Product.class));
    }

    // ==================== Schema Generation: of(Type) ====================

    @Test
    public void testOfTypeString() {
        JSONSchema schema = JSONSchema.of(String.class);
        assertEquals(JSONSchema.Type.String, schema.getType());
        assertTrue(schema.isValid("hello"));
    }

    @Test
    public void testOfTypeInteger() {
        JSONSchema schema = JSONSchema.of(int.class);
        assertEquals(JSONSchema.Type.Integer, schema.getType());
        assertTrue(schema.isValid(42));
    }

    @Test
    public void testOfTypeLong() {
        JSONSchema schema = JSONSchema.of(Long.class);
        assertEquals(JSONSchema.Type.Integer, schema.getType());
        assertTrue(schema.isValid(42L));
    }

    @Test
    public void testOfTypeDouble() {
        JSONSchema schema = JSONSchema.of(double.class);
        assertEquals(JSONSchema.Type.Number, schema.getType());
        assertTrue(schema.isValid(3.14));
    }

    @Test
    public void testOfTypeBoolean() {
        JSONSchema schema = JSONSchema.of(boolean.class);
        assertEquals(JSONSchema.Type.Boolean, schema.getType());
        assertTrue(schema.isValid(true));
    }

    @Test
    public void testOfTypeList() {
        JSONSchema schema = JSONSchema.of(List.class);
        assertEquals(JSONSchema.Type.Array, schema.getType());
    }

    @Test
    public void testOfTypeMap() {
        JSONSchema schema = JSONSchema.of(Map.class);
        assertEquals(JSONSchema.Type.Object, schema.getType());
    }

    enum Color { RED, GREEN, BLUE }

    @Test
    public void testOfTypeEnum() {
        JSONSchema schema = JSONSchema.of(Color.class);
        assertEquals(JSONSchema.Type.String, schema.getType());
        assertTrue(schema.isValid("RED"));
        assertTrue(schema.isValid("GREEN"));
        assertFalse(schema.isValid("YELLOW"));
    }

    public static class SimplePojo {
        public String name;
        public int age;
    }

    @Test
    public void testOfTypePojo() {
        JSONSchema schema = JSONSchema.of(SimplePojo.class);
        assertEquals(JSONSchema.Type.Object, schema.getType());

        ObjectSchema os = (ObjectSchema) schema;
        assertNotNull(os.getProperties());
        assertTrue(os.getProperties().containsKey("name"));
        assertTrue(os.getProperties().containsKey("age"));
        // int is primitive → required
        assertTrue(os.getRequired().contains("age"));
    }

    // ==================== Schema Generation: ofValue(Object) ====================

    @Test
    public void testOfValueMap() {
        Map<String, Object> map = Map.of("name", "John", "age", 30);
        JSONSchema schema = JSONSchema.ofValue(map);
        assertEquals(JSONSchema.Type.Object, schema.getType());

        ObjectSchema os = (ObjectSchema) schema;
        assertTrue(os.getProperties().containsKey("name"));
        assertTrue(os.getProperties().containsKey("age"));
    }

    @Test
    public void testOfValueList() {
        List<String> list = List.of("a", "b", "c");
        JSONSchema schema = JSONSchema.ofValue(list);
        assertEquals(JSONSchema.Type.Array, schema.getType());
    }

    @Test
    public void testOfValueNull() {
        assertNull(JSONSchema.ofValue(null));
    }

    @Test
    public void testOfValueString() {
        JSONSchema schema = JSONSchema.ofValue("hello");
        assertEquals(JSONSchema.Type.String, schema.getType());
    }

    @Test
    public void testOfValueInteger() {
        JSONSchema schema = JSONSchema.ofValue(42);
        assertEquals(JSONSchema.Type.Integer, schema.getType());
    }

    // ==================== POJO Direct Validation ====================

    public static class Address {
        public String city;
        public String zip;
    }

    @Test
    public void testPojoDirectValidation() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "properties": {
                        "city": {"type": "string", "minLength": 1},
                        "zip": {"type": "string", "pattern": "^[0-9]{5}$"}
                    },
                    "required": ["city"]
                }
                """);

        Address addr = new Address();
        addr.city = "NYC";
        addr.zip = "10001";
        assertTrue(schema.isValid(addr));

        Address badAddr = new Address();
        badAddr.zip = "10001";
        // missing required "city" (null)
        assertFalse(schema.isValid(badAddr));

        Address badZip = new Address();
        badZip.city = "NYC";
        badZip.zip = "ABC";
        assertFalse(schema.isValid(badZip));
    }

    @Test
    public void testPojoWithInheritance() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "properties": {
                        "name": {"type": "string"},
                        "city": {"type": "string"}
                    },
                    "required": ["name"]
                }
                """);

        // Create anonymous subclass
        Address addr = new Address() {
            public String name = "John";
        };
        addr.city = "NYC";

        // The POJO has "name" in subclass + "city" in parent
        assertTrue(schema.isValid(addr));
    }

    // ==================== @JSONType(schema=) ====================

    @JSONType(schema = "{\"required\": [\"name\"], \"maxProperties\": 3}")
    public static class TypeSchemaBean {
        public String name;
        public int age;
        public String extra1;
        public String extra2;
    }

    @Test
    public void testJSONTypeSchemaValid() {
        TypeSchemaBean bean = JSON.parseObject("{\"name\":\"John\", \"age\":30}", TypeSchemaBean.class);
        assertEquals("John", bean.name);
        assertEquals(30, bean.age);
    }

    @Test
    public void testJSONTypeSchemaRejectsMissingRequired() {
        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{\"age\":30}", TypeSchemaBean.class));
    }

    @Test
    public void testJSONTypeSchemaTooManyProperties() {
        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{\"name\":\"J\",\"age\":1,\"extra1\":\"a\",\"extra2\":\"b\"}", TypeSchemaBean.class));
    }

    // ==================== No schema: should not interfere ====================

    public static class NoSchemaBean {
        public String name;
        public int age;
    }

    @Test
    public void testNoSchemaNoValidation() {
        // Negative age should pass because there's no schema
        NoSchemaBean bean = JSON.parseObject("{\"name\":\"\", \"age\":-999}", NoSchemaBean.class);
        assertEquals("", bean.name);
        assertEquals(-999, bean.age);
    }
}
