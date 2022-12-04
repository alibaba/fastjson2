package com.alibaba.fastjson2.adapter.jackson.core;

import com.alibaba.fastjson2.adapter.jackson.databind.ObjectMapper;
import com.alibaba.fastjson2.adapter.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultPrettyPrinterTest {
    @Test
    public void test() throws Exception {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Staff staff = new Staff();
        staff.id = 1001;
        staff.name = "Jobs";

        String json = mapper.writeValueAsString(staff);
        assertEquals("{\n" +
                "\t\"id\":1001,\n" +
                "\t\"name\":\"Jobs\"\n" +
                "}", json);

        mapper.disable(SerializationFeature.INDENT_OUTPUT);

        assertEquals("{\"id\":1001,\"name\":\"Jobs\"}", mapper.writeValueAsString(staff));
    }

    public static class Staff {
        public int id;
        public String name;
    }
}
