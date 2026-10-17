package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue7782 {
    static class User {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testExtraFieldIgnored() {
        JSONObject jo = new JSONObject();
        jo.put("name", "test");
        // An extra key that does not exist on the target bean. On some JDK/architecture
        // combinations (e.g. ARM JDK8 with a key like "/*") this used to raise
        // IntrospectionException. fastjson 1.2.83 silently ignored such fields; we should too.
        // See https://github.com/alibaba/fastjson2/issues/7782
        // NOTE: the original bug only reproduces on ARM JDK8, so on x86/JDK17 CI the unpatched
        // code already succeeds here. This test is therefore a non-regression sanity check on
        // those platforms; the platform-independent contract is covered by
        // testIgnoreUnknownFallbackDirect below.
        jo.put("/*", "some_value");

        User user = JSON.toJavaObject(jo, User.class);
        assertNotNull(user);
        assertEquals("test", user.getName());
    }

    /**
     * Directly exercises the ignore-unknown-keys fallback ({@code toJavaObjectIgnoreUnknown}).
     * Unlike {@link #testExtraFieldIgnored}, this does not depend on the ARM-only
     * IntrospectionException and therefore validates the fallback's contract on every CI
     * platform: known properties are populated from the map and unknown keys are dropped
     * instead of raising an error.
     */
    @Test
    public void testIgnoreUnknownFallbackDirect() {
        JSONObject jo = new JSONObject();
        jo.put("name", "alice");
        jo.put("unrelatedKey", "dropped");

        User user = JSONObject.toJavaObjectIgnoreUnknown(jo, User.class);
        assertNotNull(user);
        assertEquals("alice", user.getName());
    }
}
