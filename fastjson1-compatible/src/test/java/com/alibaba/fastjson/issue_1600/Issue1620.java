package com.alibaba.fastjson.issue_1600;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1620 {
    @Test
    public void test() {
        Bean bean = new Bean();
        LocalDateTime ldt = LocalDateTime.of(2023, 7, 25, 18, 33, 28);
        bean.birthday = new Date(ZonedDateTime.of(ldt, DateUtils.SHANGHAI_ZONE_ID).toInstant().toEpochMilli());
        JSONObject jsonObject = JSONObject.from(bean);
        assertEquals("2023-07-25", jsonObject.get("birthday"));
    }

    public static class Bean {
        @JSONField(format = "yyyy-MM-dd")
        public Date birthday;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.birthday = LocalDateTime.of(2023, 7, 25, 18, 33, 28);
        JSONObject jsonObject = JSONObject.from(bean);
        assertEquals("2023-07-25", jsonObject.get("birthday"));
    }

    public static class Bean1 {
        @JSONField(format = "yyyy-MM-dd")
        public LocalDateTime birthday;
    }
}
