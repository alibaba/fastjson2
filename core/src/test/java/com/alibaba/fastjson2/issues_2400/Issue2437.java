package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2437 {
    @Test
    public void test() {
        Bean bean = new Bean();
        String json = JSON.toJSONString(bean, JSONWriter.Feature.FieldBased);
        assertEquals("{}", json);
        Bean bean1 = JSON.parseObject(json, Bean.class);
        assertNotNull(bean1.lock);

        assertEquals("{}", JSON.toJSONString(new ReentrantLock()));
    }

    public static class Bean {
        public Lock lock = new ReentrantLock();
    }
}
