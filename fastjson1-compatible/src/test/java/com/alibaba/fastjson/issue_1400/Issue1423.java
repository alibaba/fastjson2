package com.alibaba.fastjson.issue_1400;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1423 {
    @Test
    public void test_for_issue() throws Exception {
        Exception error = null;
        try {
            JSON.parseObject("{\"v\":9223372036854775808}", LongVal.class);
        } catch (JSONException e) {
            e.printStackTrace();
            error = e;
        }
        assertNotNull(error);
        error.printStackTrace();
    }

    @Test
    public void test_for_issue_reader() throws Exception {
        Exception error = null;
        try {
            new JSONReader(new StringReader("{\"v\":9223372036854775808}")).readObject(LongVal.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
    }

    @Test
    public void test_for_issue_arrayMapping() throws Exception {
        Exception error = null;
        try {
            JSON.parseObject("[9223372036854775808]", LongVal.class, Feature.SupportArrayToBean);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
    }

    @Test
    public void test_for_issue_arrayMapping_reader() throws Exception {
        Exception error = null;
        try {
            new JSONReader(new StringReader("[9223372036854775808]"), Feature.SupportArrayToBean).readObject(LongVal.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        error.printStackTrace();
    }

    public static class LongVal {
        private long v;

        public void setV(long v) {
            this.v = v;
        }

        @Override
        public String toString() {
            return String.valueOf(v);
        }
    }
}
