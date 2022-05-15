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
                        "price", 0
                )
        );

        assertThrows(
                JSONValidException.class,
                () -> schema.validate(JSONObject
                        .of(
                            "id", 1,
                            "name", ""
                        )
            )
        );

        assertThrows(
                JSONValidException.class,
                () -> schema.validate(JSONObject
                        .of(
                                "id", "1",
                                "name", "",
                                "price", 0
                        )
                )
        );

        assertThrows(
                JSONValidException.class,
                () -> schema.validate(JSONObject
                        .of(
                                "id", 1,
                                "name", 1,
                                "price", 0
                        )
                )
        );

        assertThrows(
                JSONValidException.class,
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

        assertThrows(JSONValidException.class, () -> jsonSchema.validate("a123"));
    }

    @Test
    public void testString2() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "required", true)
                .to(JSONSchema::of);
        assertThrows(JSONValidException.class, () -> jsonSchema.validate(null));
    }

    @Test
    public void testString3() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "minLength", 2)
                .to(JSONSchema::of);

        jsonSchema.validate("aa");
        assertThrows(JSONValidException.class, () -> jsonSchema.validate("a"));
    }

    @Test
    public void testString_pattern() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "pattern", "^(\\([0-9]{3}\\))?[0-9]{3}-[0-9]{4}$")
                .to(JSONSchema::of);

        jsonSchema.validate("555-1212");
        jsonSchema.validate("(888)555-1212");
        assertThrows(JSONValidException.class, () -> jsonSchema.validate("(888)555-1212 ext. 532"));
        assertThrows(JSONValidException.class, () -> jsonSchema.validate("(800)FLOWERS"));
    }

    @Test
    public void testInteger1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer")
                .to(JSONSchema::of);

        assertThrows(JSONValidException.class, () -> jsonSchema.validate("a"));
        assertThrows(JSONValidException.class, () -> jsonSchema.validate(1.1F));
        assertThrows(JSONValidException.class, () -> jsonSchema.validate(new BigDecimal("1.1")));

        jsonSchema.validate(null);
        jsonSchema.validate(1);
        jsonSchema.validate(Byte.MIN_VALUE);
        jsonSchema.validate(Short.MIN_VALUE);
        jsonSchema.validate(Integer.MIN_VALUE);
        jsonSchema.validate(Long.MIN_VALUE);
        jsonSchema.validate(BigInteger.ONE);
    }

    @Test
    public void testInteger2() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "minimum", 10)
                .to(JSONSchema::of);
        jsonSchema.validate(10);
        jsonSchema.validate(11);
        assertThrows(JSONValidException.class, () -> jsonSchema.validate(1));
    }

    @Test
    public void testInteger3() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "maximum", 10)
                .to(JSONSchema::of);

        jsonSchema.validate(9);
        jsonSchema.validate(10);
        assertThrows(JSONValidException.class, () -> jsonSchema.validate(11));
    }

    @Test
    public void testNumber1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number")
                .to(JSONSchema::of);

        assertThrows(JSONValidException.class, () -> jsonSchema.validate("a"));

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
        assertThrows(JSONValidException.class, () -> jsonSchema.validate(1));
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
        assertThrows(JSONValidException.class, () -> jsonSchema.validate(1));
        assertThrows(JSONValidException.class, () -> jsonSchema.validate(true));
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
        assertThrows(JSONValidException.class, () -> jsonSchema.validate(1));
    }

    @Test
    public void testArray2() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Array", "maxLength", 3)
                .to(JSONSchema::of);
        jsonSchema.validate(new Object[0]);

        assertThrows(JSONValidException.class, () -> jsonSchema.validate(new Object[] {0, 1, 2, 3}));
    }

    @Test
    public void testArray3() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Array", "minLength", 3)
                .to(JSONSchema::of);
        jsonSchema.validate(new Object[]{0, 1, 2});
        jsonSchema.validate(new Object[]{0, 1, 2, 3});

        assertThrows(JSONValidException.class, () -> jsonSchema.validate(new Object[] {}));
        assertThrows(JSONValidException.class, () -> jsonSchema.validate(new Object[] {0}));
    }
}
