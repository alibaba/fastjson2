package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSON;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class GenReport {
    static final Map<String, String> specs = new HashMap<>();
    static final Map<String, String> cases = new HashMap<>();
    static {
        specs.put("orangepi5p", "http://www.orangepi.org/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-5-Pro.html");
        specs.put("orangepi_aipro", "http://www.orangepi.cn/html/hardWare/computerAndMicrocontrollers/details/Orange-Pi-AIpro.html");
        specs.put("aliyun_ecs.g8y.large", "https://www.alibabacloud.com/zh/product/ecs/g8y");
        specs.put("aliyun_ecs.c8i.large", "https://help.aliyun.com/zh/ecs/user-guide/compute-optimized-instance-families#c8i");
        specs.put("aws_ecs.c7g.large", "https://aws.amazon.com/ec2/instance-types/c7g/");

        cases.put("EishayFuryCompatibleWrite", "这个场景是JSONB格式和Fury CompatibleMode序列化性能比较。基于KeyValue的映射，对增加和删除字段的序列化结构都能有很好的兼容性。");
        cases.put("EishayFuryCompatibleParse", "这个场景是JSONB格式和Fury CompatibleMode反序列化性能比较。基于KeyValue的映射，对增加和删除字段的序列化结构都能有很好的兼容性。");

        cases.put("EishayWriteBinary", "这个场景是二进制序列化比较，JSONB格式、JSON UTF8编码(fastjson2UTF8Bytes)、hessian、javaSerialize的比较，用于[Apache dubbo](https://github.com/apache/dubbo)的用户选择二进制协议比较");
        cases.put("EishayParseBinary", "这个场景是二进制反序列化比较，JSONB格式、JSON UTF8编码(fastjson2UTF8Bytes)、hessian、javaSerialize的比较，用于[Apache dubbo](https://github.com/apache/dubbo)的用户选择二进制协议比较");

        cases.put("EishayWriteBinaryAutoType", "这个场景是带类型信息二进制序列化比较，JSONB格式、JSON UTF8编码(fastjson2UTF8Bytes)、hessian、javaSerialize的比较，用于[Apache dubbo](https://github.com/apache/dubbo)的用户选择二进制协议比较");
        cases.put("EishayParseBinaryAutoType", "这个场景是带类型信息二进制反序列化比较，JSONB格式、JSON UTF8编码(fastjson2UTF8Bytes)、hessian、javaSerialize的比较，用于[Apache dubbo](https://github.com/apache/dubbo)的用户选择二进制协议比较");

        cases.put("EishayWriteBinaryArrayMapping", "这个场景是二进制序列化比较，JSONB格式（基于字段顺序映射）、kryo、protobuf的比较");
        cases.put("EishayParseBinaryArrayMapping", "这个场景是二进制反序列化比较，JSONB格式（基于字段顺序映射）、kryo、protobuf的比较");

        cases.put("EishayParseString", "这个场景是将没有格式化的JSON字符串反序列化为JavaBean对象，是最常用的场景，这个是fastjson1的强项。");
        cases.put("EishayParseStringPretty", "这个场景是将格式化过的JSON字符串反序列化为JavaBean对象");
        cases.put("EishayParseTreeString", "这个场景是将没有格式化的JSON字符串解析为JSONObject或者HashMap，不涉及绑定JavaBean对象。");
        cases.put("EishayParseTreeStringPretty", "这个场景是将格式化过的字符串解析为JSONObject或者HashMap，不涉及绑定JavaBean对象。");

        cases.put("EishayParseUTF8Bytes", "这个场景是将没有格式化的JSON字符串UTF8编码的byte[]数组反序列化为JavaBean对象，是最常用的场景，这个是fastjson1的强项。");
        cases.put("EishayParseUTF8BytesPretty", "这个场景是将格式化过的JSON字符串UTF8编码的byte[]数组反序列化为JavaBean对象");
        cases.put("EishayParseTreeUTF8Bytes", "这个场景是将没有格式化的JSON字符串UTF8编码的byte[]数组反序列化解析为JSONObject或者HashMap，不涉及绑定JavaBean对象。");
        cases.put("EishayParseTreeUTF8BytesPretty", "这个场景是将格式化过的字符串UTF8编码的byte[]数组反序列化解析为JSONObject或者HashMap，不涉及绑定JavaBean对象。");

        cases.put("EishayWriteString", "这个场景是将JavaBean对象序列化为字符串");
        cases.put("EishayWriteUTF8Bytes", "这个场景是将JavaBean对象序列化为UTF8编码的Bytes");
        cases.put("EishayWriteStringTree", "这个场景是将JSONObject或者Map序列化为字符串");
        cases.put("EishayWriteUTF8BytesTree", "这个场景是将JSONObject或者Map序列化为UTF8编码的Bytes");
    }

    public void gen() throws Exception {
        File dir = new File("/Users/wenshao/Work/git/fastjson2/docs/benchmark/");
        File file = new File(dir, "benchmark_" + JSON.VERSION + "_raw.md");
        File outFile = new File(dir, "benchmark_" + JSON.VERSION + ".md");

        Map<String, BenchmarkResult> benchResults = new LinkedHashMap<>();

        try (DocReader docReader = new DocReader(file)) {
            while (true) {
                Block block = docReader.readBlock();
                if (block == null) {
                    break;
                }
                for (ResultLine resultLine : block.resultLines) {
                    BenchmarkResult benchmarkResult = benchResults.get(resultLine.benchmark);
                    if (benchmarkResult == null) {
                        benchResults.put(resultLine.benchmark, benchmarkResult = new BenchmarkResult(resultLine.benchmark));
                    }
                    benchmarkResult.add(resultLine.lib, block.jdk, resultLine.score);
                }
            }
        }

        gen(outFile, benchResults);
    }

    private static void gen(
            File outFile,
            Map<String, BenchmarkResult> benchResults
    ) throws FileNotFoundException {
        PrintStream out = new PrintStream(new FileOutputStream(outFile));
        int h1 = 0;
        for (BenchmarkResult benchmarkResult : benchResults.values()) {
            benchmarkResult.libraryResults.remove("jsonbValid"); // skip jsonbValid

            if (benchmarkResult.libraryResults.size() == 2) {
                LibResult fury = benchmarkResult.libraryResults.get("fury");
                LibResult jsonb = benchmarkResult.libraryResults.get("jsonb");
                if (fury != null && jsonb != null) {
                    benchmarkResult.libraryResults.clear();
                    benchmarkResult.libraryResults.put("jsonb", jsonb);
                    benchmarkResult.libraryResults.put("fury", fury);
                }
            } else if (benchmarkResult.libraryResults.size() == 4) {
                LibResult fastjson2 = benchmarkResult.libraryResults.get("fastjson2");
                LibResult fastjson1 = benchmarkResult.libraryResults.get("fastjson1");
                LibResult jackson = benchmarkResult.libraryResults.get("jackson");
                LibResult gson = benchmarkResult.libraryResults.get("gson");
                LibResult fastjson2UTF8Bytes = benchmarkResult.libraryResults.get("fastjson2UTF8Bytes");
                LibResult fastjson1UTF8Bytes = benchmarkResult.libraryResults.get("fastjson1UTF8Bytes");
                LibResult hessian = benchmarkResult.libraryResults.get("hessian");
                LibResult javaSerialize = benchmarkResult.libraryResults.get("javaSerialize");
                LibResult jsonb = benchmarkResult.libraryResults.get("jsonb");
                LibResult kryo = benchmarkResult.libraryResults.get("kryo");
                LibResult protobuf = benchmarkResult.libraryResults.get("protobuf");
                if (fastjson1 != null && fastjson2 != null && jackson != null && gson != null) {
                    benchmarkResult.libraryResults.clear();
                    benchmarkResult.libraryResults.put("fastjson2", fastjson2);
                    benchmarkResult.libraryResults.put("fastjson1", fastjson1);
                    benchmarkResult.libraryResults.put("jackson", jackson);
                    benchmarkResult.libraryResults.put("gson", gson);
                } else if (fastjson2UTF8Bytes != null && hessian != null && javaSerialize != null && jsonb != null) {
                    benchmarkResult.libraryResults.clear();
                    benchmarkResult.libraryResults.put("jsonb", jsonb);
                    benchmarkResult.libraryResults.put("fastjson2UTF8Bytes", fastjson2UTF8Bytes);
                    benchmarkResult.libraryResults.put("hessian", hessian);
                    benchmarkResult.libraryResults.put("javaSerialize", javaSerialize);
                } else if (fastjson1UTF8Bytes != null && jsonb != null && kryo != null && protobuf != null) {
                    benchmarkResult.libraryResults.clear();
                    benchmarkResult.libraryResults.put("jsonb", jsonb);
                    benchmarkResult.libraryResults.put("kryo", kryo);
                    benchmarkResult.libraryResults.put("protobuf", protobuf);
//                    benchmarkResult.libraryResults.put("fastjson1UTF8Bytes", fastjson1UTF8Bytes);
                } else if (fastjson2UTF8Bytes != null && jsonb != null && kryo != null && protobuf != null) {
                    benchmarkResult.libraryResults.clear();
                    benchmarkResult.libraryResults.put("jsonb", jsonb);
                    benchmarkResult.libraryResults.put("kryo", kryo);
                    benchmarkResult.libraryResults.put("protobuf", protobuf);
                    benchmarkResult.libraryResults.put("fastjson2UTF8Bytes", fastjson2UTF8Bytes);
                } else {
                    LibResult fastjson2JSONB = benchmarkResult.libraryResults.get("fastjson2JSONB");
                    if (fastjson1UTF8Bytes != null && fastjson2UTF8Bytes != null && fastjson2JSONB != null && kryo != null) {
                        benchmarkResult.libraryResults.clear();
                        benchmarkResult.libraryResults.put("fastjson2JSONB", fastjson2JSONB);
                        benchmarkResult.libraryResults.put("kryo", kryo);
                        benchmarkResult.libraryResults.put("fastjson2UTF8Bytes", fastjson2UTF8Bytes);
                        benchmarkResult.libraryResults.put("fastjson1UTF8Bytes", fastjson1UTF8Bytes);
                    }
                }
            } else if (benchmarkResult.libraryResults.size() == 5) {
                LibResult fastjson2 = benchmarkResult.libraryResults.get("fastjson2");
                LibResult fastjson2_jsonb = benchmarkResult.libraryResults.get("fastjson2_jsonb");
                LibResult fastjson1 = benchmarkResult.libraryResults.get("fastjson1");
                LibResult jackson = benchmarkResult.libraryResults.get("jackson");
                LibResult gson = benchmarkResult.libraryResults.get("gson");
                LibResult dsljson = benchmarkResult.libraryResults.get("dsljson");
                LibResult fastjson1UTF8Bytes = benchmarkResult.libraryResults.get("fastjson1UTF8Bytes");
                LibResult fastjson2UTF8Bytes = benchmarkResult.libraryResults.get("fastjson2UTF8Bytes");
                LibResult kryo = benchmarkResult.libraryResults.get("kryo");
                LibResult jsonb = benchmarkResult.libraryResults.get("jsonb");
                LibResult protobuf = benchmarkResult.libraryResults.get("protobuf");
                if (fastjson1 != null && fastjson2_jsonb != null && fastjson2 != null && jackson != null && gson != null) {
                    benchmarkResult.libraryResults.clear();
                    benchmarkResult.libraryResults.put("fastjson2", fastjson2);
                    benchmarkResult.libraryResults.put("fastjson2_jsonb", fastjson2_jsonb);
                    benchmarkResult.libraryResults.put("fastjson1", fastjson1);
                    benchmarkResult.libraryResults.put("jackson", jackson);
                    benchmarkResult.libraryResults.put("gson", gson);
                } else if (fastjson1 != null && dsljson != null && fastjson2 != null && jackson != null && gson != null) {
                    benchmarkResult.libraryResults.clear();
                    benchmarkResult.libraryResults.put("fastjson2", fastjson2);
                    benchmarkResult.libraryResults.put("dsljson", dsljson);
                    benchmarkResult.libraryResults.put("fastjson1", fastjson1);
                    benchmarkResult.libraryResults.put("jackson", jackson);
                    benchmarkResult.libraryResults.put("gson", gson);
                } else if (fastjson1UTF8Bytes != null && fastjson2UTF8Bytes != null && jsonb != null && kryo != null && protobuf != null) {
                    benchmarkResult.libraryResults.clear();
                    benchmarkResult.libraryResults.put("jsonb", jsonb);
                    benchmarkResult.libraryResults.put("kryo", kryo);
                    benchmarkResult.libraryResults.put("protobuf", protobuf);
                    benchmarkResult.libraryResults.put("fastjson2UTF8Bytes", fastjson2UTF8Bytes);
                    benchmarkResult.libraryResults.put("fastjson1UTF8Bytes", fastjson1UTF8Bytes);
                }
            }

            String code = "https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/eishay/" + benchmarkResult.benchmarkCase + ".java";
            out.println("## " + (++h1) + ". [" + benchmarkResult.benchmarkCase + "](" + code + ")");
            String desc = cases.get(benchmarkResult.benchmarkCase);
            if (desc != null) {
                out.println(desc);
            }

            out.println();

            LibResult firLib = benchmarkResult.libraryResults.values().iterator().next();
            Set<String> jdks = firLib.scores.keySet();
            out.print("| aliyun ecs spec | jdk version ");
            for (LibResult libResult : benchmarkResult.libraryResults.values()) {
                out.print("\t|\t");
                out.print(libResult.library);
            }
            out.print(" |");
            out.println();

            out.print("|-----|");
            for (LibResult libResult : benchmarkResult.libraryResults.values()) {
                out.print("-----|-----");
            }
            out.print("|");
            out.println();

            Set<String> eccWrited = new HashSet<>();
            for (String jdk : jdks) {
                double firstScore = benchmarkResult.libraryResults.values().iterator().next().scores.get(jdk);

                out.print("| ");

                int p = jdk.indexOf('-');
                if (p == -1) {
                    out.print(jdk);
                    out.print(" | ");
                } else {
                    String ecs = jdk.substring(0, p);

                    if (eccWrited.add(ecs)) {
                        String link = specs.get(ecs);
                        if (link == null) {
                            out.print(ecs);
                        } else {
                            out.print('[');
                            out.print(ecs);
                            out.print("](");
                            out.print(link);
                            out.print(')');
                        }
                    }

                    out.print(" | ");
                    String jdkinfo = jdk.substring(p + 1);
                    if (jdkinfo.startsWith("graalvm-jdk-")) {
                        jdkinfo = "graalvm_" + jdkinfo.substring("graalvm-jdk-".length());
                    } else if (jdkinfo.startsWith("zulu21.0.57")) {
                        jdkinfo = "zulu21.0.57";
                    } else if (jdkinfo.startsWith("zulu21.32.17")) {
                        jdkinfo = "zulu21.32.17";
                    }
                    out.print(jdkinfo);
                }

                int i = 0;
                for (LibResult libResult : benchmarkResult.libraryResults.values()) {
                    Double score = libResult.scores.get(jdk);
                    out.print("\t|\t");
                    out.print(score);
                    if (i != 0 && score != null) {
                        double percent = score / firstScore;
                        out.print(" (" + new DecimalFormat("#,##0.##%").format(percent) + ")");
                    }
                    ++i;
                }
                out.print(" |");
                out.println();
            }

            out.println();
        }
        out.close();
    }

    static class LibResult {
        final String library;
        final Map<String, Double> scores = new LinkedHashMap<>();

        public LibResult(String library) {
            this.library = library;
        }
    }

    static class BenchmarkResult {
        final String benchmarkCase;
        final Map<String, LibResult> libraryResults = new LinkedHashMap<>();

        public BenchmarkResult(String benchmarkCase) {
            this.benchmarkCase = benchmarkCase;
        }

        public void add(String library, String jdk, double score) {
            LibResult libResult = libraryResults.get(library);
            if (libResult == null) {
                libraryResults.put(library, libResult = new LibResult(library));
            }
            libResult.scores.put(jdk, score);
        }
    }

    static class DocReader
            implements Closeable {
        BufferedReader reader;
        int lines;

        public DocReader(File file) throws IOException {
            this.reader = new BufferedReader(new FileReader(file));
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }

        public Block readBlock() throws Exception {
            String line = reader.readLine();
            lines++;
            while (line != null && line.trim().isEmpty()) {
                line = reader.readLine();
                lines++;
            }

            if (line == null || !line.startsWith("#")) {
                return null;
            }

            Block block = new Block();
            block.jdk = line.substring(1).trim();

            line = reader.readLine();
            lines++;
            if (!line.startsWith("```")) {
                return null;
            }

            do {
                line = reader.readLine();
                lines++;
            } while (line.isEmpty());

            if (line.startsWith("```")) {
                return block;
            }

            if (!line.startsWith("Benchmark")) {
                return null;
            }

            while (true) {
                line = reader.readLine();
                lines++;
                if (line.startsWith("```")) {
                    break;
                }
                line = line.trim();

//                out.println(line);
                String[] items = line.split("(\\s)+");
                String[] item0Parts = items[0].split("\\.");

                String testName = item0Parts[0];
                String lib = item0Parts[1];
                double score = Double.parseDouble(items[3]);

                block.resultLines.add(new ResultLine(testName, lib, score));
            }

            return block;
        }
    }

    static class Block {
        String jdk;
        List<ResultLine> resultLines = new ArrayList<>();
    }

    static class ResultLine {
        String benchmark;
        String lib;
        double score;

        public ResultLine(String benchmark, String lib, double score) {
            this.benchmark = benchmark;
            this.lib = lib;
            this.score = score;
        }
    }

    public static void main(String[] args) throws Exception {
        GenReport gen = new GenReport();
        gen.gen();
    }
}
