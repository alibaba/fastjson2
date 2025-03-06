package com.alibaba.fastjson2.benchmark.simdjson;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Twitter {
    static final String file = "data/simd-json/twitter.json";
    static byte[] bytes;
    static {
        try (InputStream is = Twitter.class.getClassLoader().getResourceAsStream(file)) {
            String str = IOUtils.toString(is, "UTF-8");
            bytes = str.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fastjson2_parse(Blackhole bh) {
        bh.consume(JSON.parseObject(bytes, SimdJsonTwitter.class));
    }

    public void wast_parse(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(bytes, SimdJsonTwitter.class));
    }

    public record SimdJsonUser(boolean default_profile, String screen_name) {
    }

    public record SimdJsonStatus(SimdJsonUser user) {
    }

    public record SimdJsonTwitter(List<SimdJsonStatus> statuses) {
    }
}
