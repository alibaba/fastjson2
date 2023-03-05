package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

public class Issue1191 {
    @Test
    public void test() {
        String json = "[0, 1, 2, 3]";
        JSONPath path = JSONPath.of(new String[]{"$[3]", "$[4]"}, new Type[]{Long.class, Long.class});
        Object result = path.extract(json);
        System.out.println(JSON.toJSONString(result));
    }
}
