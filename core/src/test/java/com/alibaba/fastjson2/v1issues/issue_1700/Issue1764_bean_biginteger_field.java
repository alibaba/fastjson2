package com.alibaba.fastjson2.v1issues.issue_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import junit.framework.TestCase;

import java.math.BigInteger;

public class Issue1764_bean_biginteger_field extends TestCase {
    public void test_for_issue() throws Exception {
        assertEquals("{\"value\":\"9007199254741992\"}"
                , JSON.toJSONString(
                        new Model(9007199254741992L)));

        assertEquals("{\"value\":\"-9007199254741992\"}"
                , JSON.toJSONString(
                        new Model(-9007199254741992L)));

        assertEquals("{\"value\":9007199254740990}"
                , JSON.toJSONString(
                        new Model(9007199254740990L)));

        assertEquals("{\"value\":-9007199254740990}"
                , JSON.toJSONString(
                        new Model(-9007199254740990L)));

        assertEquals("{\"value\":100}"
                , JSON.toJSONString(
                        new Model(100)));

        assertEquals("{\"value\":-100}"
                , JSON.toJSONString(
                        new Model(-100)));
    }




    public static class Model {
        @JSONField(writeFeatures = JSONWriter.Feature.BrowserCompatible)
        public BigInteger value;

        public Model() {

        }

        public Model(long value) {
            this.value = BigInteger.valueOf(value);
        }
    }
}
