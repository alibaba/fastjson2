package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue539 {
    @Test
    public void test() {
        Bean bean = new Bean();
        List<Long> list = new ArrayList<>();
        list.add(1L);
        bean.setIds(list);
        Bean bean1 = JSON.parseObject("{\"ids\":[1]}").to(Bean.class);
        assertEquals(bean.ids, bean1.ids);
    }

    public static class Bean {
        public List<Long> ids;

        public List<Long> getIds() {
            return ids;
        }

        public void setIds(List<Long> ids) {
            this.ids = ids;
        }
    }
}
