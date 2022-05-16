package com.alibaba.fastjson2.support.spring.mongodb;

import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

public class GeoJsonReaderModule implements ObjectReaderModule {
    public final static GeoJsonReaderModule INSTANCE = new GeoJsonReaderModule();

    @Override
    public void init(ObjectReaderProvider provider) {
        provider.register(GeoJsonPoint.class, GeoJsonPointReader.INSTANCE);
    }
}
