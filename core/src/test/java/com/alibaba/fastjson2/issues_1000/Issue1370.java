package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1370 {
    @Test
    public void test() {
        TokenModel tokenModel = new TokenModel();
        tokenModel.setId("token:login:session:10001");
        tokenModel.setCreateTime(1681578793249L);
        String str = JSON.toJSONString(tokenModel);
        tokenModel = JSON.parseObject(str, TokenModel.class, JSONReader.Feature.InitStringFieldAsEmpty);
        assertNotNull(tokenModel);
    }

    @Data
    @NoArgsConstructor
    static class TokenModel {
        String id;
        long createTime;
        Map<String, Object> dataMap = new ConcurrentHashMap<>();
        List<TokenSign> tokenSignList = new Vector<>();
    }

    static class TokenSign {
    }
}
