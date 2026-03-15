/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package com.alibaba.fastjson2.support.vertx.benchmark;

import com.alibaba.fastjson2.support.vertx.Fastjson2Codec;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.json.jackson.JacksonCodec;
import io.vertx.core.spi.json.JsonCodec;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.openjdk.jmh.annotations.CompilerControl.Mode.INLINE;
import static org.openjdk.jmh.annotations.Mode.AverageTime;

@State(Scope.Thread)
@BenchmarkMode(AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class JsonEncodeBenchmark extends BenchmarkBase {
    private JsonObject tiny;
    private JsonObject small;
    private JsonObject wide;
    private JsonObject deep;
    private JsonCodec fastjson2Codec;
    private JsonCodec jacksonCodec;
    private JsonCodec databindCodec;

    @Setup
    public void setup() {
        ClassLoader classLoader = getClass().getClassLoader();
        tiny = new JsonObject(Collections.singletonMap("message", "Hello, World!"));
        small = loadJson(classLoader.getResource("small_bench.json"));
        wide = loadJson(classLoader.getResource("wide_bench.json"));
        deep = loadJson(classLoader.getResource("deep_bench.json"));
        fastjson2Codec = new Fastjson2Codec();
        jacksonCodec = new JacksonCodec();
        databindCodec = new DatabindCodec();
    }

    private JsonObject loadJson(URL url) {
        try {
            return new JsonObject(new ObjectMapper().readValue(url, Map.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public String smallStringFastjson2() {
        return stringFastjson2(small);
    }

    @Benchmark
    public String smallStringJackson() {
        return stringJackson(small);
    }

    @Benchmark
    public String smallStringDatabind() {
        return stringDatabind(small);
    }

    @Benchmark
    public String wideStringFastjson2() {
        return stringFastjson2(wide);
    }

    @Benchmark
    public String wideStringJackson() {
        return stringJackson(wide);
    }

    @Benchmark
    public String wideStringDatabind() {
        return stringDatabind(wide);
    }

    @Benchmark
    public String deepStringFastjson2() {
        return stringFastjson2(deep);
    }

    @Benchmark
    public String deepStringJackson() {
        return stringJackson(deep);
    }

    @Benchmark
    public String deepStringDatabind() {
        return stringDatabind(deep);
    }

    @CompilerControl(INLINE)
    private String stringFastjson2(JsonObject jsonObject) {
        return fastjson2Codec.toString(jsonObject);
    }

    @CompilerControl(INLINE)
    private String stringJackson(JsonObject jsonObject) {
        return jacksonCodec.toString(jsonObject);
    }

    @CompilerControl(INLINE)
    private String stringDatabind(JsonObject jsonObject) {
        return databindCodec.toString(jsonObject);
    }

    @Benchmark
    public Buffer tinyBufferFastjson2() {
        return bufferFastjson2(tiny);
    }

    @Benchmark
    public Buffer tinyBufferJackson() {
        return bufferJackson(tiny);
    }

    @Benchmark
    public Buffer smallBufferFastjson2() {
        return bufferFastjson2(small);
    }

    @Benchmark
    public Buffer smallBufferJackson() {
        return bufferJackson(small);
    }

    @Benchmark
    public Buffer smallBufferDatabind() {
        return bufferDatabind(small);
    }

    @Benchmark
    public Buffer deepBufferFastjson2() {
        return bufferFastjson2(deep);
    }

    @Benchmark
    public Buffer deepBufferJackson() {
        return bufferJackson(deep);
    }

    @Benchmark
    public Buffer deepBufferDatabind() {
        return bufferDatabind(deep);
    }

    @Benchmark
    public Buffer wideBufferFastjson2() {
        return bufferFastjson2(wide);
    }

    @Benchmark
    public Buffer wideBufferJackson() {
        return bufferJackson(wide);
    }

    @Benchmark
    public Buffer wideBufferDatabind() {
        return bufferDatabind(wide);
    }

    @CompilerControl(INLINE)
    private Buffer bufferFastjson2(JsonObject jsonObject) {
        return fastjson2Codec.toBuffer(jsonObject);
    }

    @CompilerControl(INLINE)
    private Buffer bufferJackson(JsonObject jsonObject) {
        return jacksonCodec.toBuffer(jsonObject);
    }

    @CompilerControl(INLINE)
    private Buffer bufferDatabind(JsonObject jsonObject) {
        return databindCodec.toBuffer(jsonObject);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JsonEncodeBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
