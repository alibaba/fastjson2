package com.alibaba.fastjson2.read;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FJ1x {
    @Test
    public void object() {
        String str = "{\"properties\":{\"id\":123}}";
        Bean bean = JSON.parseObject(str, Bean.class);
        assertNotNull(bean.properties);
        assertEquals(123, bean.properties.get("id"));
    }

    public static class Bean {
        public JSONObject properties;
    }
}
