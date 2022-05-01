package com.alibaba.fastjson2.v1issues.issue_1300;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 03/08/2017.
 */
public class Issue1362 {
    @Test
    public void test_for_issue() throws Exception {
        JSONObject object = new JSONObject();
        object.put("val", "null");
        assertEquals(0D, object.getDoubleValue("val"));
        assertEquals(0F, object.getFloatValue("val"));
        assertEquals(0, object.getIntValue("val"));
        assertEquals(0L, object.getLongValue("val"));
        assertEquals((short) 0, object.getShortValue("val"));
        assertEquals((byte) 0, object.getByteValue("val"));
        assertEquals(false, object.getBooleanValue("val"));
    }
}
