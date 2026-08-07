package com.alibaba.fastjson2.config;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FastJsonConfigTest {
    @Test
    public void test() throws Exception {
        FastJsonConfig fastJsonConfig = new FastJsonConfig();

        fastJsonConfig.setJSONB(true);
        assertEquals(fastJsonConfig.isJSONB(), true);
        fastJsonConfig.setWriteContentLength(false);
        assertEquals(fastJsonConfig.isWriteContentLength(), false);
        fastJsonConfig.setCharset(Charset.forName("UTF-8"));
        assertEquals(fastJsonConfig.getCharset().name(), "UTF-8");
        fastJsonConfig.setDateFormat("yyyyMMdd");
        assertEquals(fastJsonConfig.getDateFormat(), "yyyyMMdd");
        fastJsonConfig.setReaderFeatures(JSONReader.Feature.FieldBased);
        assertEquals(fastJsonConfig.getReaderFeatures()[0], JSONReader.Feature.FieldBased);
        fastJsonConfig.setWriterFeatures(JSONWriter.Feature.FieldBased);
        assertEquals(fastJsonConfig.getWriterFeatures()[0], JSONWriter.Feature.FieldBased);
        fastJsonConfig.setReaderFilters(new ContextAutoTypeBeforeHandler(new String[]{
                "org.springframework.security.core.context.SecurityContextImpl"
        }));
        assertEquals(fastJsonConfig.getReaderFilters().length > 0, true);
        fastJsonConfig.setWriterFilters(new SimplePropertyPreFilter());
        assertEquals(fastJsonConfig.getWriterFilters().length > 0, true);
        fastJsonConfig.setSymbolTable("id");
        assertEquals(fastJsonConfig.getSymbolTable().size() > 0, true);
    }
}
