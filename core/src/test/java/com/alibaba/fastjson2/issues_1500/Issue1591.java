package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1591 {
    @Test
    public void test() {
        JSONArray array = new JSONArray();
        for (int i = 0; i < 10000; i++) {
            array.add(
                    JSONArray.of(
                            JSONArray.of(
                                    JSONArray.of(
                                            JSONArray.of(new JSONObject())
                                    )
                            )
                    )
            );
        }

        String str = JSON.toJSONString(array, JSONWriter.Feature.PrettyFormat);
        assertNotNull(str);
        assertEquals(array, JSON.parseArray(str));
    }

    @Test
    public void test1() {
        JSONArray array = new JSONArray();
        for (int i = 0; i < 10000; i++) {
            array.add(
                    JSONArray.of(
                            JSONArray.of(
                                    JSONArray.of(
                                            JSONArray.of(
                                                    JSONArray.of(
                                                            JSONArray.of()
                                                    )
                                            )
                                    )
                            )
                    )
            );
        }

        String str = JSON.toJSONString(array, JSONWriter.Feature.PrettyFormat);
        assertNotNull(str);
        assertEquals(array, JSON.parseArray(str));
    }
}
