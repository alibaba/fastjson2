package com.alibaba.fastjson2_demo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MappingDemo {
    @Test
    public void test_0() {
        Product product = new Product();
        product.id = 1;
        product.name = "DataWorks";
        System.out.println(JSON.toJSONString(product));
        System.out.println(JSON.toJSONString(product, JSONWriter.Feature.BeanToArray));
    }
    @Test
    public void test_0_jsonb() {
        Product product = new Product();
        product.id = 1;
        product.name = "DataWorks";
        System.out.println(Arrays.toString(JSONB.toBytes(product)));
        System.out.println(Arrays.toString(JSONB.toBytes(product, JSONWriter.Feature.BeanToArray)));
    }
    @Test
    public void test_list() {
        List<Product> productList = new ArrayList<>();
        productList.add(new Product(1, "DataWorks"));
        productList.add(new Product(2, "MaxCompute"));
        productList.add(new Product(3, "EMR"));
        productList.add(new Product(4, "Holo"));

        System.out.println(JSON.toJSONString(productList));
        System.out.println(JSON.toJSONString(productList, JSONWriter.Feature.BeanToArray));
    }
    @Test
    public void test_list_jsonb() {
        List<Product> productList = new ArrayList<>();
        productList.add(new Product(1, "DataWorks"));
        productList.add(new Product(2, "MaxCompute"));
        productList.add(new Product(3, "EMR"));
        productList.add(new Product(4, "Holo"));

        System.out.println(Arrays.toString(JSONB.toBytes(productList)));
        System.out.println(Arrays.toString(JSONB.toBytes(productList, JSONWriter.Feature.BeanToArray)));
    }
    public static class Product {
        public int id;
        public String name;

        public Product() {
        }
        public Product(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
