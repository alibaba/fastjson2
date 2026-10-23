package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONSchemaValidException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In JSON Schema, {@code enum} is an independent assertion keyword: it applies next to {@code type},
 * not instead of it. A value has to match the declared type <em>and</em> be a member of the enum list.
 * <details><summary>中文</summary>
 * 在 JSON Schema 中，{@code enum} 是与 {@code type} 并列的独立约束：值必须同时满足声明的类型和枚举列表。
 * </details>
 */
@Tag("schema")
public class TypedEnumSchemaTest {
    static JSONSchema parseSchema(String text) {
        return JSONSchema.of(JSON.parseObject(text));
    }

    @Test
    public void stringEnumIsEnforced() {
        JSONSchema schema = parseSchema("{\"type\":\"string\",\"enum\":[\"a\",\"b\"]}");
        assertTrue(schema.validate("a").isSuccess());
        assertTrue(schema.validate("b").isSuccess());
        assertFalse(schema.validate("c").isSuccess());
    }

    @Test
    public void untypedEnumIsEnforced() {
        JSONSchema schema = parseSchema("{\"enum\":[1,2]}");
        assertTrue(schema.validate(1).isSuccess());
        assertFalse(schema.validate(3).isSuccess());
    }

    @Test
    public void integerEnumIsEnforced() {
        JSONSchema schema = parseSchema("{\"type\":\"integer\",\"enum\":[1,2]}");
        assertTrue(schema.validate(1).isSuccess());
        assertTrue(schema.validate(2L).isSuccess());
        assertFalse(schema.validate(3).isSuccess());
        assertFalse(schema.validate(0).isSuccess());
    }

    @Test
    public void numberEnumIsEnforced() {
        JSONSchema schema = parseSchema("{\"type\":\"number\",\"enum\":[1,2]}");
        assertTrue(schema.validate(1).isSuccess());
        assertTrue(schema.validate(2L).isSuccess());
        assertFalse(schema.validate(3).isSuccess());
        assertFalse(schema.validate(3.0D).isSuccess());
    }

    @Test
    public void booleanEnumIsEnforced() {
        JSONSchema schema = parseSchema("{\"type\":\"boolean\",\"enum\":[true]}");
        assertTrue(schema.validate(true).isSuccess());
        assertFalse(schema.validate(false).isSuccess());
    }

    @Test
    public void objectAndArrayEnumAreEnforced() {
        JSONSchema objectSchema = parseSchema("{\"type\":\"object\",\"enum\":[{\"id\":1}]}");
        assertTrue(objectSchema.validate(JSON.parseObject("{\"id\":1}")).isSuccess());
        assertFalse(objectSchema.validate(JSON.parseObject("{\"id\":2}")).isSuccess());

        JSONSchema arraySchema = parseSchema("{\"type\":\"array\",\"enum\":[[1,2]]}");
        assertTrue(arraySchema.validate(JSON.parseArray("[1,2]")).isSuccess());
        assertFalse(arraySchema.validate(JSON.parseArray("[1,3]")).isSuccess());
    }

    /**
     * Enum membership compares numbers by value, as {@link EnumSchema} already does for an untyped enum:
     * {@code 2.0} matches the member {@code 2}, and the represented value still has to be listed.
     */
    @Test
    public void typedEnumUsesNumericRepresentationEquality() {
        JSONSchema integerSchema = parseSchema("{\"type\":\"integer\",\"enum\":[1,2]}");
        assertTrue(integerSchema.validate(new BigDecimal("2.0")).isSuccess());
        assertFalse(integerSchema.validate(new BigDecimal("3.0")).isSuccess());

        JSONSchema numberSchema = parseSchema("{\"type\":\"number\",\"enum\":[1.5]}");
        assertTrue(numberSchema.validate(new BigDecimal("1.50")).isSuccess());
        assertFalse(numberSchema.validate(new BigDecimal("2.50")).isSuccess());
    }

    /**
     * A {@code null} member only describes the member list; it does not relax the declared type,
     * and it does not make unlisted values acceptable.
     */
    @Test
    public void enumWithNullMemberKeepsListedValues() {
        JSONSchema schema = parseSchema("{\"type\":\"integer\",\"enum\":[1,null]}");
        assertTrue(schema.validate(1).isSuccess());
        assertFalse(schema.validate(2).isSuccess());
        assertFalse(schema.validate((Object) null).isSuccess());

        assertTrue(parseSchema("{\"type\":\"null\",\"enum\":[null]}").validate((Object) null).isSuccess());
        assertFalse(parseSchema("{\"type\":\"null\",\"enum\":[1]}").validate((Object) null).isSuccess());
    }

