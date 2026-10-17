package com.alibaba.fastjson.issue_7700;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue7758 {
    public static class Session {
        private String id;
        private String host;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }
    }

    @Test
    public void testAutoTypeSupportViaParse() {
        ParserConfig config = new ParserConfig();
        config.setAutoTypeSupport(true);

        Session s = new Session();
        s.setId("session-001");
        s.setHost("127.0.0.1");

        String json = JSON.toJSONString(s, SerializerFeature.WriteClassName);
        Object o = JSON.parse(json, config);

        assertNotNull(o);
        assertTrue("expected Session but got " + o.getClass(), o instanceof Session);
        assertEquals("session-001", ((Session) o).getId());
        assertEquals("127.0.0.1", ((Session) o).getHost());
    }

    @Test
    public void testDefaultIgnoresAutoType() {
        ParserConfig config = new ParserConfig();

        Session s = new Session();
        s.setId("session-001");
        s.setHost("127.0.0.1");

        String json = JSON.toJSONString(s, SerializerFeature.WriteClassName);
        Object o = JSON.parse(json, config);

        // Without autoTypeSupport, @type must be ignored and a plain JSONObject returned.
        assertTrue("expected JSONObject but got " + o.getClass(), o instanceof JSONObject);
    }
}
