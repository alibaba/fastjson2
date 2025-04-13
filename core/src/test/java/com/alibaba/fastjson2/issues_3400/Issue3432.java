package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3432 {
    @Test
    public void test_toJSON() {
        List<Node> nodes = new ArrayList<>();
        Node node = new Node();
        node.setGradeId("123");
        node.setPrivilegeIds(Arrays.asList("1", "2"));
        nodes.add(node);

        assertEquals(com.alibaba.fastjson.JSON.toJSON(nodes), JSON.toJSON(nodes, JSONWriter.Feature.ReferenceDetection));
    }

    @Data
    public static class Node {
        private String gradeId;
        private List<String> privilegeIds;
    }
}
