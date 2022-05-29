package com.alibaba.fastjson2.v1issues.issue_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue1627 {
    @Test
    public void test_for_issue() throws Exception {
        String a = "{\"101a0.test-b\":\"tt\"}";
        Object o = JSON.parse(a);
        String s = "101a0.test-b";
        assertTrue(JSONPath
                .of("$." + escapeString(s))
                .contains(o)
        );
    }

    public static String escapeString(String s) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if ((c < 48 || c > 57) && (c < 65 || c > 90) && (c < 97 || c > 122)) {
                buf.append("\\" + c);
            } else {
                buf.append(c);
            }
        }

        return buf.toString();
    }
}
