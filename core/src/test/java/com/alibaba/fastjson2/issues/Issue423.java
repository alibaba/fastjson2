package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue423 {
    @Test
    public void testFloat() {
        float[] a = {0f, 0.0f, 0.1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        assertEquals("[0.0,0.0,0.1,null,null,null]", JSON.toJSONString(a));
        assertEquals("null", JSON.toJSONString(Float.NaN));
        assertEquals("null", JSON.toJSONString(Float.POSITIVE_INFINITY));
        assertEquals("null", JSON.toJSONString(Float.NEGATIVE_INFINITY));
    }

    @Test
    public void testFloat1() {
        float[] a = {0f, 0.0f, 0.1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeAny(a);
            assertEquals("[0.0,0.0,0.1,null,null,null]", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.writeAny(a);
            assertEquals("[0.0,0.0,0.1,null,null,null]", jsonWriter.toString());
        }

        float[] array = {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        for (float value : array) {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeFloat(value);
            assertEquals("null", jsonWriter.toString());
        }
        for (float value : array) {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.writeFloat(value);
            assertEquals("null", jsonWriter.toString());
        }
    }

    @Test
    public void testDouble1() {
        double[] a = {0D, 0.0D, 0.1D, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeAny(a);
            assertEquals("[0.0,0.0,0.1,null,null,null]", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.writeAny(a);
            assertEquals("[0.0,0.0,0.1,null,null,null]", jsonWriter.toString());
        }

        double[] array = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double value : array) {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeDouble(value);
            assertEquals("null", jsonWriter.toString());
        }
        for (double value : array) {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.writeDouble(value);
            assertEquals("null", jsonWriter.toString());
        }
    }
}
