package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2526 {
    @Getter
    @Setter
    @Data
    private class MutatedSimpleBean {
        @JSONField(name = "E")
        private Long et;
    }

    @Getter
    @Setter
    @Data
    private class MutatedSimpleBean1 {
        @com.alibaba.fastjson.annotation.JSONField(name = "E")
        private Long et;
    }

    @Test
    public void testMutated() {
        String str = "{\"E\":123,\"e\":\"abc\"}";
        MutatedSimpleBean bean = JSON.parseObject(str, MutatedSimpleBean.class);
        assertEquals(123L, bean.et);
    }
//
//    @Test
//    public void testMutatedfj() {
//        String str = "{\"E\":123,\"e\":\"abc\"}";
//        MutatedSimpleBean1 bean1 = com.alibaba.fastjson.JSON.parseObject(str, MutatedSimpleBean1.class);
//        assertEquals(123L, bean1.et);
//    }

    public static class MutatedBean {
        private Long et;

        public MutatedBean(@JSONField(name = "E") Long et) {
            this.et = et;
        }
    }

    public static class MutatedBean1 {
        private Long et;

        public MutatedBean1(@com.alibaba.fastjson.annotation.JSONField(name = "E") Long et) {
            this.et = et;
        }
    }

    @Test
    public void test2Mutated() {
        String str = "{\"E\":123,\"e\":\"abc\"}";
        MutatedBean bean = JSON.parseObject(str, MutatedBean.class);
        assertEquals(123L, bean.et);
    }

    @Test
    public void test2Mutatedfj() {
        String str = "{\"E\":123,\"e\":\"abc\"}";
        MutatedBean1 bean = com.alibaba.fastjson.JSON.parseObject(str, MutatedBean1.class);
        assertEquals(123L, bean.et);
    }
}
