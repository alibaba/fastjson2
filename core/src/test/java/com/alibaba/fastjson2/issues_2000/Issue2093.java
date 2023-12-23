package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2093 {
    @Test
    public void test() {
        LocalDateTime ldt = LocalDateTime.of(2017, 7, 6, 12, 13, 14);
        Bean bean = new Bean();
        bean.times = new ArrayList<>();
        bean.times.add(ldt);
        bean.timeMap = new HashMap<>();
        bean.timeMap.put("time", ldt);

        String str = JSON.toJSONString(bean);
        assertEquals("{\"timeMap\":{\"time\":\"12:13:14\"},\"times\":[\"12:13:14\"]}", str);
    }

    @Data
    public static class Bean {
        @JSONField(format = "HH:mm:ss")
        private List<LocalDateTime> times;

        @JSONField(format = "HH:mm:ss")
        private Map<String, LocalDateTime> timeMap;
    }
}
