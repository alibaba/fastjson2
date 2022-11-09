package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue861 {
    @Test
    public void test() {
        JSONObject object = JSONObject
                .of("date", 0);

        Date date = object
                .getDate("date");
        assertEquals(0L, date.getTime());

        assertEquals(0, object.to(Bean.class).date.getTime());
    }

    public static class Bean {
        public Date date;
    }
}
