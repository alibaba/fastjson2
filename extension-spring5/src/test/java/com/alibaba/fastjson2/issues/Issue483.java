package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.support.spring.data.mongodb.GeoJsonReaderModule;
import com.alibaba.fastjson2.support.spring.data.mongodb.GeoJsonWriterModule;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue483 {
    @Test
    public void testGpsMO() {
        com.alibaba.fastjson2.JSON.register(GeoJsonReaderModule.INSTANCE);
        com.alibaba.fastjson2.JSON.register(GeoJsonWriterModule.INSTANCE);
        String str = "{\n" +
                "  \"gpsAddress\": {\n" +
                "\t  \"coordinates\": [113.92966694974888, 22.543470524848683],\n" +
                "\t  \"type\": \"Point\",\n" +
                "\t  \"x\": 113.92966694974888,\n" +
                "\t  \"y\": 22.543470524848683\n" +
                "  }\n" +
                "}";
        DispatchPositionInfoMO dispatchPositionInfoMO = com.alibaba.fastjson2.JSON.parseObject(str, DispatchPositionInfoMO.class);
        assertEquals(113.92966694974888D, dispatchPositionInfoMO.gpsAddress.getX());
        assertEquals(22.543470524848683D, dispatchPositionInfoMO.gpsAddress.getY());
    }

    public static class DispatchPositionInfoMO {
        public GeoJsonPoint gpsAddress;
    }
}
