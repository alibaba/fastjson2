package com.alibaba.fastjson.issue_1200;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

/**
 * Created by wenshao on 11/06/2017.
 */
public class Issue1205 {
    @Test
    public void test_for_issue() throws Exception {
        JSONArray array = new JSONArray();
        array.add(new JSONObject());

//        List<Model> list = array.toJavaObject(new TypeReference<List<Model>>(){});
//        assertEquals(1, list.size());
//        assertEquals(Model.class, list.get(0).getClass());
    }

    public static class Model {
    }
}
