package com.alibaba.fastjson.issue_2000;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2012 {
    @Test
    public void test_for_issue() {
        Model foo = new Model();
        foo.bytes = new byte[0];
        String str = JSON.toJSONString(foo, SerializerFeature.WriteClassName);
        assertEquals("{\"@type\":\"com.alibaba.fastjson.issue_2000.Issue2012$Model\",\"bytes\":\"\"}", str);

        foo = (Model) JSON.parseObject(str, Object.class, Feature.SupportAutoType);
        assertEquals(0, foo.bytes.length);
    }

    public static class Model {
        public byte[] bytes;
    }
}
