package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2073 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.dateTimes = new ArrayList<>();
        bean.dateTimes.add(LocalDateTime.of(2017, 11, 6, 12, 13, 14));
        String str = JSON.toJSONString(bean);
        assertEquals("{\"dateTimes\":[\"2017-11-06\"]}", str);
    }

    public static class Bean {
        @JSONField(format = "yyyy-MM-dd")
        public List<LocalDateTime> dateTimes;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.dateTimes = new ArrayList<>();
        bean.dateTimes.add(LocalDate.of(2017, 11, 6));
        String str = JSON.toJSONString(bean);
        assertEquals("{\"dateTimes\":[\"20171106\"]}", str);
    }

    public static class Bean1 {
        @JSONField(format = "yyyyMMdd")
        public List<LocalDate> dateTimes;
    }
}
