package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;

@Tag("regression")
public class Issue2671 {
    @Test
    public void test() {
        final String str = "{\"context\":{\"$ref\":\"@\"}}";
        Map map = JSON.parseObject(str, Map.class);
        assertSame(map, map.get("context"));
    }
}
