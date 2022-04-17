package com.alibaba.fastjson2.v1issues.issue_1100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import junit.framework.TestCase;

/**
 * Created by wenshao on 02/05/2017.
 */
public class Issue1177 extends TestCase {
    public void test_for_issue() throws Exception {
        String text = "{\"a\":{\"b\":\"c\",\"g\":{\"e\":\"f\"}},\"d\":{\"a\":\"f\",\"h\":[\"s1\"]}} ";
        JSONObject jsonObject = JSON.parseObject(text);
        Object eval = JSONPath.eval(jsonObject, "$..a");
        assertNotNull(eval);
    }
}
