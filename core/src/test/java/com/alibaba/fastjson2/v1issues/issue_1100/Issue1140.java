package com.alibaba.fastjson2.v1issues.issue_1100;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 11/04/2017.
 */
public class Issue1140 {
    @Test
    public void test_for_issue() throws Exception {
        String s = "\uD83C\uDDEB\uD83C\uDDF7";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JSON.writeTo(out, s);

        String str = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertEquals("\"\uD83C\uDDEB\uD83C\uDDF7\"", str);
    }
}
