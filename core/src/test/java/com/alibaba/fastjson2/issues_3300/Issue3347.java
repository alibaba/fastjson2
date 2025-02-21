package com.alibaba.fastjson2.issues_3300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

public class Issue3347 {
    @Test
    public void test() {
        String json2 = "{\"*/*\":{\"schema\":{\"$ref\":\"Error-ModelName{namespace='javax.servlet.http', name='HttpServletResponse'}\"}}}";
        System.out.println(json2);
        JSONObject jsonObject4 = JSON.parseObject(json2, JSONReader.Feature.DisableReferenceDetect);
    }
}
