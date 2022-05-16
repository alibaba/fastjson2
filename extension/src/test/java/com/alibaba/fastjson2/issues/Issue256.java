package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.support.spring.mongodb.GeoJsonReaderModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue256 {

    @BeforeEach
    public void init() {
        JSON.register(GeoJsonReaderModule.INSTANCE);
    }

    @Test
    public void test() {
        String str = "{\n" +
                "    \"_id\":\n" +
                "    {\n" +
                "        \"$oid\": \"61a591ef96eb001a9437d6c4\"\n" +
                "    },\n" +
                "    \"gpsAddress\":\n" +
                "    {\n" +
                "        \"type\": \"Point\",\n" +
                "        \"coordinates\":\n" +
                "        [\n" +
                "            114.027285886961,\n" +
                "            22.6741582981732\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        Bean bean = JSON.parseObject(str, Bean.class);
        assertNotNull(bean.gpsAddress);
        assertEquals(114.027285886961D, bean.gpsAddress.getX());
        assertEquals(22.6741582981732D, bean.gpsAddress.getY());
    }

    public static class Bean {
        public GeoJsonPoint gpsAddress;
    }
}
