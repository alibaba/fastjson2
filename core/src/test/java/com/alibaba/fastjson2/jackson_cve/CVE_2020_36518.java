package com.alibaba.fastjson2.jackson_cve;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson2.util.JDKUtils.JVM_VERSION;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CVE_2020_36518 {
    private static final int TOO_DEEP_NESTING;

    static {
        if (JVM_VERSION <= 11) {
            TOO_DEEP_NESTING = 1000;
        } else {
            TOO_DEEP_NESTING = 2000;
        }
    }

    @Test
    public void testWithArray() throws Exception {
        final String doc = _nestedDoc(TOO_DEEP_NESTING, "[ ", "] ");
        Object ob = JSON.parseObject(doc, Object.class);
        assertTrue(ob instanceof List<?>);
    }

    @Test
    public void testWithObject() throws Exception {
        final String doc = "{" + _nestedDoc(TOO_DEEP_NESTING, "\"x\":{", "} ") + "}";
        Object ob = JSON.parseObject(doc, Object.class);
        assertTrue(ob instanceof Map<?, ?>);
    }

    private String _nestedDoc(int nesting, String open, String close) {
        StringBuilder sb = new StringBuilder(nesting * (open.length() + close.length()));
        for (int i = 0; i < nesting; ++i) {
            sb.append(open);
            if ((i & 31) == 0) {
                sb.append("\n");
            }
        }
        for (int i = 0; i < nesting; ++i) {
            sb.append(close);
            if ((i & 31) == 0) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
