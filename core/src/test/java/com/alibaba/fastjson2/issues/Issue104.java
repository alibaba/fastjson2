package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue104 {
    @Test
    public void format() {
        String str = "{\n" +
                "\"passengerId\":\"220802200005217748\",\n" +
                "\"firstName\":\"Hong\",\n" +
                "\"lastName\":\"Test\",\n" +
                "\"age\":99,\n" +
                "\"telephone\":\"13104362222\"\n" +
                "}";

        JSONObject object = JSON.parseObject(str);

        String pretty = JSON.toJSONString(object, JSONWriter.Feature.PrettyFormat);
        assertEquals("{\n" +
                "\t\"passengerId\":\"220802200005217748\",\n" +
                "\t\"firstName\":\"Hong\",\n" +
                "\t\"lastName\":\"Test\",\n" +
                "\t\"age\":99,\n" +
                "\t\"telephone\":\"13104362222\"\n" +
                "}", pretty);
    }

    @Test
    public void format_2() {
        String str = "{\n" +
                "\"passengerId\":\"220802200005217748\",\n" +
                "\"firstName\":\"Hong\",\n" +
                "\"lastName\":\"Test\",\n" +
                "\"age\":99,\n" +
                "\"telephone\":\"13104362222\"\n" +
                "}";

        Passenger object = JSON.parseObject(str, Passenger.class);

        String pretty = JSON.toJSONString(object, JSONWriter.Feature.PrettyFormat);
        assertEquals("{\n" +
                "\t\"age\":99,\n" +
                "\t\"firstName\":\"Hong\",\n" +
                "\t\"lastName\":\"Test\",\n" +
                "\t\"passengerId\":\"220802200005217748\",\n" +
                "\t\"telephone\":\"13104362222\"\n" +
                "}", pretty);

        byte[] prettyBytes = JSON.toJSONBytes(object, JSONWriter.Feature.PrettyFormat);
        assertEquals("{\n" +
                "\t\"age\":99,\n" +
                "\t\"firstName\":\"Hong\",\n" +
                "\t\"lastName\":\"Test\",\n" +
                "\t\"passengerId\":\"220802200005217748\",\n" +
                "\t\"telephone\":\"13104362222\"\n" +
                "}", new String(prettyBytes));
    }

    public static class Passenger {
        public String passengerId;
        public String firstName;
        public String lastName;
        public int age;
        public String telephone;
    }
}
