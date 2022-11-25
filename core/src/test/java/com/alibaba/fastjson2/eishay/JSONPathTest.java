package com.alibaba.fastjson2.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPathTest {
    @Test
    public void test() {
        JSONPath path = JSONPath.of(
                new String[]{
                        "$.media.bitrate",
                        "$.media.duration"
                },
                new Type[]{
                        Integer.class,
                        Long.class
                }
        );

        Object[] result = (Object[]) path.extract(ParserTest.str);
        assertEquals("[262144,18000000]", JSON.toJSONString(result));
    }
}
