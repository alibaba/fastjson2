package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;


public class Issue3917 {

    @Test
    public void testLongScientificNotation() {
        String json = "{\"a\":1.4900000000000002396241300002543983538316751946695148944854736328125e-7}";

        JSONObject obj = JSONObject.parse(json, JSONReader.Feature.UseBigDecimalForDoubles, JSONReader.Feature.UseBigDecimalForFloats);

        BigDecimal expected = new BigDecimal("1.4900000000000002396241300002543983538316751946695148944854736328125e-7");
        BigDecimal actual = obj.getBigDecimal("a");

        assertEquals(expected, actual, "应该正确解析长科学计数法");
    }


    @Test
    public void testLongScientificNotationWithClass() {
        String json = "{\"value\":1.4900000000000002396241300002543983538316751946695148944854736328125e-7}";

        BigDecimalHolder holder = JSON.parseObject(json, BigDecimalHolder.class);
        BigDecimal expected = new BigDecimal("1.4900000000000002396241300002543983538316751946695148944854736328125e-7");

        assertEquals(expected, holder.value);
    }


    @Test
    public void testScientificNotationEdgeCases() {
        String[][] testCases = {{"{\"a\":1.23e-100}", "1.23e-100"}, {"{\"a\":9.99e+50}", "9.99e+50"}, {"{\"a\":1.0e-7}", "1.0e-7"}, {"{\"a\":1e10}", "1e10"}, {"{\"a\":-1.23e-5}", "-1.23e-5"}, {"{\"a\":1.234567890123456789012345678901234567890e-10}", "1.234567890123456789012345678901234567890e-10"}, {"{\"a\":0.0e10}", "0.0"}, {"{\"a\":1.5e5}", "1.5e5"}, {"{\"a\":1.23e100}", "1.23e100"},};

        for (String[] testCase : testCases) {
            JSONObject obj = JSONObject.parse(testCase[0], JSONReader.Feature.UseBigDecimalForDoubles, JSONReader.Feature.UseBigDecimalForFloats);

            BigDecimal expected = new BigDecimal(testCase[1]);
            BigDecimal actual = obj.getBigDecimal("a");

            assertEquals(expected, actual, "Failed for: " + testCase[0]);
        }
    }


    @Test
    public void testWithoutBigDecimalFeature() {
        String json = "{\"a\":1.23e-10}";

        JSONObject obj = JSONObject.parse(json);
        Object value = obj.get("a");

        assertInstanceOf(Double.class, value, "不设置feature应返回Double");
        assertEquals(1.23e-10, (Double) value, 1e-20);
    }


    @Test
    public void testNegativeScientificNotation() {
        String json = "{\"a\":-1.4900000000000002396241300002543983538316751946695148944854736328125e-7}";

        JSONObject obj = JSONObject.parse(json, JSONReader.Feature.UseBigDecimalForDoubles, JSONReader.Feature.UseBigDecimalForFloats);

        BigDecimal expected = new BigDecimal("-1.4900000000000002396241300002543983538316751946695148944854736328125e-7");
        BigDecimal actual = obj.getBigDecimal("a");

        assertEquals(expected, actual);
    }


    @Test
    public void testPositiveExponentScientificNotation() {
        String json = "{\"a\":1.4900000000000002396241300002543983538316751946695148944854736328125e+7}";

        JSONObject obj = JSONObject.parse(json, JSONReader.Feature.UseBigDecimalForDoubles, JSONReader.Feature.UseBigDecimalForFloats);

        BigDecimal expected = new BigDecimal("1.4900000000000002396241300002543983538316751946695148944854736328125e+7");
        BigDecimal actual = obj.getBigDecimal("a");

        assertEquals(expected, actual);
    }


    @Test
    public void testLowercaseEScientificNotation() {
        String json = "{\"a\":1.49e-7}";

        JSONObject obj = JSONObject.parse(json, JSONReader.Feature.UseBigDecimalForDoubles, JSONReader.Feature.UseBigDecimalForFloats);

        BigDecimal expected = new BigDecimal("1.49e-7");
        BigDecimal actual = obj.getBigDecimal("a");

        assertEquals(expected, actual);
    }


    @Test
    public void testUppercaseEScientificNotation() {
        String json = "{\"a\":1.49E-7}";

        JSONObject obj = JSONObject.parse(json, JSONReader.Feature.UseBigDecimalForDoubles, JSONReader.Feature.UseBigDecimalForFloats);

        BigDecimal expected = new BigDecimal("1.49E-7");
        BigDecimal actual = obj.getBigDecimal("a");

        assertEquals(expected, actual);
    }


    static class BigDecimalHolder {
        public BigDecimal value;
    }
}
