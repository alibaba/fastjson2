package com.alibaba.fastjson2.v1issues.issue_1500;

import org.junit.jupiter.api.Test;

public class Issue1584 {
    @Test
    public void test_for_issue() throws Exception {
//        ParserConfig config = new ParserConfig();
//
//        String json = "{\"k\":1,\"v\":\"A\"}";
//
//        {
//            Map.Entry entry = JSON.parseObject(json, Map.Entry.class, config);
//            Object key = entry.getKey();
//            Object value = entry.getValue();
//            assertTrue(key.equals("v") || key.equals("k"));
//            if (key.equals("v")) {
//                assertEquals("A", value);
//            } else {
//                assertEquals(1, value);
//            }
//        }
//
//        config.putDeserializer(Map.Entry.class, new ObjectDeserializer() {
//            public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
//                JSONObject object = parser.parseObject();
//                Object k = object.get("k");
//                Object v = object.get("v");
//
//                return (T) Collections.singletonMap(k, v).entrySet().iterator().next();
//            }
//
//            public int getFastMatchToken() {
//                return 0;
//            }
//        });
//
//        Map.Entry entry = JSON.parseObject(json, Map.Entry.class, config);
//        assertEquals(1, entry.getKey());
//        assertEquals("A", entry.getValue());
    }
}
