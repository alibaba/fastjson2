package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author 张治保
 * @since 2024/3/8
 */
public class Issue2302 {
    @Test
    void test() {
        TestA a = new TestA();
        a.areaIds.add(1000);
        String jsonStr = JSON.toJSONString(a);
        TestA newTest = JSON.parseObject(jsonStr, TestA.class);
        Assertions.assertEquals(newTest.areaIds, a.areaIds);
    }

    private static class TestA {
        public Set<Integer> areaIds = new HashSet<>();
    }
}
