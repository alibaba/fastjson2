package com.alibaba.fastjson2.issues_2300;

import cn.hutool.core.lang.tree.Tree;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.Serializable;
import java.util.List;

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

    @Test
    void test2() throws JsonProcessingException {
        String jsonString = "{\"treeList\":[{\"name\":\"name\",\"id\":\"1\",\"extra\":\"extra\"}]}";
        DemoVO2 vo2 = JSON.parseObject(jsonString, DemoVO2.class);
        List<Tree<String>> list = vo2.getTreeList();
        DemoVO2 read = new ObjectMapper().readValue(jsonString, DemoVO2.class);
        List<Tree<String>> list1 = read.getTreeList();
        Assertions.assertEquals(list, list1);
    }

    private static class DemoVO2
            implements Serializable {
        private static final long serialVersionUID = 1L;
        List<Tree<String>> treeList;

        public List<Tree<String>> getTreeList() {
            return treeList;
        }

        public void setTreeList(List<Tree<String>> treeList) {
            this.treeList = treeList;
        }
    }
}
