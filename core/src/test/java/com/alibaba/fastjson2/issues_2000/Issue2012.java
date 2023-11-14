package com.alibaba.fastjson2.issues_2000;

import cn.hutool.json.XML;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2012 {
    @Test
    public void test() {
        String resp = "{\"prnd\":1699865962262,\"code\":0,\"msg\":\"success\",\"data\":\"<?xml version='1.0' encoding='utf-8'?><businessDataResponse><cardType>身份证</cardType><cardNo>123</cardNo><name>xxx</name><beginTime>2022-11-13</beginTime><endTime>2023-11-13</endTime><results></results></businessDataResponse>\"}";
        JSONObject jsonObject = JSONObject.parseObject(resp);
        String data = jsonObject.getString("data");
        String json = XML.toJSONObject(data).getJSONObject("businessDataResponse").toString();
        BlooddonationqueryVO blooddonationqueryVO = JSONObject.parseObject(json, BlooddonationqueryVO.class);
        assertEquals("{\"beginTime\":\"2022-11-13\",\"cardNo\":\"123\",\"cardType\":\"身份证\",\"endTime\":\"2023-11-13\",\"name\":\"xxx\"}", JSON.toJSONString(blooddonationqueryVO));
    }
}
