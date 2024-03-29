package com.alibaba.fastjson2.issues_2300;

import cn.hutool.core.lang.tree.Tree;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author 张治保
 * @since 2024/3/29
 */
public class Issue2375 {
    @Test
    void test() throws JSONException {
        String jsonString = "{\"name\":\"name\",\"id\":\"1\",\"extra\":\"extra\"}";
        Tree<String> tree = JSON.parseObject(jsonString, new TypeReference<Tree<String>>() {
        });
        JSONAssert.assertEquals(jsonString, JSON.toJSONString(tree), true);

        Tree tree2 = JSON.parseObject(jsonString, Tree.class);
        JSONAssert.assertEquals(jsonString, JSON.toJSONString(tree2), true);
    }
}
