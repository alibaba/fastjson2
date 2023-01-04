package com.alibaba.fastjson.issue_2700;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2787 {
    @Test
    public void test_for_issue() throws Exception {
        Model m = new Model();
        String str = JSON.toJSONString(m, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty);
        assertEquals("{\"value\":[]}", str);
    }

    public static class Model {
        public int[] value;
    }
}
