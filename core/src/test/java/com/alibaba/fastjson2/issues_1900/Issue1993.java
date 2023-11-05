package com.alibaba.fastjson2.issues_1900;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1993 {
    @Test
    public void test() {
        JSON.configReaderZoneId(ZoneId.of("Asia/Shanghai"));
        JSON.configWriterZoneId(ZoneId.of("Asia/Shanghai"));
        String str = "{\"beginTime\":\"2023-10-30T16:00:00.000Z\",\"endTime\":\"\"}";
        OrderDTO orderQTO = JSON.parseObject(str, OrderDTO.class);
        assertEquals("{\"beginTime\":\"2023-10-31\"}", JSON.toJSONString(orderQTO));
    }

    @Data
    public static class OrderDTO {
        LocalDate beginTime;
        LocalDate endTime;
    }
}
