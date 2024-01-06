package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2124 {
    @Test
    public void test() {
        ZoneId zoneId = ZoneId.of("Asia/Karachi");

        JSONWriter.Context writeContext = JSONFactory.createWriteContext();
        writeContext.setZoneId(zoneId);

        Date date = new Date();
        String str = JSON.toJSONString(date, writeContext);

        JSONReader.Context readContext = JSONFactory.createReadContext();
        readContext.setZoneId(zoneId);

        Date date1 = JSON.parseObject(str, Date.class, readContext);
        assertEquals(date, date1);
    }
}
