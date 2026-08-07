package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1332 {
    @Test
    public void t2() {
        Aa a = new Aa();
        List<Bb> bList = new ArrayList<>();
        Bb b1 = new Bb();
        bList.add(b1);
        Bb b2 = new Bb();
        bList.add(b2);
        List<LocalDateTime> dateTimes1 = new ArrayList<>();
        dateTimes1.add(LocalDateTime.now());
        dateTimes1.add(LocalDateTime.now());
        b1.setDateTimes(dateTimes1);

        List<LocalDateTime> dateTimes2 = new ArrayList<>();
        dateTimes2.add(LocalDateTime.now());
        b2.setDateTimes(dateTimes2);
        a.setBList(bList);
        Map<String, Object> map2 = (Map<String, Object>) JSON.toJSON(a);
        String json = JSON.toJSONString(map2);
        assertNotNull(json);
    }

    @Data
    public class Aa {
        private List<Bb> bList;
    }

    @Data
    public class Bb {
        private List<LocalDateTime> dateTimes;
    }
}
