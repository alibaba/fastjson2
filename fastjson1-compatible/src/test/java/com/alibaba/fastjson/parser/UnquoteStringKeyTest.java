package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnquoteStringKeyTest {
    @Test
    public void test() {
        String str = "{name: \"xx\",displayName: \"xxx\",description: \"x\",algorithmType: 0,usingType: 0,adminClass: \"x\",workingClass: \"x\",generationMeta:{keyLength: {displayName: \"x\",description: \"x\",allowInput: \"false\",valueType: \"int\",validValues: [\"128\",\"192\",\"256\"],defaultValue: \"256\"}},workingMeta:{workMode: {displayName: \"x\",description: \"x\",allowInput: \"false\",valueType: \"string\",validValues: [\"ECB\", \"CBC\", \"CFB\", \"OFB\", \"GCM\"],defaultValue: \"x\"},padding: {displayName: \"x\",description: \"x\",allowInput: \"false\",valueType: \"string\",validValues: [\"x\", \"x\", \"x\"],defaultValue: \"x\"},initVector: {displayName: \"初\",description: \"x\",allowInput: \"false\",valueType: \"string\",validValues: [\"x\", \"x\", \"x\"],defaultValue: \"x\"}}}";
        JSONObject object = JSON.parseObject(str);
        assertEquals("xx", object.get("name"));
    }

    @Test
    public void test1() {
        assertEquals(1, JSON.parseObject("{items:{k1:{id:123}}}", Bean.class).items.size());
    }

    public static class Bean {
        public Map<String, Item> items;
    }

    public static class Item {
        public int id;
    }
}
