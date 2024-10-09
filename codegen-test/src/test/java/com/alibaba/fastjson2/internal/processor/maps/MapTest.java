package com.alibaba.fastjson2.internal.processor.maps;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;

public class MapTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.v01 = new HashMap<>();
        Item value = new Item(123);
        bean.v01.put("123", value);
        bean.v01.put("12x", value);

        String str = JSON.toJSONString(bean, JSONWriter.Feature.ReferenceDetection);

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertSame(bean1.v01.get("123"), bean1.v01.get("12x"));
    }

    @JSONCompiled
    public static class Bean {
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
