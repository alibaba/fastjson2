package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue3665 {
    @Test
    public void test() {
        String str = "{\n" +
                "  \"address\": \"中国\",\n" +
                "  \"kwhIncome\": \"\"\n" +
                "}";
        DeserializeBugDTO dto = JSON.parseObject(str, DeserializeBugDTO.class);
        assertEquals("中国", dto.getAddress());
        assertNull(dto.getKwhIncome());
    }

    @Test
    public void testNormalCase() {
        String str = "{\n" +
                "  \"address\": \"中国\",\n" +
                "  \"kwhIncome\": \"123.45\"\n" +
                "}";
        DeserializeBugDTO dto = JSON.parseObject(str, DeserializeBugDTO.class);
        assertEquals("中国", dto.getAddress());
        assertEquals(new BigDecimal("123.45"), dto.getKwhIncome());
    }

    public static class DeserializeBugDTO {
        private String address;
        private BigDecimal kwhIncome;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public BigDecimal getKwhIncome() {
            return kwhIncome;
        }

        public void setKwhIncome(BigDecimal kwhIncome) {
            this.kwhIncome = kwhIncome;
        }

        @Override
        public String toString() {
            return "DeserializeBugDTO{" +
                    "address='" + address + '\'' +
                    ", kwhIncome=" + kwhIncome +
                    '}';
        }
    }
}
