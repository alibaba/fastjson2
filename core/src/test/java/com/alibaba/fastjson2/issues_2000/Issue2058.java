package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2058 {
    @Test
    public void test() {
        String jsonStr = "{\"a\":{\"b\":{\"c\":\"xxxx\"},\"d\":{\"c\":\"qqqq\"}}}";
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        String path = "$.a.*.c";
        JSONPath jsonPath = JSONPath.of(
                new String[]{path},
                new Type[]{String.class}
        );

        Object resultMulti = ((Object[]) jsonPath.eval(jsonObject))[0];
        assertEquals("[\"xxxx\",\"qqqq\"]", resultMulti);
    }
}