    /**
     * The keyword is enforced in place: the schema keeps its declared type and exports the enum,
     * so the constraint survives {@code toJSONObject()} / {@code toString()}.
     */
    @Test
    public void typedEnumKeepsTypeAndExportsEnum() {
        JSONSchema schema = parseSchema("{\"type\":\"integer\",\"enum\":[1,2]}");
        assertEquals(JSONSchema.Type.Integer, schema.getType());
        assertEquals("integer", schema.toJSONObject().getString("type"));
        assertEquals("[1,2]", schema.toJSONObject().getJSONArray("enum").toJSONString());

        JSONSchema restored = parseSchema(schema.toString());
        assertTrue(restored.validate(2L).isSuccess());
        assertFalse(restored.validate(3).isSuccess());
    }

    @Test
    public void typedEnumHonoursCustomError() {
        JSONSchema schema = parseSchema("{\"type\":\"integer\",\"enum\":[1,2],\"error\":\"must be 1 or 2\"}");
        assertEquals("must be 1 or 2", schema.validate(3).getMessage());
        assertTrue(schema.validate(1).isSuccess());
    }

    /**
     * The class based factory used for {@code @JSONField(schema = ...)} annotations reads the keyword too,
     * for both primitive and boxed field classes.
     */
    @Test
    public void typedEnumFromDeclaredFieldClass() {
        String integerSchema = "{\"type\":\"integer\",\"enum\":[1,2]}";
        assertFalse(JSONSchema.of(JSON.parseObject(integerSchema), int.class).validate(Integer.valueOf(3)).isSuccess());
        assertFalse(JSONSchema.of(JSON.parseObject(integerSchema), Integer.class).validate(Integer.valueOf(0)).isSuccess());
        assertTrue(JSONSchema.of(JSON.parseObject(integerSchema), long.class).validate(2L).isSuccess());

        JSONSchema booleanSchema = JSONSchema.of(JSON.parseObject("{\"type\":\"boolean\",\"enum\":[true]}"), boolean.class);
        assertFalse(booleanSchema.validate(Boolean.FALSE).isSuccess());
    }

    /**
     * Every {@code validate} overload, {@code isValid} and {@code assertValidate} has to see the constraint.
     */
    @Test
    public void typedEnumCoversValidateOverloads() {
        JSONSchema integerSchema = parseSchema("{\"type\":\"integer\",\"enum\":[1,2]}");
        assertFalse(integerSchema.validate(3L).isSuccess());
        assertFalse(integerSchema.validate(Long.valueOf(3)).isSuccess());
        assertFalse(integerSchema.validate(Integer.valueOf(3)).isSuccess());
        assertTrue(integerSchema.isValid(2L));
        assertTrue(integerSchema.isValid(Integer.valueOf(2)));
        assertThrows(JSONSchemaValidException.class, () -> integerSchema.assertValidate(3));
        assertThrows(JSONSchemaValidException.class, () -> integerSchema.assertValidate(3L));

        JSONSchema numberSchema = parseSchema("{\"type\":\"number\",\"enum\":[1,2]}");
        assertTrue(numberSchema.validate(3.0D).getMessage().contains("Enum"));
        assertFalse(numberSchema.validate(Double.valueOf(3)).isSuccess());
        assertFalse(numberSchema.validate(Float.valueOf(3F)).isSuccess());
        assertTrue(numberSchema.isValid(2L));
        assertThrows(JSONSchemaValidException.class, () -> numberSchema.assertValidate(3.0D));
    }

    @Test
    public void typedEnumCombinesWithOtherConstraints() {
        JSONSchema schema = parseSchema("{\"type\":\"integer\",\"enum\":[1,2],\"minimum\":2}");
        assertFalse(schema.validate(1).isSuccess());
        assertTrue(schema.validate(2).isSuccess());
        assertFalse(schema.validate(3).isSuccess());
    }

