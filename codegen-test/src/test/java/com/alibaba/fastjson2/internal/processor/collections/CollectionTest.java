package com.alibaba.fastjson2.internal.processor.collections;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CollectionTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.v01 = new ArrayList();
        bean.v02 = new ArrayList();
        bean.v03 = new ArrayList();
        bean.v04 = new ArrayList();
        bean.v05 = new LinkedList();
        bean.v06 = new CopyOnWriteArrayList();

        String str = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.v01, bean1.v01);
        assertEquals(bean.v02, bean1.v02);
        assertEquals(bean.v03, bean1.v03);
        assertEquals(bean.v04, bean1.v04);
        assertEquals(bean.v05, bean1.v05);
        assertEquals(bean.v06, bean1.v06);
    }

    @JSONCompiled
    public static class Bean {
        public Iterable v01;
        public Collection v02;
        public List v03;
        public ArrayList v04;
        public LinkedList v05;
        public CopyOnWriteArrayList v06;
    }
}
