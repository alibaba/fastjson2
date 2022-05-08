package com.alibaba.fastjson;

import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONObjectTest6 {

    @Test
    public void test() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", 123);

        Model model = jsonObject.toJavaObject(Model.class);
        assertEquals(123, model.value);
    }

    public static class Model {
        public int value;
    }
}
