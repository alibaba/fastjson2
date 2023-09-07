package com.alibaba.fastjson2.v1issues.issue_4300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class Issue4309 {
    @Test
    public void test() {
        byte[] bytes = {0x1a, 0x5b};
        String s = new String(bytes, StandardCharsets.US_ASCII);
        assertThrows(JSONException.class,
                () -> JSON.parseObject(s, new TypeReference<ArrayList<Integer>>() {})
        );
    }
}
