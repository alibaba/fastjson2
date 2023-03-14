package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONFieldTest6 {
    @Test
    public void test() {
        String jsonStr = "{\"address\":\" 天津市 \"}";
        Bean bean = JSON.parseObject(jsonStr, Bean.class);
        assertEquals(" 天津市 ", bean.newAddress);
        String str2 = JSON.toJSONString(bean);
        assertEquals("{\"address\":\" 天津市 \"}", str2);
    }

    @Data
    public static class Bean {
        private String address;
        @JSONField(name = "address")
        private String newAddress;
    }
}
