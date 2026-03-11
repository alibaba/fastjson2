package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("regression")
public class Issue1591 {
    /**
     * Regression test for writeFloat(float[]) buffer overflow when
     * WriteNonStringValueAsString is enabled. Max float representation
     * is 15 chars (e.g. "-1.00017205E-36"), and the capacity calculation
     * must account for comma + 2 quotes + number per element.
     */
    @Test
    public void testWriteFloatArrayAsString() {
        float worstCaseFloat = -1.00017205E-36f;
        for (int len : new int[]{1, 10, 100, 456, 500, 1000}) {
            float[] arr = new float[len];
            Arrays.fill(arr, worstCaseFloat);
            {
                String str = JSON.toJSONString(arr, JSONWriter.Feature.WriteNonStringValueAsString);
                assertNotNull(str);
            }
            {
                String str = JSON.toJSONString(arr, JSONWriter.Feature.WriteNonStringValueAsString, JSONWriter.Feature.OptimizedForAscii);
                assertNotNull(str);
            }
        }
    }

    @Test
    public void test() {
        JSONArray array = new JSONArray();
        for (int i = 0; i < 10000; i++) {
            array.add(
                    JSONArray.of(
                            JSONArray.of(
                                    JSONArray.of(
                                            JSONArray.of(new JSONObject())
                                    )
                            )
                    )
            );
        }

        {
            String str = JSON.toJSONString(array, JSONWriter.Feature.PrettyFormat);
            assertNotNull(str);
            assertEquals(array, JSON.parseArray(str));
        }
        {
            String str = JSON.toJSONString(array, JSONWriter.Feature.PrettyFormat, JSONWriter.Feature.OptimizedForAscii);
            assertNotNull(str);
            assertEquals(array, JSON.parseArray(str));
        }
    }

    @Test
    public void test1() {
        JSONArray array = new JSONArray();
        for (int i = 0; i < 10000; i++) {
            array.add(
                    JSONArray.of(
                            JSONArray.of(
                                    JSONArray.of(
                                            JSONArray.of(
                                                    JSONArray.of(
                                                            JSONArray.of()
                                                    )
                                            )
                                    )
                            )
                    )
            );
        }

        {
            String str = JSON.toJSONString(array, JSONWriter.Feature.PrettyFormat);
            assertNotNull(str);
            assertEquals(array, JSON.parseArray(str));
        }
        {
            String str = JSON.toJSONString(array, JSONWriter.Feature.PrettyFormat, JSONWriter.Feature.OptimizedForAscii);
            assertNotNull(str);
            assertEquals(array, JSON.parseArray(str));
        }
    }
}
