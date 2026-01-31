package com.alibaba.fastjson2.issues_2800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2809 {
    @Test
    public void test() {
        Person p = new Person();
        Map<String, Boolean> map = new HashMap<>();
        map.put("btn1", true);
        map.put("btn2", false);
        p.setMap(map);
        p.setFormMeta(JSON.parseObject("{\"name\":\"xxx\",\"options\":{\"tableId\":11}}"));

        String json = JSON.toJSONString(p, JSONWriter.Feature.WriteClassName);
        Object object = JSON.parseObject(json, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(p.getFormMeta().toString(), ((Person) object).getFormMeta().toString());
    }

    @Data
    public class Person {
        private String name;
        private String age;
        private JSONObject formMeta;
        private Map<String, Boolean> map;
    }
}
