package com.alibaba.fastjson2.v1issues.issue_1800;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1821C {
    @Test
    public void test_for_issue() throws Exception {
        String str = "{\"type\":800,\"data\":\"HuYgMIxwfqdtvOJNv6kK025g5fh3yFHI2kaByO7udKk6FOBC3PGRWkGfwV0\\/vWQW6roN5ftKDHFZ3PWl0715OYue0rZj\\/VwrNsMvIL4MqTUNBBUGFU9SgZu87ss7RqmyijH6\\/sM968cK1Dv5U7Rrw79idl\\/hW8SILLn1YXvUa60=\"}";
        String expectStr = "{\"type\":800,\"data\":\"HuYgMIxwfqdtvOJNv6kK025g5fh3yFHI2kaByO7udKk6FOBC3PGRWkGfwV0/vWQW6roN5ftKDHFZ3PWl0715OYue0rZj/VwrNsMvIL4MqTUNBBUGFU9SgZu87ss7RqmyijH6/sM968cK1Dv5U7Rrw79idl/hW8SILLn1YXvUa60=\"}";
        Model m = JSON.parseObject(str, Model.class);
        assertEquals(expectStr, JSON.toJSONString(m));

        str = "{\"type\":800,\"data\":\"Y29tLmFsaWJhYmEuZmFzdGpzb24=\"}";
        m = JSON.parseObject(str, Model.class);
        assertEquals(str, JSON.toJSONString(m));
        assertEquals("com.alibaba.fastjson", new String(m.data));

        expectStr = str;
        str = "{\"type\":800,\"data\":\"\\u005929tLmFsaWJ\\u0068YmEuZmFzdGpzb24\\u003d\"}";
        m = JSON.parseObject(str, Model.class);
        assertEquals(expectStr, JSON.toJSONString(m));
        assertEquals("com.alibaba.fastjson", new String(m.data));
    }

    @JSONType
    public static class Model {
        @JSONField(name = "type", ordinal = 1)
        public int type;

        @JSONField(name = "data", ordinal = 2, format = "base64")
        public byte[] data;
    }
}
