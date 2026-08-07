package com.alibaba.fastjson.issue_1100;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

/**
 * Created by wenshao on 02/05/2017.
 */
public class Issue1178 {
    @Test
    public void test_for_issue() throws Exception {
        String json = "{\n" +
                " \"info\": {\n" +
                "        \"test\": \"\", \n" +
                "    }\n" +
                "}";

        JSONObject jsonObject = JSON.parseObject(json);
    }

    public static class TestModel
            implements Serializable {
        public String info;
    }
}
