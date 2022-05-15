package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class JSONSchemaTest {
    @Test
    public void test0() {
        URL url = JSONSchemaTest.class.getClassLoader().getResource("schema/schema_01.json");
        JSONObject object = JSON.parseObject(url, JSONObject.class);

        JSONSchema.ObjectSchema schema = (JSONSchema.ObjectSchema) JSONSchema.of(object);
        JSONSchema.ObjectSchema schema1 = (JSONSchema.ObjectSchema) JSON.parseObject(url, JSONSchema::of);
        assertEquals(schema.hashCode(), schema1.hashCode());
        assertEquals(schema, schema1);

        assertEquals("Product", schema.getTitle());
        assertEquals("A product from Acme's catalog", schema.getDescription());
        assertEquals(JSONSchema.Type.Object, schema.getType());

        assertEquals(3, schema.getProperties().size());

        JSONSchema propertyId = schema.getProperty("id");
        assertEquals("The unique identifier for a product", propertyId.getDescription());
        assertEquals(JSONSchema.Type.Integer, propertyId.getType());

        JSONSchema propertyName = schema.getProperty("name");
        assertEquals("Name of the product", propertyName.getDescription());
        assertEquals(JSONSchema.Type.String, propertyName.getType());

        JSONSchema propertyPrice = schema.getProperty("price");
        assertNull(propertyPrice.getDescription());
        assertEquals(JSONSchema.Type.Number, propertyPrice.getType());

        Set<String> required = schema.getRequired();
        assertEquals(3, required.size());
        assertTrue(required.contains("id"));
        assertTrue(required.contains("name"));
        assertTrue(required.contains("price"));

        schema.validate(JSONObject
                .of(
                        "id", 1,
                        "name", "",
                        "price", 1
                )
        );

        assertThrows(
                JSONSchemaValidException.class,
                () -> schema.validate(JSONObject
                        .of(
                            "id", 1,
                            "name", ""
                        )
            )
        );

        assertThrows(
                JSONSchemaValidException.class,
                () -> schema.validate(JSONObject
                        .of(
                                "id", "1",
                                "name", "",
                                "price", 0
                        )
                )
        );

        assertThrows(
                JSONSchemaValidException.class,
                () -> schema.validate(JSONObject
                        .of(
                                "id", 1,
                                "name", 1,
                                "price", 0
                        )
                )
        );

        assertThrows(
                JSONSchemaValidException.class,
                () -> schema.validate(JSONObject
                        .of(
                                "id", 1,
                                "name", "",
                                "price", "x"
                        )
                )
        );
    }

    @Test
    public void testString1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "maxLength", 3)
                .to(JSONSchema::of);
        jsonSchema.validate("aa");
        jsonSchema.validate(null);

        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate("a123"));
    }

    @Test
    public void testString2() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "required", true)
                .to(JSONSchema::of);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(null));
    }

    @Test
    public void testString3() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "minLength", 2)
                .to(JSONSchema::of);

        jsonSchema.validate("aa");
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate("a"));
    }

    @Test
    public void testString_pattern() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "pattern", "^(\\([0-9]{3}\\))?[0-9]{3}-[0-9]{4}$")
                .to(JSONSchema::of);

        jsonSchema.validate("555-1212");
        jsonSchema.validate("(888)555-1212");
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate("(888)555-1212 ext. 532"));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate("(800)FLOWERS"));
    }

    @Test
    public void testInteger1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer")
                .to(JSONSchema::of);

        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate("a"));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(1.1F));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(new BigDecimal("1.1")));

        jsonSchema.validate(null);
        jsonSchema.validate(1);
        jsonSchema.validate(Byte.MIN_VALUE);
        jsonSchema.validate(Short.MIN_VALUE);
        jsonSchema.validate(Integer.MIN_VALUE);
        jsonSchema.validate(Long.MIN_VALUE);
        jsonSchema.validate(BigInteger.ONE);
    }

    @Test
    public void testInteger_minimum() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "minimum", 10)
                .to(JSONSchema::of);
        jsonSchema.validate(10);
        jsonSchema.validate(11);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(1));
    }

    @Test
    public void testInteger_exclusiveMinimum() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "minimum", 10, "exclusiveMinimum", true)
                .to(JSONSchema::of);
        jsonSchema.validate(11);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(10));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(9));
    }

    @Test
    public void testInteger_exclusiveMinimum1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "exclusiveMinimum", 10)
                .to(JSONSchema::of);
        jsonSchema.validate(11);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(10));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(9));
    }

    @Test
    public void testInteger_maximum() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "maximum", 10)
                .to(JSONSchema::of);

        jsonSchema.validate(9);
        jsonSchema.validate(10);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(11));
    }

    @Test
    public void testInteger_exclusiveMaximum() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "maximum", 10, "exclusiveMaximum", true)
                .to(JSONSchema::of);

        jsonSchema.validate(9);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(10));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(11));
    }

    @Test
    public void testInteger_exclusiveMaximum1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "exclusiveMaximum", 10)
                .to(JSONSchema::of);

        jsonSchema.validate(9);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(10));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(11));
    }

    @Test
    public void testInteger_range() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "minimum", 0, "exclusiveMaximum", 100)
                .to(JSONSchema::of);

        jsonSchema.validate(0);
        jsonSchema.validate(10);
        jsonSchema.validate(99);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(-1));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(100));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(101));
    }

    @Test
    public void testInteger_multipleOf() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "multipleOf", 10)
                .to(JSONSchema::of);

        jsonSchema.validate(0);
        jsonSchema.validate(10);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(-1));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(99));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(101));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(23));
    }

    @Test
    public void testNumber1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number")
                .to(JSONSchema::of);

        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate("a"));

        jsonSchema.validate(null);
        jsonSchema.validate(1);
        jsonSchema.validate(1.1F);
        jsonSchema.validate(1.1D);
        jsonSchema.validate(Byte.MIN_VALUE);
        jsonSchema.validate(Short.MIN_VALUE);
        jsonSchema.validate(Integer.MIN_VALUE);
        jsonSchema.validate(Long.MIN_VALUE);
        jsonSchema.validate(BigInteger.ONE);
    }

    @Test
    public void testNumber_minimum() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number", "minimum", 10)
                .to(JSONSchema::of);
        jsonSchema.validate(10);
        jsonSchema.validate(11);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(1));
    }

    @Test
    public void testNumber_exclusiveMinimum() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number", "minimum", 10, "exclusiveMinimum", true)
                .to(JSONSchema::of);
        jsonSchema.validate(11);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(1));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(9));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(10));
    }

    @Test
    public void testNumber_exclusiveMinimum1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number", "exclusiveMinimum", 10)
                .to(JSONSchema::of);
        jsonSchema.validate(11);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(1));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(9));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(10));
    }

    @Test
    public void testNumber_maximum() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number", "maximum", 10)
                .to(JSONSchema::of);

        jsonSchema.validate(9);
        jsonSchema.validate(10);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(11));
    }

    @Test
    public void testNumber_exclusiveMaximum() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number", "maximum", 10, "exclusiveMaximum", true)
                .to(JSONSchema::of);

        jsonSchema.validate(9);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(10));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(11));
    }

    @Test
    public void testNumber_exclusiveMaximum1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number", "exclusiveMaximum", 10)
                .to(JSONSchema::of);

        jsonSchema.validate(9);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(10));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(11));
    }

    @Test
    public void testNumber_range() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number", "minimum", 0, "exclusiveMaximum", 100)
                .to(JSONSchema::of);

        jsonSchema.validate(0);
        jsonSchema.validate(10);
        jsonSchema.validate(99);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(-1));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(100));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(101));
    }

    @Test
    public void testNumber_multipleOf() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number", "multipleOf", 10)
                .to(JSONSchema::of);

        jsonSchema.validate(0);
        jsonSchema.validate(10);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(-1));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(99));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(101));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(23));
    }

    @Test
    public void testBoolean1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Boolean")
                .to(JSONSchema::of);
        JSONSchema jsonSchema1 = JSONObject
                .of("type", "Boolean")
                .to(JSONSchema::of);
        assertEquals(jsonSchema.hashCode(), jsonSchema1.hashCode());
        assertEquals(jsonSchema, jsonSchema1);
        assertEquals(jsonSchema.getType(), jsonSchema1.getType());

        jsonSchema.validate(null);
        jsonSchema.validate(true);
        jsonSchema.validate(false);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(1));
    }

    @Test
    public void testNull1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Null")
                .to(JSONSchema::of);
        JSONSchema jsonSchema1 = JSONObject
                .of("type", "Null")
                .to(JSONSchema::of);
        assertEquals(jsonSchema.hashCode(), jsonSchema1.hashCode());
        assertEquals(jsonSchema, jsonSchema1);
        assertEquals(jsonSchema.getType(), jsonSchema1.getType());

        jsonSchema.validate(null);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(1));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(true));
    }

    @Test
    public void testArray1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Array")
                .to(JSONSchema::of);
        JSONSchema jsonSchema1 = JSONObject
                .of("type", "Array")
                .to(JSONSchema::of);
        assertEquals(jsonSchema.hashCode(), jsonSchema1.hashCode());
        assertEquals(jsonSchema, jsonSchema1);
        assertEquals(jsonSchema.getType(), jsonSchema1.getType());

        jsonSchema.validate(null);
        jsonSchema.validate(new Object[0]);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(1));
    }

    @Test
    public void testArray2() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Array", "maxLength", 3)
                .to(JSONSchema::of);
        jsonSchema.validate(new Object[0]);

        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(new Object[] {0, 1, 2, 3}));
    }

    @Test
    public void testArray3() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Array", "minLength", 3)
                .to(JSONSchema::of);
        jsonSchema.validate(new Object[]{0, 1, 2});
        jsonSchema.validate(new Object[]{0, 1, 2, 3});

        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(new Object[] {}));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(new Object[] {0}));
    }
}
