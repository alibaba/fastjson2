package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue772 {
    @Test
    public void testCase() {
        NameFilter nameFilter = (o, s, o1) -> s;
        ValueFilter localDateFormatter1 = (o, s, source) -> {
            return source;
        };
        Filter[] defaultSerializeFilters = {nameFilter, localDateFormatter1};

        JSONWriter.Feature[] serializeFeatures = {
                /*
                 * 基于字段序列化，如果不配置，会默认基于public的field和getter方法序列化。配置后，会基于非static的field（包括private）做反序列化。
                 * 注意:如果指定了filter，不配置此参数，IgnoreErrorGetter 会失效。
                 * */
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
        };

        DataVO4 d = new DataVO4("0");
        DataVO4 d1 = new DataVO4("1");
        DataVO4 d2 = new DataVO4("2");
        d2.dv = d1;
        d1.dv = d;
        d.dv = d2;
        String json1 = JSON.toJSONString(d, "yyyy-MM-dd HH:mm:ss", defaultSerializeFilters, serializeFeatures);
        assertEquals("{\"dv\":{\"dv\":{\"dv\":{\"$ref\":\"$\"},\"name\":\"1\"},\"name\":\"2\"},\"name\":\"0\"}", json1);
    }

    static class DataVO4 {
        DataVO4(String name) {
            this.name = name;
        }

        String name;
        DataVO4 dv;
    }
}
