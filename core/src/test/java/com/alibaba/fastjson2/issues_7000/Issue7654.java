package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue7654 {
    @Test
    public void testNonStaticInnerClass() {
        String json = "{\"issues\":[],\"summary\":{\"errors\":0,\"warnings\":0,\"messagesProcessed\":7,\"messagesAccepted\":7,\"messagesInvalid\":0}}";
        assertSummary(JSON.parseObject(json, AmzListingJsonFeedResult.class).getSummary());

        String summaryJson = "{\"errors\":0,\"warnings\":0,\"messagesProcessed\":7,\"messagesAccepted\":7,\"messagesInvalid\":0}";
        for (ObjectReaderCreator creator : TestUtils.readerCreators()) {
            ObjectReader<AmzListingJsonFeedResult.Summary> objectReader
                    = creator.createObjectReader(AmzListingJsonFeedResult.Summary.class);
            assertSummary(objectReader.readObject(JSONReader.of(summaryJson), 0));
        }
    }

    private static void assertSummary(AmzListingJsonFeedResult.Summary summary) {
        assertNotNull(summary);
        assertEquals(7, summary.getMessagesAccepted());
        assertEquals(7, summary.getMessagesProcessed());
        assertEquals(0, summary.getErrors());
        assertEquals(0, summary.getWarnings());
        assertEquals(0, summary.getMessagesInvalid());
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

        for (ObjectReaderCreator creator : TestUtils.readerCreators()) {
            ObjectReader<Outer.Inner> objectReader = creator.createObjectReader(Outer.Inner.class);
            Outer.Inner value = objectReader.readObject(JSONReader.of("{\"value\":123}"), 0);
            assertNotNull(value);
            assertEquals(123, value.getValue());
        }
    }

    /**
     * A non-static inner class whose constructor declares one explicit parameter in addition to
     * the synthetic enclosing instance (two parameters total), which routes through the
     * {@code BiFunction} fast path of {@code ConstructorFunction.apply}. The constructor
     * dereferences the enclosing instance, so a {@code null} {@code this$0} throws on every JDK.
     */
    @Test
    public void testInnerClassWithExplicitConstructorParameter() {
        String json = "{\"id\":123}";
        for (ObjectReaderCreator creator : TestUtils.readerCreators()) {
            ObjectReader<Outer2.InnerWithOneParam> objectReader
                    = creator.createObjectReader(Outer2.InnerWithOneParam.class);
            Outer2.InnerWithOneParam value = objectReader.readObject(JSONReader.of(json), 0);
            assertNotNull(value);
            assertEquals(123, value.getId());
        }
    }

    /**
     * A non-static inner class whose constructor declares two explicit parameters in addition to
     * the synthetic enclosing instance (three parameters total), which routes through the general
     * argument loop of {@code ConstructorFunction.apply} rather than the single/dual fast paths.
     * The constructor dereferences the enclosing instance, so a {@code null} {@code this$0} throws
     * on every JDK.
     */
    @Test
    public void testInnerClassWithTwoExplicitConstructorParameters() {
        String json = "{\"id\":123,\"name\":\"fastjson2\"}";
        for (ObjectReaderCreator creator : TestUtils.readerCreators()) {
            ObjectReader<Outer2.InnerWithTwoParams> objectReader
                    = creator.createObjectReader(Outer2.InnerWithTwoParams.class);
            Outer2.InnerWithTwoParams value = objectReader.readObject(JSONReader.of(json), 0);
            assertNotNull(value);
            assertEquals(123, value.getId());
            assertEquals("fastjson2", value.getName());
        }
    }

    /**
     * The enclosing class is abstract, so it cannot be allocated via
     * {@code Unsafe.allocateInstance} (which throws {@link InstantiationException}). The readers
     * must fall back to passing {@code null} for the enclosing instance instead of propagating the
     * exception (the pre-JDK-25 behavior). This module is compiled for Java 8, so the inner class
     * constructor does not reject a {@code null} enclosing instance.
     */
    @Test
    public void testAbstractEnclosingClass() {
        String json = "{\"value\":42}";
        for (ObjectReaderCreator creator : TestUtils.readerCreators()) {
            ObjectReader<AbstractOuter.Inner> objectReader
                    = creator.createObjectReader(AbstractOuter.Inner.class);
            AbstractOuter.Inner value = objectReader.readObject(JSONReader.of(json), 0);
            assertNotNull(value);
            assertEquals(42, value.getValue());
        }
    }

    /**
     * The enclosing class is package-private, so a generated ASM reader cannot reference it via
     * {@code ldc}/{@code checkcast} and instance creation is delegated to the reflective creator.
     * The inner constructor dereferences the enclosing instance, so a {@code null} {@code this$0}
     * throws on every JDK.
     */
    @Test
    public void testPackagePrivateEnclosingClass() {
        String json = "{\"value\":42}";
        for (ObjectReaderCreator creator : TestUtils.readerCreators()) {
            ObjectReader<PackagePrivateOuter.Inner> objectReader
                    = creator.createObjectReader(PackagePrivateOuter.Inner.class);
            PackagePrivateOuter.Inner value = objectReader.readObject(JSONReader.of(json), 0);
            assertNotNull(value);
            assertEquals(42, value.getValue());
        }
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

    public static class Outer2 {
        private String name = "outer2";

        public String name() {
            return name;
        }

        public class InnerWithOneParam {
            private final int id;

            public InnerWithOneParam(int id) {
                // invokevirtual on this$0: NPE if the enclosing instance is null
                Outer2.this.name();
                this.id = id;
            }

            public int getId() {
                return id;
            }
        }

        public class InnerWithTwoParams {
            private final int id;
            private final String name;

            public InnerWithTwoParams(int id, String name) {
                // invokevirtual on this$0: NPE if the enclosing instance is null
                Outer2.this.name();
                this.id = id;
                this.name = name;
            }

            public int getId() {
                return id;
            }

            public String getName() {
                return name;
            }
        }
    }

    public abstract static class AbstractOuter {
        public class Inner {
            private int value;

            public int getValue() {
                return value;
            }

            public void setValue(int value) {
                this.value = value;
            }
        }
    }

    static class PackagePrivateOuter {
        public class Inner {
            private int value;

            public Inner() {
                // invokevirtual on this$0: NPE if the enclosing instance is null
                PackagePrivateOuter.this.toString();
            }

            public int getValue() {
                return value;
            }

            public void setValue(int value) {
                this.value = value;
            }
        }
    }
}
