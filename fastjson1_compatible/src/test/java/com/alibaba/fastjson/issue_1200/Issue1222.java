package com.alibaba.fastjson.issue_1200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.serializer.SerializerFeature;
import junit.framework.TestCase;

/**
 * Created by wenshao on 01/06/2017.
 */
public class Issue1222 extends TestCase {
    public void test_for_issue() throws Exception {
        Model model = new Model();
        model.type = Type.A;
        String text = JSON.toJSONString(model, SerializerFeature.WriteEnumUsingToString);
        assertEquals("{\"type\":\"TypeA\"}", text);
    }

    public static class Model {
        public Type type;
    }

    public static enum Type {
        A, B;

        public String toString() {
            return "Type" + this.name() + "";
        }
    }
}
