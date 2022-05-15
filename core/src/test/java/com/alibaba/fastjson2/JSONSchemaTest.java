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
        jsonSchema.validate((Object) null);

        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate("a123"));
    }

    @Test
    public void testString2() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "String", "required", true)
                .to(JSONSchema::of);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate((Object) null));
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
                .of("type", "String", "format", "email")
                .to(JSONSchema::of);

        jsonSchema.validate("abc@alibaba-inc.com");
        jsonSchema.validate("xxx@hotmail.com");
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate("(888)555-1212 ext. 532"));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate("(800)FLOWERS"));
    }

    @Test
    public void testString_format() {
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

        jsonSchema.validate((Object) null);
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

        jsonSchema.validate((Object) null);
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

        jsonSchema.validate((Integer) null);
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

        jsonSchema.validate((Long) null);
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

        jsonSchema.validate((Integer) null);
        jsonSchema.validate(new Object[0]);
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(1));
    }

    @Test
    public void testArray2() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Array", "maxItems", 3)
                .to(JSONSchema::of);
        jsonSchema.validate(new Object[0]);

        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(new Object[] {0, 1, 2, 3}));
    }

    @Test
    public void testArray3() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Array", "minItems", 3)
                .to(JSONSchema::of);
        jsonSchema.validate(new Object[]{0, 1, 2});
        jsonSchema.validate(new Object[]{0, 1, 2, 3});

        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(new Object[] {}));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(new Object[] {0}));
    }

    @Test
    public void testArray4() {
        JSONSchema jsonSchema = JSON.parseObject("{ \"type\": \"array\" }")
                .to(JSONSchema::of);
        jsonSchema.validate(JSON.parse("[1, 2, 3, 4, 5]"));
        jsonSchema.validate(JSON.parse("[3, \"different\", { \"types\" : \"of values\" }]"));
        JSON.parseArray("[1, 2, 3, 4, 5]").validate(jsonSchema);

        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(
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

        jsonSchema.validate(JSON.parse("[1, 2, 3, 4, 5]"));
        jsonSchema.validate(JSON.parse("[]"));

        assertThrows(JSONSchemaValidException.class, () ->
                jsonSchema.validate(
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

        jsonSchema.validate(JSON.parse("[1600, \"Pennsylvania\", \"Avenue\", \"NW\"]"));
        jsonSchema.validate(JSON.parse("[10, \"Downing\", \"Street\"]"));
        jsonSchema.validate(JSON.parse("[1600, \"Pennsylvania\", \"Avenue\", \"NW\", \"Washington\"]"));
        jsonSchema.validate(new Object[] {10, "Downing", "Street"});

        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(
                JSON.parse("[24, \"Sussex\", \"Drive\"]")
        ));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(
                JSON.parse("[\"Palais de l'Élysée\"]")
        ));

        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(
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

        jsonSchema.validate(JSON.parse("[1600, \"Pennsylvania\", \"Avenue\", \"NW\"]"));
        jsonSchema.validate(JSON.parse("[10, \"Downing\", \"Street\"]"));
        jsonSchema.validate(new Object[] {10, "Downing", "Street"});

        assertThrows(JSONSchemaValidException.class, ()
                -> jsonSchema.validate(
                        JSON.parse("[1600, \"Pennsylvania\", \"Avenue\", \"NW\", \"Washington\"]")
                )
        );
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(
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

        jsonSchema.validate(JSON.parse("[\"life\", \"universe\", \"everything\", 42]"));
        jsonSchema.validate(JSON.parse("[1, 2, 3, 4, 5]"));
        jsonSchema.validate(new Object[] {10, "Downing", "Street"});
        jsonSchema.validate(new Object[] {"Downing", "Street", 10});

        assertThrows(JSONSchemaValidException.class, ()
                        -> jsonSchema.validate(
                        JSON.parse("[\"life\", \"universe\", \"everything\", \"forty-two\"]")
                )
        );
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(
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

        jsonSchema.validate(JSON.parse("[\"apple\", \"orange\", 2, 4]"));
        jsonSchema.validate(JSON.parse("[\"apple\", \"orange\", 2, 4, 8]"));
        jsonSchema.validate(new Object[] {"apple", "orange", 2, 4});
        jsonSchema.validate(new Object[] {"apple", "orange", 2, 4, 8});

        assertThrows(JSONSchemaValidException.class, ()
                        -> jsonSchema.validate(
                        JSON.parse("[\"apple\", \"orange\", 2]")
                )
        );
        assertThrows(JSONSchemaValidException.class, ()
                        -> jsonSchema.validate(
                        JSON.parse("[\"apple\", \"orange\", 2, 4, 8, 16]")
                )
        );
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(
                new Object[] {"apple", "orange", 2}
        )); assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(
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

        jsonSchema.validate(JSON.parse("[1, 2, 3, 4, 5]"));
        jsonSchema.validate(JSON.parse("[]"));
        jsonSchema.validate(new Object[] {1, 2, 3, 4, 5});
        jsonSchema.validate(new Object[] {});
        jsonSchema.validate(new int[] {});
        jsonSchema.validate(new int[] {1, 2, 3, 4, 5});

        assertThrows(JSONSchemaValidException.class, ()
                        -> jsonSchema.validate(
                        JSON.parse("[1, 2, 3, 3, 4]")
                )
        );
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(
                new Object[] {1, 2, 3, 3, 4}
        ));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(
                new int[] {1, 2, 3, 3, 4}
        ));
    }

    @Test
    public void testObject1() {
        JSONSchema jsonSchema = JSONObject
                .of("type", "Object")
                .to(JSONSchema::of);

        jsonSchema.validate(JSONObject.of());
        jsonSchema.validate(new Bean());
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(new Object[] {}));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(1));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(1L));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate('A'));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(1F));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(1D));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(Byte.MIN_VALUE));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(Short.MIN_VALUE));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(BigDecimal.ZERO));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(BigInteger.ZERO));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(true));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(JSONSchema.Type.Object));
    }

    @Test
    public void testObject2() {
        JSONSchema jsonSchema = JSONObject
                .of(
                        "type", "Object",
                        "required", new String[] {"id"}
                )
                .to(JSONSchema::of);

        jsonSchema.validate(JSONObject.of("id", 101));
        jsonSchema.validate(JSONObject.of("id", 101).toJavaObject(Bean1.class));

        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(JSONObject.of()));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(JSONObject.of().toJavaObject(Bean1.class)));
        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(new Bean()));
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

        JSON.parseObject("{ \"number\": 1600, \"street_name\": \"Pennsylvania\", \"street_type\": \"Avenue\" }")
                .validate(jsonSchema);


        JSON.parseObject("{ \"number\": 1600, \"street_name\": \"Pennsylvania\" }")
                .validate(jsonSchema);

        JSON.parseObject("{}")
                .validate(jsonSchema);


        JSON.parseObject("{ \"number\": 1600, \"street_name\": \"Pennsylvania\", \"street_type\": \"Avenue\", \"direction\": \"NW\" }")
                .validate(jsonSchema);

        assertThrows(JSONSchemaValidException.class, () -> jsonSchema.validate(
                JSON.parseObject("{ \"number\": \"1600\", \"street_name\": \"Pennsylvania\", \"street_type\": \"Avenue\" }")
        ));
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

        JSON.parseObject("{}")
                .validate(jsonSchema);

        JSON.parseObject("{ \"S_25\": \"This is a string\" }")
                .validate(jsonSchema);

        JSON.parseObject("{ \"I_0\": 42 }")
                .validate(jsonSchema);

        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{ \"S_0\": 42 }")
                .validate(jsonSchema)
        );
        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{ \"I_42\": \"This is a string\" }")
                        .validate(jsonSchema)
        );

        JSON.parseObject("{ \"keyword\": \"value\" }")
                .validate(jsonSchema);
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

        JSON.parseObject("{ \"number\": 1600, \"street_name\": \"Pennsylvania\", \"street_type\": \"Avenue\" }")
                .validate(jsonSchema);
        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{ \"number\": 1600, \"street_name\": \"Pennsylvania\", \"street_type\": \"Avenue\", \"direction\": \"NW\" }")
                        .validate(jsonSchema)
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

        JSON.parseObject("{ \"number\": 1600, \"street_name\": \"Pennsylvania\", \"street_type\": \"Avenue\" }\n")
                .validate(jsonSchema);
        JSON.parseObject("{ \"number\": 1600, \"street_name\": \"Pennsylvania\", \"street_type\": \"Avenue\", \"direction\": \"NW\" }")
                .validate(jsonSchema);

        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{ \"number\": 1600, \"street_name\": \"Pennsylvania\", \"street_type\": \"Avenue\", \"office_number\": 201 }")
                        .validate(jsonSchema)
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

        JSON.parseObject("{\n" +
                        "  \"name\": \"William Shakespeare\",\n" +
                        "  \"email\": \"bill@stratford-upon-avon.co.uk\"\n" +
                        "}")
                .validate(jsonSchema);
        JSON.parseObject("{\n" +
                        "  \"name\": \"William Shakespeare\",\n" +
                        "  \"email\": \"bill@stratford-upon-avon.co.uk\",\n" +
                        "  \"address\": \"Henley Street, Stratford-upon-Avon, Warwickshire, England\",\n" +
                        "  \"authorship\": \"in question\"\n" +
                        "}")
                .validate(jsonSchema);

        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{\n" +
                                "  \"name\": \"William Shakespeare\",\n" +
                                "  \"address\": \"Henley Street, Stratford-upon-Avon, Warwickshire, England\",\n" +
                                "}")
                        .validate(jsonSchema)
        );
        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{\n" +
                                "  \"name\": \"William Shakespeare\",\n" +
                                "  \"address\": \"Henley Street, Stratford-upon-Avon, Warwickshire, England\",\n" +
                                "  \"email\": null\n" +
                                "}\n")
                        .validate(jsonSchema)
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

        JSON.parseObject("{\n" +
                        "  \"_a_proper_token_001\": \"value\"\n" +
                        "}")
                .validate(jsonSchema);
        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{\n" +
                                "  \"001 invalid\": \"value\"\n" +
                                "}")
                        .validate(jsonSchema)
        );
    }

    @Test
    public void testObject_size() {
        JSONSchema jsonSchema = JSON.parseObject("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"minProperties\": 2,\n" +
                "  \"maxProperties\": 3\n" +
                "}").to(JSONSchema::of);

        JSON.parseObject("{ \"a\": 0, \"b\": 1 }")
                .validate(jsonSchema);
        JSON.parseObject("{ \"a\": 0, \"b\": 1, \"c\": 2 }")
                .validate(jsonSchema);

        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{}")
                        .validate(jsonSchema)
        );
        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{ \"a\": 0 }")
                        .validate(jsonSchema)
        );
        assertThrows(JSONSchemaValidException.class, () ->
                JSON.parseObject("{ \"a\": 0, \"b\": 1, \"c\": 2, \"d\": 3 }")
                        .validate(jsonSchema)
        );
    }
}
