package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.*;

public class Issue1302 {
    @Test
    public void test() {
        Set<String> set = new HashSet();
        constructOutbound(set);
    }

    public List<Map<String, Object>> constructOutbound(Set<String> guids) {
        // 省略 一些代码
        List<Map<String, Object>> results = new ArrayList<>();
        results = JSON.parseObject(
                JSON.toJSONString(
                        results,
                        (ValueFilter) (o, name, value) -> {
                            if (StringUtils.isEmpty(Objects.toString(value, null))) {
                                return null;
                            }
                            return value;
                        },
                        JSONWriter.Feature.WriteNulls
                ),
                new TypeReference<List<LinkedHashMap<String, Object>>>() {}.getType()
        );
        return results;
    }
}
