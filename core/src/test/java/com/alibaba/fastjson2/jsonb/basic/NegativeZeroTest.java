package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("jsonb")
public class NegativeZeroTest {
    @Test
    public void testDouble() {
        assertNegativeZero((Double) JSONB.parse(JSONB.toBytes(-0D)));
        assertNegativeZero(JSONB.parseObject(JSONB.toBytes(-0D), Double.class));

        double[] primitiveArray = JSONB.parseObject(
                JSONB.toBytes(new double[]{-0D}),
                double[].class
        );
        assertNegativeZero(primitiveArray[0]);

        Double[] boxedArray = JSONB.parseObject(
                JSONB.toBytes(new Double[]{-0D}),
                Double[].class
        );
        assertNegativeZero(boxedArray[0]);
    }

    @Test
    public void testFloat() {
        assertNegativeZero((Float) JSONB.parse(JSONB.toBytes(-0F)));
        assertNegativeZero(JSONB.parseObject(JSONB.toBytes(-0F), Float.class));

        float[] primitiveArray = JSONB.parseObject(
                JSONB.toBytes(new float[]{-0F}),
                float[].class
        );
        assertNegativeZero(primitiveArray[0]);

        Float[] boxedArray = JSONB.parseObject(
                JSONB.toBytes(new Float[]{-0F}),
                Float[].class
        );
        assertNegativeZero(boxedArray[0]);
    }

    @Test
    public void testBean() {
        Bean bean = new Bean();
        bean.primitiveDouble = -0D;
        bean.boxedDouble = -0D;
        bean.primitiveFloat = -0F;
        bean.boxedFloat = -0F;

        Bean parsed = JSONB.parseObject(JSONB.toBytes(bean), Bean.class);
        assertNegativeZero(parsed.primitiveDouble);
        assertNegativeZero(parsed.boxedDouble);
        assertNegativeZero(parsed.primitiveFloat);
        assertNegativeZero(parsed.boxedFloat);
    }

    private static void assertNegativeZero(double value) {
        assertEquals(
                Double.doubleToRawLongBits(-0D),
                Double.doubleToRawLongBits(value)
        );
    }

    private static void assertNegativeZero(float value) {
        assertEquals(
                Float.floatToRawIntBits(-0F),
                Float.floatToRawIntBits(value)
        );
    }

    public static class Bean {
        public double primitiveDouble;
        public Double boxedDouble;
        public float primitiveFloat;
        public Float boxedFloat;
    }
}
