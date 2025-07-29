package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriterUtilDateAsMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3650 {
    @Test
    public void test() {
        long millis = System.currentTimeMillis();
        Date date = new Date(millis);

        String str = JSON.toJSONString(date, WriterUtilDateAsMillis);
        assertEquals(Long.toString(millis), str);

        Bean bean = new Bean();
        bean.date = new Date(millis);
        assertEquals(
                "{\"date\":" + millis + "}",
                JSON.toJSONString(bean, WriterUtilDateAsMillis));

        Bean1 bean1 = new Bean1();
        bean1.date = new Date(millis);
        assertEquals(
                "{\"date\":" + millis + "}",
                JSON.toJSONString(bean1, WriterUtilDateAsMillis));
    }

    public static class Bean {
        public Date date;
    }

    public static class Bean1 {
        private Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
