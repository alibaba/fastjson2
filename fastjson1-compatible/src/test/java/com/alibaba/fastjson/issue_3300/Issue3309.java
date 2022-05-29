package com.alibaba.fastjson.issue_3300;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * @Author ：Nanqi
 * @Date ：Created in 16:27 2020/6/29
 */
public class Issue3309 {
    @Test
    public void test_for_issue() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("formatDate", "20200623 15:20:01");
        DateFormatTest dateFormatTest = jsonObj.toJavaObject(DateFormatTest.class);
        JSON.toJSONString(dateFormatTest);
    }

    static class DateFormatTest {
        @JSONField(format = "yyyyMMdd HH:mm:ss")
        private Date formatDate;

        public Date getFormatDate() {
            return formatDate;
        }

        public void setFormatDate(Date formatDate) {
            this.formatDate = formatDate;
        }
    }
}
