package com.alibaba.fastjson2.issues_3700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3799 {
    @Test
    public void test() throws JsonProcessingException {
        assertEquals("\"String\"", JSON.toJSONString(DataType.STRING));
        assertEquals("\"STRING\"", JSON.toJSONString(DataType.STRING, JSONWriter.Feature.WriteEnumsUsingName));
        assertEquals("\"STRING\"", new ObjectMapper().writeValueAsString(DataType.STRING));
    }

    public enum DataType {
        STRING("String"),
        NUMBER("Number");

        @JsonProperty
        private String code;

        DataType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
