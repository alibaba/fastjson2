package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1320 {
    @Test
    public void test() {
        String json = "{\"values\":\"\"}";

        Bean bean = JSONObject.parseObject(json, Bean.class);
        assertEquals(0, bean.values.size());
    }

    @Test
    public void test1() {
        String json = "{\"values\":\"\"}";

        ObjectReader<Bean> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean.class);
        Bean bean = objectReader.readObject(JSONReader.of(json));
        assertEquals(0, bean.values.size());
    }

    public static class Bean {
        private List<String> values;

        public List<String> getValues() {
            return values;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }
    }
}
