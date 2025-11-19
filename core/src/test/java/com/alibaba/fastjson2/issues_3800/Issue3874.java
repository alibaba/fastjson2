package com.alibaba.fastjson2.issues_3800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.LabelFilter;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;
import lombok.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3874 {
    @Test
    public void test() {
        Address address = Address.builder().name("zhang san").mobile("123456789").city("beijing").addr("花园大街").build();
        Order order = Order.builder().orderCode("456987987").userName("zhang san").userId("123456").address(address).build();

        testCompositePropertyPreFilter(order);
        testCompositePropertyFilter(order);
        testCompositeLabelFilter(order);
    }

    private static void testCompositePropertyPreFilter(Order order) {
        SimplePropertyPreFilter f1 = new SimplePropertyPreFilter(Address.class);
        f1.getExcludes().addAll(Arrays.asList("name", "mobile"));
        SimplePropertyPreFilter f2 = new SimplePropertyPreFilter(Order.class);
        f2.getExcludes().addAll(Arrays.asList("userName", "userId"));

        Filter[] filters = {f1, f2};
        String json = JSON.toJSONString(order, filters);
        assertEquals("{\"address\":{\"addr\":\"花园大街\",\"city\":\"beijing\"},\"orderCode\":\"456987987\"}", json);
    }

    private static void testCompositePropertyFilter(Order order) {
        PropertyFilter f1 = (object, name, value) -> !"zhang san".equals(value);
        PropertyFilter f2 = (object, name, value) -> !("userId".equals(name) || "mobile".equals(name));

        Filter[] filters = {f1, f2};
        String json = JSON.toJSONString(order, filters);
        assertEquals("{\"address\":{\"addr\":\"花园大街\",\"city\":\"beijing\"},\"orderCode\":\"456987987\"}", json);
    }

    private static void testCompositeLabelFilter(Order order) {
        LabelFilter f1 = "user"::equals;
        LabelFilter f2 = label -> !"sensitive".equals(label);

        Filter[] filters = {f1, f2};
        String json = JSON.toJSONString(order, filters);
        assertEquals("{\"orderCode\":\"456987987\",\"userId\":\"123456\",\"userName\":\"zhang san\"}", json);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Address {
        private String name;
        private String mobile;
        private String city;
        private String addr;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Order {
        @JSONField(label = "sensitive")
        private Address address;
        private String orderCode;
        @JSONField(label = "user")
        private String userName;
        private String userId;
    }
}
