package com.alibaba.fastjson.issue_1100;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

/**
 * Created by wenshao on 11/04/2017.
 */
public class Issue1140 {
    @Test
    public void test_for_issue() throws Exception {
        String s = "\uD83C\uDDEB\uD83C\uDDF7";

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        JSON.writeJSONString(out, s);
    }
}
