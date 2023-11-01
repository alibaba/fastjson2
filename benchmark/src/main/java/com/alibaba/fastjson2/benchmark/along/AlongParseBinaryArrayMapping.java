package com.alibaba.fastjson2.benchmark.along;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.benchmark.along.vo.HarmDTO;
import com.alibaba.fastjson2.benchmark.along.vo.SkillCategory;
import com.alibaba.fastjson2.benchmark.along.vo.SkillFire_S2C_Msg;
import io.fury.Fury;
import io.fury.config.Language;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static com.alibaba.fastjson2.JSONReader.Feature.FieldBased;
import static com.alibaba.fastjson2.JSONReader.Feature.SupportArrayToBean;



public class AlongParseBinaryArrayMapping {
    static Fury fury;

    static SkillFire_S2C_Msg object;
    static byte[] fastjson2JSONBBytes;
    static byte[] furyBytes;

    /**
     * 从资源文件 "data/along.json" 中加载 JSON 字符串，并使用 Fury 的 JSONReader 将其解析为 SkillFire_S2C_Msg 对象。
     * 配置 Fury 实例，包括选择编程语言（JAVA）、禁用引用跟踪、禁用类注册、启用数字压缩等。
     * 使用 Fury 的 register 方法注册 SkillCategory、SkillFire_S2C_Msg 和 HarmDTO 类型。
     * 使用 Fastjson2 将 object 序列化为字节数组，使用 Fury 将其序列化为字节数组。
     * */
    static {
        try {
            InputStream is = AlongParseBinaryArrayMapping.class.getClassLoader().getResourceAsStream("data/along.json");
            String str = IOUtils.toString(is, "UTF-8");
            object = JSONReader.of(str).read(SkillFire_S2C_Msg.class);

            fury = Fury.builder().withLanguage(Language.JAVA)
                    .withRefTracking(false)
                    .requireClassRegistration(false)
                    .withNumberCompressed(true)
                    .build();

            fury.register(SkillCategory.class);
            fury.register(SkillFire_S2C_Msg.class);
            fury.register(HarmDTO.class);

            fastjson2JSONBBytes = JSONB.toBytes(object, JSONWriter.Feature.BeanToArray);
            furyBytes = fury.serializeJavaObject(object);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 执行 Fastjson2 的反序列化操作，并使用 Blackhole 来消耗结果，以防止 JIT 编译器优化掉这些操作。
     * */
    @Benchmark
    public void jsonb(Blackhole bh) {
        bh.consume(JSONB.parseObject(fastjson2JSONBBytes, SkillFire_S2C_Msg.class, FieldBased, SupportArrayToBean));
    }

    /**
     * 执行 Fury 的反序列化操作，同样使用 Blackhole 消耗结果。
     * */
    @Benchmark
    public void fury(Blackhole bh) {
        bh.consume(fury.deserializeJavaObject(furyBytes, SkillFire_S2C_Msg.class));
    }


    /**
     * 创建 JMH（Java Microbenchmarking Harness）测试的配置选项，包括测试类的全名、性能测试模式、时间单位、预热迭代次数、并行度、线程数等。
     * 使用 JMH 的 Runner 运行性能测试。
     * */
    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(AlongParseBinaryArrayMapping.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
