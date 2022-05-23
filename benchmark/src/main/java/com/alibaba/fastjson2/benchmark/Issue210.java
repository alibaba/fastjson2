package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Issue210 {
    final static JSONPath jsonPath = JSONPath.of("$.password");
    final static Sha256BiFunction callbackBiFunction = new Sha256BiFunction();
    final static JSONObject object;
    final static Bean bean;

    static  {
        bean = new Bean();
        bean.password = "12345678";
        object = JSONObject.of("password", bean.password);
    }

    @Benchmark
    public void objectSet() {
        jsonPath.setCallback(object, callbackBiFunction);
    }

    @Benchmark
    public void beanSet() {
        jsonPath.setCallback(bean, callbackBiFunction);
    }

    final static class Sha256BiFunction implements BiFunction {

        @Override
        public Object apply(Object o, Object o2) {
            String str = (String) o2;
            return str;
        }
    }

    final static class Sha256 implements Function {
        @Override
        public Object apply(Object val) {
            String str = (String) val;
            return str;
        }
    }

    public static class Bean {
        private String password;

        public Bean() {

        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static void main(String[] args) throws RunnerException {
        new Issue210().beanSet();
//        Options options = new OptionsBuilder()
//                .include(Issue210.class.getName())
//                .mode(Mode.Throughput)
//                .timeUnit(TimeUnit.MILLISECONDS)
//                .forks(1)
//                .build();
//        new Runner(options).run();
    }
}
