package com.alibaba.fastjson2.springfox;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.springfox.SwaggerJsonWriter;
import org.junit.jupiter.api.Test;
import springfox.documentation.spring.web.json.Json;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SwaggerJsonWriterTest {
    String jsonStr = "{\"abc\":\"cde中文\"}";

    @Test
    public void test() {
        SwaggerJsonWriter writer = SwaggerJsonWriter.INSTANCE;
        writer.write(JSONWriter.of(), null);
        writer.write(JSONWriter.of(), new Json(jsonStr));
    }

    @Test
    public void test1() {
        JSONFactory.getDefaultObjectWriterProvider().register(Json.class, SwaggerJsonWriter.INSTANCE);
        assertEquals(jsonStr, JSON.toJSONString(new Json(jsonStr)));
        JSONFactory.getDefaultObjectWriterProvider().unregister(Json.class, SwaggerJsonWriter.INSTANCE);

    }
}
