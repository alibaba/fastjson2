package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Non-String map keys are written with the caller's context, so its date format and filters apply to them as
 * they do to values; a context with filters takes the filtering write path, which serializes such keys separately.
 */
public class MapKeyContextTest {
    public static class Dated {
        public Map<LocalDate, String> byDay = new LinkedHashMap<>(Collections.singletonMap(LocalDate.of(2026, 10, 6), "v"));
        public LocalDate day = LocalDate.of(2026, 10, 6);
    }

    @Test
    public void dateKeysKeepTheContextFormatWhenFiltersAreSet() {
        JSONWriter.Context context = new JSONWriter.Context();
        context.setDateFormat("yyyy/MM/dd");
        context.configFilter((PropertyFilter) (object, name, value) -> true);
        assertEquals("{\"byDay\":{\"2026/10/06\":\"v\"},\"day\":\"2026/10/06\"}", JSON.toJSONString(new Dated(), context));
    }

    public static class Secret {
        public String password = "hunter2";
        public String user = "u";
    }

    public static class KeyedBySecret {
        public Map<Secret, String> m = new LinkedHashMap<>(Collections.singletonMap(new Secret(), "v"));
    }

    @Test
    public void beanKeysGoThroughTheContextFilters() {
        JSONWriter.Context context = new JSONWriter.Context();
        context.configFilter((ValueFilter) (object, name, value) -> "password".equals(name) ? "***" : value);
        assertEquals("{\"m\":{\"{\\\"password\\\":\\\"***\\\",\\\"user\\\":\\\"u\\\"}\":\"v\"}}",
                JSON.toJSONString(new KeyedBySecret(), context));
    }
}
