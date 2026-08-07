package com.alibaba.fastjson.serializer.filters;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.alibaba.fastjson.serializer.filters.PropertyFilterTest.A;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValueFilterTest {
    @Test
    public void test_valuefilter() throws Exception {
        ValueFilter filter = (source, name, value) -> {
            if (name.equals("id")) {
                return "AAA";
            }

            return value;
        };

        SerializeWriter out = new SerializeWriter();
        JSONSerializer serializer = new JSONSerializer(out);
        serializer.getValueFilters().add(filter);

        A a = new A();
        serializer.write(a);

        String text = out.toString();
        assertEquals("{\"id\":\"AAA\"}", text);
    }

    @Test
    public void test_toJSONString() throws Exception {
        ValueFilter filter = (source, name, value) -> {
            if (name.equals("id")) {
                return "AAA";
            }

            return value;
        };

        assertEquals("{\"id\":\"AAA\"}", JSON.toJSONString(new A(), filter));
    }

    @Test
    public void test_valuefilter_1() throws Exception {
        ValueFilter filter = (source, name, value) -> {
            if (name.equals("name")) {
                return "AAA";
            }

            return value;
        };

        SerializeWriter out = new SerializeWriter();
        JSONSerializer serializer = new JSONSerializer(out);
        serializer.getValueFilters().add(filter);
        serializer.config(SerializerFeature.WriteMapNullValue, true);

        A a = new A();
        serializer.write(a);

        String text = out.toString();
        assertEquals("{\"id\":0,\"name\":\"AAA\"}", text);
    }

    @Test
    public void test_valuefilter_2() {
        ValueFilter filter = (source, name, value) -> {
            if (name.equals("name")) {
                return "AAA";
            }

            return value;
        };

        SerializeWriter out = new SerializeWriter();
        JSONSerializer serializer = new JSONSerializer(out);
        serializer.getValueFilters().add(filter);
        serializer.config(SerializerFeature.WriteMapNullValue, true);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", null);
        serializer.write(map);

        String text = out.toString();
        assertEquals("{\"name\":\"AAA\"}", text);
    }

    @Test
    public void test_valuefilter_3() throws Exception {
        ValueFilter filter = (source, name, value) -> {
            if (name.equals("name")) {
                return null;
            }
            return value;
        };

        SerializeWriter out = new SerializeWriter();
        JSONSerializer serializer = new JSONSerializer(out);
        serializer.getValueFilters().add(filter);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "AA");
        serializer.write(map);

        String text = out.toString();
        assertEquals("{}", text);
    }

    @Test
    public void test_valuefilter_4() throws Exception {
        ValueFilter filter0 = (source, name, value) -> {
            if (name.equals("id")) {
                return ((Integer) value).intValue() + 1;
            }
            return value;
        };

        ValueFilter filter1 = (source, name, value) -> {
            if (name.equals("id")) {
                return ((Integer) value).intValue() + 10;
            }
            return value;
        };

        Bean bean = new Bean();
        bean.id = 100;
        String str = JSON.toJSONString(bean, ValueFilter.compose(filter0, filter1));
        assertEquals("{\"id\":111}", str);
    }

    public static class Bean {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
