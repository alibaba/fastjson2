package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * https://github.com/alibaba/fastjson2/issues/3516
 * A shared object nested one level below a Map keyed by Long/Integer produced a $ref path
 * that skipped the map key segment, so the reference could not be resolved on read and the
 * field was silently set to null instead of pointing back at the shared object.
 */
@Tag("codec")
public class RefTest8 {
    @Test
    public void test_issue3516_longKeyedMap() {
        Map<String, Long> reversePriceMap = new HashMap<>();
        reversePriceMap.put("1", 1L);

        Item item1 = new Item();
        item1.itemId = 1L;
        item1.reversePriceMap = reversePriceMap;
        RiskInfo riskInfo1 = new RiskInfo();
        riskInfo1.itemMap = new HashMap<>();
        riskInfo1.itemMap.put(item1.itemId, item1);

        Item item2 = new Item();
        item2.itemId = 2L;
        item2.reversePriceMap = reversePriceMap;
        RiskInfo riskInfo2 = new RiskInfo();
        riskInfo2.itemMap = new HashMap<>();
        riskInfo2.itemMap.put(item2.itemId, item2);

        List<RiskInfo> list = List.of(riskInfo1, riskInfo2);

        String json = JSON.toJSONString(list, JSONWriter.Feature.ReferenceDetection);
        assertEquals(
                "[{\"itemMap\":{1:{\"itemId\":1,\"reversePriceMap\":{\"1\":1}}}},"
                        + "{\"itemMap\":{2:{\"itemId\":2,\"reversePriceMap\":{\"$ref\":\"$[0].itemMap.1.reversePriceMap\"}}}}]",
                json
        );

        List<RiskInfo> list2 = JSON.parseObject(json, new TypeReference<List<RiskInfo>>() {
        });
        Item item1Parsed = list2.get(0).itemMap.get(1L);
        Item item2Parsed = list2.get(1).itemMap.get(2L);
        assertEquals(Map.of("1", 1L), item1Parsed.reversePriceMap);
        assertSame(item1Parsed.reversePriceMap, item2Parsed.reversePriceMap);
    }

    @Test
    public void test_longKeyedMap_directShare() {
        Item shared = new Item();
        shared.itemId = 100L;

        Bean bean = new Bean();
        bean.map1 = new HashMap<>();
        bean.map1.put(1L, shared);
        bean.map2 = new HashMap<>();
        bean.map2.put(2L, shared);

        String json = JSON.toJSONString(bean, JSONWriter.Feature.ReferenceDetection);
        assertEquals(
                "{\"map1\":{1:{\"itemId\":100}},\"map2\":{2:{\"$ref\":\"$.map1.1\"}}}",
                json
        );

        Bean bean2 = JSON.parseObject(json, Bean.class);
        assertSame(bean2.map1.get(1L), bean2.map2.get(2L));
        assertEquals(100L, bean2.map1.get(1L).itemId);
    }

    @Test
    public void test_integerKeyedMap_directShare() {
        Item shared = new Item();
        shared.itemId = 200L;

        BeanInt bean = new BeanInt();
        bean.map1 = new HashMap<>();
        bean.map1.put(1, shared);
        bean.map2 = new HashMap<>();
        bean.map2.put(2, shared);

        String json = JSON.toJSONString(bean, JSONWriter.Feature.ReferenceDetection);
        assertEquals(
                "{\"map1\":{1:{\"itemId\":200}},\"map2\":{2:{\"$ref\":\"$.map1.1\"}}}",
                json
        );

        BeanInt bean2 = JSON.parseObject(json, BeanInt.class);
        assertSame(bean2.map1.get(1), bean2.map2.get(2));
        assertEquals(200L, bean2.map1.get(1).itemId);
    }

    @Test
    public void test_longKeyedMap_noSharing_outputUnchanged() {
        Bean bean = new Bean();
        bean.map1 = new HashMap<>();
        bean.map1.put(1L, new Item());
        bean.map2 = new HashMap<>();
        bean.map2.put(2L, new Item());

        assertEquals(
                "{\"map1\":{1:{}},\"map2\":{2:{}}}",
                JSON.toJSONString(bean, JSONWriter.Feature.ReferenceDetection)
        );
    }

    public static class RiskInfo {
        public Map<Long, Item> itemMap;
    }

    public static class Item {
        public Long itemId;
        public Map<String, Long> reversePriceMap;
    }

    public static class Bean {
        public Map<Long, Item> map1;
        public Map<Long, Item> map2;
    }

    public static class BeanInt {
        public Map<Integer, Item> map1;
        public Map<Integer, Item> map2;
    }
}
