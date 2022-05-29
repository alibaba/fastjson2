package com.alibaba.fastjson.issue_3000;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.BeforeFilter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3373 {
    @Test
    public void test_for_issue() throws Exception {
        RefBeforeFilterTest refAfterFilterTest = new RefBeforeFilterTest();

        List<Item> items = new ArrayList<Item>(2);
        Category category = new Category("category");
        items.add(new Item("item1", category));
        items.add(new Item("item2", category));

        assertEquals(
                "[{\"afterFilterCategory\":{\"name\":\"afterFilterCategory\"},\"afterFilterTwo\":\"two\",\"category\":{\"name\":\"category\"},\"name\":\"item1\"},{\"afterFilterCategory\":{\"name\":\"afterFilterCategory\"},\"afterFilterTwo\":\"two\",\"category\":{\"$ref\":\"$[0].category\"},\"name\":\"item2\"}]",
                JSON.toJSONString(items, refAfterFilterTest)
        );
    }

    public static class RefBeforeFilterTest
            extends BeforeFilter {
        private Category category = new Category("afterFilterCategory");

        @Override
        public void writeBefore(Object object) {
            if (object instanceof Item) {
                this.writeKeyValue("afterFilterCategory", category);
                /*多加一个属性报错,原因是category是object也触发了writeAfter,当前线程变量serializer被设置为null了serializerLocal.set(null);
                 *这两个write换个顺序就不会报错
                 */
                this.writeKeyValue("afterFilterTwo", "two");
            }
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

    public static class Item {
        private String name;

        private Category category;

        private String barcode;

        public Item(String name, Category category) {
            this.name = name;
            this.category = category;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Category getCategory() {
            return category;
        }

        public void setCategory(Category category) {
            this.category = category;
        }

        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }
    }
}
