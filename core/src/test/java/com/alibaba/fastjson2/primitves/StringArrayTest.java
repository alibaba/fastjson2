package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringArrayTest {
    @Test
    public void test_writeNull() {
        assertEquals("{\"values\":null}",
                JSON.toJSONString(new VO(), JSONWriter.Feature.WriteNulls));
        assertEquals("{\"values\":null}",
                new String(
                        JSON.toJSONBytes(new VO(), JSONWriter.Feature.WriteNulls)));
    }

    @Test
    public void test_writeNull2() {
        assertEquals("{}",
                JSON.toJSONString(new VO2()));

        assertEquals("{\"values\":null}",
                new String(
                        JSON.toJSONBytes(new VO2(), JSONWriter.Feature.WriteNulls)));
    }

    public static class VO {
        public String[] values;
    }

    public static class VO2 {
        private String[] values;

        public String[] getValues() {
            return values;
        }

        public void setValues(String[] values) {
            this.values = values;
        }
    }
}