    /**
     * The class based factory applies the keyword even when the schema declares no {@code type}: the declared field
     * class is the type the members are checked against, and the export keeps both.
     */
    @Test
    public void typedEnumFromDeclaredFieldClassWithoutType() {
        JSONSchema schema = JSONSchema.of(JSON.parseObject("{\"enum\":[1,2]}"), Integer.class);
        assertTrue(schema.validate(1).isSuccess());
        assertFalse(schema.validate(3).isSuccess());
        assertFalse(JSONSchema.of(JSON.parseObject("{\"enum\":[1,2]}"), int.class).validate(9).isSuccess());
        assertFalse(JSONSchema.of(JSON.parseObject("{\"enum\":[1,2]}"), long.class).validate(9L).isSuccess());
        assertFalse(JSONSchema.of(JSON.parseObject("{\"enum\":[1,2]}"), Number.class).validate(2.5D).isSuccess());
        assertFalse(JSONSchema.of(JSON.parseObject("{\"enum\":[true]}"), Boolean.class).validate(Boolean.FALSE).isSuccess());

        assertEquals(JSONSchema.Type.Integer, schema.getType());
        assertEquals("integer", schema.toJSONObject().getString("type"));
        assertEquals("[1,2]", schema.toJSONObject().getJSONArray("enum").toJSONString());
        assertFalse(JSONSchema.parseSchema(schema.toString()).validate(3).isSuccess());
    }

    /**
     * A {@code null} typed schema keeps rejecting values outside the list.
     */
    @Test
    public void nullTypeEnumIsEnforced() {
        JSONSchema schema = parseSchema("{\"type\":\"null\",\"enum\":[1]}");
        assertEquals(JSONSchema.Type.Null, schema.getType());
        assertFalse(schema.validate((Object) null).isSuccess());
        assertFalse(schema.validate(1).isSuccess());

        assertTrue(parseSchema("{\"type\":\"null\",\"enum\":[null]}").validate((Object) null).isSuccess());
        assertTrue(parseSchema("{\"type\":\"null\"}").validate((Object) null).isSuccess());
    }

    /**
     * A declared field class is the type the members are checked against even when the whole member list is made of
     * strings, exactly like an explicit {@code "type":"integer"} with a string member list rejects an integer.
     */
    @Test
    public void declaredFieldClassRejectsIntegerOutsideStringEnum() {
        JSONSchema schema = JSONSchema.of(JSON.parseObject("{\"enum\":[\"a\"]}"), Integer.class);
        assertEquals(JSONSchema.Type.Integer, schema.getType());
        assertFalse(schema.validate(1).isSuccess());
        assertFalse(schema.validate(Integer.valueOf(1)).isSuccess());
        assertFalse(schema.validate((Object) 1).isSuccess());
        assertFalse(schema.isValid(1));

        assertFalse(JSONSchema.of(JSON.parseObject("{\"enum\":[\"a\"]}"), int.class).validate(1).isSuccess());
        assertFalse(JSONSchema.of(JSON.parseObject("{\"enum\":[\"a\"]}"), Long.class).validate(1L).isSuccess());
        assertFalse(JSONSchema.of(JSON.parseObject("{\"enum\":[\"a\"]}"), Number.class).validate(1.0D).isSuccess());
        assertFalse(JSONSchema.of(JSON.parseObject("{\"enum\":[\"a\"]}"), Boolean.class).validate(Boolean.TRUE).isSuccess());

        // the explicitly typed variant the declared factory has to match
        assertFalse(parseSchema("{\"type\":\"integer\",\"enum\":[\"a\"]}").validate(1).isSuccess());
    }

    /**
     * A {@code type} array is a list of allowed types, and {@code enum} keeps applying next to it: a value has to
     * match one of the declared types <em>and</em> be a member of the list, so {@code "a"} is rejected although it is
     * listed, because no declared type admits a string.
     */
    @Test
    public void typeArrayWithEnumIsEnforced() {
        JSONSchema schema = parseSchema("{\"type\":[\"integer\",\"null\"],\"enum\":[1,\"a\",null]}");
        assertEquals(JSONSchema.Type.AnyOf, schema.getType());
        assertTrue(schema.validate(1).isSuccess());
        assertTrue(schema.validate(1L).isSuccess());
        assertTrue(schema.validate((Object) null).isSuccess());
        assertFalse(schema.validate("a").isSuccess());
        assertFalse(schema.validate(2).isSuccess());
        assertFalse(schema.isValid("a"));

        JSONSchema single = parseSchema("{\"type\":[\"integer\"],\"enum\":[1,\"a\"]}");
        assertTrue(single.validate(1).isSuccess());
        assertFalse(single.validate("a").isSuccess());
        assertFalse(single.validate(2).isSuccess());
    }

