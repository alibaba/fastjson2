package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue769 {
    @Test
    public void testCase() {
        NameFilter nameFilter = (o, s, o1) -> s;
        ValueFilter localDateFormatter1 = (o, s, source) -> {
            return source;
        };
        Filter[] defaultSerializeFilters = {nameFilter, localDateFormatter1};

        JSONWriter.Feature[] serializeFeatures = {
                /* 序列化enum使用name */
                JSONWriter.Feature.WriteEnumsUsingName,
                /* 序列化BigDecimal使用toPlainString，避免科学计数法*/
                JSONWriter.Feature.WriteBigDecimalAsPlain,
                /* 忽略getter方法异常 */
                JSONWriter.Feature.IgnoreErrorGetter,
                /*
                 * 基于字段序列化，如果不配置，会默认基于public的field和getter方法序列化。配置后，会基于非static的field（包括private）做反序列化。
                 * 注意:如果指定了filter，不配置此参数，IgnoreErrorGetter 会失效。
                 * */
                //JSONWriter.Feature.FieldBased,
        };

        DataVO3 d = new DataVO3();
        d.setV(null);

        String json = JSON.toJSONString(d, "yyyy-MM-dd HH:mm:ss", defaultSerializeFilters, serializeFeatures);
        assertEquals("{}", json);
    }

    static class DataVO3 {
        LocalDateTime v;

        public LocalDateTime getV() {
            throw new RuntimeException("mock an exception");
        }

        public void setV(LocalDateTime v) {
            this.v = v;
        }
    }
}
