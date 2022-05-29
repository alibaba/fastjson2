package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OverrideTest {
    @Test
    public void test_override() throws Exception {
        ObjectReaderCreator creator = TestUtils.READER_CREATOR;
        ObjectReader<Cat> objectReader = creator.createObjectReader(Cat.class);
        Cat cat = objectReader.readObject(JSONReader.of("{\"id\":1001}"), 0);
        assertEquals(0, cat.id);
        assertEquals(1001, cat.catId);

        String str = JSON.toJSONString(cat);
        assertEquals("{\"id\":1001}", str);
    }

    public static class Animal {
        int id;

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }
    }

    public static class Cat
            extends Animal {
        private int catId;

        public void setId(int id) {
            this.catId = id;
        }

        public int getId() {
            return this.catId;
        }
    }
}
