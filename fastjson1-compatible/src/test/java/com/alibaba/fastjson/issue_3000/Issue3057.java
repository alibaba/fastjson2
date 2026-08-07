package com.alibaba.fastjson.issue_3000;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3057 {
    @Test
    public void test_for_issue() throws Exception {
        String str = "{\"q\":[]}";
        Bean bean = JSON.parseObject(str, Bean.class);
        assertEquals(0, bean.q.size());
    }

    public static class Bean {
        public java.util.Deque q;
    }
}
