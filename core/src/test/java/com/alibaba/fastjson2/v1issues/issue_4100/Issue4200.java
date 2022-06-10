package com.alibaba.fastjson2.v1issues.issue_4100;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4200 {
    @Test
    public void test() {
        Object mockObj;
        try {
            mockObj = Mockito.mock(SomePOJO.class);
        } catch (Throwable ignored) {
            return;
        }

        Object object = JSON.toJSON(mockObj);
        assertEquals("{\"id\":0}", JSON.toJSONString(object));
        assertEquals("{\"id\":0}", JSON.toJSONString(mockObj));
    }

    public static class SomePOJO {
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
