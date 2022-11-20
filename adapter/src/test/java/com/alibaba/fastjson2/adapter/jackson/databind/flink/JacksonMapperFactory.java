package com.alibaba.fastjson2.adapter.jackson.databind.flink;

import com.alibaba.fastjson2.adapter.jackson.databind.ObjectMapper;
import com.alibaba.fastjson2.adapter.jackson.dataformat.csv.CsvMapper;

public class JacksonMapperFactory {
    public static ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }

    public static CsvMapper createCsvMapper() {
        final CsvMapper csvMapper = new CsvMapper();
        return csvMapper;
    }
}
