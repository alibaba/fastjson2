package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.JSONSchemaValidException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JSONSchemaTest {
    // ==================== Type Validation ====================

    @Test
    public void testStringType() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\"}");
        assertEquals(JSONSchema.Type.String, schema.getType());
        assertTrue(schema.isValid("hello"));
        assertTrue(schema.isValid(""));
        assertFalse(schema.isValid(123));
        assertFalse(schema.isValid(true));
        assertFalse(schema.isValid(null));
    }

    @Test
    public void testIntegerType() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"integer\"}");
        assertEquals(JSONSchema.Type.Integer, schema.getType());
        assertTrue(schema.isValid(42));
        assertTrue(schema.isValid(0));
        assertTrue(schema.isValid(-1));
        assertTrue(schema.isValid(42L));
        assertFalse(schema.isValid(3.14));
        assertFalse(schema.isValid("hello"));
        assertFalse(schema.isValid(null));
    }

    @Test
    public void testNumberType() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"number\"}");
        assertEquals(JSONSchema.Type.Number, schema.getType());
        assertTrue(schema.isValid(42));
        assertTrue(schema.isValid(3.14));
        assertTrue(schema.isValid(-1.5));
        assertFalse(schema.isValid("hello"));
        assertFalse(schema.isValid(null));
    }

    @Test
    public void testBooleanType() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"boolean\"}");
        assertEquals(JSONSchema.Type.Boolean, schema.getType());
        assertTrue(schema.isValid(true));
        assertTrue(schema.isValid(false));
        assertFalse(schema.isValid(1));
        assertFalse(schema.isValid("true"));
        assertFalse(schema.isValid(null));
    }

    @Test
    public void testNullType() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"null\"}");
        assertEquals(JSONSchema.Type.Null, schema.getType());
        assertTrue(schema.isValid(null));
        assertFalse(schema.isValid(0));
        assertFalse(schema.isValid(""));
        assertFalse(schema.isValid(false));
    }

    @Test
    public void testObjectType() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"object\"}");
        assertEquals(JSONSchema.Type.Object, schema.getType());
        assertTrue(schema.isValid(new JSONObject()));
        assertFalse(schema.isValid("hello"));
        assertFalse(schema.isValid(null));
    }

    @Test
    public void testArrayType() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"array\"}");
        assertEquals(JSONSchema.Type.Array, schema.getType());
        assertTrue(schema.isValid(new java.util.ArrayList<>()));
        assertFalse(schema.isValid("hello"));
        assertFalse(schema.isValid(null));
    }

    // ==================== Boolean Schema ====================

    @Test
    public void testBooleanSchemaTrue() {
        JSONSchema schema = JSONSchema.parseSchema("true");
        assertTrue(schema.isValid("anything"));
        assertTrue(schema.isValid(42));
        assertTrue(schema.isValid(null));
    }

    @Test
    public void testBooleanSchemaFalse() {
        JSONSchema schema = JSONSchema.parseSchema("false");
        assertFalse(schema.isValid("anything"));
        assertFalse(schema.isValid(42));
        assertFalse(schema.isValid(null));
    }

    // ==================== String Constraints ====================

    @Test
    public void testStringMinLength() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\", \"minLength\":3}");
        assertTrue(schema.isValid("abc"));
        assertTrue(schema.isValid("abcd"));
        assertFalse(schema.isValid("ab"));
        assertFalse(schema.isValid(""));
    }

    @Test
    public void testStringMaxLength() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\", \"maxLength\":5}");
        assertTrue(schema.isValid("hello"));
        assertTrue(schema.isValid("hi"));
        assertTrue(schema.isValid(""));
        assertFalse(schema.isValid("toolong"));
    }

    @Test
    public void testStringPattern() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\", \"pattern\":\"^[a-z]+$\"}");
        assertTrue(schema.isValid("abc"));
        assertFalse(schema.isValid("ABC"));
        assertFalse(schema.isValid("123"));
    }

    @Test
    public void testStringConst() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\", \"const\":\"fixed\"}");
        assertTrue(schema.isValid("fixed"));
        assertFalse(schema.isValid("other"));
    }

    @Test
    public void testStringEnum() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\", \"enum\":[\"red\",\"green\",\"blue\"]}");
        assertTrue(schema.isValid("red"));
        assertTrue(schema.isValid("blue"));
        assertFalse(schema.isValid("yellow"));
    }

    // ==================== String Formats ====================

    @Test
    public void testFormatEmail() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\", \"format\":\"email\"}");
        assertTrue(schema.isValid("user@example.com"));
        assertFalse(schema.isValid("not-an-email"));
        assertFalse(schema.isValid("@example.com"));
    }

    @Test
    public void testFormatIPv4() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\", \"format\":\"ipv4\"}");
        assertTrue(schema.isValid("192.168.1.1"));
        assertTrue(schema.isValid("0.0.0.0"));
        assertTrue(schema.isValid("255.255.255.255"));
        assertFalse(schema.isValid("256.1.1.1"));
        assertFalse(schema.isValid("1.2.3"));
        assertFalse(schema.isValid("not-an-ip"));
    }

    @Test
    public void testFormatUUID() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\", \"format\":\"uuid\"}");
        assertTrue(schema.isValid("550e8400-e29b-41d4-a716-446655440000"));
        assertFalse(schema.isValid("not-a-uuid"));
        assertFalse(schema.isValid("550e8400-e29b-41d4-a716"));
    }

    @Test
    public void testFormatDate() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\", \"format\":\"date\"}");
        assertTrue(schema.isValid("2024-01-15"));
        assertFalse(schema.isValid("2024/01/15"));
        assertFalse(schema.isValid("not-a-date"));
    }

    @Test
    public void testFormatDateTime() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\", \"format\":\"date-time\"}");
        assertTrue(schema.isValid("2024-01-15T10:30:00Z"));
        assertTrue(schema.isValid("2024-01-15T10:30:00+08:00"));
        assertFalse(schema.isValid("2024-01-15"));
        assertFalse(schema.isValid("not-a-datetime"));
    }

    @Test
    public void testFormatTime() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\", \"format\":\"time\"}");
        assertTrue(schema.isValid("10:30:00"));
        assertTrue(schema.isValid("23:59:59"));
        assertFalse(schema.isValid("not-a-time"));
    }

    @Test
    public void testFormatDuration() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\", \"format\":\"duration\"}");
        assertTrue(schema.isValid("PT1H30M"));
        assertTrue(schema.isValid("P1D"));
        assertFalse(schema.isValid("1 hour"));
    }

    @Test
    public void testFormatURI() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\", \"format\":\"uri\"}");
        assertTrue(schema.isValid("https://example.com"));
        assertTrue(schema.isValid("ftp://files.example.com"));
        assertFalse(schema.isValid("not a uri"));
    }

    // ==================== Number Constraints ====================

    @Test
    public void testNumberMinimum() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"number\", \"minimum\":0}");
        assertTrue(schema.isValid(0));
        assertTrue(schema.isValid(1));
        assertTrue(schema.isValid(100));
        assertFalse(schema.isValid(-1));
    }

    @Test
    public void testNumberMaximum() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"number\", \"maximum\":100}");
        assertTrue(schema.isValid(100));
        assertTrue(schema.isValid(0));
        assertFalse(schema.isValid(101));
    }

    @Test
    public void testNumberExclusiveMinimum() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"number\", \"exclusiveMinimum\":0}");
        assertTrue(schema.isValid(1));
        assertTrue(schema.isValid(0.001));
        assertFalse(schema.isValid(0));
        assertFalse(schema.isValid(-1));
    }

    @Test
    public void testNumberExclusiveMaximum() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"number\", \"exclusiveMaximum\":100}");
        assertTrue(schema.isValid(99));
        assertTrue(schema.isValid(99.99));
        assertFalse(schema.isValid(100));
        assertFalse(schema.isValid(101));
    }

    @Test
    public void testNumberMultipleOf() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"number\", \"multipleOf\":3}");
        assertTrue(schema.isValid(0));
        assertTrue(schema.isValid(3));
        assertTrue(schema.isValid(9));
        assertFalse(schema.isValid(4));
        assertFalse(schema.isValid(7));
    }

    // ==================== Integer Constraints ====================

    @Test
    public void testIntegerMinMax() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"integer\", \"minimum\":1, \"maximum\":10}");
        assertTrue(schema.isValid(1));
        assertTrue(schema.isValid(5));
        assertTrue(schema.isValid(10));
        assertFalse(schema.isValid(0));
        assertFalse(schema.isValid(11));
    }

    @Test
    public void testIntegerExclusive() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"integer\", \"exclusiveMinimum\":0, \"exclusiveMaximum\":10}");
        assertTrue(schema.isValid(1));
        assertTrue(schema.isValid(9));
        assertFalse(schema.isValid(0));
        assertFalse(schema.isValid(10));
    }

    @Test
    public void testIntegerMultipleOf() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"integer\", \"multipleOf\":5}");
        assertTrue(schema.isValid(0));
        assertTrue(schema.isValid(5));
        assertTrue(schema.isValid(15));
        assertFalse(schema.isValid(3));
    }

    @Test
    public void testIntegerConst() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"integer\", \"const\":42}");
        assertTrue(schema.isValid(42));
        assertFalse(schema.isValid(43));
    }

    // ==================== Object Constraints ====================

    @Test
    public void testObjectProperties() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "properties": {
                        "name": {"type": "string"},
                        "age": {"type": "integer", "minimum": 0}
                    }
                }
                """);

        assertTrue(schema.isValid(JSON.parseObject("{\"name\":\"John\", \"age\":30}")));
        assertFalse(schema.isValid(JSON.parseObject("{\"name\":\"John\", \"age\":-1}")));
        assertTrue(schema.isValid(JSON.parseObject("{\"name\":\"John\"}")));
    }

    @Test
    public void testObjectRequired() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "properties": {
                        "name": {"type": "string"},
                        "email": {"type": "string"}
                    },
                    "required": ["name", "email"]
                }
                """);

        assertTrue(schema.isValid(JSON.parseObject("{\"name\":\"John\", \"email\":\"john@example.com\"}")));
        assertFalse(schema.isValid(JSON.parseObject("{\"name\":\"John\"}")));
        assertFalse(schema.isValid(JSON.parseObject("{}")));
    }

    @Test
    public void testObjectAdditionalPropertiesFalse() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "properties": {
                        "name": {"type": "string"}
                    },
                    "additionalProperties": false
                }
                """);

        assertTrue(schema.isValid(JSON.parseObject("{\"name\":\"John\"}")));
        assertFalse(schema.isValid(JSON.parseObject("{\"name\":\"John\", \"extra\":\"value\"}")));
    }

    @Test
    public void testObjectAdditionalPropertiesSchema() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "properties": {
                        "name": {"type": "string"}
                    },
                    "additionalProperties": {"type": "integer"}
                }
                """);

        assertTrue(schema.isValid(JSON.parseObject("{\"name\":\"John\", \"age\":30}")));
        assertFalse(schema.isValid(JSON.parseObject("{\"name\":\"John\", \"extra\":\"string\"}")));
    }

    @Test
    public void testObjectMinMaxProperties() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"type": "object", "minProperties": 1, "maxProperties": 3}
                """);

        assertFalse(schema.isValid(JSON.parseObject("{}")));
        assertTrue(schema.isValid(JSON.parseObject("{\"a\":1}")));
        assertTrue(schema.isValid(JSON.parseObject("{\"a\":1,\"b\":2,\"c\":3}")));
        assertFalse(schema.isValid(JSON.parseObject("{\"a\":1,\"b\":2,\"c\":3,\"d\":4}")));
    }

    @Test
    public void testObjectPatternProperties() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "patternProperties": {
                        "^x-": {"type": "string"}
                    }
                }
                """);

        assertTrue(schema.isValid(JSON.parseObject("{\"x-custom\":\"value\"}")));
        assertFalse(schema.isValid(JSON.parseObject("{\"x-custom\":123}")));
    }

    @Test
    public void testObjectPropertyNames() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "propertyNames": {"maxLength": 3}
                }
                """);

        assertTrue(schema.isValid(JSON.parseObject("{\"abc\":1}")));
        assertFalse(schema.isValid(JSON.parseObject("{\"toolong\":1}")));
    }

    @Test
    public void testObjectDependentRequired() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "dependentRequired": {
                        "credit_card": ["billing_address"]
                    }
                }
                """);

        assertTrue(schema.isValid(JSON.parseObject("{\"name\":\"John\"}")));
        assertTrue(schema.isValid(JSON.parseObject("{\"credit_card\":\"1234\", \"billing_address\":\"123 St\"}")));
        assertFalse(schema.isValid(JSON.parseObject("{\"credit_card\":\"1234\"}")));
    }

    @Test
    public void testObjectDependentSchemas() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "dependentSchemas": {
                        "bar": {
                            "properties": {
                                "foo": {"type": "integer"},
                                "bar": {"type": "integer"}
                            },
                            "required": ["foo"]
                        }
                    }
                }
                """);

        assertTrue(schema.isValid(JSON.parseObject("{\"foo\":1}")));
        assertTrue(schema.isValid(JSON.parseObject("{\"bar\":2, \"foo\":1}")));
        assertFalse(schema.isValid(JSON.parseObject("{\"bar\":2}")));
    }

    @Test
    public void testObjectIfThenElse() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "if": {
                        "properties": {"country": {"const": "US"}},
                        "required": ["country"]
                    },
                    "then": {
                        "properties": {"postal_code": {"pattern": "^[0-9]{5}$"}}
                    },
                    "else": {
                        "properties": {"postal_code": {"pattern": "^[A-Z][0-9][A-Z]"}}
                    }
                }
                """);

        assertTrue(schema.isValid(JSON.parseObject("{\"country\":\"US\", \"postal_code\":\"12345\"}")));
        assertFalse(schema.isValid(JSON.parseObject("{\"country\":\"US\", \"postal_code\":\"ABC\"}")));
        assertTrue(schema.isValid(JSON.parseObject("{\"country\":\"CA\", \"postal_code\":\"A1B\"}")));
    }

    // ==================== Array Constraints ====================

    @Test
    public void testArrayItems() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"type": "array", "items": {"type": "integer"}}
                """);

        assertTrue(schema.isValid(JSON.parseArray("[1, 2, 3]")));
        assertTrue(schema.isValid(JSON.parseArray("[]")));
        assertFalse(schema.isValid(JSON.parseArray("[1, \"two\", 3]")));
    }

    @Test
    public void testArrayMinMaxItems() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"type": "array", "minItems": 1, "maxItems": 3}
                """);

        assertFalse(schema.isValid(JSON.parseArray("[]")));
        assertTrue(schema.isValid(JSON.parseArray("[1]")));
        assertTrue(schema.isValid(JSON.parseArray("[1, 2, 3]")));
        assertFalse(schema.isValid(JSON.parseArray("[1, 2, 3, 4]")));
    }

    @Test
    public void testArrayUniqueItems() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"type": "array", "uniqueItems": true}
                """);

        assertTrue(schema.isValid(JSON.parseArray("[1, 2, 3]")));
        assertFalse(schema.isValid(JSON.parseArray("[1, 2, 1]")));
    }

    @Test
    public void testArrayPrefixItems() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "array",
                    "prefixItems": [
                        {"type": "string"},
                        {"type": "integer"},
                        {"type": "boolean"}
                    ]
                }
                """);

        assertTrue(schema.isValid(JSON.parseArray("[\"hello\", 42, true]")));
        assertFalse(schema.isValid(JSON.parseArray("[42, \"hello\", true]")));
    }

    @Test
    public void testArrayContains() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"type": "array", "contains": {"type": "string"}}
                """);

        assertTrue(schema.isValid(JSON.parseArray("[1, \"hello\", 3]")));
        assertFalse(schema.isValid(JSON.parseArray("[1, 2, 3]")));
    }

    @Test
    public void testArrayContainsMinMax() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "array",
                    "contains": {"type": "string"},
                    "minContains": 2,
                    "maxContains": 3
                }
                """);

        assertFalse(schema.isValid(JSON.parseArray("[\"a\", 1, 2]")));
        assertTrue(schema.isValid(JSON.parseArray("[\"a\", \"b\", 1]")));
        assertTrue(schema.isValid(JSON.parseArray("[\"a\", \"b\", \"c\"]")));
        assertFalse(schema.isValid(JSON.parseArray("[\"a\", \"b\", \"c\", \"d\"]")));
    }

    // ==================== Combinators ====================

    @Test
    public void testAllOf() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "allOf": [
                        {"type": "integer", "minimum": 0},
                        {"type": "integer", "maximum": 100}
                    ]
                }
                """);

        assertTrue(schema.isValid(50));
        assertTrue(schema.isValid(0));
        assertTrue(schema.isValid(100));
        assertFalse(schema.isValid(-1));
        assertFalse(schema.isValid(101));
    }

    @Test
    public void testAnyOf() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "anyOf": [
                        {"type": "string"},
                        {"type": "integer"}
                    ]
                }
                """);

        assertTrue(schema.isValid("hello"));
        assertTrue(schema.isValid(42));
        assertFalse(schema.isValid(true));
    }

    @Test
    public void testOneOf() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "oneOf": [
                        {"type": "integer", "multipleOf": 3},
                        {"type": "integer", "multipleOf": 5}
                    ]
                }
                """);

        assertTrue(schema.isValid(3));
        assertTrue(schema.isValid(5));
        assertFalse(schema.isValid(15)); // matches both
        assertFalse(schema.isValid(2));  // matches neither
    }

    @Test
    public void testNot() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"not": {"type": "string"}}
                """);

        assertTrue(schema.isValid(42));
        assertTrue(schema.isValid(true));
        assertFalse(schema.isValid("hello"));
    }

    // ==================== Enum ====================

    @Test
    public void testEnumMixed() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"enum": [1, "two", true, null]}
                """);

        assertTrue(schema.isValid(1));
        assertTrue(schema.isValid("two"));
        assertTrue(schema.isValid(true));
        assertTrue(schema.isValid(null));
        assertFalse(schema.isValid(2));
        assertFalse(schema.isValid("three"));
    }

    // ==================== Multi-type ====================

    @Test
    public void testMultiType() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"type": ["string", "integer"]}
                """);

        assertTrue(schema.isValid("hello"));
        assertTrue(schema.isValid(42));
        assertFalse(schema.isValid(3.14));
        assertFalse(schema.isValid(true));
    }

    // ==================== $ref ====================

    @Test
    public void testDefinitionsRef() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "definitions": {
                        "address": {
                            "type": "object",
                            "properties": {
                                "street": {"type": "string"},
                                "city": {"type": "string"}
                            },
                            "required": ["street", "city"]
                        }
                    },
                    "properties": {
                        "home": {"$ref": "#/definitions/address"},
                        "work": {"$ref": "#/definitions/address"}
                    }
                }
                """);

        assertTrue(schema.isValid(JSON.parseObject("""
                {
                    "home": {"street": "123 Main St", "city": "Springfield"},
                    "work": {"street": "456 Oak Ave", "city": "Shelbyville"}
                }
                """)));

        assertFalse(schema.isValid(JSON.parseObject("""
                {"home": {"street": "123 Main St"}}
                """)));
    }

    @Test
    public void testDefsRef() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "$defs": {
                        "positiveInt": {
                            "type": "integer",
                            "minimum": 1
                        }
                    },
                    "properties": {
                        "count": {"$ref": "#/$defs/positiveInt"}
                    }
                }
                """);

        assertTrue(schema.isValid(JSON.parseObject("{\"count\": 5}")));
        assertFalse(schema.isValid(JSON.parseObject("{\"count\": 0}")));
    }

    // ==================== assertValidate ====================

    @Test
    public void testAssertValidate() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"integer\", \"minimum\":0}");

        assertDoesNotThrow(() -> schema.assertValidate(1));
        assertThrows(JSONSchemaValidException.class, () -> schema.assertValidate(-1));
    }

    // ==================== Custom Error Message ====================

    @Test
    public void testCustomErrorMessage() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"type": "integer", "minimum": 0, "error": "must be non-negative"}
                """);

        ValidateResult result = schema.validate(-1);
        assertFalse(result.isSuccess());
        assertEquals("must be non-negative", result.getMessage());
    }

    // ==================== Title and Description ====================

    @Test
    public void testTitleDescription() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"type": "string", "title": "Name", "description": "User name"}
                """);

        assertEquals("Name", schema.getTitle());
        assertEquals("User name", schema.getDescription());
    }

    // ==================== Type-specific validate methods ====================

    @Test
    public void testValidateLong() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"integer\", \"minimum\":0}");
        assertTrue(schema.validate(5L).isSuccess());
        assertFalse(schema.validate(-1L).isSuccess());
        assertTrue(schema.isValid(5L));
        assertFalse(schema.isValid(-1L));
    }

    @Test
    public void testValidateDouble() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"number\", \"minimum\":0}");
        assertTrue(schema.validate(5.0).isSuccess());
        assertFalse(schema.validate(-1.0).isSuccess());
    }

    @Test
    public void testValidateBoxedInteger() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"integer\", \"minimum\":0}");
        assertTrue(schema.validate((Object) Integer.valueOf(5)).isSuccess());
        assertFalse(schema.validate((Object) Integer.valueOf(-1)).isSuccess());
    }

    // ==================== Encoded ====================

    @Test
    public void testObjectEncoded() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "encoded": true,
                    "properties": {
                        "name": {"type": "string"}
                    },
                    "required": ["name"]
                }
                """);

        assertTrue(schema.isValid("{\"name\":\"John\"}"));
        assertFalse(schema.isValid("{}")); // missing required
        assertFalse(schema.isValid("not json"));
        assertFalse(schema.isValid(123)); // not a string
    }

    // ==================== Complex Scenarios ====================

    @Test
    public void testComplexSchema() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "properties": {
                        "name": {
                            "type": "string",
                            "minLength": 1,
                            "maxLength": 100
                        },
                        "age": {
                            "type": "integer",
                            "minimum": 0,
                            "maximum": 150
                        },
                        "email": {
                            "type": "string",
                            "format": "email"
                        },
                        "tags": {
                            "type": "array",
                            "items": {"type": "string"},
                            "uniqueItems": true,
                            "maxItems": 10
                        },
                        "address": {
                            "type": "object",
                            "properties": {
                                "city": {"type": "string"},
                                "zip": {"type": "string", "pattern": "^[0-9]{5}$"}
                            },
                            "required": ["city"]
                        }
                    },
                    "required": ["name", "age"]
                }
                """);

        assertTrue(schema.isValid(JSON.parseObject("""
                {
                    "name": "John",
                    "age": 30,
                    "email": "john@example.com",
                    "tags": ["developer", "java"],
                    "address": {"city": "NYC", "zip": "10001"}
                }
                """)));

        // Missing required
        assertFalse(schema.isValid(JSON.parseObject("{}")));

        // Invalid age
        assertFalse(schema.isValid(JSON.parseObject("{\"name\":\"John\", \"age\":-1}")));

        // Invalid email
        assertFalse(schema.isValid(JSON.parseObject("{\"name\":\"John\", \"age\":30, \"email\":\"bad\"}")));

        // Duplicate tags
        assertFalse(schema.isValid(JSON.parseObject("{\"name\":\"John\", \"age\":30, \"tags\":[\"a\",\"a\"]}")));

        // Invalid zip
        assertFalse(schema.isValid(JSON.parseObject("""
                {"name":"John", "age":30, "address":{"city":"NYC", "zip":"ABC"}}
                """)));
    }

    // ==================== Empty Schema ====================

    @Test
    public void testEmptySchema() {
        JSONSchema schema = JSONSchema.parseSchema("{}");
        assertTrue(schema.isValid("anything"));
        assertTrue(schema.isValid(42));
        assertTrue(schema.isValid(null));
    }

    // ==================== Edge Cases (from review) ====================

    @Test
    public void testIntegerRejectsNaN() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"integer\"}");
        assertFalse(schema.isValid(Double.NaN));
        assertFalse(schema.isValid(Float.NaN));
    }

    @Test
    public void testIntegerRejectsInfinity() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"integer\"}");
        assertFalse(schema.isValid(Double.POSITIVE_INFINITY));
        assertFalse(schema.isValid(Double.NEGATIVE_INFINITY));
        assertFalse(schema.isValid(Float.POSITIVE_INFINITY));
    }

    @Test
    public void testEnumIntLongMatch() {
        // JSON parsing produces Integer for small numbers. Validation with Long should still match.
        JSONSchema schema = JSONSchema.parseSchema("{\"enum\": [1, 2, 3]}");
        assertTrue(schema.isValid(1L));
        assertTrue(schema.isValid(2));
        assertFalse(schema.isValid(4));
    }

    @Test
    public void testNumberRejectsNaN() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"number\"}");
        assertFalse(schema.isValid(Double.NaN));
        assertFalse(schema.isValid(Float.NaN));
        assertFalse(schema.isValid(Double.POSITIVE_INFINITY));
        assertFalse(schema.isValid(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testExclusiveMinMaxWithoutType() {
        // exclusiveMinimum/Maximum without "type" should still create a NumberSchema
        JSONSchema schema = JSONSchema.parseSchema("{\"exclusiveMinimum\":0, \"exclusiveMaximum\":10}");
        assertTrue(schema.isValid(5));
        assertFalse(schema.isValid(0));
        assertFalse(schema.isValid(10));
    }

    @Test
    public void testContainsWithoutType() {
        // "contains" without "type":"array" should create an ArraySchema
        JSONSchema schema = JSONSchema.parseSchema("{\"contains\":{\"type\":\"string\"}}");
        assertTrue(schema.isValid(JSON.parseArray("[1, \"hello\", 3]")));
        assertFalse(schema.isValid(JSON.parseArray("[1, 2, 3]")));
    }

    @Test
    public void testNullParseSchema() {
        JSONSchema schema = JSONSchema.parseSchema(null);
        assertTrue(schema.isValid("anything"));
    }

    @Test
    public void testEmptyStringParseSchema() {
        JSONSchema schema = JSONSchema.parseSchema("");
        assertTrue(schema.isValid("anything"));
    }

    @Test
    public void testBooleanSchemaUntypedAcceptsAll() {
        // BooleanSchema without "type":"boolean" should accept any value
        JSONSchema schema = JSONSchema.of(new JSONObject());
        assertTrue(schema.isValid("string"));
        assertTrue(schema.isValid(42));
    }

    @Test
    public void testObjectGetProperties() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "properties": {
                        "name": {"type": "string"}
                    }
                }
                """);

        assertTrue(schema instanceof com.alibaba.fastjson3.schema.ObjectSchema);
        ObjectSchema os = (ObjectSchema) schema;
        assertNotNull(os.getProperties());
        assertEquals(1, os.getProperties().size());
        assertTrue(os.getProperties().containsKey("name"));
    }

    @Test
    public void testArrayGetItemSchema() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"type": "array", "items": {"type": "string"}}
                """);

        assertTrue(schema instanceof com.alibaba.fastjson3.schema.ArraySchema);
        ArraySchema as = (ArraySchema) schema;
        assertNotNull(as.getItemSchema());
    }

    // ==================== toJSONObject Round-Trip ====================

    @Test
    public void testStringSchemaRoundTrip() {
        String input = "{\"type\":\"string\",\"minLength\":1,\"maxLength\":10,\"pattern\":\"^[a-z]+$\",\"format\":\"email\"}";
        JSONSchema schema = JSONSchema.parseSchema(input);
        JSONObject obj = schema.toJSONObject();
        assertEquals("string", obj.getString("type"));
        assertEquals(1, obj.getIntValue("minLength"));
        assertEquals(10, obj.getIntValue("maxLength"));
        assertEquals("^[a-z]+$", obj.getString("pattern"));
        assertEquals("email", obj.getString("format"));
    }

    @Test
    public void testIntegerSchemaRoundTrip() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"integer\",\"minimum\":0,\"maximum\":100,\"multipleOf\":5}");
        JSONObject obj = schema.toJSONObject();
        assertEquals("integer", obj.getString("type"));
        assertEquals(0L, obj.getLongValue("minimum"));
        assertEquals(100L, obj.getLongValue("maximum"));
        assertEquals(5L, obj.getLongValue("multipleOf"));
    }

    @Test
    public void testObjectSchemaRoundTrip() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"type":"object","properties":{"name":{"type":"string"}},"required":["name"]}
                """);
        JSONObject obj = schema.toJSONObject();
        assertEquals("object", obj.getString("type"));
        assertNotNull(obj.get("properties"));
        assertNotNull(obj.get("required"));
    }

    @Test
    public void testArraySchemaRoundTrip() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"type":"array","items":{"type":"string"},"minItems":1,"maxItems":10,"uniqueItems":true}
                """);
        JSONObject obj = schema.toJSONObject();
        assertEquals("array", obj.getString("type"));
        assertEquals(1, obj.getIntValue("minItems"));
        assertEquals(10, obj.getIntValue("maxItems"));
        assertTrue(obj.getBooleanValue("uniqueItems"));
    }

    @Test
    public void testSchemaToString() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"boolean\"}");
        String str = schema.toString();
        assertTrue(str.contains("boolean"));
    }

    @Test
    public void testSchemaEquals() {
        JSONSchema s1 = JSONSchema.parseSchema("{\"type\":\"integer\",\"minimum\":0}");
        JSONSchema s2 = JSONSchema.parseSchema("{\"type\":\"integer\",\"minimum\":0}");
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    // ==================== JSON Path in Error Messages ====================

    @Test
    public void testErrorPathProperty() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"type":"object","properties":{"age":{"type":"integer","minimum":0}}}
                """);
        ValidateResult result = schema.validate(JSON.parseObject("{\"age\":-1}"));
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().startsWith("$.age:"), "Expected path prefix $.age:, got: " + result.getMessage());
    }

    @Test
    public void testErrorPathNested() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"type":"object","properties":{
                    "address":{"type":"object","properties":{
                        "zip":{"type":"string","pattern":"^[0-9]{5}$"}
                    }}
                }}
                """);
        ValidateResult result = schema.validate(JSON.parseObject("{\"address\":{\"zip\":\"ABC\"}}"));
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("$.address.zip"), "Expected nested path, got: " + result.getMessage());
    }

    @Test
    public void testErrorPathArray() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"type":"array","items":{"type":"integer"}}
                """);
        ValidateResult result = schema.validate(JSON.parseArray("[1, 2, \"bad\"]"));
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("$.[2]"), "Expected array path, got: " + result.getMessage());
    }

    @Test
    public void testErrorPathGetPath() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {"type":"object","properties":{"name":{"type":"string","minLength":1}}}
                """);
        ValidateResult result = schema.validate(JSON.parseObject("{\"name\":\"\"}"));
        assertFalse(result.isSuccess());
        assertEquals("name", result.getPath());
    }

    // ==================== Format: additional validators ====================

    @Test
    public void testFormatURIReference() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\",\"format\":\"uri-reference\"}");
        assertTrue(schema.isValid("https://example.com"));
        assertTrue(schema.isValid("/path/to/resource"));
        assertTrue(schema.isValid("relative/path"));
        assertFalse(schema.isValid("not a valid uri:://["));
    }

    @Test
    public void testFormatJSONPointer() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\",\"format\":\"json-pointer\"}");
        assertTrue(schema.isValid(""));
        assertTrue(schema.isValid("/foo"));
        assertTrue(schema.isValid("/foo/0"));
        assertTrue(schema.isValid("/a~1b"));
        assertTrue(schema.isValid("/c~0d"));
        assertFalse(schema.isValid("no-leading-slash"));
        assertFalse(schema.isValid("/bad~escape"));
    }

    @Test
    public void testFormatRegex() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\",\"format\":\"regex\"}");
        assertTrue(schema.isValid("^[a-z]+$"));
        assertTrue(schema.isValid("\\d{3}-\\d{4}"));
        assertFalse(schema.isValid("[invalid"));
    }

    // ==================== Format: hostname ====================

    @Test
    public void testFormatHostname() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\",\"format\":\"hostname\"}");
        assertTrue(schema.isValid("example.com"));
        assertTrue(schema.isValid("sub.domain.org"));
        assertFalse(schema.isValid("not a hostname!"));
        assertFalse(schema.isValid("-invalid.com"));
    }

    @Test
    public void testVisitorAccept() {
        JSONSchema schema = JSONSchema.parseSchema("""
                {
                    "type": "object",
                    "properties": {
                        "a": {"type": "string"},
                        "b": {"type": "integer"}
                    }
                }
                """);

        java.util.List<JSONSchema.Type> types = new java.util.ArrayList<>();
        schema.accept(s -> {
            types.add(s.getType());
            return true;
        });
        // Should visit the ObjectSchema and its 2 property schemas
        assertEquals(3, types.size());
    }
}
