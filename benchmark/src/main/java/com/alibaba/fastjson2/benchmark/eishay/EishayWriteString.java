package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.benchmark.eishay.vo.MediaContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class EishayWriteString {
    static MediaContent mc;
    static ObjectMapper mapper = new ObjectMapper();

    static {
        try {
            InputStream is = EishayWriteString.class.getClassLoader().getResourceAsStream("data/eishay.json");
            String str = IOUtils.toString(is, "UTF-8");
            mc = JSONReader.of(str)
                    .read(MediaContent.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson1(Blackhole bh) {
        bh.consume(com.alibaba.fastjson.JSON.toJSONString(mc));
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(JSON.toJSONString(mc));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(mapper.writeValueAsString(mc));
    }

    public static void fastjson2_perf(Blackhole bh) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            bh.consume(JSON.toJSONString(mc));
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("EishayParseUTF8Bytes : " + millis);
        // zulu17.32.13 :
        // zulu11.52.13 : 361 354
        // zulu8.58.0.13 : 353
    }

    public void fastjson2_perf_test() {
        for (int i = 0; i < 10; i++) {
            Blackhole bh = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
            fastjson2_perf(bh);
        }
    }

    public static void main(String[] args) throws RunnerException {
//        new EishayWriteString().fastjson2_perf_test();
        Options options = new OptionsBuilder()
                .include(EishayWriteString.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
