package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONTypeNamingKabab {
    @Test
    public void test0() {
        Bean bean = new Bean();
        bean.beanId = 101;
        bean.beanName = "DataWorks";
        assertEquals("{\"bean-id\":101,\"bean-name\":\"DataWorks\"}", JSON.toJSONString(bean));
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.beanId = 101;
        bean.beanName = "DataWorks";
        assertEquals("{\"bean-id\":101,\"bean-name\":\"DataWorks\"}", JSON.toJSONString(bean));
    }

    @JSONType(naming = PropertyNamingStrategy.KebabCase)
    public static class Bean {
        public int beanId;
        public String beanName;
    }

    @JSONType(naming = PropertyNamingStrategy.KebabCase)
    public static class Bean1 {
        private int beanId;
        private String beanName;

        public int getBeanId() {
            return beanId;
        }

        public void setBeanId(int beanId) {
            this.beanId = beanId;
        }

        public String getBeanName() {
            return beanName;
        }

        public void setBeanName(String beanName) {
            this.beanName = beanName;
        }
    }
}
