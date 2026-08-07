package com.alibaba.fastjson.issue_1200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 01/06/2017.
 */
public class Issue1222_1 {
    @Test
    public void test_for_issue() throws Exception {
        Model model = new Model();
        model.type = Type.A;
        String text = JSON.toJSONString(model, SerializerFeature.WriteEnumUsingToString);
        assertEquals("{\"type\":\"TypeA\"}", text);
    }

    private static class Model {
        public Type type;
    }

    private static enum Type {
        A, B;

        public String toString() {
            return "Type" + this.name();
        }
    }
}
