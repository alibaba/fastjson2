package com.alibaba.fastjson2.rocketmq;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RocketMQTest {
    @Test
    public void test() {
        BrokerData brokerData = new BrokerData();
        brokerData.setBrokerAddrs(new HashMap<>());
        brokerData.getBrokerAddrs().put(0L, "0.0.0.0");

        String str = JSON.toJSONString(brokerData);
        assertEquals("{\"brokerAddrs\":{0:\"0.0.0.0\"}}", str);

        BrokerData brokerData1 = JSON.parseObject(str, BrokerData.class);
        assertEquals("0.0.0.0", brokerData1.getBrokerAddrs().get(0L));
    }
}
