package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Issue599 {
    @Test
    public void test() {
        String str = "{\"success\":true}";
        Bean bean = JSON.parseObject(str, Bean.class, JSONReader.Feature.FieldBased);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertFalse(bean.isSuccess);
        assertTrue(bean1.isSuccess);

        String str1 = JSON.toJSONString(bean);
        assertEquals("{\"success\":false}", str1);

        Bean testB1 = new Bean();
        testB1.setSuccess(true);
        String str2 = JSON.toJSONString(testB1);
        assertEquals("{\"success\":true}", str2);
    }

    @Data
    public static class Bean {
        private boolean isSuccess;
    }
}
