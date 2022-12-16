package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue716 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 123;
        String str = JSON.toJSONString(bean);
        assertEquals(str, "{\"id\":123}");
    }

    public static class Bean {
        @JsonIgnore(value = false)
        public int id;
    }
}
