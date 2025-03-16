package com.alibaba.fastjson2.issues_3200;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3276 {
    @Test
    public void jsonSerializationTest3() {
        String json = "{\n" +
                "    \"c\":[\"1.1\"]\n" +
                "}";

        Bean bean = JSON.parseObject(json, Bean.class);
        assertEquals(1.1D, bean.getC());
    }

    @Test
    public void jsonSerializationTest2() {
        String json = "{\n" +
                "    \"b\":['1']\n" +
                "}";
        Bean bean = JSON.parseObject(json, Bean.class);
        assertEquals('1', bean.getB());
    }

    @Data
    public static class Bean {
        private Character b;
        private Double c;
    }
}
