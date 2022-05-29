package com.alibaba.fastjson2.v1issues.issue_1200;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 11/06/2017.
 */
public class Issue1205 {
    @Test
    public void test_for_issue() throws Exception {
        JSONArray array = new JSONArray();
        array.add(new JSONObject());

        List<Model> list = array.toJavaObject(new TypeReference<List<Model>>() {
        }.getType());
        assertEquals(1, list.size());
        assertEquals(Model.class, list.get(0).getClass());
    }

    public static class Model {
    }
}
