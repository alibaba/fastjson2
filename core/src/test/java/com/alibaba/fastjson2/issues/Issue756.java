package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue756 {
    @Test
    public void test() {
        NoneSerializable noneSerializable = new NoneSerializable();
        noneSerializable.setParam("Test");
        byte[] bytes = JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.IgnoreNoneSerializable);
        Object o = JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertNull(o);
    }

    @Test
    public void test_error() {
        NoneSerializable noneSerializable = new NoneSerializable();
        noneSerializable.setParam("Test");
        assertThrows(
                JSONException.class,
                () -> JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.ErrorOnNoneSerializable)
        );
    }

    public static class NoneSerializable {
        private String param;

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }
    }
}
