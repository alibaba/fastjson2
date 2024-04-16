package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

public class Issue2448 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.a1 = 1;
        bean.a2 = 2;
        bean.a3 = 3;
        String json = JSON.toJSONString(bean);
        System.out.println(json);
    }

    @JSONType(alphabetic = false)
    public static class Bean {
        public int a2;
        public int a1;
        public int a3;
    }
}
