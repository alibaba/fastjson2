package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2691 {
    @Test
    void testUnmodifiableList() {
        UnmodifiableList list = new UnmodifiableList();
        ArrayList<String> strings = new ArrayList<>();
        strings.add("1");
        list.setList(strings);
        writeReade(list);
    }

    @Test
    void testUnmodifiableMap() {
        UnmodifiableMap map = new UnmodifiableMap();
        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("1", "1");
        map.setMap(stringMap);
        writeReade(map);
    }

    @Test
    void testUnmodifiableSet() {
        UnmodifiableSet set = new UnmodifiableSet();
        Set<String> stringSet = new HashSet<>();
        stringSet.add("1");
        set.setSet(stringSet);
        writeReade(set);
    }

    void writeReade(Object value) {
        byte[] bytes = JSONB.toBytes(
                value,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        Object result = JSONB.parseObject(
                bytes,
                value.getClass(),
                JSONReader.Feature.FieldBased);
        assertNotNull(result);
        assertEquals(result, value);
    }

    @Test
    public void test() {
        Page<String> page = new Page<>();
        List<String> data = new ArrayList<>();
        data.add(null);
        data.add("abc");
        data.add(null);
        page.setData(data);
        byte[] bytes = JSONB.toBytes(page);
        Page<String> page1 = JSONB.parseObject(bytes, Page.class);
        assertNotNull(page1);
        assertEquals(3, page1.data.size());
        assertEquals("abc", page1.data.get(1));
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @Accessors(chain = true)
    private static class UnmodifiableList {
        private List<String> list = Collections.emptyList();
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @Accessors(chain = true)
    private static class UnmodifiableSet {
        private Set<String> set = Collections.emptySet();
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @Accessors(chain = true)
    private static class UnmodifiableMap {
        private Map<String, String> map = Collections.emptyMap();
    }

    public static class Page<T> {
        public List<T> data = Collections.emptyList();

        public void setData(List<T> items) {
            this.data = items;
        }
    }
}
