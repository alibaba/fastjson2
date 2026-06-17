package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecordNamingStrategyTest {
    @JSONType(naming = PropertyNamingStrategy.SnakeCase)
    public record RecordDto(String currencyCode) {}

    public record GlobalRecordDto(String currencyCode) {}

    @JSONType(naming = PropertyNamingStrategy.SnakeCase)
    public record BooleanRecordDto(boolean isActive) {}

    @Test
    public void testRecordAnnotationNaming() {
        assertEquals("{\"currency_code\":\"840\"}", JSON.toJSONString(new RecordDto("840")));
        assertEquals(new RecordDto("840"), JSON.parseObject("{\"currency_code\":\"840\"}", RecordDto.class));
    }

    @Test
    public void testBooleanFieldNotStripped() {
        assertEquals("{\"is_active\":true}", JSON.toJSONString(new BooleanRecordDto(true)));
        assertEquals(new BooleanRecordDto(true), JSON.parseObject("{\"is_active\":true}", BooleanRecordDto.class));
    }

    @Test
    public void testRecordGlobalNaming() {
        ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
        PropertyNamingStrategy prev = provider.getNamingStrategy();
        try {
            provider.setNamingStrategy(PropertyNamingStrategy.SnakeCase);
            assertEquals("{\"currency_code\":\"840\"}", JSON.toJSONString(new GlobalRecordDto("840")));
        } finally {
            provider.setNamingStrategy(prev);
        }
    }
}
