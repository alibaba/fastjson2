package com.alibaba.fastjson.date;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class DateTest_dotnet_1 {
    @Test
    public void test_date() throws Exception {
        String text = "{\"date\":\"/Date(1461081600000)/\"}";

        Model model = JSON.parseObject(text, Model.class);
        Assertions.assertEquals(1461081600000L, model.date.getTime());
    }

    public static class Model {
        private Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
