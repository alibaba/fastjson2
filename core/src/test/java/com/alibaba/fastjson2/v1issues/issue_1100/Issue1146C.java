package com.alibaba.fastjson2.v1issues.issue_1100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson.annotation.JSONType;
import junit.framework.TestCase;

/**
 * Created by wenshao on 14/04/2017.
 */
public class Issue1146C extends TestCase {
    public void test_for_issue() throws Exception {
        String json = JSON.toJSONString(new Test());
        assertEquals("{\"id\":101}", json);
    }

    @JSONType(ignores = {"id2", "id3"})
    public static class Test {

        public int getId() {
            return 101;
        }
        public int getId2() {
            return 102;
        }
        public int getId3() {
            return 103;
        }
    }
}
