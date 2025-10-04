package com.alibaba.fastjson2.issues_3700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Issue3763 {
    @Test
    public void test() throws Throwable {
        String str = "{\"queryParam\":{},\"columnOrderParam\":[{\"field\":\"RTIME\",\"sortType\":\"DESC\"}],\"simpleQueryParam\":[],\"columnFilterParam\":[{\"field\":\"XXXX$USER_ID\",\"dateFormat\":\"\",\"value\":\"\",\"fieldValueRel\":\"like\",\"conditionRel\":\"and\",\"conditions\":[{\"expression\":\"eq\",\"value\":\"77c21b37d87e457e8669508310420ed7\"},{\"expression\":\"eq\",\"value\":\"\"}]}]}";

        final int taskCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(taskCount);
        Thread[] threads = new Thread[taskCount];
        for (int i = 0; i < taskCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    JSON.parseObject(str, new TypeReference<Map<String, Object>>() {
                    });
                    JSON.parseObject(str);
                } catch (InterruptedException ignored) {
                    // ignore
                } finally {
                    endLatch.countDown();
                }
            });
            threads[i].start();
        }
        startLatch.countDown();
        endLatch.await();
    }
}
