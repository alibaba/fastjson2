package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("regression")
public class Issue1058 {
    @Test
    public void test() throws Exception {
        assertThrows(JSONException.class, () -> JSON.parseArray("[{[]}]"));
    }
}
