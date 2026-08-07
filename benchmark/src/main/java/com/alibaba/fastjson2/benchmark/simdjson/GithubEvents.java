package com.alibaba.fastjson2.benchmark.simdjson;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.benchmark.eishay.EishayParseBinary;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.InputStream;

public class GithubEvents {
    static final String file = "data/simd-json/github_events.json";
    static byte[] bytes;
    static {
        try (InputStream is = EishayParseBinary.class.getClassLoader().getResourceAsStream(file)) {
            String str = IOUtils.toString(is, "UTF-8");
            bytes = str.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fastjson2_parse(Blackhole bh) {
        bh.consume(JSON.parseArray(bytes));
    }
}
