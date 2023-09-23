package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1888 {
    @Test
    public void test() {
        Device device = new Device("aaa", "123");
        assertEquals("{\"deviceName\":\"123\",\"username\":\"aaa\"}", JSON.toJSONString(device));

        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Device.class);

        assertEquals("{\"deviceName\":\"123\",\"username\":\"aaa\"}", objectWriter.write(device));
    }

    public record Device(String username, String deviceName) {
        @Override
        public String toString() {
            return username + "@" + deviceName;
        }
    }
}
