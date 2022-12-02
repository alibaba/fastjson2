package com.alibaba.fastjson2.adapter.jackson.module.jsonSchema;

import com.alibaba.fastjson2.adapter.jackson.databind.JsonMappingException;
import com.alibaba.fastjson2.adapter.jackson.databind.ObjectMapper;

public class JsonSchemaGenerator {
    protected final ObjectMapper mapper;

    public JsonSchemaGenerator(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JsonSchema generateSchema(Class<?> type) throws JsonMappingException {
        // TODO
        throw new JsonMappingException("TODO");
    }
}
