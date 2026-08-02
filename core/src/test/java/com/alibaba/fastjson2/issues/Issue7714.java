package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression for issue #7714: when a bean has an {@code Object} (or generic) field,
 * {@link com.alibaba.fastjson2.JSON#toJSON(Object)} reused the field writer cached for the
 * first serialized value type without checking it still matches the current value, raising
 * {@code JSONException: key get error} (a wrapped ClassCastException), and — for a subclass
 * value — silently dropping the subclass fields by reusing the parent writer.
 */
@Tag("regression")
public class Issue7714 {
    public static class DataChangeNotification {
        private String key;
        private String tenantId;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }
    }

    // Subclass adds a field; if toJSONObject reused the parent's cached writer it would be lost.
    public static class DataChangeNotificationExt
            extends DataChangeNotification {
        private String scope;

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }
    }

    // MQ-style envelope with an untyped payload; the generic getter makes fieldClass erase to Object.
    public static class DefaultMessage {
        private Object payLoad;

        public <T> T getPayload() {
            return (T) payLoad;
        }

        public boolean setPayload(Object payload) {
            this.payLoad = payload;
            return true;
        }
    }

    @Test
    public void toJSON_afterWriterCachedForDifferentType() {
        // Step 1: write path caches a DataChangeNotification writer for the payload FieldWriterObject.
        DefaultMessage m1 = new DefaultMessage();
        DataChangeNotification dcn = new DataChangeNotification();
        dcn.setKey("K1");
        dcn.setTenantId("10000");
        m1.setPayload(dcn);
        JSON.toJSONString(m1);

        // Step 2: toJSONObject path with a String payload must not reuse that cached writer.
        // The String value must be preserved as-is instead of throwing "key get error".
        DefaultMessage m2 = new DefaultMessage();
        m2.setPayload("{\"key\":\"abc\"}");
        Object r = JSON.toJSON(m2);

        JSONObject json = (JSONObject) r;
        assertEquals("{\"key\":\"abc\"}", json.get("payload"));
    }

    @Test
    public void toJSON_consistentAfterWriterCachedForSameType() {
        // Regression guard: once the writer is cached for DataChangeNotification, a second
        // DataChangeNotification payload must still convert to a nested JSONObject.
        DefaultMessage m1 = new DefaultMessage();
        DataChangeNotification dcn1 = new DataChangeNotification();
        dcn1.setKey("K1");
        dcn1.setTenantId("10000");
        m1.setPayload(dcn1);
        JSON.toJSONString(m1);

        DefaultMessage m2 = new DefaultMessage();
        DataChangeNotification dcn2 = new DataChangeNotification();
        dcn2.setKey("K2");
        dcn2.setTenantId("20000");
        m2.setPayload(dcn2);
        Object r = JSON.toJSON(m2);

        JSONObject payload = (JSONObject) ((JSONObject) r).get("payload");
        assertEquals("K2", payload.get("key"));
        assertEquals("20000", payload.get("tenantId"));
    }

    @Test
    public void toJSON_beanToBeanSubclassReparseAndRecurses() {
        // Step 1: cache the writer for the parent type on the payload FieldWriterObject.
        DefaultMessage m1 = new DefaultMessage();
        DataChangeNotification dcn = new DataChangeNotification();
        dcn.setKey("K1");
        dcn.setTenantId("10000");
        m1.setPayload(dcn);
        JSON.toJSONString(m1);

        // Step 2: payload is now a subclass value. The write path (typeMatch) requires an exact
        // class match, so toJSONObject must re-resolve the writer by the subclass and recurse,
        // rather than reuse the parent writer and silently drop DataChangeNotificationExt.scope.
        DefaultMessage m2 = new DefaultMessage();
        DataChangeNotificationExt ext = new DataChangeNotificationExt();
        ext.setKey("K2");
        ext.setTenantId("20000");
        ext.setScope("online");
        m2.setPayload(ext);
        Object r = JSON.toJSON(m2);

        // The payload must be a nested JSONObject built from the subclass writer, not the raw bean.
        Object payload = ((JSONObject) r).get("payload");
        assertTrue(payload instanceof JSONObject, "payload should be a nested JSONObject, not the raw bean");
        assertNotSame(ext, payload, "payload should be a converted JSONObject, not the original bean");

        JSONObject payloadJson = (JSONObject) payload;
        assertEquals("K2", payloadJson.get("key"));
        assertEquals("20000", payloadJson.get("tenantId"));
        assertEquals("online", payloadJson.get("scope"));

        // toJSON and toJSONString must agree on the same object (the consistency guarantee from
        // aligning toJSONObject's type-match with the write path).
        String expected = JSON.toJSONString(m2);
        assertEquals(expected, ((JSONObject) r).toJSONString());
    }
}
