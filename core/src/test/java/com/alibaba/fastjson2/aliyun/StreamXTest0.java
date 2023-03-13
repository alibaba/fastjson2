package com.alibaba.fastjson2.aliyun;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StreamXTest0 {
    @Test
    public void test1() {
        String path = "$.array[1].aa[2]";
        String jsonData = "{\"dynamic\":{\"c1\":1,\"c2\":[\"a\",\"b\"],\"c3\":{\"name\":\"john\"}},\"array\":[{\"aa\":[1,2,3]},"
                + "{\"aa\":[4,5,6]}],\"long\":1,\"bool\":true,\"double\":1.23,\"string\":\"test\"}";
        JSONPath jsonPath = JSONPath.of(new String[]{path}, new Type[]{String.class});
        Object[] data = (Object[]) jsonPath.extract(jsonData);
        assertEquals(data[0], "6");
    }

    @Test
    public void test2() {
        JSONPath jsonPath = JSONPath.of(
                new String[]{
                        "$.array[0].aa",
                        "$.array[1].aa",
                        "$.dynamic",
                        "$.long",
                        "$.bool",
                        "$.double"
                },
                new Type[]{
                        String.class,
                        String.class,
                        String.class,
                        String.class,
                        String.class,
                        String.class
                }
        );
        assertNotNull(jsonPath);
    }
}
