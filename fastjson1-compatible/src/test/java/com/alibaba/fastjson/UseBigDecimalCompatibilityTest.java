package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UseBigDecimalCompatibilityTest {
    private static final String DECIMAL = "1E+100";
    private static final BigDecimal EXPECTED = new BigDecimal(DECIMAL);

    @Test
    public void testDefault() {
        Object scalar = JSON.parse(DECIMAL);
        assertEquals(BigDecimal.class, scalar.getClass());
        assertEquals(EXPECTED, scalar);

        JSONObject object = JSON.parseObject("{\"value\":" + DECIMAL + "}");
        assertEquals(BigDecimal.class, object.get("value").getClass());
        assertEquals(EXPECTED, object.get("value"));

        JSONArray array = JSON.parseArray('[' + DECIMAL + ']');
        assertEquals(BigDecimal.class, array.get(0).getClass());
        assertEquals(EXPECTED, array.get(0));
    }

    @Test
    public void testExplicitFeature() {
        Object value = JSON.parse(DECIMAL, Feature.UseBigDecimal);
        assertEquals(BigDecimal.class, value.getClass());
        assertEquals(EXPECTED, value);
    }

    @Test
    public void testDisabledFeature() {
        int features = JSON.DEFAULT_PARSER_FEATURE & ~Feature.UseBigDecimal.mask;
        Object value = JSON.parse(DECIMAL, features);
        assertEquals(Double.class, value.getClass());
        assertEquals(1E+100D, value);
    }
}
