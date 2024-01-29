package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JSON_copyTo {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.date = LocalDate.of(2012, 12, 13);

        Bean1 bean1 = JSON.copyTo(bean, Bean1.class);
        assertEquals("20121213", bean1.date);

        Bean bean_x1 = JSON.copyTo(bean1, Bean.class);
        assertEquals(bean.date, bean_x1.date);

        Bean2 bean2 = JSON.copyTo(bean1, Bean2.class);
        assertNotNull(bean2.date);
        assertEquals(
                bean.date.atStartOfDay(DateUtils.DEFAULT_ZONE_ID).toInstant().toEpochMilli(),
                bean2.date.getTime()
        );

        Bean1 bean1_x1 = JSON.copyTo(bean2, Bean1.class);
        assertEquals(bean1.date, bean1_x1.date);
    }

    public static class Bean {
        @JSONField(format = "yyyyMMdd")
        public LocalDate date;
    }

    public static class Bean1 {
        public String date;
    }

    public static class Bean2 {
        @JSONField(format = "yyyyMMdd")
        public Date date;
    }
}
