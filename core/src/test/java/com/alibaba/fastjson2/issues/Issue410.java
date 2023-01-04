package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue410 {
    @Test
    public void test() {
        String json = "{\"key1\":11444-sdsdsd\"key2\":\"sdsdssdsda\"}";
        assertFalse(
                JSON.isValid(json)
        );

        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(json)
        );
    }
}
