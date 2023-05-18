package com.alibaba.fastjson2.benchmark.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.benchmark.primitves.vo.DoubleValue20Field;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DoubleValue20L {
    static final String str = "{\"v0000\":0.7790456752571444,\"v0001\":0.660951133487578,\"v0002\":0.016859966762709178,\"v0003\":0.08792646995171371,\"v0004\":0.5004314371646074,\"v0005\":0.40819072662809397,\"v0006\":0.467977801426522,\"v0007\":0.5157402268914001,\"v0008\":0.02573911575880017,\"v0009\":0.3954472301618003,\"v0010\":0.8488319941451605,\"v0011\":0.16331853548023045,\"v0012\":0.15614021653886967,\"v0013\":0.13241092483919703,\"v0014\":0.5292598224995992,\"v0015\":0.5025147692434769,\"v0016\":0.042355711968873444,\"v0017\":0.3480302380487452,\"v0018\":0.5439821627623341,\"v0019\":0.8083989078490904}";
    static byte[] jsonbBytes;
    static ObjectMapper mapper = new ObjectMapper();
    static final Class<DoubleValue20Field> OBJECT_CLASS = DoubleValue20Field.class;

    public DoubleValue20L() {
        try {
            Random r = new Random();
            DoubleValue20Field bean = JSON.parseObject(str, OBJECT_CLASS);
            jsonbBytes = JSONB.toBytes(bean);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(
                com.alibaba.fastjson.JSON.parseObject(str, OBJECT_CLASS)
        );
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(
                JSON.parseObject(str, OBJECT_CLASS)
        );
    }

    @Benchmark
    public void fastjson2_jsonb(Blackhole bh) {
        bh.consume(
                JSONB.parseObject(jsonbBytes, OBJECT_CLASS)
        );
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(
                mapper.readValue(str, OBJECT_CLASS)
        );
    }

    @Benchmark
    public void wastjson(Blackhole bh) throws Exception {
        bh.consume(
                io.github.wycst.wast.json.JSON.parseObject(str, OBJECT_CLASS)
        );
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(DoubleValue20L.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
