package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue2140 {
    @Test
    public void test() {
        String jsonString = "{\"ProjectAttr\":[{\"ZYAttr\":[{\"ProjectAttrID\":\"4777014153688645650\",\"BidNodeID\":1,\"AttrValue\":\"3.0\"}]}],\"ZYAttr\":[{\"$ref\":\"$.ProjectAttr[0].ZYAttr[0]\"}]}";
        JSONObject doc = JSON.parseObject(jsonString);
        assertSame(
                doc.getJSONArray("ProjectAttr").getJSONObject(0).getJSONArray("ZYAttr").get(0),
                doc.getJSONArray("ZYAttr").get(0)
        );
    }
}
