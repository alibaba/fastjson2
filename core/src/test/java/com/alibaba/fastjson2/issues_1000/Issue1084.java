package com.alibaba.fastjson2.issues_1000;

import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1084 {
    @Test
    public void test() {
        String s = "{\"code\":200,\"total\":1061,\"timezone\":\"\",\"message\":\"\\u9009\\u62e9\\u67e5\\u770b\\u5f53\\u524d\\u7b5b\\u9009\\u9879\\u548c\\u7ef4\\u5ea6\\uff0c\\u4e0d\\u652f\\u6301\\u7edf\\u8ba1dau,deu,arpu,newUsers,newUserRate,enterAdScene,enterAdSceneUsers,clickUsers,appRequest\", \"items\":[{\n" +
                "  \"date\": \"2022-12-06\",\n" +
                "  \"appId\": \"appId\",\n" +
                "  \"platform\": 1,\n" +
                "  \"packageName\": \"packageName\",\n" +
                "  \"placementId\": \"\",\n" +
                "  \"placementName\": \"\",\n" +
                "  \"adFormat\": 0,\n" +
                "  \"adFormatName\": \"\",\n" +
                "  \"area\": \"SO\",\n" +
                "  \"network\": 40,\n" +
                "  \"networkName\": \"TradPlus Exchange\",\n" +
                "  \"networkPlacementName\": \"\",\n" +
                "  \"networkPlacementId\": \"\",\n" +
                "  \"networkPlacementInfo\": \"\",\n" +
                "  \"adSceneId\": \"\",\n" +
                "  \"adSceneName\": \"\",\n" +
                "  \"dau\": 0,\n" +
                "  \"deu\": 0,\n" +
                "  \"arpu\": 0,\n" +
                "  \"newUsers\": 0,\n" +
                "  \"newUserRate\": 0,\n" +
                "  \"requestApi\": 0,\n" +
                "  \"fillApi\": 0,\n" +
                "  \"fillrateApi\": 0,\n" +
                "  \"impressionApi\": 0,\n" +
                "  \"clickApi\": 0,\n" +
                "  \"ecpmApi\": 0,\n" +
                "  \"ctrApi\": 0,\n" +
                "  \"Revenue\": 0,\n" +
                "  \"enterAdScene\": 0,\n" +
                "  \"enterAdSceneUsers\": 0,\n" +
                "  \"impression\": 0,\n" +
                "  \"impressionRatio\": 0,\n" +
                "  \"click\": 0,\n" +
                "  \"clickUsers\": 0,\n" +
                "  \"ctr\": 0,\n" +
                "  \"estimateRevenue\": 0,\n" +
                "  \"request\": 0,\n" +
                "  \"fill\": 0,\n" +
                "  \"fillrate\": 0,\n" +
                "  \"bidRequestApi\": 7,\n" +
                "  \"bidRequest\": 7,\n" +
                "  \"bidResponseApi\": 0,\n" +
                "  \"bidResponse\": 0,\n" +
                "  \"bidResponseRateApi\": 0,\n" +
                "  \"bidResponseRate\": 0,\n" +
                "  \"bidWinRateApi\": 0,\n" +
                "  \"bidWinRate\": 0,\n" +
                "  \"appRequest\": 0\n" +
                "}]}";

        com.alibaba.fastjson2.JSONObject parse = com.alibaba.fastjson2.JSONObject.parse(s);
        TradPlusAdVO tradPlusAdVO1
                = com.alibaba.fastjson2.JSONObject.parseObject(s, TradPlusAdVO.class);
        assertEquals(new BigDecimal("0"), tradPlusAdVO1.items.get(0).Revenue);
    }

    @Data
    public static class TradPlusAdVO
            implements Serializable {
        private static final long serialVersionUID = 1L;

        private List<TradPlusItem> items;

        private String timezone;
    }

    @Data
    public static class TradPlusItem
            implements Serializable {
        public static final long serialVersionUID = 1L;
        // 所属日期
        private String date;
        // appId
        private String appId;
        // 包名
        private String packageName;
        // 城市
        private String area;
        // 平台id
        private Integer network;
        // 平台名称
        private String networkName;
        // 收入
        private BigDecimal Revenue;
    }
}
