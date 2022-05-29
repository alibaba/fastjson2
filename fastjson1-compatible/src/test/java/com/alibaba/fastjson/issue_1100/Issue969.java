package com.alibaba.fastjson.issue_1100;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 08/05/2017.
 */
public class Issue969 {
    @Test
    public void test_for_issue() throws Exception {
        JSONObject jsonObject = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(new Model());
        jsonObject.put("models", jsonArray);

        List<Model> list = jsonObject.getObject("models", new TypeReference<List<Model>>() {
        }.getType());

        assertEquals(1, list.size());
        assertEquals(Model.class, list.get(0).getClass());
    }

    @Test
    public void test_for_issue_1() throws Exception {
        JSONObject jsonObject = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(new Model());
        jsonObject.put("models", jsonArray);

        List list = jsonObject.getObject("models", new TypeReference<List<Model>>() {
        }.getType());

        assertEquals(1, list.size());
        assertEquals(Model.class, list.get(0).getClass());
    }

    public static class Model {
    }
}
