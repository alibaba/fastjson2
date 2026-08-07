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

public class Invoke {
    static final Method METHOD_SET_ID;
    static final ObjIntConsumer FUNC_SET_ID;

    static final Object OBJECT;
    static int value = 12345;

    static {
        Method method = null;
        try {
            method = Bean.class.getMethod("setId", int.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        METHOD_SET_ID = method;

        ObjIntConsumer function = null;
        try {
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
            function = (ObjIntConsumer) callSite.getTarget().invoke();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        FUNC_SET_ID = function;

        Bean bean = new Bean();
        bean.id = 1234;
        bean.name = "DataWorks";

        OBJECT = bean;
    }

    @Benchmark
    public void reflect(Blackhole bh) throws Exception {
        Object obj = OBJECT;
        METHOD_SET_ID.invoke(obj, value);
        bh.consume(obj);
    }

    @Benchmark
    public void direct(Blackhole bh) throws Exception {
        Bean obj = (Bean) OBJECT;
        obj.setId(value);
        bh.consume(obj);
    }

    @Benchmark
    public void lambda(Blackhole bh) throws Exception {
        Object obj = OBJECT;
        FUNC_SET_ID.accept(obj, value);
        bh.consume(obj);
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
                .include(Invoke.class.getName())
                .include(InvokeFirst.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
