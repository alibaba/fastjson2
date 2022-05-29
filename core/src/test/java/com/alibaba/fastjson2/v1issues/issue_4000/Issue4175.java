package com.alibaba.fastjson2.v1issues.issue_4000;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue4175 {
    @Test
    public void test0() {
        String str = " {\"hasNextNode\":true,\"hasPreNode\":false,\"jump\":true,\"nextNode\":{\"hasNextNode\":true,\"hasPreNode\":true,\"jump\":true,\"nextNode\":{\"hasNextNode\":false,\"hasPreNode\":true,\"jump\":true,\"passType\":1,\"postId\":\"8422e7b960781225747439c620cd0919\",\"preNode\":{\"$ref\":\"..\"},\"sort\":3},\"passType\":1,\"postId\":\"1e1e159ad5f8293124208207aa4b76b1\",\"preNode\":{\"$ref\":\"..\"},\"sort\":2},\"passType\":1,\"postId\":\"b68aac95774a9682aeddb9a2d15a0c38\",\"preNode\":{\"hasNextNode\":false,\"hasPreNode\":false,\"jump\":false,\"sort\":0},\"sort\":1}";
        ApproveConfigJson config = JSON.parseObject(str, ApproveConfigJson.class);
        assertEquals(false, config.hasPreNode);
        assertSame(config, config.preNode);
    }

    @Data
    public static class ApproveConfigJson
            implements Serializable {
        private String postId;

        private boolean hasPreNode = false;

        private ApproveConfigJson preNode;

        private boolean hasNextNode = false;

        private ApproveConfigJson nextNode;
    }
}
