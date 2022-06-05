package com.alibaba.fastjson2.mixins;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MixinTest6 {
    @Test
    public void test() {
        JSON.mixIn(Address.class, AddressMixin.class);

        Address address = new Address("HangZhou");
        String str = JSON.toJSONString(address);
        assertEquals("\"HangZhou\"", str);
        Address address1 = JSON.parseObject(str, Address.class);
        assertEquals(address.getAddress(), address1.getAddress());
    }

    private static class AddressMixin {
        @JSONCreator
        public AddressMixin(@JSONField(value = true) String address) {
        }

        @JSONField(value = true)
        public String getAddress() {
            return null;
        }
    }

    public static class Address {
        private final String address;

        public Address(String address) {
            this.address = address;
        }

        public String getAddress() {
            return address;
        }
    }
}
