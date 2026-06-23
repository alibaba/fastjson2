package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
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
        ObjectWriterProvider writerProvider = JSONFactory.getDefaultObjectWriterProvider();
        ObjectReaderProvider readerProvider = JSONFactory.getDefaultObjectReaderProvider();
        PropertyNamingStrategy prevWriter = writerProvider.getNamingStrategy();
        PropertyNamingStrategy prevReader = readerProvider.getNamingStrategy();
        try {
            writerProvider.setNamingStrategy(PropertyNamingStrategy.SnakeCase);
            readerProvider.setNamingStrategy(PropertyNamingStrategy.SnakeCase);
            assertEquals("{\"currency_code\":\"840\"}", JSON.toJSONString(new GlobalRecordDto("840")));
            assertEquals(new GlobalRecordDto("840"), JSON.parseObject("{\"currency_code\":\"840\"}", GlobalRecordDto.class));
        } finally {
            writerProvider.setNamingStrategy(prevWriter);
            readerProvider.setNamingStrategy(prevReader);
        }
    }
}
