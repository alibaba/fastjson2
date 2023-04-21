package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue1396 {
    @Test
    public void test() {
        String name = "bob";
        User user = new User(name);
        JSONObject userJsonObject = JSONObject.from(user, JSONWriter.Feature.IgnoreNonFieldGetter);
        String userJsonString = JSONObject.toJSONString(user);

        // ...
        // transport via http/redis/file
        // ...

        // Normal interface method call
        LivingObject normal = new User(name);
        assertEquals(name, normal.getSignature().toString());            // -> "bob"
        assertEquals(name, normal.getSignatureString());                 // -> "bob"
        assertEquals(name, normal.withSignatureString());                // -> "bob"

        // Normal deserialize
        LivingObject deserializeLiving = userJsonObject.to(LivingObject.class);
        assertEquals(name, deserializeLiving.getSignature().toString()); // -> "bob"
        assertEquals(name, deserializeLiving.getSignatureString());      // -> "bob"
        assertEquals(name, deserializeLiving.withSignatureString());     // -> This method 'withSignatureString' is not a getter

        // Unknown instance type
        LivingObject living = JSONObject.parseObject(userJsonString, LivingObject.class);
        assertNull(living.getSignature().toString());            // -> "null"
        assertEquals(name, living.getSignatureString());                 // -> "bob"
        assertEquals(name, living.withSignatureString());                // -> This method 'withSignatureString' is not a getter
    }

    public interface LivingObject {
        String getName();

        String withName();

        default Signature getSignature() {
            return new Signature(getName());
        }

        default String getSignatureString() {
            return new Signature(getName()).toString();
        }

        default String withSignatureString() {
            return withName();
        }
    }

    public static class Signature {
        private String sign;

        public Signature(String sign) {
            this.sign = sign;
        }

        @Override
        public String toString() {
            return sign;
        }
    }

    public static class User
            implements LivingObject {
        private String name;

        public User(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String withName() {
            return name;
        }
    }
}
