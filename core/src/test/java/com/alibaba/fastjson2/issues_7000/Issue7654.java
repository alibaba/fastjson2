package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue7654 {
    @Test
    public void testNonStaticInnerClass() {
        String json = "{\"issues\":[],\"summary\":{\"errors\":0,\"warnings\":0,\"messagesProcessed\":7,\"messagesAccepted\":7,\"messagesInvalid\":0}}";
        AmzListingJsonFeedResult result = JSON.parseObject(json, AmzListingJsonFeedResult.class);
        assertNotNull(result.getSummary());
        assertEquals(7, result.getSummary().getMessagesAccepted());
        assertEquals(7, result.getSummary().getMessagesProcessed());
        assertEquals(0, result.getSummary().getErrors());
        assertEquals(0, result.getSummary().getWarnings());
        assertEquals(0, result.getSummary().getMessagesInvalid());
    }

    /**
     * The inner class constructor dereferences the enclosing instance, so a {@code null}
     * {@code this$0} throws NPE on every JDK version. Unlike {@link #testNonStaticInnerClass()},
     * this gates the fix on JDK &lt; 25 as well.
     */
    @Test
    public void testInnerClassConstructorDereferencingOuter() {
        Outer.Inner inner = JSON.parseObject("{\"value\":123}", Outer.Inner.class);
        assertNotNull(inner);
        assertEquals(123, inner.getValue());
    }

    public static class Outer {
        private String name = "outer";

        public String name() {
            return name;
        }

        public class Inner {
            private int value;

            public Inner() {
                // invokevirtual on this$0: NPE if the enclosing instance is null
                Outer.this.name();
            }

            public int getValue() {
                return value;
            }

            public void setValue(int value) {
                this.value = value;
            }
        }
    }

    public static class AmzListingJsonFeedResult {
        private Summary summary;

        public Summary getSummary() {
            return summary;
        }

        public void setSummary(Summary summary) {
            this.summary = summary;
        }

        public class Summary {
            private Integer errors;
            private Integer warnings;
            private Integer messagesProcessed;
            private Integer messagesAccepted;
            private Integer messagesInvalid;

            public Integer getErrors() {
                return errors;
            }

            public void setErrors(Integer errors) {
                this.errors = errors;
            }

            public Integer getWarnings() {
                return warnings;
            }

            public void setWarnings(Integer warnings) {
                this.warnings = warnings;
            }

            public Integer getMessagesProcessed() {
                return messagesProcessed;
            }

            public void setMessagesProcessed(Integer messagesProcessed) {
                this.messagesProcessed = messagesProcessed;
            }

            public Integer getMessagesAccepted() {
                return messagesAccepted;
            }

            public void setMessagesAccepted(Integer messagesAccepted) {
                this.messagesAccepted = messagesAccepted;
            }

            public Integer getMessagesInvalid() {
                return messagesInvalid;
            }

            public void setMessagesInvalid(Integer messagesInvalid) {
                this.messagesInvalid = messagesInvalid;
            }
        }
    }
}
