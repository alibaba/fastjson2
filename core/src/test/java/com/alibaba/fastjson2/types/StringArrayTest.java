package com.alibaba.fastjson2.types;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StringArrayTest {
    @Test
    public void test() {
        ObjectReader<Bean> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean.class);
        String str = JSONObject.of("values", JSONArray.of("a", "b")).toJSONString();
        Bean bean = objectReader.readObject(JSONReader.of(str));
        assertNotNull(bean.values);
        assertEquals(2, bean.values.length);
        assertEquals("a", bean.values[0]);
        assertEquals("b", bean.values[1]);

        FieldReader fieldReader = objectReader.getFieldReader("values");
        assertNotNull(fieldReader.getObjectReader(JSONReader.of(str)));
        assertNotNull(fieldReader.getObjectReader(JSONFactory.createReadContext()));
    }

    public static class Bean {
        public String[] values;
    }

    @Test
    public void test1() {
        ObjectReader<Bean1> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean1.class);
        String str = JSONObject.of("values", JSONArray.of("a", "b")).toJSONString();
        Bean1 bean = objectReader.readObject(JSONReader.of(str));
        assertNotNull(bean.values);
        assertEquals(2, bean.values.length);
        assertEquals("a", bean.values[0]);
        assertEquals("b", bean.values[1]);
    }

    public static class Bean1 {
        private String[] values;

        public String[] getValues() {
            return values;
        }

        public void setValues(String[] values) {
            this.values = values;
        }
    }
}
