package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue788 {
    @Test
    public void test() {
        String str = "{\"@type\":\"com.alibaba.fastjson2.issues.Issue788$GoodsDetailDTO\",\"goodsName\":\"HK GOLD香港黄金\",\"goodsPrice\":100.00}";
        GoodsDetailDTO dto = (GoodsDetailDTO) JSON.parseObject(str, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals("HK GOLD香港黄金", dto.goodsName);
        assertEquals("100.00", dto.goodsPrice.toString());
    }

    public static class GoodsDetailDTO {
        public String goodsName;
        public String goodsPrice;
    }
}
