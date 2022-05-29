package com.alibaba.fastjson2.v1issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CanalTest {
    @Test
    public void test0() {
        InetSocketAddress address = new InetSocketAddress("localhost", 3306);
        String str = JSON.toJSONString(address);
        assertEquals("{\"address\":\"localhost\",\"port\":3306}", str);
        InetSocketAddress address1 = JSON.parseObject(str, InetSocketAddress.class);
        assertEquals(address1.getAddress(), address.getAddress());
        assertEquals(address1.getPort(), address.getPort());
    }
}
