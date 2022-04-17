package com.alibaba.fastjson2.v1issues.issue_1100;

import com.alibaba.fastjson2.JSON;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;

/**
 * Created by wenshao on 11/04/2017.
 */
public class Issue1140 extends TestCase {
    public void test_for_issue() throws Exception {
        String s = "\uD83C\uDDEB\uD83C\uDDF7";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JSON.writeTo(out, s);

        String str = new String(out.toByteArray());
        assertEquals("\"\uD83C\uDDEB\uD83C\uDDF7\"", str);
    }
}
