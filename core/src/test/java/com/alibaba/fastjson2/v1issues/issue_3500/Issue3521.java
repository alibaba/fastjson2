package com.alibaba.fastjson2.v1issues.issue_3500;

import com.alibaba.fastjson2.JSON;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue3521 {
    @Test
    public void test_for_issue() throws Exception {
//         assertFalse(JSON.isValid("{\"cat\":\"dog\"\"cat\":\"dog\"}")); // TODO true
    }
}
