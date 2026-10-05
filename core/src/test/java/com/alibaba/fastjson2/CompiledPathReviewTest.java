package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompiledPathReviewTest {
    @Test
    public void referenceSetter() {
        JSONPath path = JSONPathCompilerReflectASM.INSTANCE.compile(TextBean.class, JSONPath.of("$.value"));
        TextBean bean = new TextBean();
        path.set(bean, "updated");
        assertEquals("updated", path.eval(bean));
    }

    @Test
    public void readOnlyGetter() {
        JSONPath path = JSONPathCompilerReflectASM.INSTANCE.compile(ReadOnlyBean.class, JSONPath.of("$.value"));
        assertEquals(42, path.eval(new ReadOnlyBean()));
    }

    @Test
    public void wideSetterReturn() {
        JSONPath path = JSONPathCompilerReflectASM.INSTANCE.compile(ReturningBean.class, JSONPath.of("$.value"));
        ReturningBean bean = new ReturningBean();
        path.setInt(bean, 42);
        assertEquals(42, path.eval(bean));
    }

    public static class TextBean {
        public String value;
    }

    public static class ReadOnlyBean {
        public int getValue() {
            return 42;
        }
    }

    public static class ReturningBean {
        private int value;

        public int getValue() {
            return value;
        }

        public long setValue(int value) {
            this.value = value;
            return value;
        }
    }
}
