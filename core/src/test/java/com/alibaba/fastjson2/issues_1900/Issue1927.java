package com.alibaba.fastjson2.issues_1900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.PropertyFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1927 {
    @Test
    public void test() {
        Bean bean = new Bean();
        ItemA itemA = new ItemA();
        itemA.name = "abc";
        bean.item = itemA;
        String str = JSON.toJSONString(bean, new Filter[] {
                new PropertyFilter() {
                    @Override
                    public boolean apply(Object object, String name, Object value) {
                        return true;
                    }
                }
        }, JSONWriter.Feature.WriteClassName);
        assertEquals(
                "{\"@type\":\"com.alibaba.fastjson2.issues_1900.Issue1927$Bean\",\"item\":{\"@type\":\"com.alibaba.fastjson2.issues_1900.Issue1927$ItemA\",\"name\":\"abc\"}}",
                str);
    }

    public static class Bean {
        public Item item;
    }

    public static class Item {
    }

    public static class ItemA
            extends Item {
        public String name;
    }
}
