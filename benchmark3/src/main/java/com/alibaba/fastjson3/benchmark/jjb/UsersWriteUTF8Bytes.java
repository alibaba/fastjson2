package com.alibaba.fastjson3.benchmark.jjb;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class UsersWriteUTF8Bytes {
    static Users users;
    static com.alibaba.fastjson3.ObjectWriter<Users> reflectWriter;
    static com.alibaba.fastjson3.ObjectWriter<Users> asmWriter;

    static {
        try {
            InputStream is = UsersWriteUTF8Bytes.class.getClassLoader().getResourceAsStream("data/jjb/user.json");
            byte[] bytes = is.readAllBytes();
            users = com.alibaba.fastjson2.JSON.parseObject(bytes, Users.class);
            reflectWriter = com.alibaba.fastjson3.writer.ObjectWriterCreator.createObjectWriter(Users.class);
            asmWriter = com.alibaba.fastjson3.writer.ObjectWriterCreatorASM.createObjectWriter(Users.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.toJSONBytes(users));
    }

    @Benchmark
    public void fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.toJSONBytes(users));
    }

    @Benchmark
    public void fastjson3_reflect(Blackhole bh) {
        try (com.alibaba.fastjson3.JSONGenerator gen = com.alibaba.fastjson3.JSONGenerator.ofUTF8()) {
            reflectWriter.write(gen, users, null, null, 0);
            bh.consume(gen.toByteArray());
        }
    }

    @Benchmark
    public void fastjson3_asm(Blackhole bh) {
        try (com.alibaba.fastjson3.JSONGenerator gen = com.alibaba.fastjson3.JSONGenerator.ofUTF8()) {
            asmWriter.write(gen, users, null, null, 0);
            bh.consume(gen.toByteArray());
        }
    }

    @Benchmark
    public void wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.toJsonBytes(users));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(UsersWriteUTF8Bytes.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(2)
                .measurementIterations(3)
                .forks(2)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
