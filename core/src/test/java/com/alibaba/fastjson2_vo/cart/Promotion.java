package com.alibaba.fastjson2_vo.cart;

import java.util.List;

public class Promotion
        extends CartObject<Promotion.PromotionFields> {
    public static class PromotionFields {
        public String promotionType;
        public List<String> titles;
    }
}
