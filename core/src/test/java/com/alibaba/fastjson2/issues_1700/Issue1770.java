package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class Issue1770 {
    @Test
    public void test() {
        String str = "{\"data\":\"tp20140101\",\"effectiveDate\":1388505600000}";
        TimelineParameter p = JSON.parseObject(str, TimelineParameter.class);
    }

    public static class TimelineParameter {
        public DateTime effectiveDate;
        public String data;

        public TimelineParameter(String data, DateTime effectiveDate) {
            this.effectiveDate = effectiveDate;
            this.data = data;
        }

        public void setEffectiveDate(Date effectiveDate) {
            this.effectiveDate = new DateTime(effectiveDate);
        }
    }
}
