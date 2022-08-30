package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.filter.NameFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue682 {
    @Test
    public void test() {
        String str = "{\n" +
                "\"userName\":\"john\",\n" +
                "\"userAge\":20,\n" +
                "\"cityName\":\"beijing\"\n" +
                "}";

        JSONObject jsonObject = JSON.parseObject(str);

        assertEquals(
                "{\"user_name\":\"john\",\"user_age\":20,\"city_name\":\"beijing\"}",
                JSON.toJSONString(jsonObject, NameFilter.of(PropertyNamingStrategy.LowerCaseWithUnderScores))
        );
        assertEquals(
                "{\n" +
                        "\t\"user_name\":\"john\",\n" +
                        "\t\"user_age\":20,\n" +
                        "\t\"city_name\":\"beijing\"\n" +
                        "}",
                JSON.toJSONString(jsonObject, NameFilter.of(PropertyNamingStrategy.LowerCaseWithUnderScores), JSONWriter.Feature.PrettyFormat)
        );
    }
}
