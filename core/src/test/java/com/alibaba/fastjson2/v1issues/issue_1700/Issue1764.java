package com.alibaba.fastjson2.v1issues.issue_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1764 {
    @Test
    public void test_for_issue() throws Exception {
        Model model = new Model();
        model.value = 9007199254741992L;

        String str = JSON.toJSONString(model);
        assertEquals("{\"value\":\"9007199254741992\"}", str);
    }

    @Test
    public void test_for_issue_1() throws Exception {
        Model1 model = new Model1();
        model.value = 9007199254741992L;

        String str = JSON.toJSONString(model);
        assertEquals("{\"value\":\"9007199254741992\"}", str);
    }

    @Test
    public void test_for_issue_2() throws Exception {
        Model2 model = new Model2();
        model.value = 9007199254741992L;

        String str = JSON.toJSONString(model);
        assertEquals("{\"value\":\"9007199254741992\"}", str);
    }

    public static class Model {
        @JSONField(serializeFeatures = JSONWriter.Feature.BrowserCompatible)
        public long value;
    }

    public static class Model1 {
        @JSONField(serializeFeatures = JSONWriter.Feature.BrowserCompatible)
        private long value;

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    @JSONType(serializeFeatures = JSONWriter.Feature.BrowserCompatible)
    public static class Model2 {
        private long value;

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }
}
