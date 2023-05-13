package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1417 {
    @Test
    public void test() {
        String str = "{\"value\":Set[1]}";
        JSONReader jsonReader = JSONReader.of(str);
        Map<String, Object> object = jsonReader.readObject();
        Set value = (Set) object.get("value");
        assertEquals("[1]", JSON.toJSONString(value));
    }
}
