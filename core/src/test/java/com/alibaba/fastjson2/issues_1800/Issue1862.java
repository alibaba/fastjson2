package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1862 {
    @Test
    public void test() {
        String str = "{\"custom\":{},\"uePolicySet\":{\"vendorSpecific-000111\":{\"consumerAttrs\":{\"blobValue\":{\"DataOpt\":\"nidhi123\",\"Entitlements\":[\"ALL-IN\",\"OCS-SY\"],\"BillingPlanCode\":\"5GL03\",\"4GPFO\":\"DPRDTL\",\"5GPFO\":\"5GSPDTL\"}}}}}\n";

        String configuredPath = "uePolicySet[\"vendorSpecific-000111\"].consumerAttrs.blobValue";
        JSONObject jsonObject = (JSONObject) JSONPath.extract(str, configuredPath);
        assertEquals(
                "{\"DataOpt\":\"nidhi123\",\"Entitlements\":[\"ALL-IN\",\"OCS-SY\"],\"BillingPlanCode\":\"5GL03\",\"4GPFO\":\"DPRDTL\",\"5GPFO\":\"5GSPDTL\"}",
                jsonObject.toString()
        );
    }
}
