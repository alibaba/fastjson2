package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.benchmark.eishay.gen.EishayClassGen;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.DynamicClassLoader;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayForyParseNoneCacheTest {
    static final EishayForyParseNoneCache benchmark = new EishayForyParseNoneCache();
    static final int COUNT = 1000;

    static JSONWriter.Feature[] writerFeatures = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.IgnoreNoneSerializable,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.BeanToArray
    };

    static JSONReader.Feature[] features = {
            JSONReader.Feature.SupportAutoType,
            JSONReader.Feature.IgnoreNoneSerializable,
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.UseNativeObject,
            JSONReader.Feature.FieldBased,
            JSONReader.Feature.SupportArrayToBean
    };

    public static void genJSONBDataFiles() throws Exception {
        EishayClassGen gen = new EishayClassGen();
        byte[][] bytes = gen.genFastjsonJSONBBytes(50_000, writerFeatures);
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream("EishayForyParseNoneCache_data_fastjson.bin"))
        ) {
            out.writeObject(bytes);
        }
    }

    public static void genForyDataFiles() throws Exception {
        EishayClassGen gen = new EishayClassGen();
        byte[][] bytes = gen.genForyBytes(20_000);
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream("EishayForyParseNoneCache_data_fory.bin"))
        ) {
            out.writeObject(bytes);
        }
    }

    public static void genCodes() throws Exception {
        EishayClassGen gen = new EishayClassGen();
        LinkedHashMap<String, byte[]> codeMap = gen.genCodes(50_000);
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream("EishayForyParseNoneCache_classes.bin"))
        ) {
            out.writeObject(codeMap);
        }
    }

    public static void testSingle() throws Exception {
        byte[] jsonbBytes;
        // com.alibaba.fastjson2.benchmark.eishay.vo
        String packageName = "com/alibaba/fastjson2/benchmark/eishay0";

        try (InputStream is = EishayForyWriteNoneCache.class.getClassLoader()
                .getResourceAsStream("data/eishay.json")
        ) {
            String str = IOUtils.toString(is, "UTF-8");
            DynamicClassLoader classLoader = new DynamicClassLoader();
            EishayClassGen gen = new EishayClassGen();
            Class objectClass = gen.genMedia(classLoader, packageName);
            ObjectReaderProvider provider = new ObjectReaderProvider();
            JSONReader.Context context = JSONFactory.createReadContext(provider);
            Object object = JSONReader.of(str, context).read(objectClass);
            jsonbBytes = JSONB.toBytes(object, writerFeatures);
        }

        JSONB.dump(jsonbBytes);
//
        DynamicClassLoader classLoader = DynamicClassLoader.getInstance();
        EishayClassGen gen = new EishayClassGen();
        Class clazz = gen.genMedia(classLoader, packageName);
        Thread.currentThread().setContextClassLoader(classLoader);

        Object object = JSONB.parseObject(jsonbBytes, Object.class, features);
    }

    public static void fastjson2JSONB() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fastjson2JSONB(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayForyParseNoneCache-fastjson2_jsonb millis : " + millis);
            // zulu8.62.0.19 : 1095
            // zulu11.52.13 : 916
            // zulu17.38.21 : 845
        }
    }

    public static void fory() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < COUNT; ++i) {
                benchmark.fory(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayForyParseNoneCache-fastjson2_jsonb millis : " + millis);
            // zulu8.62.0.19 : 16705
            // zulu11.52.13 : 12418
            // zulu17.38.21 : 10889
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2JSONB();
//        fory();
//        genJSONBDataFiles();
//        genForyDataFiles();
//        genCodes();
//        testSingle();
    }
}
