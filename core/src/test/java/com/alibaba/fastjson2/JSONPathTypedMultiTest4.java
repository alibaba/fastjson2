package com.alibaba.fastjson2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class JSONPathTypedMultiTest4 {
    private String str;

    @BeforeEach
    public void setUp() {
        URL resource = JSONPathTypedMultiTest4.class.getClassLoader().getResource("data/d2.json");
        str = JSON.parseObject(resource).toJSONString();
    }

    @Test
    public void test() {
        JSONPath path = JSONPath.of(
                new String[]{
                        "$.firstName",
                        "$.age"
                },
                new Type[]{
                        String.class,
                        Integer.class
                }
        );
        Object[] values = (Object[]) path.extract(str);
        assertArrayEquals(
                new Object[]{
                        "John",
                        27
                },
                values
        );
    }

    @Test
    public void test1() {
        JSONPath path = JSONPath.of(
                new String[]{
                        "$.address.city",
                        "$.address.state"
                },
                new Type[]{
                        String.class,
                        String.class
                }
        );
        Object[] values = (Object[]) path.extract(str);
        assertArrayEquals(
                new Object[]{
                        "New York",
                        "NY"
                },
                values
        );
    }
}
