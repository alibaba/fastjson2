package com.alibaba.fastjson2.issues_1000;

import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1338 {
    @Test
    public void test() {
        Dict dict = new Dict();
        String str = JSON.toJSONString(dict);
        assertEquals("{}", str);
        Dict dict1 = JSON.parseObject(str, Dict.class);
        assertEquals(dict, dict1);
    }

    @Test
    public void test1() {
        Dict dict = new Dict();
        dict.put("id", 123);
        String str = JSON.toJSONString(dict);
        assertEquals("{\"id\":123}", str);
        Dict dict1 = JSON.parseObject(str, Dict.class);
        assertEquals(dict, dict1);
    }

    @Test
    public void test2() {
        Dict dict = new Dict();
        dict.put("id", 123);
        String str = JSON.toJSONString(dict, JSONWriter.Feature.FieldBased);
        assertEquals("{\"id\":123}", str);
        Dict dict1 = JSON.parseObject(str, Dict.class);
        assertEquals(dict, dict1);
    }
}
