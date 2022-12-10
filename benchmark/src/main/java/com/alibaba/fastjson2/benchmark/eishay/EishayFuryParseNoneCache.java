package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.DynamicClassLoader;
import com.alibaba.fastjson2.util.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipInputStream;

public class EishayFuryParseNoneCache {
    static final int COUNT = 10_000;
    static final Class[] classes = new Class[COUNT];

    static JSONReader.Feature[] features = {
            JSONReader.Feature.SupportAutoType,
            JSONReader.Feature.IgnoreNoneSerializable,
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.UseNativeObject,
            JSONReader.Feature.FieldBased,
            JSONReader.Feature.SupportArrayToBean
    };

    static DynamicClassLoader classLoader = DynamicClassLoader.getInstance();

    static byte[][] fastjson2JSONBBytes = new byte[COUNT][];
    static byte[][] furyBytes = new byte[COUNT][];
    static int index;
//
//    static io.fury.ThreadSafeFury fury = io.fury.Fury.builder()
//            .withLanguage(io.fury.Language.JAVA)
//            .withReferenceTracking(true)
//            .disableSecureMode()
//            .withClassLoader(classLoader)
//            .buildThreadSafeFury();

    static {
        String classZipDataFile = "data/EishayFuryParseNoneCache_classes.bin.zip";
        String jsonbZipDataFile = "data/EishayFuryParseNoneCache_data_fastjson.bin.zip";
        String furyZipDataFile = "data/EishayFuryParseNoneCache_data_fury.bin.zip";

        try {
            {
                InputStream fis = EishayFuryParseNoneCache.class.getClassLoader().getResourceAsStream(classZipDataFile);
                ZipInputStream zipIn = new ZipInputStream(fis);
                zipIn.getNextEntry();

                ObjectInputStream is = new ObjectInputStream(zipIn);
                Map<String, byte[]> codeMap = (Map<String, byte[]>) is.readObject();
                Map<String, Class> classMap = new HashMap<>(codeMap.size());
                codeMap.forEach((name, code) -> {
                    Class<?> clazz = classLoader.loadClass(name, code, 0, code.length);
                    classMap.put(name, clazz);
                });

                for (int i = 0; i < COUNT; i++) {
                    String packageName = "com.alibaba.fastjson2.benchmark.eishay" + i;
                    classLoader.definePackage(packageName);
                    String className = packageName + ".MediaContent";
                    Class mediaClass = classMap.get(className);
                    classes[i] = mediaClass;
                }
                IOUtils.close(zipIn);
                IOUtils.close(is);
                IOUtils.close(fis);
            }

            {
                InputStream fis = EishayFuryParseNoneCache.class.getClassLoader().getResourceAsStream(jsonbZipDataFile);
                ZipInputStream zipIn = new ZipInputStream(fis);
                zipIn.getNextEntry();

                ObjectInputStream is = new ObjectInputStream(zipIn);
                fastjson2JSONBBytes = (byte[][]) is.readObject();
                IOUtils.close(zipIn);
                IOUtils.close(is);
                IOUtils.close(fis);
            }
            {
                InputStream fis = EishayFuryParseNoneCache.class.getClassLoader().getResourceAsStream(furyZipDataFile);
                ZipInputStream zipIn = new ZipInputStream(fis);
                zipIn.getNextEntry();

                ObjectInputStream is = new ObjectInputStream(zipIn);
                furyBytes = (byte[][]) is.readObject();
                IOUtils.close(zipIn);
                IOUtils.close(is);
                IOUtils.close(fis);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2JSONB(Blackhole bh) {
        Thread.currentThread().setContextClassLoader(classLoader);
        byte[] bytes = fastjson2JSONBBytes[index++];
        bh.consume(
                JSONB.parseObject(bytes, Object.class, features)
        );
    }

    //    @Benchmark
    public void fury(Blackhole bh) {
        Thread.currentThread().setContextClassLoader(classLoader);
        byte[] bytes = furyBytes[index++];
//        bh.consume(fury.deserialize(bytes));
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(EishayFuryParseNoneCache.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
