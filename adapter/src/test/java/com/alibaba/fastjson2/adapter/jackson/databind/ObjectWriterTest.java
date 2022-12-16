package com.alibaba.fastjson2.adapter.jackson.databind;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectWriterTest {
    @Test
    public void test() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader(Bean.class);

        String str = "{\"id\":123}";
        Bean bean = reader.readValue(mapper.createParser(str));
        assertEquals(123, bean.id);

        Bean bean1 = reader.readValue(str, Bean.class);
        assertEquals(123, bean1.id);

        Bean bean2 = reader.readValue(str.getBytes(StandardCharsets.UTF_8));
        assertEquals(123, bean2.id);

        Bean bean3 = mapper.reader().readValue(str, Bean.class);
        assertEquals(123, bean3.id);
    }

    public static class Bean {
        public int id;
    }
}
