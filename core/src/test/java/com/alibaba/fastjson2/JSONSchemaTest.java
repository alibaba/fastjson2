package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Set;
import java.util.UUID;

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

        assertTrue(schema
                .isValid(JSONObject.of(
                        "id", 1,
                        "name", "",
                        "price", 1)
                )
        );

        assertFalse(
                schema.isValid(JSONObject
                        .of(
                            "id", 1,
                            "name", ""
                        )
                )
        );

        assertFalse(schema.isValid(JSONObject
                        .of(
                                "id", "1",
                                "name", "",
                                "price", 0
                        )
                )
        );

        assertFalse(schema.isValid(JSONObject
                        .of(
                                "id", 1,
                                "name", 1,
                                "price", 0
                        )
                )
        );

        assertFalse(schema.isValid(JSONObject
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
        assertTrue(jsonSchema.isValid("aa"));
        assertTrue(jsonSchema.validate((Object) null).isSuccess());

        assertFalse(jsonSchema.validate("a123").isSuccess());
    }

    @Test
    public void testString2() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "required", true)
                .to(JSONSchema::of);
        assertFalse(jsonSchema.validate((Object) null).isSuccess());
    }

    @Test
    public void testString3() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "minLength", 2)
                .to(JSONSchema::of);

        assertTrue(jsonSchema.isValid("aa"));
        assertFalse(jsonSchema.isValid("a"));
    }

    @Test
    public void testString_format_email() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "format", "email")
                .to(JSONSchema::of);

        assertTrue(jsonSchema.isValid("abc@alibaba-inc.com"));
        assertTrue(jsonSchema.isValid("xxx@hotmail.com"));
        assertFalse(jsonSchema.isValid("(888)555-1212 ext. 532"));
        assertFalse(jsonSchema.isValid("(800)FLOWERS"));
    }

    @Test
    public void testString_format_date() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "format", "date")
                .to(JSONSchema::of);

        assertTrue(
                jsonSchema.isValid("2018-07-12"));
        assertTrue(
                jsonSchema.isValid("1970-11-13"));
        assertFalse(
                jsonSchema.isValid("1970-13-13"));
        assertFalse(
                jsonSchema.isValid("1970-02-31"));
        assertFalse(jsonSchema.isValid("(888)555-1212 ext. 532"));
        assertFalse(jsonSchema.isValid("(800)FLOWERS"));
    }

    @Test
    public void testString_format_datetime() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "format", "date-time")
                .to(JSONSchema::of);

        assertTrue(
                jsonSchema.isValid("2018-07-12 12:13:14"));
        assertTrue(
                jsonSchema.isValid("1970-11-13 12:13:14"));
        assertFalse(
                jsonSchema.isValid("1970-13-13 12:13:14"));
        assertFalse(
                jsonSchema.isValid("1970-02-31 12:13:14"));
        assertFalse(jsonSchema.isValid("(888)555-1212 ext. 532"));
        assertFalse(jsonSchema.isValid("(800)FLOWERS"));
    }

    @Test
    public void testString_format_time() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "format", "time")
                .to(JSONSchema::of);

        assertTrue(
                jsonSchema.isValid("12:13:14"));
        assertTrue(
                jsonSchema.isValid("12:13:14"));
        assertFalse(
                jsonSchema.isValid("25:13:14"));
        assertFalse(
                jsonSchema.isValid("1970-02-01 12:13:14"));
        assertFalse(jsonSchema.isValid("(888)555-1212 ext. 532"));
        assertFalse(jsonSchema.isValid("(800)FLOWERS"));
    }

    @Test
    public void testString_format_uuid() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "format", "uuid")
                .to(JSONSchema::of);

        assertTrue(
                jsonSchema.isValid("a7f41390-39a9-4ca6-a13b-88cf07a41108"));
        assertTrue(
                jsonSchema.isValid("A7F41390-39A9-4CA6-A13B-88CF07A41108"));
        assertTrue(
                jsonSchema.isValid("a7f4139039a94ca6a13b88cf07a41108"));
        assertTrue(
                jsonSchema.isValid("A7F4139039A94CA6A13B88CF07A41108"));
        assertFalse(
                jsonSchema.isValid("*7F4139039A94CA6A13B88CF07A41108"));
        assertFalse(
                jsonSchema.isValid("1970-02-01 12:13:14"));
        assertFalse(jsonSchema.isValid("(888)555-1212 ext. 532 (888)555-1212 ext. 532"));
        assertFalse(jsonSchema.isValid("(888)555-1212 ext. 532"));
        assertFalse(jsonSchema.isValid("(800)FLOWERS"));
    }

    @Test
    public void testString_format_url() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "format", "uri")
                .to(JSONSchema::of);

        assertTrue(
                jsonSchema.isValid("http://github.com/alibaba/fastjson"));
        assertFalse(
                jsonSchema.isValid("1970-02-01 12:13:14"));
        assertFalse(jsonSchema.isValid("(888)555-1212 ext. 532 (888)555-1212 ext. 532"));
        assertFalse(jsonSchema.isValid("(888)555-1212 ext. 532"));
    }

    @Test
    public void testString_pattern() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "pattern", "^(\\([0-9]{3}\\))?[0-9]{3}-[0-9]{4}$")
                .to(JSONSchema::of);

        assertTrue(jsonSchema.isValid("555-1212"));
        assertTrue(jsonSchema.isValid("(888)555-1212"));
        assertFalse(jsonSchema.isValid("(888)555-1212 ext. 532"));
        assertFalse(jsonSchema.isValid("(800)FLOWERS"));
    }

    @Test
    public void testInteger1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer")
                .to(JSONSchema::of);

        assertFalse(jsonSchema.isValid("a"));
        assertFalse(jsonSchema.isValid(1.1F));
        assertFalse(jsonSchema.isValid(new BigDecimal("1.1")));

        assertFalse(jsonSchema.isValid((Object) null));
        assertTrue(jsonSchema.isValid(1));
        assertTrue(jsonSchema.isValid(Byte.MIN_VALUE));
        assertTrue(jsonSchema.isValid(Short.MIN_VALUE));
        assertTrue(jsonSchema.isValid(Integer.MIN_VALUE));
        assertTrue(jsonSchema.isValid(Long.MIN_VALUE));
        assertTrue(jsonSchema.isValid(BigInteger.ONE));
    }

    @Test
    public void testInteger_minimum() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "minimum", 10)
                .to(JSONSchema::of);
        assertTrue(jsonSchema.isValid(10));
        assertTrue(jsonSchema.isValid(11));
        assertFalse(jsonSchema.isValid(1));
    }

    @Test
    public void testInteger_exclusiveMinimum() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "minimum", 10, "exclusiveMinimum", true)
                .to(JSONSchema::of);
        assertTrue(jsonSchema.isValid(11));
        assertFalse(jsonSchema.isValid(10));
        assertFalse(jsonSchema.isValid(9));
    }

    @Test
    public void testInteger_exclusiveMinimum1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "exclusiveMinimum", 10)
                .to(JSONSchema::of);
        assertTrue(jsonSchema.isValid(11));
        assertFalse(jsonSchema.isValid(10));
        assertFalse(jsonSchema.isValid(9));
    }

    @Test
    public void testInteger_maximum() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "maximum", 10)
                .to(JSONSchema::of);

        assertTrue(jsonSchema.isValid(9));
        assertTrue(jsonSchema.isValid(10));
        assertFalse(jsonSchema.isValid(11));
    }

    @Test
    public void testInteger_exclusiveMaximum() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "maximum", 10, "exclusiveMaximum", true)
                .to(JSONSchema::of);

        assertTrue(jsonSchema.isValid(9));
        assertFalse(jsonSchema.isValid(10));
        assertFalse(jsonSchema.isValid(11));
    }

    @Test
    public void testInteger_exclusiveMaximum1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "exclusiveMaximum", 10)
                .to(JSONSchema::of);

        assertTrue(jsonSchema.isValid(9));
        assertFalse(jsonSchema.isValid(10));
        assertFalse(jsonSchema.isValid(11));
    }

    @Test
    public void testInteger_range() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "minimum", 0, "exclusiveMaximum", 100)
                .to(JSONSchema::of);

        assertTrue(jsonSchema.isValid(0));
        assertTrue(jsonSchema.validate(10).isSuccess());
        assertTrue(jsonSchema.validate(99).isSuccess());
        assertFalse(jsonSchema.isValid(-1));
        assertFalse(jsonSchema.isValid(100));
        assertFalse(jsonSchema.isValid(101));
    }

    @Test
    public void testInteger_multipleOf() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Integer", "multipleOf", 10)
                .to(JSONSchema::of);

        assertTrue(jsonSchema.validate(0).isSuccess());
        assertTrue(jsonSchema.validate(10).isSuccess());
        assertFalse(jsonSchema.validate(-1).isSuccess());
        assertFalse(jsonSchema.validate(99).isSuccess());
        assertFalse(jsonSchema.validate(101).isSuccess());
        assertFalse(jsonSchema.validate(23).isSuccess());
    }

    @Test
    public void testNumber1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number")
                .to(JSONSchema::of);

        assertFalse(jsonSchema.isValid("a"));

        assertFalse(
                jsonSchema.isValid(
                        (Object) null));
        assertTrue(
                jsonSchema.isValid(
                        1));
        assertTrue(
                jsonSchema.isValid(
                        1.1F));
        assertTrue(
                jsonSchema.isValid(
                        1.1D));
        assertTrue(
                jsonSchema.isValid(
                        Byte.MIN_VALUE));
        assertTrue(
                jsonSchema.isValid(
                        Short.MIN_VALUE));
        assertTrue(
                jsonSchema.isValid(
                        Integer.MIN_VALUE));
        assertTrue(
                jsonSchema.isValid(
                        Long.MIN_VALUE));
        assertTrue(
                jsonSchema.isValid(
                        BigInteger.ONE));
    }

    @Test
    public void testNumber_minimum() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number", "minimum", 10)
                .to(JSONSchema::of);
        assertTrue(jsonSchema.isValid(10));
        assertTrue(jsonSchema.isValid(11));
        assertFalse(jsonSchema.isValid(1));
    }

    @Test
    public void testNumber_exclusiveMinimum() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number", "minimum", 10, "exclusiveMinimum", true)
                .to(JSONSchema::of);
        assertTrue(jsonSchema.isValid(11));
        assertFalse(jsonSchema.isValid(1));
        assertFalse(jsonSchema.isValid(9));
        assertFalse(jsonSchema.isValid(10));
    }

    @Test
    public void testNumber_exclusiveMinimum1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number", "exclusiveMinimum", 10)
                .to(JSONSchema::of);
        assertTrue(jsonSchema.isValid(11));
        assertFalse(jsonSchema.isValid(1));
        assertFalse(jsonSchema.isValid(9));
        assertFalse(jsonSchema.isValid(10));
    }

    @Test
    public void testNumber_maximum() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number", "maximum", 10)
                .to(JSONSchema::of);

        assertTrue(
                jsonSchema.isValid(
                        9));
        assertTrue(
                jsonSchema.isValid(
                        10));
        assertFalse(jsonSchema.isValid(11));
    }

    @Test
    public void testNumber_exclusiveMaximum() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number", "maximum", 10, "exclusiveMaximum", true)
                .to(JSONSchema::of);

        assertTrue(
                jsonSchema.isValid(
                        9));
        assertFalse(jsonSchema.isValid(10));
        assertFalse(jsonSchema.isValid(11));
    }

    @Test
    public void testNumber_exclusiveMaximum1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number", "exclusiveMaximum", 10)
                .to(JSONSchema::of);

        assertTrue(jsonSchema.isValid(9));
        assertFalse(jsonSchema.isValid(10));
        assertFalse(jsonSchema.isValid(11));
    }

    @Test
    public void testNumber_range() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number", "minimum", 0, "exclusiveMaximum", 100)
                .to(JSONSchema::of);

        assertTrue(jsonSchema.isValid(0));
        assertTrue(jsonSchema.isValid(10));
        assertTrue(jsonSchema.isValid(99));
        assertFalse(jsonSchema.isValid(-1));
        assertFalse(jsonSchema.isValid(100));
        assertFalse(jsonSchema.isValid(101));
    }

    @Test
    public void testNumber_multipleOf() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Number", "multipleOf", 10)
                .to(JSONSchema::of);

        assertTrue(jsonSchema.isValid(0));
        assertTrue(jsonSchema.isValid(10));
        assertFalse(jsonSchema.isValid(-1));
        assertFalse(jsonSchema.isValid(99));
        assertFalse(jsonSchema.isValid(101));
        assertFalse(jsonSchema.isValid(23));
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

        assertFalse(jsonSchema.isValid((Integer) null));
        assertTrue(jsonSchema.isValid(true));
        assertTrue(jsonSchema.isValid(false));
        assertFalse(jsonSchema.isValid(1));
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

        assertTrue(jsonSchema.isValid((Long) null));
        assertFalse(jsonSchema.isValid(1));
        assertFalse(jsonSchema.isValid(true));
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

        assertFalse(
                jsonSchema.isValid((Integer) null));
        assertTrue(
                jsonSchema.isValid(new Object[0]));
        assertFalse(
                jsonSchema.isValid(1));
    }

    @Test
    public void testArray2() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Array", "maxItems", 3)
                .to(JSONSchema::of);
        assertTrue(jsonSchema.isValid(new Object[0]));

        assertFalse(jsonSchema.isValid(new Object[] {0, 1, 2, 3}));
    }

    @Test
    public void testArray3() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Array", "minItems", 3)
                .to(JSONSchema::of);
        assertTrue(jsonSchema.isValid(new Object[]{0, 1, 2}));
        assertTrue(jsonSchema.isValid(new Object[]{0, 1, 2, 3}));

        assertFalse(jsonSchema.isValid(new Object[] {}));
        assertFalse(jsonSchema.isValid(new Object[] {0}));
    }

    @Test
    public void testArray4() {
        JSONSchema jsonSchema = JSON.parseObject("{ \"type\": \"array\" }")
                .to(JSONSchema::of);
        assertTrue(jsonSchema.isValid(JSON.parse("[1, 2, 3, 4, 5]")));
        assertTrue(jsonSchema.isValid(JSON.parse("[3, \"different\", { \"types\" : \"of values\" }]")));
        assertTrue(JSON.parseArray("[1, 2, 3, 4, 5]").isValid(jsonSchema));

        assertFalse(jsonSchema.isValid(
                JSON.parse("{\"Not\": \"an array\"}")
        ));
    }

    @Test
    public void testArray_items() {
        JSONSchema jsonSchema = JSON.parseObject("{\n" +
                        "  \"type\": \"array\",\n" +
                        "  \"items\": {\n" +
                        "    \"type\": \"number\"\n" +
                        "  },\n" +
                        "}")
                .to(JSONSchema::of);

        assertTrue(jsonSchema.isValid(JSON.parse("[1, 2, 3, 4, 5]")));
        assertTrue(jsonSchema.isValid(JSON.parse("[]")));

        assertFalse(
                jsonSchema.isValid(
                        JSON.parse("[1, 2, \"3\", 4, 5]")
        ));
    }

    @Test
    public void testArray_tuple() {
        JSONSchema jsonSchema = JSON.parseObject("{\n" +
                        "  \"type\": \"array\",\n" +
                        "  \"prefixItems\": [\n" +
                        "    { \"type\": \"number\" },\n" +
                        "    { \"type\": \"string\" },\n" +
                        "    { \"enum\": [\"Street\", \"Avenue\", \"Boulevard\"] },\n" +
                        "    { \"enum\": [\"NW\", \"NE\", \"SW\", \"SE\"] }\n" +
                        "  ]\n" +
                        "}")
                .to(JSONSchema::of);

        assertTrue(
                jsonSchema.isValid(
                        JSON.parse("[1600, \"Pennsylvania\", \"Avenue\", \"NW\"]")));
        assertTrue(
                jsonSchema.isValid(
                        JSON.parse("[10, \"Downing\", \"Street\"]"))
        );
        assertTrue(
                jsonSchema.isValid(
                        JSON.parse("[1600, \"Pennsylvania\", \"Avenue\", \"NW\", \"Washington\"]"))
        );
        assertTrue(
                jsonSchema.isValid(new Object[] {10, "Downing", "Street"})
        );

        assertFalse(
                jsonSchema.isValid(
                JSON.parse("[24, \"Sussex\", \"Drive\"]")
        ));
        assertFalse(
                jsonSchema.isValid(
                        JSON.parse("[\"Palais de l'Élysée\"]")
        ));

        assertFalse(
                jsonSchema.isValid(
                        new String[] {"Palais de l'Élysée"}
        ));
    }

    @Test
    public void testArray_tuple_items() {
        JSONSchema jsonSchema = JSON.parseObject("{\n" +
                        "  \"type\": \"array\",\n" +
                        "  \"prefixItems\": [\n" +
                        "    { \"type\": \"number\" },\n" +
                        "    { \"type\": \"string\" },\n" +
                        "    { \"enum\": [\"Street\", \"Avenue\", \"Boulevard\"] },\n" +
                        "    { \"enum\": [\"NW\", \"NE\", \"SW\", \"SE\"] }\n" +
                        "  ],\n" +
                        "  \"items\": false\n" +
                        "}")
                .to(JSONSchema::of);

        assertTrue(
                jsonSchema.isValid(
                        JSON.parse("[1600, \"Pennsylvania\", \"Avenue\", \"NW\"]")));
        assertTrue(
                jsonSchema.isValid(
                        JSON.parse("[10, \"Downing\", \"Street\"]")));
        assertTrue(
                jsonSchema.isValid(
                        new Object[] {10, "Downing", "Street"}));

        assertFalse(jsonSchema.isValid(
                        JSON.parse("[1600, \"Pennsylvania\", \"Avenue\", \"NW\", \"Washington\"]")
                )
        );
        assertFalse(jsonSchema.isValid(
                new Object[] {1600, "Pennsylvania", "Avenue", "NW", "Washington"}
        ));
    }

    @Test
    public void testArray_contains() {
        JSONSchema jsonSchema = JSON.parseObject("{\n" +
                        "   \"type\": \"array\",\n" +
                        "   \"contains\": {\n" +
                        "     \"type\": \"number\"\n" +
                        "   }\n" +
                        "}")
                .to(JSONSchema::of);

        assertTrue(
                jsonSchema.isValid(
                        JSON.parse("[\"life\", \"universe\", \"everything\", 42]")));
        assertTrue(
                jsonSchema.isValid(
                        JSON.parse("[1, 2, 3, 4, 5]")));
        assertTrue(
                jsonSchema.isValid(
                        new Object[] {10, "Downing", "Street"}));
        assertTrue(
                jsonSchema.isValid(
                        new Object[] {"Downing", "Street", 10}));

        assertFalse(
                jsonSchema.isValid(
                        JSON.parse("[\"life\", \"universe\", \"everything\", \"forty-two\"]")
                )
        );
        assertFalse(jsonSchema.isValid(
                new Object[] {"life", "universe", "everything", "forty-two"}
        ));
    }

    @Test
    public void testArray_contains_range() {
        JSONSchema jsonSchema = JSON.parseObject("{\n" +
                        "  \"type\": \"array\",\n" +
                        "  \"contains\": {\n" +
                        "    \"type\": \"number\"\n" +
                        "  },\n" +
                        "  \"minContains\": 2,\n" +
                        "  \"maxContains\": 3\n" +
                        "}")
                .to(JSONSchema::of);

        assertTrue(
                jsonSchema.isValid(
                        JSON.parse("[\"apple\", \"orange\", 2, 4]")));
        assertTrue(
                jsonSchema.isValid(
                        JSON.parse("[\"apple\", \"orange\", 2, 4, 8]")));
        assertTrue(
                jsonSchema.isValid(
                        new Object[] {"apple", "orange", 2, 4}));
        assertTrue(
                jsonSchema.isValid(
                        new Object[] {"apple", "orange", 2, 4, 8}));

        assertFalse(jsonSchema.isValid(
                        JSON.parse("[\"apple\", \"orange\", 2]")
                )
        );
        assertFalse(jsonSchema.isValid(
                        JSON.parse("[\"apple\", \"orange\", 2, 4, 8, 16]")
                )
        );
        assertFalse(jsonSchema.isValid(
                new Object[] {"apple", "orange", 2}
        ));
        assertFalse(jsonSchema.isValid(
                new Object[] {"apple", "orange", 2, 4, 8, 16}
        ));
    }

    @Test
    public void testArray_tuple_unique() {
        JSONSchema jsonSchema = JSON.parseObject("{\n" +
                        "  \"type\": \"array\",\n" +
                        "  \"uniqueItems\": true\n" +
                        "}")
                .to(JSONSchema::of);

        assertTrue(
                jsonSchema.isValid(
                        JSON.parse("[1, 2, 3, 4, 5]")));
        assertTrue(
                jsonSchema.isValid(
                        JSON.parse("[]")));
        assertTrue(
                jsonSchema.isValid(
                        new Object[] {1, 2, 3, 4, 5}));
        assertTrue(
                jsonSchema.isValid(
                        new Object[] {}));
        assertTrue(
                jsonSchema.isValid(
                        new int[] {}));
        assertTrue(
                jsonSchema.isValid(
                        new int[] {1, 2, 3, 4, 5}));

        assertFalse(jsonSchema.isValid(
                        JSON.parse("[1, 2, 3, 3, 4]")
                )
        );
        assertFalse(jsonSchema.isValid(
                new Object[] {1, 2, 3, 3, 4}
        ));
        assertFalse(jsonSchema.isValid(
                new int[] {1, 2, 3, 3, 4}
        ));
    }

    @Test
    public void testObject1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Object")
                .to(JSONSchema::of);

        assertTrue(jsonSchema.isValid(JSONObject.of()));
        assertTrue(jsonSchema.isValid(new Bean()));
        assertFalse(jsonSchema.isValid(new Object[] {}));
        assertFalse(jsonSchema.isValid(1));
        assertFalse(jsonSchema.isValid(1L));
        assertFalse(jsonSchema.isValid('A'));
        assertFalse(jsonSchema.isValid(1F));
        assertFalse(jsonSchema.isValid(1D));
        assertFalse(jsonSchema.isValid(Byte.MIN_VALUE));
        assertFalse(jsonSchema.isValid(Short.MIN_VALUE));
        assertFalse(jsonSchema.isValid(BigDecimal.ZERO));
        assertFalse(jsonSchema.isValid(BigInteger.ZERO));
        assertFalse(jsonSchema.isValid(true));
        assertFalse(jsonSchema.isValid(JSONSchema.Type.Object));
    }

    @Test
    public void testObject2() {
        JSONSchema jsonSchema = JSONObject
                .of(
                        "type", "Object",
                        "required", new String[] {"id"}
                )
                .to(JSONSchema::of);

        assertTrue(jsonSchema.validate(JSONObject.of("id", 101)).isSuccess());
        assertTrue(jsonSchema.validate(JSONObject.of("id", 101).toJavaObject(Bean1.class)).isSuccess());

        assertFalse(jsonSchema.validate(JSONObject.of()).isSuccess());
        assertFalse(jsonSchema.validate(JSONObject.of().toJavaObject(Bean1.class)).isSuccess());
        assertFalse(jsonSchema.validate(new Bean()).isSuccess());
    }

    public static class Bean {
        public Integer id;
    }

    public static class Bean1 {
        public Integer id;
    }

    @Test
    public void testObject3() {
        JSONSchema jsonSchema = JSONObject
                .of(
                        "type", "Object",
                        "properties", JSONObject.of(
                                "number", JSONObject.of("type", "number"),
                                "street_name", JSONObject.of("type", "string"),
                                "street_type", JSONObject.of("enum", JSONArray.of("Street", "Avenue", "Boulevard"))
                        )
                )
                .to(JSONSchema::of);

        assertTrue(JSON
                .parseObject("{ \"number\": 1600, \"street_name\": \"Pennsylvania\", \"street_type\": \"Avenue\" }")
                .isValid(jsonSchema)
        );

        assertTrue(JSON
                .parseObject("{ \"number\": 1600, \"street_name\": \"Pennsylvania\" }")
                .isValid(jsonSchema)
        );

        assertTrue(JSON
                .parseObject("{}")
                .isValid(jsonSchema)
        );


        assertTrue(JSON
                .parseObject("{ \"number\": 1600, \"street_name\": \"Pennsylvania\", \"street_type\": \"Avenue\", \"direction\": \"NW\" }")
                .isValid(jsonSchema));

        assertFalse(jsonSchema.validate(
                JSON.parseObject("{ \"number\": \"1600\", \"street_name\": \"Pennsylvania\", \"street_type\": \"Avenue\" }")
        ).isSuccess());
    }

    @Test
    public void testObject_patternProperties() {
        JSONSchema jsonSchema = JSON.parseObject("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"patternProperties\": {\n" +
                "    \"^S_\": { \"type\": \"string\" },\n" +
                "    \"^I_\": { \"type\": \"integer\" }\n" +
                "  }\n" +
                "}").to(JSONSchema::of);

        assertTrue(JSON
                .parseObject("{}")
                .isValid(jsonSchema));

        assertTrue(JSON
                .parseObject("{ \"S_25\": \"This is a string\" }")
                .isValid(jsonSchema));

        assertTrue(JSON
                .parseObject("{ \"I_0\": 42 }")
                .isValid(jsonSchema));

        assertFalse(JSON
                .parseObject("{ \"S_0\": 42 }")
                .isValid(jsonSchema)
        );
        assertFalse(JSON
                .parseObject("{ \"I_42\": \"This is a string\" }")
                .isValid(jsonSchema)
        );

        assertTrue(JSON.parseObject("{ \"keyword\": \"value\" }")
                .isValid(jsonSchema));
    }

    @Test
    public void testObject_additionalProperties() {
        JSONSchema jsonSchema = JSON.parseObject("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"number\": { \"type\": \"number\" },\n" +
                "    \"street_name\": { \"type\": \"string\" },\n" +
                "    \"street_type\": { \"enum\": [\"Street\", \"Avenue\", \"Boulevard\"] }\n" +
                "  },\n" +
                "  \"additionalProperties\": false\n" +
                "}").to(JSONSchema::of);

        assertTrue(JSON.parseObject("{ \"number\": 1600, \"street_name\": \"Pennsylvania\", \"street_type\": \"Avenue\" }")
                .isValid(jsonSchema));
        assertFalse(
                JSON.parseObject("{ \"number\": 1600, \"street_name\": \"Pennsylvania\", \"street_type\": \"Avenue\", \"direction\": \"NW\" }")
                        .isValid(jsonSchema)
        );
    }

    @Test
    public void testObject_additionalProperties1() {
        JSONSchema jsonSchema = JSON.parseObject("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"number\": { \"type\": \"number\" },\n" +
                "    \"street_name\": { \"type\": \"string\" },\n" +
                "    \"street_type\": { \"enum\": [\"Street\", \"Avenue\", \"Boulevard\"] }\n" +
                "  },\n" +
                "  \"additionalProperties\": { \"type\": \"string\" }\n" +
                "}").to(JSONSchema::of);

        assertTrue(JSON.parseObject("{ \"number\": 1600, \"street_name\": \"Pennsylvania\", \"street_type\": \"Avenue\" }\n")
                .isValid(jsonSchema));
        assertTrue(JSON.parseObject("{ \"number\": 1600, \"street_name\": \"Pennsylvania\", \"street_type\": \"Avenue\", \"direction\": \"NW\" }")
                .isValid(jsonSchema));

        assertFalse(
                JSON.parseObject("{ \"number\": 1600, \"street_name\": \"Pennsylvania\", \"street_type\": \"Avenue\", \"office_number\": 201 }")
                        .isValid(jsonSchema)
        );
    }

    @Test
    public void testObject_required1() {
        JSONSchema jsonSchema = JSON.parseObject("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"name\": { \"type\": \"string\" },\n" +
                "    \"email\": { \"type\": \"string\" },\n" +
                "    \"address\": { \"type\": \"string\" },\n" +
                "    \"telephone\": { \"type\": \"string\" }\n" +
                "  },\n" +
                "  \"required\": [\"name\", \"email\"]\n" +
                "}").to(JSONSchema::of);

        assertTrue(JSON.parseObject("{\n" +
                        "  \"name\": \"William Shakespeare\",\n" +
                        "  \"email\": \"bill@stratford-upon-avon.co.uk\"\n" +
                        "}")
                .isValid(jsonSchema));
        assertTrue(JSON.parseObject("{\n" +
                        "  \"name\": \"William Shakespeare\",\n" +
                        "  \"email\": \"bill@stratford-upon-avon.co.uk\",\n" +
                        "  \"address\": \"Henley Street, Stratford-upon-Avon, Warwickshire, England\",\n" +
                        "  \"authorship\": \"in question\"\n" +
                        "}")
                .isValid(jsonSchema));

        assertFalse(
                JSON.parseObject("{\n" +
                                "  \"name\": \"William Shakespeare\",\n" +
                                "  \"address\": \"Henley Street, Stratford-upon-Avon, Warwickshire, England\",\n" +
                                "}")
                        .isValid(jsonSchema)
        );
        assertFalse(
                JSON.parseObject("{\n" +
                                "  \"name\": \"William Shakespeare\",\n" +
                                "  \"address\": \"Henley Street, Stratford-upon-Avon, Warwickshire, England\",\n" +
                                "  \"email\": null\n" +
                                "}\n")
                        .isValid(jsonSchema)
        );
    }

    @Test
    public void testObject_propertyNamesPattern() {
        JSONSchema jsonSchema = JSON.parseObject("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"propertyNames\": {\n" +
                "    \"pattern\": \"^[A-Za-z_][A-Za-z0-9_]*$\"\n" +
                "  }\n" +
                "}").to(JSONSchema::of);

        assertTrue(JSON.parseObject("{\n" +
                        "  \"_a_proper_token_001\": \"value\"\n" +
                        "}")
                .isValid(jsonSchema));
        assertFalse(
                JSON.parseObject("{\n" +
                                "  \"001 invalid\": \"value\"\n" +
                                "}")
                        .isValid(jsonSchema)
        );
    }

    @Test
    public void testObject_size() {
        JSONSchema jsonSchema = JSON.parseObject("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"minProperties\": 2,\n" +
                "  \"maxProperties\": 3\n" +
                "}").to(JSONSchema::of);

        assertTrue(JSON.parseObject("{ \"a\": 0, \"b\": 1 }")
                .isValid(jsonSchema));
        assertTrue(JSON.parseObject("{ \"a\": 0, \"b\": 1, \"c\": 2 }")
                .isValid(jsonSchema));

        assertFalse(JSON.parseObject("{}")
                        .isValid(jsonSchema)
        );
        assertFalse(JSON.parseObject("{ \"a\": 0 }")
                        .isValid(jsonSchema)
        );
        assertFalse(JSON.parseObject("{ \"a\": 0, \"b\": 1, \"c\": 2, \"d\": 3 }")
                        .isValid(jsonSchema)
        );
    }

    @Test
    public void testConstant() {
        JSONSchema jsonSchema = JSON.parseObject("{\n" +
                "  \"properties\": {\n" +
                "    \"country\": {\n" +
                "      \"const\": \"United States of America\"\n" +
                "    }\n" +
                "  }\n" +
                "}").to(JSONSchema::of);

        assertTrue(JSON.parseObject("{ \"country\": \"United States of America\" }")
                .isValid(jsonSchema));

        assertFalse(JSON.parseObject("{ \"country\": \"Canada\" }")
                        .isValid(jsonSchema)
        );
    }
}
