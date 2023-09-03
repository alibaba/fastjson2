package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue1821 {
    @Test
    public void test() {
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        "[1,,3]",
                        new TypeReference<List<Integer>>() {})
        );
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        "[1,,3]",
                        new TypeReference<List<Long>>() {})
        );
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        "[1,,3]",
                        new TypeReference<List<String>>() {})
        );
    }
}
