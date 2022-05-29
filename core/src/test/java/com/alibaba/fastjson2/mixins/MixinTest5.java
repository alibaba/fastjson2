package com.alibaba.fastjson2.mixins;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MixinTest5 {
    @Test
    public void test() {
        JSON.mixIn(Product.class, ProductMixin.class);

        Product product = new Product();
        product.name = "DataWorks";
        assertEquals("{\"productName\":\"DataWorks\"}", JSON.toJSONString(product));

        Product productParsed = JSON.parseObject("{\"productName\":\"DataWorks\"}", Product.class);
        assertEquals("DataWorks", productParsed.name);
    }

    public static class Product {
        public String name;
    }

    public abstract class ProductMixin {
        @JSONField(name = "productName")
        String name;
    }
}
