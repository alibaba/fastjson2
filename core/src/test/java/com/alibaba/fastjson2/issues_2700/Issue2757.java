package com.alibaba.fastjson2.issues_2700;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2757 {
    @Test
    public void test() throws Exception {
        Date expected = new Date(122, 2, 1, 12, 0, 0);
        String str = "{\"date\":\"01-03-2022 12:00:00\"}";

        Model model = JSON.parseObject(str, Model.class);
        assertEquals(expected, model.date);

        Model model1 = JSON.parseObject(str.toCharArray(), Model.class);
        assertEquals(expected, model1.date);

        Model model2 = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model.class);
        assertEquals(expected, model2.date);
    }

    public static class Model {
        public Date date;
    }
}
