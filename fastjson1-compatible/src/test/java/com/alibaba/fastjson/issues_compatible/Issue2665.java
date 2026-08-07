package com.alibaba.fastjson.issues_compatible;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2665 {
    @Test
    public void test() {
        String json = "[\r\n"
                + "    {\r\n"
                + "        \"date\": \"2024-06-10\",\r\n"
                + "        \"slots\": [\r\n"
                + "            {\r\n"
                + "                \"createDate\": \"2024-06-10T11:54:58.240+01:00\",\r\n"
                + "                \"expireDate\": \"2024-06-10T11:59:58.240+01:00\",\r\n"
                + "                \"id\": 1176592662,\r\n"
                + "                \"intervention\": {\r\n"
                + "                    \"code\": \"FS\",\r\n"
                + "                    \"name\": \"FA Simple\"\r\n"
                + "                },\r\n"
                + "                \"timeslot\": {\r\n"
                + "                    \"code\": \"AM\",\r\n"
                + "                    \"end\": \"13:00\",\r\n"
                + "                    \"name\": \"Morning\",\r\n"
                + "                    \"start\": \"08:00\"\r\n"
                + "                }\r\n"
                + "            }\r\n"
                + "        ]\r\n"
                + "    },\r\n"
                + "    {\r\n"
                + "        \"date\": \"2024-06-10\",\r\n"
                + "        \"slots\": [\r\n"
                + "            {\r\n"
                + "                \"createDate\": \"2024-05-22T11:54:58.240+01:00\",\r\n"
                + "                \"expireDate\": \"2024-05-22T11:59:58.240+01:00\",\r\n"
                + "                \"id\": 1176592671,\r\n"
                + "                \"intervention\": {\r\n"
                + "                    \"code\": \"FS\",\r\n"
                + "                    \"name\": \"FA Simple\"\r\n"
                + "                },\r\n"
                + "                \"timeslot\": {\r\n"
                + "                    \"code\": \"PM\",\r\n"
                + "                    \"end\": \"18:00\",\r\n"
                + "                    \"name\": \"Afternoon\",\r\n"
                + "                    \"start\": \"13:00\"\r\n"
                + "                }\r\n"
                + "            }\r\n"
                + "        ]\r\n"
                + "    }\r\n"
                + "]";

        JSONArray jsonArr = JSONArray.parseArray(json);
        List list = jsonArr.toJavaObject(List.class);
        assertEquals(JSON.toJSONString(jsonArr), JSON.toJSONString(list));
    }
}
