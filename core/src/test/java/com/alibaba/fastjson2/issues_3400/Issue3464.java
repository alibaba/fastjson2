package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3464 {
    @Test
    public void test() {
        Data data = new Data(TimeUnit.MINUTES);
        JSONObject json = (JSONObject) JSON.toJSON(data, JSONWriter.Feature.WriteEnumsUsingName);
        assertEquals(json.get("unit").getClass().toString(), "class java.lang.String");
    }

    @Getter
    @AllArgsConstructor
    private static class Data {
        private TimeUnit unit;
    }
}
