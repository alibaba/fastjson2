package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaReviewRegressionTest {
    @Test
    public void invalidInternationalDomainReturnsFalse() {
        String domain = "\ud800.example.com";
        assertEquals(domain, DomainValidator.unicodeToASCII(domain));
        assertFalse(DomainValidator.isValid(domain));
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"string\",\"format\":\"email\"}");
        assertFalse(schema.isValid("user@" + domain));
        assertTrue(DomainValidator.isValid("b\u00fccher.de"));
    }

    @Test
    public void containsAppliesEquallyToArraysAndCollections() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"array\",\"contains\":{\"type\":\"integer\"},\"minContains\":0,\"maxContains\":0}");
        assertTrue(schema.isValid(new Object[]{"a"}));
        assertTrue(schema.isValid(new int[0]));
        assertTrue(schema.isValid(Collections.singletonList("a")));
        assertFalse(schema.isValid(new int[]{1}));
        assertFalse(schema.isValid(Collections.singletonList(1)));

        JSONSchema ignored = JSONSchema.parseSchema("{\"type\":\"array\",\"minContains\":2,\"maxContains\":3}");
        assertTrue(ignored.isValid(new Object[0]));
        assertTrue(ignored.isValid(new int[0]));
        assertTrue(ignored.isValid(Collections.emptyList()));
    }

    @Test
    public void arrayLengthRoundTrip() {
        JSONSchema original = JSONSchema.parseSchema("{\"type\":\"array\",\"minItems\":1,\"maxItems\":2}");
        JSONObject object = original.toJSONObject();
        assertEquals(1, object.getIntValue("minItems"));
        assertEquals(2, object.getIntValue("maxItems"));
        JSONSchema restored = JSONSchema.parseSchema(original.toString());
        assertFalse(restored.isValid(new int[0]));
        assertTrue(restored.isValid(new int[]{1}));
        assertFalse(restored.isValid(new int[]{1, 2, 3}));
    }

    @Test
    public void additionalItemsRoundTrip() {
        JSONSchema original = JSONSchema.parseSchema("{\"type\":\"array\",\"prefixItems\":[{\"type\":\"integer\"}],\"additionalItems\":{\"type\":\"string\"}}");
        JSONSchema restored = JSONSchema.parseSchema(original.toString());
        assertTrue(restored.isValid(new Object[]{1, "a"}));
        assertFalse(restored.isValid(new Object[]{1, 2}));
    }

    @Test
    public void stringConstraintsRoundTrip() {
        JSONSchema original = JSONSchema.parseSchema("{\"type\":\"string\",\"minLength\":1,\"maxLength\":2,\"pattern\":\"^[a-z]+$\"}");
        assertEquals("^[a-z]+$", original.toJSONObject().get("pattern"));
        JSONSchema restored = JSONSchema.parseSchema(original.toString());
        assertTrue(restored.isValid("ab"));
        assertFalse(restored.isValid("abc"));
        assertFalse(restored.isValid("12"));
    }

    @Test
    public void integralDecimalsRespectConstraints() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"integer\",\"minimum\":2,\"maximum\":8,\"multipleOf\":2}");
        assertTrue(schema.isValid(new BigDecimal("4.00")));
        assertFalse(schema.isValid(new BigDecimal("0.00")));
        assertFalse(schema.isValid(new BigDecimal("10.00")));
        assertFalse(schema.isValid(new BigDecimal("3.00")));
        assertFalse(schema.isValid(new BigDecimal("3.50")));
    }

    @Test
    public void largeIntegersDoNotWrap() {
        BigInteger twoTo64 = BigInteger.ONE.shiftLeft(64);
        JSONSchema positive = JSONSchema.parseSchema("{\"type\":\"integer\",\"minimum\":1}");
        JSONSchema negative = JSONSchema.parseSchema("{\"type\":\"integer\",\"maximum\":-1}");
        assertTrue(positive.isValid(twoTo64));
        assertFalse(positive.isValid(twoTo64.negate()));
        assertTrue(negative.isValid(twoTo64.negate()));
        assertFalse(negative.isValid(twoTo64));
        JSONSchema multiples = JSONSchema.parseSchema("{\"type\":\"integer\",\"multipleOf\":3}");
        assertFalse(multiples.isValid(twoTo64));
        assertTrue(multiples.isValid(twoTo64.add(BigInteger.valueOf(2))));
        assertFalse(multiples.isValid(new BigDecimal(twoTo64)));
    }

    @Test
    public void nullPropertiesTriggerDependencies() {
        JSONSchema required = JSONSchema.parseSchema("{\"type\":\"object\",\"dependentRequired\":{\"a\":[\"b\"]}}");
        assertFalse(required.isValid(JSONObject.of("a", null)));
        assertTrue(required.isValid(JSONObject.of("a", null, "b", null)));
        assertTrue(required.isValid(new JSONObject()));
        JSONSchema dependent = JSONSchema.parseSchema("{\"type\":\"object\",\"dependentSchemas\":{\"a\":{\"required\":[\"b\"]}}}");
        assertFalse(dependent.isValid(JSONObject.of("a", null)));
        assertTrue(dependent.isValid(JSONObject.of("a", null, "b", null)));
        assertTrue(dependent.isValid(new JSONObject()));
    }

    @Test
    @Timeout(2)
    public void beanDependencyFailureTerminates() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"object\",\"dependentRequired\":{\"a\":[\"missing\"],\"b\":[\"other\"]}}");
        DependencyBean bean = new DependencyBean();
        bean.a = 1;
        ValidateResult result = schema.validate(bean);
        assertFalse(result.isSuccess());
        assertEquals("property a, dependentRequired property missing", result.getMessage());
        bean.a = null;
        bean.b = 1;
        result = schema.validate(bean);
        assertFalse(result.isSuccess());
        assertEquals("property b, dependentRequired property other", result.getMessage());
    }

    @Test
    public void absentBeanDependencyIsIgnored() {
        JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"object\",\"dependentRequired\":{\"absent\":[\"missing\"]}}");
        assertTrue(schema.isValid(new DependencyBean()));
    }

    @Test
    public void inferCollectionContainingOnlyNulls() {
        JSONSchema schema = JSONSchema.ofValue(Arrays.asList(null, null));
        assertEquals(JSON.parseObject("{\"type\":\"array\"}"), schema.toJSONObject());
        assertTrue(schema.isValid(Arrays.asList(null, null)));
    }

    public static class DependencyBean {
        public Integer a;
        public Integer b;
    }
}
