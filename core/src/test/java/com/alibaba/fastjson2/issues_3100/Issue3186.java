package com.alibaba.fastjson2.issues_3100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3186 {
    @Test
    public void test() {
        assertEquals("{}", JSON.toJSONString(new Bean(), JSONWriter.Feature.NotWriteDefaultValue));
        assertEquals("{}", new String(JSON.toJSONBytes(new Bean(), JSONWriter.Feature.NotWriteDefaultValue)));
    }

    @Getter
    @Setter
    public static class Bean {
        private boolean booleanV;
        private char charV;
        private byte byteV;
        private int intV;
        private short shortV;
        private float floatV;
        private double doubleV;
        private long longV;
    }
}
