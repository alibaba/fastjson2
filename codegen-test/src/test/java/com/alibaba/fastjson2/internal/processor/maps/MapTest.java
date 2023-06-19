package com.alibaba.fastjson2.internal.processor.maps;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.v01 = new HashMap<>();
        bean.v01.put("123", new Item(123));

        String str = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.v01, bean1.v01);
    }

    @JSONCompiled
    public static class Bean {
        public Map<String, Item> v01;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.v01 = new HashMap<>();
        bean.v01.put("123", new Item(123));

        String str = JSON.toJSONString(bean);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.v01.get("123").id, bean1.v01.get("123").id);
    }

    @JSONCompiled(referenceDetect = false)
    public static class Bean1 {
        public Map<String, Item> v01;
    }

    @JSONCompiled
    @Data
    public static class Item {
        public int id;

        public Item() {
        }

        public Item(@JSONField(name = "id") int id) {
            this.id = id;
        }
    }
}
