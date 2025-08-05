package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3605 {
    @Test
    public void test() {
        String test = "{\"id\":0.3D}";
        assertEquals(0.3D,
                JSON.parseObject(test, Bean.class, JSONReader.Feature.FieldBased).getId());
        assertEquals(0.3D,
                JSON.parseObject(test, Bean.class).getId());
    }

    public static class Bean {
        private double id;

        public double getId() {
            return id;
        }

        public void setId(double id) {
            this.id = id;
        }
    }
}
