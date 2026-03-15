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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.json.jackson.JacksonCodec;
import io.vertx.core.spi.json.JsonCodec;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@State(Scope.Thread)
public class JsonDecodeBenchmark extends BenchmarkBase {
    private Buffer small;
    private Buffer wide;
    private Buffer deep;
    private String smallString;
    private String wideString;
    private String deepString;
    private JsonCodec fastjson2Codec;
    private JsonCodec jacksonCodec;
    private JsonCodec databindCodec;

    @Setup
    public void setup() {
        small = loadJsonAsBuffer("small_bench.json");
        wide = loadJsonAsBuffer("wide_bench.json");
        deep = loadJsonAsBuffer("deep_bench.json");
        smallString = small.toString();
        wideString = wide.toString();
        deepString = deep.toString();
        fastjson2Codec = new Fastjson2Codec();
        jacksonCodec = new JacksonCodec();
        databindCodec = new DatabindCodec();
    }

    private Buffer loadJsonAsBuffer(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream file = classLoader.getResourceAsStream(filename)) {
            String str = new BufferedReader(new InputStreamReader(file, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining());
            Buffer encoded = Buffer.buffer(str);
            return Buffer.buffer().appendBuffer(encoded);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void smallStringFastjson2(Blackhole blackhole) {
        stringFastjson2(smallString, blackhole);
    }

    @Benchmark
    public void smallStringJackson(Blackhole blackhole) {
        stringJackson(smallString, blackhole);
    }

    @Benchmark
    public void smallStringDatabind(Blackhole blackhole) throws Exception {
        stringDatabind(smallString, blackhole);
    }

    @Benchmark
    public void wideStringFastjson2(Blackhole blackhole) {
        stringFastjson2(wideString, blackhole);
    }

    @Benchmark
    public void wideStringJackson(Blackhole blackhole) {
        stringJackson(wideString, blackhole);
    }

    @Benchmark
    public void wideStringDatabind(Blackhole blackhole) throws Exception {
        stringDatabind(wideString, blackhole);
    }

    @Benchmark
    public void deepStringFastjson2(Blackhole blackhole) {
        stringFastjson2(deepString, blackhole);
    }

    @Benchmark
    public void deepStringJackson(Blackhole blackhole) {
        stringJackson(deepString, blackhole);
    }

    @Benchmark
    public void deepStringDatabind(Blackhole blackhole) throws Exception {
        stringDatabind(deepString, blackhole);
    }

    private void stringFastjson2(String str, Blackhole blackhole) {
        blackhole.consume(fastjson2Codec.fromString(str, JsonObject.class));
    }

    private void stringJackson(String str, Blackhole blackhole) {
//        blackhole.consume(jacksonCodec.fromString(str, JsonObject.class));
    }

    private void stringDatabind(String str, Blackhole blackhole) {
        blackhole.consume(databindCodec.fromString(str, JsonObject.class));
    }

    @Benchmark
    public void smallBufferFastjson2(Blackhole blackhole) {
        bufferFastjson2(small, blackhole);
    }

    @Benchmark
    public void smallBufferJackson(Blackhole blackhole) {
        bufferJackson(small, blackhole);
    }

    @Benchmark
    public void smallBufferDatabind(Blackhole blackhole) throws Exception {
        bufferDatabind(small, blackhole);
    }

    @Benchmark
    public void wideBufferFastjson2(Blackhole blackhole) {
        bufferFastjson2(wide, blackhole);
    }

    @Benchmark
    public void wideBufferJackson(Blackhole blackhole) {
        bufferJackson(wide, blackhole);
    }

    @Benchmark
    public void wideBufferDatabind(Blackhole blackhole) throws Exception {
        bufferDatabind(wide, blackhole);
    }

    @Benchmark
    public void deepBufferFastjson2(Blackhole blackhole) {
        bufferFastjson2(deep, blackhole);
    }

    @Benchmark
    public void deepBufferJackson(Blackhole blackhole) {
        bufferJackson(deep, blackhole);
    }

    @Benchmark
    public void deepBufferDatabind(Blackhole blackhole) throws Exception {
        bufferDatabind(deep, blackhole);
    }

    private void bufferFastjson2(Buffer buffer, Blackhole blackhole) {
        blackhole.consume(fastjson2Codec.fromBuffer(buffer, JsonObject.class));
    }

    private void bufferJackson(Buffer buffer, Blackhole blackhole) {
//        blackhole.consume(jacksonCodec.fromBuffer(buffer, JsonObject.class));
    }

    private void bufferDatabind(Buffer buffer, Blackhole blackhole) throws Exception {
        blackhole.consume(databindCodec.fromBuffer(buffer, JsonObject.class));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JsonDecodeBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
