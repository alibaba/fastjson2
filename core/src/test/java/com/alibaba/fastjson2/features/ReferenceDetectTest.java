package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReferenceDetectTest {
    @Test
    public void test() {
        Bean bean = new Bean();

        StackTraceElement e1 = new StackTraceElement(
                "d1",
                "m1",
                "f1",
                101
        );
        StackTraceElement e2 = new StackTraceElement(
                "d2",
                "m2",
                "f2",
                102
        );
        bean.stackTrace = new StackTraceElement[]{
                e1,
                e2,
                e1,
                e2
        };

        String str = JSON.toJSONString(bean, JSONWriter.Feature.ReferenceDetection);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertArrayEquals(bean.stackTrace, bean1.stackTrace);

        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.ReferenceDetection);
        Bean bean2 = JSONB.parseObject(jsonbBytes, Bean.class);
        assertArrayEquals(bean.stackTrace, bean2.stackTrace);
    }

    public static class Bean {
        public StackTraceElement[] stackTrace;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();

        StackTraceElement e1 = new StackTraceElement(
                "d1",
                "m1",
                "f1",
                101
        );
        StackTraceElement e2 = new StackTraceElement(
                "d2",
                "m2",
                "f2",
                102
        );
        bean.stackTrace = new StackTraceElement[]{
                e1,
                e2,
                e1,
                e2
        };

        String str = JSON.toJSONString(bean, JSONWriter.Feature.ReferenceDetection);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertArrayEquals(bean.stackTrace, bean1.stackTrace);

        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.ReferenceDetection);
        Bean1 bean2 = JSONB.parseObject(jsonbBytes, Bean1.class);
        assertArrayEquals(bean.stackTrace, bean2.stackTrace);
    }

    public static class Bean1 {
        private StackTraceElement[] stackTrace;

        public StackTraceElement[] getStackTrace() {
            return stackTrace;
        }

        public void setStackTrace(StackTraceElement[] stackTrace) {
            this.stackTrace = stackTrace;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();

        StackTraceElement e1 = new StackTraceElement(
                "d1",
                "m1",
                "f1",
                101
        );
        StackTraceElement e2 = new StackTraceElement(
                "d2",
                "m2",
                "f2",
                102
        );
        bean.stackTrace = Arrays.asList(
                new StackTraceElement[]{
                        e1,
                        e2,
                        e1,
                        e2
                }
        );

        String str = JSON.toJSONString(bean, JSONWriter.Feature.ReferenceDetection);
        Bean2 bean1 = JSON.parseObject(str, Bean2.class);
        assertEquals(bean.stackTrace, bean1.stackTrace);
        assertFalse(str.contains("$ref"));

        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.ReferenceDetection);
        Bean2 bean2 = JSONB.parseObject(jsonbBytes, Bean2.class);
        assertEquals(bean.stackTrace, bean2.stackTrace);
    }

    public static class Bean2 {
        public List<StackTraceElement> stackTrace;
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();

        StackTraceElement e1 = new StackTraceElement(
                "d1",
                "m1",
                "f1",
                101
        );
        StackTraceElement e2 = new StackTraceElement(
                "d2",
                "m2",
                "f2",
                102
        );
        bean.stackTrace = Arrays.asList(
                new StackTraceElement[]{
                        e1,
                        e2,
                        e1,
                        e2
                }
        );

        String str = JSON.toJSONString(bean, JSONWriter.Feature.ReferenceDetection);
        Bean3 bean1 = JSON.parseObject(str, Bean3.class);
        assertEquals(bean.stackTrace, bean1.stackTrace);
        assertFalse(str.contains("$ref"));

        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.ReferenceDetection);
        Bean3 bean2 = JSONB.parseObject(jsonbBytes, Bean3.class);
        assertEquals(bean.stackTrace, bean2.stackTrace);
    }

    public static class Bean3 {
        private List<StackTraceElement> stackTrace;

        public List<StackTraceElement> getStackTrace() {
            return stackTrace;
        }

        public void setStackTrace(List<StackTraceElement> stackTrace) {
            this.stackTrace = stackTrace;
        }
    }
}
