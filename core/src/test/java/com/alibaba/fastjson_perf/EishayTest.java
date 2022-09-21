package com.alibaba.fastjson_perf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.eishay.vo.*;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.util.JSONBDump;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.caucho.hessian.io.Hessian2Output;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EishayTest {
    private String str;
    private MediaContent mc;
    private ObjectMapper mapper = new ObjectMapper();

    private byte[] jsonbBytes;

    public EishayTest() throws Exception {
        InputStream is = Int2Test.class.getClassLoader().getResourceAsStream("data/eishay.json");
        str = IOUtils.toString(is, "UTF-8");
        mc = JSONReader.of(str)
                .read(MediaContent.class);

        System.out.println("java.vm.version : " + System.getProperty("java.vm.version"));

//        jsonbBytes = JSONB.toBytes(mc,
//                JSONWriter.Feature.ReferenceDetection,
//                JSONWriter.Feature.WriteClassName,
//                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
//                JSONWriter.Feature.WriteNulls,
//                JSONWriter.Feature.NotWriteDefaultValue,
//                JSONWriter.Feature.FieldBased,
//                JSONWriter.Feature.IgnoreErrorGetter);
//
    }

    @Test
    public void test_write_0_asm() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONWriter jw = JSONWriter.of();
                jw.writeAny(mc);
                jw.toString();
                jw.close();
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_0-asm millis : " + millis);
            // JDK 1.8_311 : 572 585
            // JDK 11.0.31 : 666
            // JDK 17      :
        }
        System.out.println();
    }

    @Test
    public void test_write_0_default() {
        mc = JSONReader.of(str)
                .read(MediaContent.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                String str = com.alibaba.fastjson2.JSON.toJSONString(mc);
                str.length();
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_0-asm millis : " + millis);
            // JDK 1.8_311 : 572
            // JDK 11.0.31 : 666
            // JDK 17      :
        }
        System.out.println();
    }

    @Test
    public void test_write_utf8_default() {
        mc = JSONReader.of(str)
                .read(MediaContent.class);

        int len;
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                byte[] str = com.alibaba.fastjson2.JSON.toJSONBytes(mc);
                len = str.length;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_0-asm millis : " + millis);
            // JDK 1.8_311 : 572
            // JDK 11.0.31 : 666
            // JDK 17      :
            // Zulu8       : 399
        }
        System.out.println();
    }

    @Test
    public void test_read_utf8_default() {
        mc = JSONReader.of(str)
                .read(MediaContent.class);
        byte[] utf8Bytes = com.alibaba.fastjson2.JSON.toJSONBytes(mc);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                com.alibaba.fastjson2.JSON.parseObject(utf8Bytes, MediaContent.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_0-asm millis : " + millis);
            // JDK 1.8_311 :
            // JDK 11.0.31 :
            // JDK 17      :
        }
        System.out.println();
    }

    @Test
    public void test_write_jsonb_default() {
        mc = JSONReader.of(str)
                .read(MediaContent.class);

        int len;
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                byte[] str = JSONB.toBytes(mc);
                len = str.length;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_0-asm millis : " + millis);
            // JDK 1.8_311 : 572 420 404
            // JDK 11.0.31 : 666
            // JDK 17      :
            // zulu8       : 309 341 330 310 280 278
            // zulu11      : 179
        }
        System.out.println();
    }

    @Test
    public void test_write_jsonb_default2() {
        mc = JSONReader.of(str)
                .read(MediaContent.class);
        {
            jsonbBytes = JSONB.toBytes(
                    mc,
                    JSONWriter.Feature.ReferenceDetection,
                    JSONWriter.Feature.WriteClassName,
                    JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                    JSONWriter.Feature.WriteNulls,
                    JSONWriter.Feature.NotWriteDefaultValue,
                    JSONWriter.Feature.FieldBased,
                    JSONWriter.Feature.IgnoreErrorGetter);

            JSONBDump.dump(jsonbBytes);
        }

        int len;
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                byte[] str = JSONB.toBytes(
                        mc,
                        JSONWriter.Feature.ReferenceDetection,
                        JSONWriter.Feature.WriteClassName,
                        JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                        JSONWriter.Feature.WriteNulls,
                        JSONWriter.Feature.NotWriteDefaultValue,
                        JSONWriter.Feature.FieldBased,
                        JSONWriter.Feature.IgnoreErrorGetter);
                len = str.length;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_jsonb millis : " + millis);
            // JDK 1.8_311 :
            // JDK 11.0.31 :
            // JDK 17      :
            // zulu8       : 455 518 458 448 437 431
            // zulu11      : 378
        }
        System.out.println();
    }

    @Test
    public void test_write_jsonb_default2_symbols() {
        mc = JSONReader.of(str)
                .read(MediaContent.class);

        SymbolTable symbolTable = JSONB.symbolTable("com.alibaba.fastjson2.eishay.vo.MediaContent", "images", "media", "height", "size", "title", "uri", "width", "bitrate", "duration", "format", "persons", "player");

        {
            jsonbBytes = JSONB.toBytes(
                    mc,
                    symbolTable,
                    JSONWriter.Feature.ReferenceDetection,
                    JSONWriter.Feature.WriteClassName,
                    JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                    JSONWriter.Feature.WriteNulls,
                    JSONWriter.Feature.NotWriteDefaultValue,
                    JSONWriter.Feature.FieldBased,
                    JSONWriter.Feature.IgnoreErrorGetter);

            JSONBDump.dump(jsonbBytes, symbolTable);

            Object result
                    = JSONB.parseObject(
                    jsonbBytes,
                    Object.class,
                    symbolTable,
                    JSONReader.Feature.SupportAutoType,
                    JSONReader.Feature.UseNativeObject,
                    JSONReader.Feature.FieldBased
            );
        }

        int len;
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                byte[] str = JSONB.toBytes(
                        mc,
                        symbolTable,
                        JSONWriter.Feature.ReferenceDetection,
                        JSONWriter.Feature.WriteClassName,
                        JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                        JSONWriter.Feature.WriteNulls,
                        JSONWriter.Feature.NotWriteDefaultValue,
                        JSONWriter.Feature.FieldBased,
                        JSONWriter.Feature.IgnoreErrorGetter);
                len = str.length;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_jsonb millis : " + millis);
            // JDK 1.8_311 :
            // JDK 11.0.31 :
            // JDK 17      :
            // zulu8       : 385 293 297 290 284 283 / 306 455 518 458 448 400
            // zulu11      :
        }
        System.out.println();
    }

    @Test
    public void test_write_jsonb_h2() throws Exception {
        mc = JSONReader.of(str)
                .read(MediaContent.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
                hessian2Output.writeObject(mc);

                hessian2Output.flush();

                byteArrayOutputStream.toByteArray();
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_h2 millis : " + millis);
            // JDK 1.8_311 :
            // JDK 11.0.31 :
            // JDK 17      :
            // zulu8       :
            // zulu11      :
        }
        System.out.println();
    }

    @Test
    public void test_write_jsonb_default_ref_detec() {
        mc = JSONReader.of(str)
                .read(MediaContent.class);

        int len;
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                byte[] str = JSONB.toBytes(mc, JSONWriter.Feature.ReferenceDetection);
                len = str.length;
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_0-asm millis : " + millis);
            // JDK 1.8_311 : 572 420 404
            // JDK 11.0.31 : 666
            // JDK 17      :
        }
        System.out.println();
    }

    @Test
    public void test_write_jsonb_array_mapping_default() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONB.toBytes(mc, JSONWriter.Feature.BeanToArray, JSONWriter.Feature.FieldBased);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_0-asm millis : " + millis);
            // JDK 1.8_311 : 318
            // JDK 11.0.31 :
            // JDK 17      :
            // zulu 8.58.0 : 233
        }
        System.out.println();
    }

    @Test
    public void test_write_jsonb_array_mapping_default2() {
        ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(MediaContent.class, MediaContent.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
//                JSONB.toBytes(mc, JSONWriter.Feature.BeanToArray);
                try (JSONWriter writer = JSONWriter.ofJSONB()) {
                    writer.config(JSONWriter.Feature.BeanToArray);
                    objectWriter.writeArrayMappingJSONB(writer, mc, null, null, 0);
                    writer.getBytes();
                }
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_0-asm millis : " + millis);
            // JDK 1.8_311 : 315
            // JDK 11.0.31 :
            // JDK 17      :
        }
        System.out.println();
    }

    @Test
    public void test_read_jsonb_array_mapping_default() {
        mc = JSONReader.of(str)
                .read(MediaContent.class);

        byte[] jsonbBytes = JSONB.toBytes(mc, JSONWriter.Feature.BeanToArray);
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONB.parseObject(jsonbBytes, MediaContent.class, JSONReader.Feature.SupportArrayToBean);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_0-asm millis : " + millis);
            // JDK 1.8_311 : 572 313
            // JDK 11.0.31 : 666
            // JDK 17      :
        }
        System.out.println();
    }

    @Test
    public void test_write_0_utf8_asm() {
        mc = JSONReader.of(str)
                .read(MediaContent.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONWriter jw = JSONWriter.ofUTF8();
                jw.writeAny(mc);
                jw.getBytes();
                jw.close();
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_0_utf8-asm millis : " + millis);
            // JDK 1.8_311              : 541
            // JDK 11.0.31              : 590 593
            // 11.0.3-AJDK+0-Alibaba    :
            // JDK 17                   :
        }
        System.out.println();
    }

    @Test
    public void test_write_0_jsonb_asm() {
        mc = JSONReader.of(str)
                .read(MediaContent.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONWriter jw = JSONWriter.ofJSONB();
                jw.writeAny(mc);
                jw.getBytes();
                jw.close();
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_0_jsonb-asm millis : " + millis);
            // JDK 1.8_311              : 549 523 520 516 500 463 444
            // JDK 11.0.31              : 246
            // 11.0.3-AJDK+0-Alibaba    :
            // JDK 17                   : 425 400
        }
        System.out.println();
    }

    @Test
    public void test_write_0_jsonb_arrayMapping() {
        mc = JSONReader.of(str)
                .read(MediaContent.class);

        for (int i = 0; i < 5; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                JSONWriter jw = JSONWriter.ofJSONB();
                jw.config(JSONWriter.Feature.BeanToArray);
                jw.writeAny(mc);
                jw.getBytes();
                jw.close();
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_0_jsonb_arrayMapping millis : " + millis);
            // JDK 1.8_311              : 3526 3169
            // JDK 11.0.31              : 3419 1761
            // 11.0.3-AJDK+0-Alibaba    :
            // JDK 17                   : 2527
        }
        System.out.println();
    }

    @Test
    public void test_write_0_f12() {
        mc = JSONReader.of(str)
                .read(MediaContent.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSON.toJSONString(mc);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_0-f12 millis : " + millis); // 1664
        }
        System.out.println();
    }

    @Test
    public void test_write_0_jackson() throws Exception {
        mc = JSONReader.of(str)
                .read(MediaContent.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                mapper.writeValueAsString(mc);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_0-jackson millis : " + millis);
            // 1087
            // zlulu8   : 588
        }
        System.out.println();
    }

    @Test
    public void test_write_0_utf8_jackson() throws Exception {
        mc = JSONReader.of(str)
                .read(MediaContent.class);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                mapper.writeValueAsBytes(mc);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("write_0_utf8-jackson millis : " + millis); // 1198
        }
        System.out.println();
    }

    @Test
    public void test_read_0_asm() throws Exception {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader jr = JSONReader.of(str);
                jr.read(MediaContent.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("read_0-asm millis : " + millis); // 1187 956 916 907
            // JDK_11.13 :
            // JDK_17 : 981
        }
        System.out.println();
    }

    @Test
    public void test_read_0_asm_utf8() throws Exception {
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader jr = JSONReader.of(utf8);
                jr.read(MediaContent.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("read_0-asm millis : " + millis);
            // JDK8_311 : 1258 1067
            // JDK_11.13 : 1298 1055
            // JDK_17 : 1017
        }
        System.out.println();
    }

    @Test
    public void test_read_0_jsonb() throws Exception {
        jsonbBytes = JSONB.toBytes(mc,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.IgnoreErrorGetter);
        JSONBDump.dump(jsonbBytes);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                Object result
                        = JSONB.parseObject(
                        jsonbBytes,
                        Object.class,
                        JSONReader.Feature.SupportAutoType,
                        JSONReader.Feature.UseNativeObject,
                        JSONReader.Feature.FieldBased
                );
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("read_0_jsonb-asm millis : " + millis);
            // JDK8_311  : 449
            // JDK_11.13 :
            // JDK_17    : 467 464
            // Zulu 8    : 463 397 395 390 358 424 410 400
        }
        System.out.println();
    }

    @Test
    public void test_read_0_asm_jsonb_symbol_table() throws Exception {
        mc = JSONReader.of(str)
                .read(MediaContent.class);

        SymbolTable symbolTable = JSONB.symbolTable(
                "images",
                "height",
                "size",
                "title",
                "uri",
                "width",
                "media",
                "bitrate",
                "duration",
                "format",
                "persons",
                "player",
                "LARGE",
                "SMALL",
                "JAVA",
                "FLASH",
                "copyright"
        );

        JSONWriter writer = JSONWriter.ofJSONB(symbolTable);
        writer.writeAny(mc);
        byte[] jsonbBytes = writer.getBytes();

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                JSONReader jr = JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length, symbolTable);
                jr.read(MediaContent.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("read_0_jsonb_symbol-asm millis : " + millis); // 432
            // JDK_11.13 :
            // JDK_17 :
        }
        System.out.println();
    }

    @Test
    public void test_read_0_jackson() throws Exception {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                mapper.readValue(str, MediaContent.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("read_0-jackson millis : " + millis); // 2247
            // JDK_11.13 : 2499
        }
        System.out.println();
    }

    @Test
    public void test_read_gen_asm() throws Exception {
        ObjectReaderCreator creator = TestUtils.READER_CREATOR;

        for (int i = 0; i < 5; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 10; ++j) {
                creator.createObjectReader(MediaContent.class);
//                creator.createObjectReader(Media.class);
//                creator.createObjectReader(Image.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("read_gen-asm millis : " + millis);
            // JDK_8     : 2452 1837 1744 1670 1791 1723 1665 1655 2907 2879
            // JDK_11.13 :
        }
        System.out.println();
    }

    @Test
    public void test_read_gen_lambda() throws Exception {
        for (int i = 0; i < 5; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 10; ++j) {
                TestUtils.createObjectReaderLambda(MediaContent.class);
//                creator.createObjectReader(Media.class);
//                creator.createObjectReader(Image.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("read_gen-lambda millis : " + millis);
            // JDK_8     : 2005
            // JDK_11.13 :
        }
        System.out.println();
    }

    @Test
    public void test_read_gen_reflect() throws Exception {
        ObjectReaderCreator creator = ObjectReaderCreator.INSTANCE;

        for (int i = 0; i < 5; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000; ++j) {
                creator.createObjectReader(MediaContent.class);
//                creator.createObjectReader(Media.class);
//                creator.createObjectReader(Image.class);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("read_gen-reflect millis : " + millis);
            // JDK_8     : 4297
            // JDK_11.13 :
        }
        System.out.println();
    }
}
