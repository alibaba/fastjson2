package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2334 {
    @Test
    public void test() {
        String rawTx = "\"{\\\"data\\\":{\\\"signature\\\":\\\"xxx\\\",\\\"payment\\\":{\\\"to\\\":\\\"xxx\\\",\\\"from\\\":\\\"xxx\\\",\\\"fee\\\":\\\"200000000\\\",\\\"token\\\":\\\"1\\\",\\\"nonce\\\":\\\"0\\\",\\\"memo\\\":\\\"\\\",\\\"amount\\\":\\\"0\\\",\\\"valid_until\\\":\\\"0\\\"},\\\"stake_delegation\\\":null,\\\"create_token\\\":null,\\\"create_token_account\\\":null,\\\"mint_tokens\\\":null}}\"";
        JSONObject signedTx = JSONObject.parseObject(JSONObject.parse(rawTx).toString());
        assertNotNull(signedTx);
        assertEquals("xxx", signedTx.getJSONObject("data").getString("signature"));
    }
}
