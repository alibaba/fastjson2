package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.NameFilter;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapFilteredSortTest {
    @JSONType(alphabetic = false)
    public static class KV {
        public int zeta = 3;
        public int alpha = 1;
    }

    @Test
    public void filteredSortedMapValuesStaySorted() {
        // a registered map writer carrying the sort bit: any filter that routes through
        // writeWithFilter must not lose the sorted value writers (context word alone dropped it)
        ObjectWriterImplMap mapWriter = new ObjectWriterImplMap(LinkedHashMap.class,
                JSONWriter.Feature.SortFieldNamesAlphabetically.mask);
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.register(LinkedHashMap.class, mapWriter);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("k", new KV());

        NameFilter nameFilter = (object, name, value) -> name;

        assertEquals("{\"k\":{\"alpha\":1,\"zeta\":3}}",
                JSON.toJSONString(map, new JSONWriter.Context(provider)));

        JSONWriter.Context filtered = new JSONWriter.Context(provider);
        filtered.setNameFilter(nameFilter);
        assertEquals("{\"k\":{\"alpha\":1,\"zeta\":3}}", JSON.toJSONString(map, filtered));

        JSONWriter.Context contextSorted = new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically);
        contextSorted.setNameFilter(nameFilter);
        assertEquals("{\"k\":{\"alpha\":1,\"zeta\":3}}", JSON.toJSONString(map, contextSorted));
    }
}
