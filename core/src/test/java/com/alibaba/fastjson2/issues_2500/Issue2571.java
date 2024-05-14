package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.lang.ref.ReferenceQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2571 {
    @Test
    public void test() {
        Bean bean = new Bean();
        String str = JSON.toJSONString(bean);
        assertEquals("{}", str);
        JSON.parseObject(str, Bean.class);
    }

    public static class Bean {
        public ReferenceQueue ref = new ReferenceQueue();
    }
}
