package com.alibaba.fastjson2.benchmark.fastcode;

import com.alibaba.fastjson2.util.JDKUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.ObjIntConsumer;

public class InvokeFirst {
    @Benchmark
    public void genLambda(Blackhole bh) throws Throwable {
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(Bean.class);
        MethodType invokedType = MethodType.methodType(ObjIntConsumer.class);
        MethodHandle target = lookup.findVirtual(Bean.class, "setId", MethodType.methodType(void.class, int.class));
        MethodType instantiatedMethodType = MethodType.methodType(void.class, Bean.class, int.class);
        MethodType samMethodType = MethodType.methodType(void.class, Object.class, int.class);

        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,
                "accept",
                invokedType,
                samMethodType,
                target,
                instantiatedMethodType
        );
        ObjIntConsumer function = (ObjIntConsumer) callSite.getTarget().invoke();

        Bean bean = new Bean();
        function.accept(bean, 123);
        bh.consume(bean);
    }

    @Benchmark
    public void getMethod(Blackhole bh) throws Throwable {
        Method method = Bean.class.getMethod("setId", int.class);

        Bean bean = new Bean();
        method.invoke(bean, 123);
        bh.consume(bean);
    }

    public static class Bean {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(InvokeFirst.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
