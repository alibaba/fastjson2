package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue606 {
    @Test
    public void test() {
        TestVo testVo = new TestVo();
        testVo.k = "text";
        testVo.extVo = new ExtVo();
        testVo.extVo.k = "ext";

        String str = JSON.toJSONString(testVo, JSONWriter.Feature.BrowserCompatible,
                JSONWriter.Feature.WriteMapNullValue,
                JSONWriter.Feature.WriteNullNumberAsZero,
                JSONWriter.Feature.WriteNullStringAsEmpty,
                JSONWriter.Feature.WriteNullListAsEmpty,
                JSONWriter.Feature.WriteNullBooleanAsFalse,
                JSONWriter.Feature.WriteNonStringKeyAsString);
        assertEquals("{\"extVo\":{\"k\":\"ext\",\"v\":null},\"k\":\"text\",\"v\":null}", str);
    }

    static class ExtVo {
        String k;
        Object v;

        public String getK() {
            return k;
        }

        public void setK(String k) {
            this.k = k;
        }

        public Object getV() {
            return v;
        }

        public void setV(Object v) {
            this.v = v;
        }
    }

    static class TestVo {
        String k;

        @JSONField(serializeFeatures = {
                JSONWriter.Feature.BrowserCompatible,
                JSONWriter.Feature.WriteNullNumberAsZero,
                JSONWriter.Feature.WriteNullStringAsEmpty,
                JSONWriter.Feature.WriteNullListAsEmpty,
                JSONWriter.Feature.WriteNullBooleanAsFalse,
                JSONWriter.Feature.WriteNonStringKeyAsString
        })
        Object v;
        ExtVo extVo;

        public String getK() {
            return k;
        }

        public void setK(String k) {
            this.k = k;
        }

        public Object getV() {
            return v;
        }

        public void setV(Object v) {
            this.v = v;
        }

        public ExtVo getExtVo() {
            return extVo;
        }

        public void setExtVo(ExtVo extVo) {
            this.extVo = extVo;
        }
    }
}
