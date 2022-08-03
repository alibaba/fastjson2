package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class Issue609 {
    private static final List<Student> objList;
    private static final List<String> strList;
    private static final String source;

    static {
        objList = new ArrayList<>(100000);
        strList = new ArrayList<>(100000);
        for (int i = 0; i < 100000; i++) {
            Student student = new Student("学生姓名" + i, i % 10, "黑龙江省哈尔滨市南方区哈尔滨大街267号" + i);
            objList.add(student);
            strList.add(JSON.toJSONString(student));
        }
        source = JSON.toJSONString(objList);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Issue609.class.getName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void fastJSON1ObjSeThroughput() {
        for (Student student : objList) {
            JSON.toJSONString(student);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void fastJSON1ObjDeThroughput() {
        for (String student : strList) {
            JSON.parseObject(student, Student.class);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void fastJSON2ObjSeThroughput() {
        for (Student student : objList) {
            com.alibaba.fastjson2.JSON.toJSONString(student);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void fastJSON2ObjDeThroughput() {
        for (String student : strList) {
            com.alibaba.fastjson2.JSON.parseObject(student, Student.class);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void fastJSON1ArraySeThroughput() {
        JSON.toJSONString(objList);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void fastJSON1ArrayDeThroughput() {
        JSON.parseArray(source, Student.class);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void fastJSON2ArraySeThroughput() {
        com.alibaba.fastjson2.JSON.toJSONString(objList, JSONWriter.Feature.ReferenceDetection);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void fastJSON2ArrayDeThroughput() {
        com.alibaba.fastjson2.JSON.parseArray(source, Student.class);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void fastJSON1ObjSeTime() {
        for (Student student : objList) {
            JSON.toJSONString(student);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void fastJSON1ObjDeTime() {
        for (String student : strList) {
            JSON.parseObject(student, Student.class);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void fastJSON2ObjSeTime() {
        for (Student student : objList) {
            com.alibaba.fastjson2.JSON.toJSONString(student);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void fastJSON2ObjDeTime() {
        for (String student : strList) {
            com.alibaba.fastjson2.JSON.parseObject(student, Student.class);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void fastJSON1ArraySeTime() {
        JSON.toJSONString(objList);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void fastJSON1ArrayDeTime() {
        JSON.parseArray(source, Student.class);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void fastJSON2ArraySeTime() {
        com.alibaba.fastjson2.JSON.toJSONString(objList, JSONWriter.Feature.ReferenceDetection);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void fastJSON2ArrayDeTime() {
        com.alibaba.fastjson2.JSON.parseArray(source, Student.class);
    }

    private static class Student {
        private String name;
        private int age;
        private String address;

        public Student() {
        }

        public Student(String name, int age, String address) {
            this.name = name;
            this.age = age;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
}
