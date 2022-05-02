package com.alibaba.json.bvt.issue_2200;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2201 {
    @Test
    public void test_for_issue() throws Exception {
//        ParserConfig.getGlobalInstance().register("M2001", Model.class);

        String json = "{\"@type\":\"M2001\",\"id\":3}";
        Model m = (Model) JSON.parseObject(json, Object.class);
        assertEquals(3, m.id);
    }

    public static class Model {
        public int id;
    }
}
