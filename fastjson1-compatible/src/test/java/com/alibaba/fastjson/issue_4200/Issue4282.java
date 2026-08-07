package com.alibaba.fastjson.issue_4200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertNotNull;

public class Issue4282 {
    TimeZone timeZone;

    @BeforeEach
    public void setUp() {
        this.timeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @AfterEach
    public void tearDown() {
        TimeZone.setDefault(timeZone);
    }

    @Test
    public void test() throws Exception {
        String time = "2022-09-01 00:00:00";
        Order order = new Order();
        order.setTransactionTime(parseDateWithTZ(time, "yyyy-MM-dd HH:mm:ss", "UTC"));
        String json = JSON.toJSONString(order, SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.UseISO8601DateFormat);
        order = JSON.parseObject(json, Order.class, Feature.AllowISO8601DateFormat);
        assertNotNull(order);
    }

    static Date parseDateWithTZ(String str, String format, String timeZone) throws ParseException {
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setTimeZone(TimeZone.getTimeZone(timeZone));
        return fmt.parse(str);
    }

    public static class Order {
        private Date transactionTime;

        public Date getTransactionTime() {
            return transactionTime;
        }

        public void setTransactionTime(Date transactionTime) {
            this.transactionTime = transactionTime;
        }
    }
}
