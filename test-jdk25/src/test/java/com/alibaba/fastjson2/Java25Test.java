package com.alibaba.fastjson2;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Java25Test {
    @Test
    public void test() {
        Bean bean = new Bean(123, "abc");

        String str = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str, Bean.class);

        assertEquals(bean.id(), bean1.id());
        assertEquals(bean.name(), bean1.name());
    }

    public record Bean(int id, String name) {
    }
}
