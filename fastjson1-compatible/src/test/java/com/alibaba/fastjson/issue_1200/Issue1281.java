package com.alibaba.fastjson.issue_1200;

import com.alibaba.fastjson.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Created by wenshao on 24/06/2017.
 */
public class Issue1281 {
    @Test
    public void test_for_issue() throws Exception {
        Type type1 = new TypeReference<Result<Map<String, Object>>>() {
        }.getType();
        Type type2 = new TypeReference<Result<Map<String, Object>>>() {
        }.getType();
        assertSame(type1, type2);
    }

    public static class Result<T> {
        public T value;
    }
}
