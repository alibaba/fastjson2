package com.alibaba.fastjson2.issues_3100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
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

    @Test
    public void test1() {
        assertEquals("{}", JSON.toJSONString(new Bean1(), JSONWriter.Feature.NotWriteDefaultValue));
        assertEquals("{}", new String(JSON.toJSONBytes(new Bean1(), JSONWriter.Feature.NotWriteDefaultValue)));
    }

    @Getter
    @Setter
    public static class Bean1 {
        private byte byteV;
    }

    @Test
    public void test2() {
        assertEquals("{}", JSON.toJSONString(new Bean2()));
        assertEquals("{}", new String(JSON.toJSONBytes(new Bean2())));
    }

    @Getter
    @Setter
    @JSONType(serializeFeatures = JSONWriter.Feature.NotWriteDefaultValue)
    public static class Bean2 {
        private boolean booleanV;
        private char charV;
        private byte byteV;
        private int intV;
        private short shortV;
        private float floatV;
        private double doubleV;
        private long longV;
    }

    @Test
    public void test3() {
        assertEquals("{}", JSON.toJSONString(new Bean3()));
        assertEquals("{}", new String(JSON.toJSONBytes(new Bean3())));
    }

    @Getter
    @Setter
    @JSONType(serializeFeatures = JSONWriter.Feature.NotWriteDefaultValue)
    public static class Bean3 {
        private byte byteV;
    }

    @Test
    public void test4() {
        assertEquals("{}", JSON.toJSONString(new Bean4()));
        assertEquals("{}", new String(JSON.toJSONBytes(new Bean4())));
    }

    @Getter
    @Setter
    @JSONType(serializeFeatures = JSONWriter.Feature.NotWriteDefaultValue)
    public static class Bean4 {
        private char charV;
    }
}
