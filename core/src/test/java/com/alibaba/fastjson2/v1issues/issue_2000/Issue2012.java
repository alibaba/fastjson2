package com.alibaba.fastjson2.v1issues.issue_2000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteClassName;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2012 {
    @Test
    public void test_for_issue() {
        Model foo = new Model();
        foo.bytes = new byte[0];
        String str = JSON.toJSONString(foo, WriteClassName);
        assertEquals("{\"@type\":\"com.alibaba.fastjson2.v1issues.issue_2000.Issue2012$Model\",\"bytes\":[]}", str);

        foo = (Model) JSON.parseObject(str, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(0, foo.bytes.length);
    }

    public static class Model {
        public byte[] bytes;
    }
}
