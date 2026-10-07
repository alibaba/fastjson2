package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A Map field typed as Map&lt;Long, V&gt; / Map&lt;Integer, V&gt; used to round-trip its keys back as
 * String instead of Long/Integer when serialized to JSONB with the default JSONWriter features (i.e.
 * without JSONWriter.Feature.WriteClassName). No shared/circular references are involved; this
 * reproduced even for a single, unshared entry.
 *
 * Because the actual stored key was a String while the field is declared Map&lt;Long, Item&gt;, the two
 * realistic access patterns failed differently:
 *  - map.get(1L) silently returned null (Map.get(Object) doesn't check the declared key type), and
 *  - `for (Long key : map.keySet())` threw ClassCastException, because javac inserts an implicit
 *    checkcast to Long for the generically-typed iteration.
 */
@Tag("jsonb")
public class MapLongKeyTypeTest {
    public static class Item {
        public Long itemId;
    }

    public static class Bean {
        public Map<Long, Item> map1;
    }

    public static class BeanInt {
        public Map<Integer, Item> map1;
    }

    @Test
    public void test_longKey_jsonb_default() {
        Item item = new Item();
        item.itemId = 300L;

        Bean bean = new Bean();
        bean.map1 = new HashMap<>();
        bean.map1.put(1L, item);

        byte[] bytes = JSONB.toBytes(bean);
        Bean bean2 = JSONB.parseObject(bytes, Bean.class);

        assertEquals(300L, bean2.map1.get(1L).itemId);
        for (Long key : bean2.map1.keySet()) {
            assertEquals(1L, key);
        }
    }

    @Test
    public void test_integerKey_jsonb_default() {
        Item item = new Item();
        item.itemId = 200L;

        BeanInt bean = new BeanInt();
        bean.map1 = new HashMap<>();
        bean.map1.put(1, item);

        byte[] bytes = JSONB.toBytes(bean);
        BeanInt bean2 = JSONB.parseObject(bytes, BeanInt.class);

        assertEquals(200L, bean2.map1.get(1).itemId);
        for (Integer key : bean2.map1.keySet()) {
            assertEquals(1, key);
        }
    }

    @Test
    public void test_longKey_textJson_control() {
        // Control case: the equivalent text-JSON round-trip preserves the Long key correctly,
        // showing this was specific to the JSONB codec rather than Map<Long,V> type handling in general.
        Item item = new Item();
        item.itemId = 300L;

        Bean bean = new Bean();
        bean.map1 = new HashMap<>();
        bean.map1.put(1L, item);

        String json = JSON.toJSONString(bean);
        Bean bean2 = JSON.parseObject(json, Bean.class);

        assertEquals(300L, bean2.map1.get(1L).itemId);
        for (Long key : bean2.map1.keySet()) {
            assertEquals(1L, key);
        }
    }

    @Test
    public void test_longKey_jsonb_writeClassName_control() {
        // Control case: turning on WriteClassName also preserved the Long key correctly,
        // showing the default (WriteClassName off) JSONB write path was where the type got lost.
        Item item = new Item();
        item.itemId = 300L;

        Bean bean = new Bean();
        bean.map1 = new HashMap<>();
        bean.map1.put(1L, item);

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        Bean bean2 = JSONB.parseObject(bytes, Bean.class, JSONReader.Feature.SupportAutoType);

        assertEquals(300L, bean2.map1.get(1L).itemId);
        for (Long key : bean2.map1.keySet()) {
            assertEquals(1L, key);
        }
    }
}
