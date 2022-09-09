package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue743 {
    @Test
    public void test() {
        String json = "{\"xSpeed\":20.7,\"aName\":\"bug\"}";
        Bean t = JSON.parseObject(json, Bean.class);
        assertEquals(20.7F, t.xSpeed);
        assertEquals("bug", t.aName);
    }

    static class Bean {
        private float xSpeed;
        private String aName;

        public float getXSpeed() {
            return xSpeed;
        }

        public void setXSpeed(float xSpeed) {
            this.xSpeed = xSpeed;
        }

        public String getAName() {
            return aName;
        }

        public void setAName(String aName) {
            this.aName = aName;
        }
    }
}
