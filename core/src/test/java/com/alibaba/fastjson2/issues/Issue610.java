package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("regression")
public class Issue610 {
    @Test
    public void test() {
        assertThrows(
                JSONException.class,
                () -> JSON.parse("{\"bbbb\":\"cccc\"\f[+D�HPndroid 10\"}")
        );
    }
}
