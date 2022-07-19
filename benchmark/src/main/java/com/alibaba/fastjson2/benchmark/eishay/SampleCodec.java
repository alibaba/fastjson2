package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.benchmark.eishay.vo.Sample;
import io.fury.Fury;
import org.openjdk.jmh.annotations.Benchmark;

public class SampleCodec {
    private static Sample sample;
    private static byte[] furyBytes;
    private static Fury fury;
    static {
        sample = new Sample();
        sample.populate(true);
        fury = Fury.builder()
                .withClassRegistrationRequired(false)
                .withStringCompressed(true)
                .ignoreStringReference(true).build();
        furyBytes = fury.serialize(sample);
    }

    @Benchmark
    public Object fury_serialize() {
       return fury.serialize(sample);
    }


    @Benchmark
    public Object fury_deserialize() {
        return fury.deserialize(furyBytes);
    }
}
