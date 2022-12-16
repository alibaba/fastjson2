package com.alibaba.fastjson2.issues;

import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue779 {
    @Test
    public void testJson() {
        String content1 = "{\"eskuSnList\":[\"abc\",\"cde\"]}";
        String content2 = "{\"eSkuSnList\":[\"abc\",\"cde\"]}";
        String content3 = "{\"ESkuSnList\":[\"abc\",\"cde\"]}";

        SimpleDto simpleDto2 = com.alibaba.fastjson2.JSON.parseObject(content1, SimpleDto.class);
        assertNotNull(simpleDto2.getESkuSnList());
        SimpleDto simpleDto21 = com.alibaba.fastjson2.JSON.parseObject(content2, SimpleDto.class);
        assertNotNull(simpleDto21.getESkuSnList());
        SimpleDto simpleDto22 = com.alibaba.fastjson2.JSON.parseObject(content3, SimpleDto.class);
        assertNotNull(simpleDto22.getESkuSnList());
    }

    @Data
    public static class SimpleDto {
        private List<String> eSkuSnList;
    }
}
