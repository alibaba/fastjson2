package com.alibaba.fastjson2.internal.processor.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigDecimalTest {
    @Test
    public void test() {
        StringTest.Bean bean = new StringTest.Bean();
        assertEquals(
                "{\"name\":null}",
                JSON.toJSONString(bean, JSONWriter.Feature.WriteNulls)
        );
    }

    @JSONCompiled(debug = true)
    public static class Bean {
        public BigDecimal name;
    }
}
