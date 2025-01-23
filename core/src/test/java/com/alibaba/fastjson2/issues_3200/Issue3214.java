package com.alibaba.fastjson2.issues_3200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue3214 {
    @Test
    public void test() {
        String json = "{\n"
                + "  \"k1\":{\n"
                + "    \"k2\":[\n"
                + "      {\n"
                + "        {} : {}\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "}";
        JSONObject jsonObject = JSON.parseObject(json);
        assertNotNull(jsonObject);
    }

    @Test
    public void test1() {
        JSONReader.Context context = JSONFactory.createReadContext();
        context.setObjectSupplier(com.alibaba.fastjson.JSONObject::new);
        String json = "{\n"
                + "  \"k1\":{\n"
                + "    \"k2\":[\n"
                + "      {\n"
                + "        {} : {}\n"
                + "      }\n"
                + "    ]\n"
                + "  }\n"
                + "}";
        com.alibaba.fastjson.JSONObject jsonObject = (com.alibaba.fastjson.JSONObject) JSON.parse(json, context);
        assertNotNull(jsonObject);
    }
}
