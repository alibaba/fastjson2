package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.RunnerException;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Issue210 {
    static final JSONPath jsonPath = JSONPath.of("$.password");
    static final Sha256BiFunction callbackBiFunction = new Sha256BiFunction();
    static final JSONObject object;
    static final Bean bean;

    static {
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

    static final class Sha256BiFunction
            implements BiFunction {
        @Override
        public Object apply(Object o, Object o2) {
            String str = (String) o2;
            return str;
        }
    }

    static final class Sha256
            implements Function {
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
