package com.alibaba.fastjson2.v1issues.issue_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class Issue1727 {
    @Test
    public void test_for_issue() throws Exception {
        String jsonString = "{\"gmtCreate\":\"20180131214157805-0800\"}";
        JSON.parseObject(jsonString, Model.class); //正常解析
        JSON.parseObject(jsonString).toJavaObject(Model.class);
    }

    public static class Model {
        @JSONField(format="yyyyMMddHHmmssSSSZ")
        public Date gmtCreate;
    }
}
