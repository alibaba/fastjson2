package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2102 {
    @Test
    public void test() {
        byte[] bytes = new byte[]{
                123, 34, 109, 97, 112, 34, 58, 123, 49, 58, 49, 125, 125
        };
        String str = new String(bytes, StandardCharsets.UTF_8);
        BeanA beanA = JSON.parseObject(bytes, BeanA.class);
        assertEquals(1, beanA.map.size());
        {
            BeanB beanB = JSON.parseObject(bytes, BeanB.class, JSONReader.Feature.AllowUnQuotedFieldNames);
            assertNotNull(beanB);
        }
        {
            BeanB beanB = JSON.parseObject(str, BeanB.class, JSONReader.Feature.AllowUnQuotedFieldNames);
            assertNotNull(beanB);
        }
        {
            BeanB beanB = JSON.parseObject(str.toCharArray(), BeanB.class, JSONReader.Feature.AllowUnQuotedFieldNames);
            assertNotNull(beanB);
        }
    }

    public static class BeanA {
        private Map<Integer, Integer> map = new HashMap<>();

        public Map<Integer, Integer> getMap() {
            return map;
        }

        public void setMap(Map<Integer, Integer> map) {
            this.map = map;
        }
    }

    public static class BeanB {
    }
}
