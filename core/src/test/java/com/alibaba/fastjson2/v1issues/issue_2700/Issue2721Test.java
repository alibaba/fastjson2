package com.alibaba.fastjson2.v1issues.issue_2700;

import com.alibaba.fastjson.JSONPath;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue2721Test {
    @Test
    public void test2721() {
        String chineseKeyString = "[{\"名称\": \"脆皮青豆\", \"配料\": [\"豌豆\", \"棕榈油\", \"白砂糖\", \"食用盐\", \"玉米淀粉\"]}]";
        assertEquals("[{\"名称\":\"脆皮青豆\",\"配料\":[\"豌豆\",\"棕榈油\",\"白砂糖\",\"食用盐\",\"玉米淀粉\"]}]",
                JSONPath.extract(chineseKeyString, "$[?(@.名称 = '脆皮青豆')]").toString());
        // [{"名称":"脆皮青豆","配料":["豌豆","棕榈油","白砂糖","食用盐","玉米淀粉"]}]

        String normalKeyString = "[{ \"name\": \"脆皮青豆\", \"配料\": [\"豌豆\", \"棕榈油\", \"白砂糖\", \"食用盐\", \"玉米淀粉\"] }]";
        assertEquals("[{\"name\":\"脆皮青豆\",\"配料\":[\"豌豆\",\"棕榈油\",\"白砂糖\",\"食用盐\",\"玉米淀粉\"]}]",
                JSONPath.extract(normalKeyString, "$[?(@.name = '脆皮青豆')]").toString());
        // [{"name":"脆皮青豆","配料":["豌豆","棕榈油","白砂糖","食用盐","玉米淀粉"]}]
//
        assertFalse(((List) JSONPath.extract(chineseKeyString, "$[?(@.名称 = '脆皮青豆')]")).isEmpty());
        assertFalse(((List) JSONPath.extract(normalKeyString, "$[?(@.name = '脆皮青豆')]")).isEmpty());
    }
}
