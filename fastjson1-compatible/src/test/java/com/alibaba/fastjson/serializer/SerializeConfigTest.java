package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONArray;
import org.junit.jupiter.api.Test;

public class SerializeConfigTest {
    @Test
    public void test() {
        SerializeConfig config = new SerializeConfig();
        config.addFilter(Bean.class, new SimplePropertyPreFilter("id"));

        config.put(JSONArray.class, ListSerializer.instance);
    }

    public static class Bean {
    }
}
