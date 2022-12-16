package com.alibaba.fastjson2.v1issues.issue_4300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4328 {
    @Test
    public void test() {
        JSONObject jsonObject = JSON.parseObject(json);

        // 使用fastjson的JSONPath.eval匹配的结果为空列表
        String fastjsonPath = "$..body[?(@.name == 'level3')]";
        Object result = JSONPath.eval(jsonObject, fastjsonPath);
        System.out.println("fastjson eval: " + result);
        assertEquals(
                "[{\"name\":\"level3\",\"field\":\"leve3Field\",\"body\":{\"name\":\"level4\",\"field\":\"leve4Field\"}}]",
                JSON.toJSONString(result)
        );
    }

    static final String json = "{\n" +
            "  \"body\": [\n" +
            "    {\n" +
            "      \"name\": \"level1\",\n" +
            "      \"field\": \"leve1Field\",\n" +
            "      \"body\": [\n" +
            "        {\n" +
            "          \"name\": \"level2\",\n" +
            "          \"field\": \"leve2Field\",\n" +
            "          \"body\": [\n" +
            "            {\n" +
            "              \"name\": \"level3\",\n" +
            "              \"field\": \"leve3Field\",\n" +
            "              \"body\": {\n" +
            "                \"name\": \"level4\",\n" +
            "                \"field\": \"leve4Field\"\n" +
            "              }\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
}
