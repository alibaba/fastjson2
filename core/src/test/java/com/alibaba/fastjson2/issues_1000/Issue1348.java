package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.filter.NameFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1348 {
    @Test
    public void test() {
        String str = JSON.toJSONString(
                Bean.builder().build(),
                NameFilter.of(PropertyNamingStrategy.UpperCamelCaseWithUnderScores),
                new JSONWriter.Feature[]{JSONWriter.Feature.WriteMapNullValue}
        );
        assertEquals("{\"A_Map\":null}", str);
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class Bean {
        Map<String, Date> aMap;
    }
}
