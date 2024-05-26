package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.PropertyPreFilter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue2590A {
    @Test
    public void test_map1_childItem() {
        Bean bean = new Bean();
        bean.map1 = new HashMap<String, Item>() {
            {
                put("childItem", new Item(301));
                put("childItem1", new Item(401));
            }
        };

        List<String> paths = new ArrayList<>();
        FilterDemo filter = new FilterDemo(paths);

        JSON.toJSONString(bean, filter, JSONWriter.Feature.ReferenceDetection);
        assertTrue(paths.contains("$.map1.childItem"));
        assertTrue(paths.contains("$.map1.childItem1"));
    }

    public static class FilterDemo
            implements PropertyPreFilter {
        private final List<String> paths;

        public FilterDemo(List<String> paths) {
            this.paths = paths;
        }

        @Override
        public boolean process(JSONWriter writer, Object source, String name) {
            String path = writer.getPath();
            paths.add(path);
            return true;
        }
    }

    public static class Bean {
        public Map<String, Item> map1;
    }

    public static class Item {
        public int id;

        public Item() {
        }

        public Item(int id) {
            this.id = id;
        }
    }
}
