package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue529 {
    @Test
    public void test() {
        URL resource = Issue529.class.getClassLoader().getResource("issue529.json");
        DTO dto = JSON.parseObject(resource, DTO.class);
        assertNotNull(dto);
        assertEquals(1, dto.checkedSkuList.get(0).cartType);
    }
    public static class DTO {
        public Map skuPromotionDetail;
        public Map storeCoupons;
        public List<Sku> skuList;
        public PriceDetailDTO priceDetailDTO;
        public List<Sku> checkedSkuList;
    }

    public static class Sku {
        public int cartType;
    }

    public static class PriceDetailDTO {
    }
}
