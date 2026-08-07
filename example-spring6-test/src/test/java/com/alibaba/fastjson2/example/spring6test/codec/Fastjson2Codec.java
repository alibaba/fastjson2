package com.alibaba.fastjson2.example.spring6test.codec;

import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring6.http.codec.Fastjson2Decoder;
import com.alibaba.fastjson2.support.spring6.http.codec.Fastjson2Encoder;
import lombok.Getter;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Getter
public class Fastjson2Codec {
    private final Decoder<Object> decoder;
    private final Encoder<Object> encoder;

    public Fastjson2Codec() {
        var config = new FastJsonConfig();
        var objectMapper = Jackson2ObjectMapperBuilder.json().build();
        this.decoder = new Fastjson2Decoder(objectMapper, config);
        this.encoder = new Fastjson2Encoder(objectMapper, config);
    }
}
