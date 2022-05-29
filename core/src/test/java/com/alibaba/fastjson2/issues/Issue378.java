package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class Issue378 {
    @Test
    public void test() {
        List<ApproveConfigJson> approveConfigJsons = new ArrayList<>();
        approveConfigJsons.add(new ApproveConfigJson("1", 1));
        approveConfigJsons.add(new ApproveConfigJson("2", 2));
        approveConfigJsons.add(new ApproveConfigJson("3", 3));

        ApproveConfigJson finalNode = list2LinkedJson2(approveConfigJsons);
        String s = JSON.toJSONString(finalNode, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.PrettyFormat);

        {
            ApproveConfigJson parsed = JSON.parseObject(s, ApproveConfigJson.class);
            assertEquals(1, parsed.getSort());

            assertSame(parsed, parsed.nextNode.preNode);
            assertSame(parsed.nextNode, parsed.nextNode.nextNode.preNode);
        }

        {
            ApproveConfigJson parsed = JSON.parseObject(s.getBytes(StandardCharsets.UTF_8), ApproveConfigJson.class);
            assertEquals(1, parsed.getSort());

            assertSame(parsed, parsed.nextNode.preNode);
            assertSame(parsed.nextNode, parsed.nextNode.nextNode.preNode);
        }

        {
            JSONReader jsonReader = TestUtils.createJSONReaderStr(s);
            ApproveConfigJson parsed = jsonReader.read(ApproveConfigJson.class);
            jsonReader.handleResolveTasks(parsed);
            assertEquals(1, parsed.getSort());

            assertSame(parsed, parsed.nextNode.preNode);
            assertSame(parsed.nextNode, parsed.nextNode.nextNode.preNode);
        }

        {
            byte[] jsonbBytes = JSONB.toBytes(finalNode, JSONWriter.Feature.ReferenceDetection);
            ApproveConfigJson parsed = JSONB.parseObject(jsonbBytes, ApproveConfigJson.class);
            assertEquals(1, parsed.getSort());

            assertSame(parsed, parsed.nextNode.preNode);
            assertSame(parsed.nextNode, parsed.nextNode.nextNode.preNode);
        }
    }

    public static ApproveConfigJson list2LinkedJson2(List<ApproveConfigJson> approveConfigJsons) {
        approveConfigJsons = approveConfigJsons.stream().sorted(Comparator.comparing(ApproveConfigJson::getSort)).collect(Collectors.toList());
        ApproveConfigJson approveUtil = null;
        Iterator<ApproveConfigJson> iterator = approveConfigJsons.iterator();
        ApproveConfigJson local = new ApproveConfigJson();
        while (iterator.hasNext()) {
            ApproveConfigJson next = iterator.next();
            if (approveUtil == null) {
                approveUtil = next;
                local = next;
            } else {
                approveUtil.setHasNextNode(true);
                approveUtil.setNextNode(next);
                next.setHasPreNode(true);
                next.setPreNode(local);
                local = next;
                approveUtil = approveUtil.getNextNode();
                approveUtil.setNextNode(null);
            }
        }

        ApproveConfigJson finalNode = approveUtil;
        while (finalNode.isHasPreNode()) {
            finalNode = finalNode.getPreNode();
        }

        return finalNode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApproveConfigJson
            implements Serializable {
        private String postId;
        private boolean hasPreNode;
        private ApproveConfigJson preNode;
        private boolean hasNextNode;
        private ApproveConfigJson nextNode;
        private int sort;

        public ApproveConfigJson(String postId, int sort) {
            this.postId = postId;
            this.sort = sort;
        }
    }
}
