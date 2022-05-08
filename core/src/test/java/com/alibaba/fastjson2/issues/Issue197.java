package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.dingtalk.api.response.OapiGettokenResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue197 {
    @Test
    public void test() {
        String respBody = "{\"errcode\":0,\"access_token\":\"ABCEE2306d883e85bb733a50f045925f\",\"errmsg\":\"ok\",\"expires_in\":7200}";
        OapiGettokenResponse rsp = JSON.parseObject(respBody, OapiGettokenResponse.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals("ABCEE2306d883e85bb733a50f045925f", rsp.getAccessToken());
    }
}
