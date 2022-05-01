package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue125 {
    @Test
    public void writeNonStringValueAsStringTest() {
        People people = new People();
        people.setAge(20);
        people.setAdult(true);
        people.setWeight(145.5);
        people.setWeight2(145.5f);
        String peopleStr = JSON.toJSONString(people, JSONWriter.Feature.WriteNonStringValueAsString);
        assertEquals("{\"adult\":\"true\",\"age\":\"20\",\"weight\":\"145.5\",\"weight2\":\"145.5\"}", peopleStr);

        JSONObject jsonObj = JSON.parseObject(peopleStr);
        assert (jsonObj.get("age") instanceof String);
        assert (jsonObj.get("adult") instanceof String);
        assert (jsonObj.get("weight") instanceof String);
    }

    @Data
    public static class People {
        public int age;
        public boolean adult;
        public double weight;
        public float weight2;
    }
}
