package com.alibaba.fastjson2.internal.processor.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NestedTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.item = new Item();
        bean.item.id = 101;

        String str = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.item.id, bean1.item.id);
    }

    @JSONCompiled
    public static class Bean {
        public Item item;
    }

    @JSONCompiled
    public static class Item {
        public int id;
    }
}
