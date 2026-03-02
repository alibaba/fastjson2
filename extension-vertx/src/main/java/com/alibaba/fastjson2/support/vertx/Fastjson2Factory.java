package com.alibaba.fastjson2.support.vertx;

import io.vertx.core.spi.JsonFactory;
import io.vertx.core.spi.json.JsonCodec;

public class Fastjson2Factory implements JsonFactory {

    public static final Fastjson2Factory INSTANCE = new Fastjson2Factory();

    public static final Fastjson2Codec CODEC = new Fastjson2Codec();

    @Override
    public JsonCodec codec() {
        return CODEC;
    }
}