    /**
     * The primitive {@code long} / {@code double} overloads apply the keyword too, and a schema without {@code enum}
     * keeps its own message on those overloads instead of reporting an enum mismatch.
     */
    @Test
    public void primitiveOverloadsKeepEnumConstraint() {
        JSONSchema integerSchema = parseSchema("{\"type\":\"integer\",\"enum\":[1,2]}");
        assertTrue(integerSchema.validate(2L).isSuccess());
        assertFalse(integerSchema.validate(3L).isSuccess());
        assertFalse(integerSchema.validate(3.0D).isSuccess());
        assertThrows(JSONSchemaValidException.class, () -> integerSchema.assertValidate(3L));

        JSONSchema numberSchema = parseSchema("{\"type\":\"number\",\"enum\":[1,2]}");
        assertTrue(numberSchema.validate(2L).isSuccess());
        assertFalse(numberSchema.validate(3L).isSuccess());
        assertFalse(numberSchema.validate(3.0D).isSuccess());

        JSONSchema plain = parseSchema("{\"type\":\"integer\",\"minimum\":2,\"error\":\"too small\"}");
        assertEquals("too small", plain.validate(1L).getMessage());
        assertTrue(plain.validate(3L).isSuccess());
    }

    @Test
    public void emptyAndNullEnumStayUnconstrained() {
        assertTrue(parseSchema("{\"type\":\"integer\",\"enum\":[]}").validate(3).isSuccess());
        assertTrue(parseSchema("{\"type\":\"integer\",\"enum\":null}").validate(3).isSuccess());
    }

