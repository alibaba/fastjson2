package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2614 {
    @Test
    public void test() {
        String dateStr = "\"2024-05-21T16:15:19.371Z\"";
        Date date = JSON.parseObject(dateStr, Date.class);

        String str = "{\"ttl\": {\"$date\": " + dateStr + "}}";
        Message msg = JSON.parseObject(str, Message.class);
        assertEquals(date.getTime(), msg.ttl.getTime());

        JSONObject object = JSON.parseObject(str);
        Date ttl = object.getObject("ttl", Date.class);
        assertEquals(date.getTime(), ttl.getTime());
    }

    @Getter
    @Setter
    public static class Message {
        private Date ttl;
    }
}
