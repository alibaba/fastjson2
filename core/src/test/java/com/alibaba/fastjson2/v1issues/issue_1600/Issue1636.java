package com.alibaba.fastjson2.v1issues.issue_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1636 {
    @Test
    public void test_for_issue_1() throws Exception {
        Item1 item = JSON.parseObject("{\"modelId\":1001}", Item1.class);
        assertEquals(1001, item.modelId);
    }

    @Test
    public void test_for_issue_2() throws Exception {
        Item2 item = JSON.parseObject("{\"modelId\":1001}", Item2.class);
        assertEquals(1001, item.modelId);
    }

    public static class Item1 {
        @JSONField
        private int modelId;

        @JSONCreator
        public Item1(@JSONField int modelId) {
            // 这里为零
            this.modelId = modelId;
        }
    }

    public static class Item2 {
        private int modelId;

        @JSONCreator
        public Item2(int modelId) {
            // 这里为零
            this.modelId = modelId;
        }
    }
}