    @Test
    public void typeArrayWithEnumKeepsOtherConstraints() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":[\"integer\",\"null\"],\"enum\":[1,3,null],\"minimum\":2}");
        assertFalse(schema.isValid(1));
        assertTrue(schema.isValid(3));
        assertTrue(schema.isValid((Object) null));
    }

    @Test
    public void typeArrayEnumNotRejectsExcludedValueAndKeepsSiblings() {
        JSONSchema schema = parseSchema("{\"type\":[\"integer\",\"string\"],\"enum\":[1,2],\"not\":{\"enum\":[1]}}");
        assertFalse(schema.isValid(1));
        assertTrue(schema.isValid(2));
        assertFalse(schema.isValid(3));
        assertFalse(schema.isValid(true));
    }

    @Test
    public void typeArrayEnumNotTrueRejectsEveryValue() {
        JSONSchema schema = parseSchema("{\"type\":[\"integer\",\"string\"],\"enum\":[1,2],\"not\":true}");
        assertFalse(schema.isValid(1));
        assertFalse(schema.isValid(2));
        assertFalse(schema.isValid((Object) null));
    }

    @Test
    public void typeArrayEnumNotFalseKeepsTypeAndEnum() {
        JSONSchema schema = parseSchema("{\"type\":[\"integer\",\"string\"],\"enum\":[1,2],\"not\":false}");
        assertTrue(schema.isValid(1));
        assertTrue(schema.isValid(2));
        assertFalse(schema.isValid(3));
        assertFalse(schema.isValid(true));
        assertFalse(schema.isValid((Object) null));
    }

    @Test
    public void typeArrayEnumEmptyNotObjectRejectsEveryValue() {
        JSONSchema schema = parseSchema("{\"type\":[\"integer\",\"string\"],\"enum\":[1,2],\"not\":{}}");
        assertFalse(schema.isValid(1));
        assertFalse(schema.isValid(2));
    }

    @Test
    public void nullableTypeArrayEnumNotNullBoundary() {
        JSONSchema schema = parseSchema("{\"type\":[\"integer\",\"null\"],\"enum\":[1,null],\"not\":{\"type\":\"null\"}}");
        assertTrue(schema.isValid(1));
        assertFalse(schema.isValid((Object) null));
        assertFalse(schema.isValid("1"));
    }

    @Test
    public void typedArrayEnumNotKeepsExportAndRoundTrip() {
        JSONSchema textSchema = parseSchema("{\"type\":[\"integer\",\"string\"],\"enum\":[1,2],\"not\":{\"enum\":[1]}}");
        JSONObject exported = textSchema.toJSONObject();
        JSONSchema objectSchema = JSONSchema.of(exported);
        for (JSONSchema schema : new JSONSchema[]{textSchema, objectSchema, JSONSchema.parseSchema(textSchema.toString())}) {
            assertTrue(schema.validate(2).isSuccess());
            assertFalse(schema.validate(1).isSuccess());
            assertFalse(schema.validate(3).isSuccess());
            assertFalse(schema.validate("a").isSuccess());
            assertFalse(schema.validate(true).isSuccess());
            assertFalse(schema.validate((Object) null).isSuccess());
        }

        assertEquals(2, exported.getJSONArray("type").size());
        assertEquals("integer", exported.getJSONArray("type").getString(0));
        assertEquals("string", exported.getJSONArray("type").getString(1));
        assertEquals(2, exported.getJSONArray("enum").size());
        assertEquals(Integer.valueOf(1), exported.getJSONArray("enum").get(0));
        assertEquals(Integer.valueOf(2), exported.getJSONArray("enum").get(1));
        assertEquals(1, exported.getJSONObject("not").getJSONArray("enum").size());
    }

    @Test
    public void typedArrayEnumNotBooleanAndNullableRoundTrip() {
        JSONSchema nullable = parseSchema("{\"type\":[\"integer\",\"null\"],\"enum\":[1,null],\"not\":{\"type\":\"null\"}}");
        JSONSchema nullableReloaded = JSONSchema.of(nullable.toJSONObject());
        JSONSchema anything = parseSchema("{\"type\":[\"integer\",\"string\"],\"enum\":[1,2],\"not\":true}");
        JSONSchema anythingReloaded = JSONSchema.of(anything.toJSONObject());
        JSONSchema nothing = parseSchema("{\"type\":[\"integer\",\"string\"],\"enum\":[1,2],\"not\":false}");
        JSONSchema nothingReloaded = JSONSchema.of(nothing.toJSONObject());

        for (JSONSchema schema : new JSONSchema[]{nullable, nullableReloaded, JSONSchema.parseSchema(nullable.toString())}) {
            assertTrue(schema.validate(1).isSuccess());
            assertFalse(schema.validate(2).isSuccess());
            assertFalse(schema.validate((Object) null).isSuccess());
        }
        for (JSONSchema schema : new JSONSchema[]{anything, anythingReloaded, JSONSchema.parseSchema(anything.toString())}) {
            assertFalse(schema.validate(1).isSuccess());
            assertFalse(schema.validate(2).isSuccess());
        }
        for (JSONSchema schema : new JSONSchema[]{nothing, nothingReloaded, JSONSchema.parseSchema(nothing.toString())}) {
            assertTrue(schema.validate(1).isSuccess());
            assertTrue(schema.validate(2).isSuccess());
            assertFalse(schema.validate(3).isSuccess());
        }
    }

    @Test
    public void exportedTypedArrayEnumNotIsDetachedFromSchema() {
        JSONSchema textSchema = parseSchema("{\"type\":[\"integer\",\"string\"],\"enum\":[1,2],\"not\":{\"enum\":[1]}}");
        JSONObject exported = textSchema.toJSONObject();
        exported.getJSONArray("enum").clear();
        exported.getJSONObject("not").getJSONArray("enum").clear();
        exported.put("type", "null");

        assertTrue(textSchema.validate(2).isSuccess());
        assertFalse(textSchema.validate(1).isSuccess());

        JSONObject again = textSchema.toJSONObject();
        assertEquals(2, again.getJSONArray("type").size());
        assertEquals(2, again.getJSONArray("enum").size());
        assertEquals(1, again.getJSONObject("not").getJSONArray("enum").size());
        assertFalse(JSONSchema.of(again).validate(1).isSuccess());
    }

    @Test
    public void exportedEnumCannotChangeValidation() {
        JSONSchema schema = parseSchema("{\"type\":\"integer\",\"enum\":[1,2]}");
        JSONObject exported = schema.toJSONObject();
        ((Collection<?>) exported.get("enum")).clear();
        assertTrue(schema.isValid(1));
        assertFalse(schema.isValid(3));
    }

    @Test
    public void otherNumberArgumentsKeepTheEnumConstraint() {
        JSONSchema schema = parseSchema("{\"type\":\"integer\",\"enum\":[1,2]}");
        assertFalse(schema.validate(new BigDecimal("3")).isSuccess());
        assertFalse(schema.validate(BigInteger.valueOf(3)).isSuccess());
        assertFalse(schema.validate(Short.valueOf((short) 3)).isSuccess());
    }
}
