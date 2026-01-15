package com.alibaba.fastjson.issue_3300;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @Author ：Nanqi
 * @Date ：Created in 19:07 2020/7/21
 */
public class Issue3358 {
    @BeforeEach
    protected void setUp() throws Exception {
        JSON.defaultTimeZone = TimeZone.getTimeZone(DateUtils.DEFAULT_ZONE_ID);
        JSON.defaultLocale = Locale.US;
    }

    @Test
    public void test_for_issue() throws Exception {
        ZoneId zoneId = JSONFactory.createWriteContext().getZoneId();
        ZoneId zoneIdReader = JSONFactory.createReadContext().getZoneId();
        Model validateCode = new Model("111", 600);
        String jsonString = JSON.toJSONString(validateCode);
        LocalDateTime expireTime = validateCode.getExpireTime();
        long millis = expireTime.atZone(zoneId)
                .toInstant()
                .toEpochMilli();
        String expected = "{\"code\":\"111\",\"expireTime\":" + millis + ",\"expried\":false}";
        assertEquals(expected, jsonString,
                () -> "writerId " + zoneId + ", readerZonedI " + zoneIdReader);
        assertEquals(expected, new String(JSON.toJSONBytes(validateCode)));
        Model backModel = JSON.parseObject(jsonString, Model.class);
        assertEquals(
                expireTime.truncatedTo(ChronoUnit.MILLIS),
                backModel.getExpireTime(),
                () -> "writerId " + zoneId + ", readerZonedI " + zoneIdReader);

        jsonString = "{\"code\":\"111\"}";
        backModel = JSON.parseObject(jsonString, Model.class);
        assertNull(backModel.getExpireTime());
    }

    public static class Model {
        private String code;

        private LocalDateTime expireTime;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public LocalDateTime getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(LocalDateTime expireTime) {
            this.expireTime = expireTime;
        }

        public Model(String code, int expireIn) {
            this.code = code;
            this.expireTime = LocalDateTime.now().plusSeconds(expireIn);
        }

        @JSONCreator
        public Model(String code, LocalDateTime expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }

        public boolean isExpried() {
            return LocalDateTime.now().isAfter(getExpireTime());
        }
    }
}
