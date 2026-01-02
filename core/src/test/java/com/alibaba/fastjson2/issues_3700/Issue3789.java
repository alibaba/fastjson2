package com.alibaba.fastjson2.issues_3700;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Issue3789 {
    @Test
    public void test() {
        String name = "abc";

        assertThrows(Exception.class,
                () -> JSONObject.of("name", null).to(Bean.class));

        assertNull(JSONObject.of("name", null).to(Bean.class, JSONReader.Feature.IgnoreSetNullValue).name);
        assertNull(JSONObject.of().to(Bean.class).name);

        assertEquals(name,
                JSONObject.of("name", name).to(Bean.class).name);
    }

    public static class Bean {
        private String name;

        public void setName(String name) {
            if (name == null) {
                throw new IllegalArgumentException();
            }
            this.name = name;
        }
    }
}
