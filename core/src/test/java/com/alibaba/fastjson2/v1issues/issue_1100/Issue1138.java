package com.alibaba.fastjson2.v1issues.issue_1100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 10/04/2017.
 */
public class Issue1138 {
    @Test
    public void test_for_issue() throws Exception {
        Model model = new Model();
        model.id = 1001;
        model.name = "gaotie";

        // {"id":1001,"name":"gaotie"}
        String text_normal = JSON.toJSONString(model);
        assertEquals("{\"id\":1001,\"name\":\"gaotie\"}", text_normal);

        // [1001,"gaotie"]
        String text_beanToArray = JSON.toJSONString(model, JSONWriter.Feature.BeanToArray);
        assertEquals("[1001,\"gaotie\"]", text_beanToArray);
    }

    static class Model {
        public int id;
        public String name;
    }
}
