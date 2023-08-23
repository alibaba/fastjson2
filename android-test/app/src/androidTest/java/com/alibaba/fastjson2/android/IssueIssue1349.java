package com.alibaba.fastjson2.android;

import static org.junit.Assert.assertEquals;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;

import org.junit.jupiter.api.Test;

import java.util.Date;

public class IssueIssue1349 {
    @Test
    public void test4() {
        Bean4 bean = JSON.parseObject("{\"date\":\"1863792000000\"}", Bean4.class);
        assertEquals(1863792000000L, bean.date.getTime());
    }

    public class Bean4 {
        @JSONField(locale = "zh", format = "yyyy-MM-dd")
        private Date date;

        public Bean4() {
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
