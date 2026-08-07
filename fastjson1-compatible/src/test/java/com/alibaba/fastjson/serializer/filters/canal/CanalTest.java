package com.alibaba.fastjson.serializer.filters.canal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CanalTest {
    @Test
    public void test() {
        SerializeConfig.getGlobalInstance().put(Address.class, AddressSerializer.instance);

        Address address = new Address();
        address.setHostName("local");

        assertEquals("\"local\"", JSON.toJSONString(address));
    }
}
