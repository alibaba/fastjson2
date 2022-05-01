package com.alibaba.fastjson2.v1issues.issue_3300;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @Author ：Nanqi
 * @Date ：Created in 01:09 2020/8/2
 */
public class Issue3375 {
    @Test
    public void test_for_issue() throws Exception {
        List<Map<String, String>> models = new ArrayList<Map<String, String>>();
        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("name", "nanqi01");
        models.add(map1);

        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("name", "nanqi02");
        models.add(map2);

        for (Map<String, String> model : models) {
            String modelStr = JSON.toJSONString(model);
            Model modelObj = JSON.parseObject(modelStr, Model.class);
            assertTrue(modelObj.getName().contains("nanqi"));
        }
    }

    public static class Model {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
