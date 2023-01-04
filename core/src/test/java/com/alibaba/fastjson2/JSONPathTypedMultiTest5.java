package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class JSONPathTypedMultiTest5 {
    @Test
    public void test() {
        String str = JSONArray.of(
                JSONObject.of(
                        "firstName", "John",
                        "age", 27
                )
        ).toJSONString();

        JSONPath path = JSONPath.of(
                new String[]{
                        "$[0].firstName",
                        "$[0].age"
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
        Bean bean = new Bean();
        bean.firstName = "John";
        bean.age = 27;

        JSONPath path = JSONPath.of(
                new String[]{
                        "$[0].firstName",
                        "$[0].age"
                },
                new Type[]{
                        String.class,
                        Integer.class
                }
        );
        Object[] values = (Object[]) path.eval(new Object[]{bean});
        assertArrayEquals(
                new Object[]{
                        "John",
                        27
                },
                values
        );
    }

    public static class Bean {
        public String firstName;
        public Integer age;
    }
}
