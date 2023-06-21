package com.alibaba.fastjson2.internal.processor.collections;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListTest1 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.v01 = new ArrayList<>();
        Item item = new Item();
        item.id = 123;
        bean.v01.add(item);

        String str = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.v01, bean1.v01);
    }

    @JSONCompiled
    public static class Bean {
        public List<Item> v01;
    }

    @Data
    @JSONCompiled
    public static class Item {
        public int id;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.v01 = new ArrayList<>();
        bean.v01.add("123");

        String str = JSON.toJSONString(bean);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.v01, bean1.v01);
    }

    @JSONCompiled
    public static class Bean1 {
        public List<String> v01;
    }
}
