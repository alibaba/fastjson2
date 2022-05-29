package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONBTableTest5 {
    @Test
    public void test_d() {
        JSONObject object = JSONObject
                .of("values",
                        JSONArray.of(
                                JSONObject.of("id", 101).fluentPut("name", "DataWorks"),
                                JSONObject.of("id", 102).fluentPut("name", "MaxCompute")
                        )
                );

        byte[] bytes = JSONB.toBytes(object);

        JSONBDump.dump(bytes);

        Exception error = null;
        try {
            JSONB.parseObject(bytes, A.class);
        } catch (JSONException ex) {
            ex.printStackTrace();
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_1() {
        JSONObject object = JSONObject
                .of("@type", B.class.getName())
                .fluentPut("values",
                        JSONArray.of(
                                JSONObject.of("id", 101).fluentPut("name", "DataWorks"),
                                JSONObject.of("id", 102).fluentPut("name", "MaxCompute")
                        )
                );

        System.out.println(object);

        byte[] bytes = JSONB.toBytes(object, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.IgnoreErrorGetter);

        JSONBDump.dump(bytes);

        JSONObject b = (JSONObject) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertTrue(b.get("values") != null);
    }

    public static class A {
        public JSONObject values;
    }

    public static class B {
        public final JSONObject values;

        public B(JSONObject values) {
            this.values = values;
        }
    }
}
