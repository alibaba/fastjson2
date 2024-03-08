package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2199 {
    @Data
    public static class Obj1 {
        @JSONField(format = "yyyy-MM-dd hh:mm:ss")
        private Date startTime;
    }

    @Test
    public void test() throws Exception {
        Obj1 orderBO = new Obj1();
        orderBO.setStartTime(new Date());
        String s = JSONObject.toJSONString(orderBO);
        Obj1 obj1 = JSONObject.parseObject(s, Obj1.class);
        assertEquals(s, JSON.toJSONString(obj1));
    }
}
