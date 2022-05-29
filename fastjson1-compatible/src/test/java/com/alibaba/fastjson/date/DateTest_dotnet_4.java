package com.alibaba.fastjson.date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class DateTest_dotnet_4 {
    @Test
    public void test_date() throws Exception {
        String text = "{\"date\":\"/Date(1461081600321+5000)/\"}";

        JSONObject model = JSON.parseObject(text);
        Assertions.assertEquals(1461081600321L, ((Date) model.getObject("date", Date.class)).getTime());
    }

    private static class Model {
        private Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
