package com.alibaba.fastjson.issue_1200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by wenshao on 18/06/2017.
 */
public class Issue1272 {
    @Test
    public void test_for_issue1() throws Exception {
        Exception exception = null;

        try {
            JSON.toJSONString(new Point());
        } catch (JSONException ex) {
            exception = ex;
        }
        assertNotNull(exception);
        assertEquals(NullPointerException.class, exception.getCause().getClass());
    }

    @Test
    public void test_for_issue2() throws Exception {
        Exception exception = null;

        try {
            JSON.toJSONBytes(new Point());
        } catch (JSONException ex) {
            exception = ex;
        }
        assertNotNull(exception);
        assertEquals(NullPointerException.class, exception.getCause().getClass());
    }

    @Test
    public void test_for_issue3() throws Exception {
        Exception exception = null;

        try {
            JSON.writeJSONString(new ByteArrayOutputStream(), new Point());
        } catch (JSONException ex) {
            exception = ex;
        }
        assertNotNull(exception);
        assertEquals(NullPointerException.class, exception.getCause().getClass());
    }

    public static class Point {
        private Long userId;

        public long getUserId() {
            return userId;
        }
    }
}
