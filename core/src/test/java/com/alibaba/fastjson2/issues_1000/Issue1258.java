package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue1258 {
    @Getter
    @Setter
    public static class Bean {
        @JSONField(name = "e")
        private String event;

        @JSONField(name = "E")
        private Long et;
    }

    @Test
    public void testSimpleBean() {
        String str = "{\"E\":123,\"e\":\"abc\"}";
        SimpleBean bean = JSON.parseObject(str, SimpleBean.class);
        assertEquals(123L, bean.et);
    }

    @Test
    public void testSimpleBean1() {
        String str = "{\"E\":123,\"e\":\"abc\"}";
        ObjectReader<SimpleBean> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(SimpleBean.class);
        SimpleBean bean = objectReader.readObject(JSONReader.of(str));
        assertEquals(123L, bean.et);
    }

    @Getter
    @Setter
    @Data
    private class SimpleBean {
        @JSONField(name = "E")
        private Long et;
    }

    @Test
    public void test1() {
        String str = "{\"E\":123,\"e\":\"abc\"}";
        Bean1 bean = JSON.parseObject(str, Bean1.class);
        assertEquals(123L, bean.et);
    }

    public static class Bean1 {
        private Long et;

        public Bean1(@JSONField(name = "E") Long et) {
            this.et = et;
        }
    }

    @Test
    public void test2() {
        String str = "{\"E\":123,\"e\":\"abc\"}";
        Bean2 bean = JSON.parseObject(str, Bean2.class);
        assertEquals(123L, bean.et);
        assertEquals("abc", bean.event);
    }

    public static class Bean2 {
        private String event;
        private Long et;

        public Bean2(@JSONField(name = "E") Long et, @JSONField(name = "e") String event) {
            this.et = et;
            this.event = event;
        }
    }

    @Test
    public void test3() {
        String str = "{\"E\":123,\"e\":\"abc\"}";
        Bean3 bean = JSON.parseObject(str, Bean3.class);
        assertEquals(123L, bean.et);
    }

    public static class Bean3 {
        private long et;

        public Bean3(@JSONField(name = "E") long et) {
            this.et = et;
        }
    }

    @Test
    public void test4() {
        String str = "{\"E1\":101,\"E2\":102,\"E3\":103}";
        Bean4 bean = JSON.parseObject(str, Bean4.class);
        assertEquals(101, bean.f0);
        assertEquals(102, bean.f1);
        assertEquals(103, bean.f2);
    }

    public static class Bean4 {
        private int f0;
        private int f1;
        private int f2;

        public Bean4(
                @JSONField(name = "E1")
                int f0,

                @JSONField(name = "E2")
                int f1,

                @JSONField(name = "E3")
                int f2
        ) {
            this.f0 = f0;
            this.f1 = f1;
            this.f2 = f2;
        }
    }

    @Test
    public void test5() {
        String str = "{\"E1\":101,\"E2\":102,\"E3\":103}";
        assertThrows(Exception.class, () -> JSON.parseObject(str, Bean5.class));
    }

    public static class Bean5 {
        private int f0;
        private int f1;
        private int f2;

        public Bean5(
                @JSONField(name = "E1")
                int f0,

                @JSONField(name = "E2")
                int f1,

                @JSONField(name = "E3")
                int f2
        ) {
            throw new RuntimeException();
        }
    }
}
