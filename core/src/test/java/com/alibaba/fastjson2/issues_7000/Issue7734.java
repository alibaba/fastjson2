package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("regression")
@Tag("compat-jackson")
public class Issue7734 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.quantity = new BigDecimal("6.56000000001");
        assertEquals("{\"quantity\":\"6.56000000001\"}", JSON.toJSONString(bean));
    }

    @Test
    public void testIntegerScale() {
        Bean bean = new Bean();
        bean.quantity = new BigDecimal("200");
        assertEquals("{\"quantity\":\"200\"}", JSON.toJSONString(bean));
    }

    @Test
    public void testBigDecimalArray() {
        BeanArray bean = new BeanArray();
        bean.values = new BigDecimal[]{new BigDecimal("200"), new BigDecimal("300")};
        assertEquals("{\"values\":[\"200\",\"300\"]}", JSON.toJSONString(bean));
    }

    @Test
    public void testDouble() {
        BeanDouble bean = new BeanDouble();
        bean.price = 6.56;
        assertEquals("{\"price\":\"6.56\"}", JSON.toJSONString(bean));
    }

    public static class Bean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public BigDecimal quantity;
    }

    public static class BeanArray {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public BigDecimal[] values;
    }

    public static class BeanDouble {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Double price;
    }
}
