package com.alibaba.fastjson.issue_3100;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.AfterFilter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3150 {
    @Test
    public void test_for_issue() throws Exception {
        MyRefAfterFilter refAfterFilterTest = new MyRefAfterFilter();

        List<Item> items = new ArrayList<Item>(2);
        Category category = new Category("category");
        items.add(new Item("item1", category));
        items.add(new Item("item2", category));

        String str = JSON.toJSONString(items, refAfterFilterTest);
        assertEquals(
                "[{\"category\":{\"name\":\"category\"},\"name\":\"item1\",\"afterFilterCategory\":{\"name\":\"afterFilterCategory\"}},{\"category\":{\"$ref\":\"$[0].category\"},\"name\":\"item2\",\"afterFilterCategory\":{\"name\":\"afterFilterCategory\"}}]",
                str
        );
    }

    public static class MyRefAfterFilter
            extends AfterFilter {
        private Category category = new Category("afterFilterCategory");

        @Override
        public void writeAfter(Object object) {
            if (object instanceof Item) {
                this.writeKeyValue("afterFilterCategory", category);
            }
        }
    }

    public static class Item {
        private String name;

        private Category category;

        public Item(String name, Category category) {
            this.name = name;
            this.category = category;
        }

        public String getName() {
            return name;
        }

        public Category getcategory() {
            return category;
        }
    }

    public static class Category {
        private String name;

        public Category(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
