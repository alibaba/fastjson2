package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1060 {
    @Test
    public void test() {
        String str = "{\"createTime\":\"2023-01-09 15:38:13\"}";
        assertNotNull(JSON.parseObject(str, BizDTO.class).getCreateTime());
        assertNotNull(JSON.parseObject(str.toCharArray(), BizDTO.class).getCreateTime());
        assertNotNull(JSON.parseObject(str.getBytes(), BizDTO.class).getCreateTime());
        assertNotNull(JSON.parseObject(str).toJavaObject(BizDTO.class).getCreateTime());
    }

    @Data
    public static class BizDTO
            implements Serializable {
        private Long createTime;
    }
}
