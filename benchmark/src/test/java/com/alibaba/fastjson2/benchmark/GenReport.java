package com.alibaba.fastjson2.benchmark;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class GenReport {
    public void gen() throws Exception {
        File dir = new File("/Users/wenshao/Work/git/fastjson2/docs/benchmark/");
        File file = new File(dir, "benchmark_2.0.34_raw.md");
        File outFile = new File(dir, "benchmark_2.0.34.md");

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
            if (benchmarkResult.libraryResults.size() == 4) {
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

            out.println("## " + (++h1) + " " + benchmarkResult.benchmarkCase);

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
                        out.print(ecs);
                    }

                    out.print(" | ");
                    String jdkinfo = jdk.substring(p + 1);
                    if (jdkinfo.startsWith("graalvm-jdk-")) {
                        jdkinfo = "graalvm_" + jdkinfo.substring("graalvm-jdk-".length());
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

            LinkedHashMap<String, String[]> graalvm17_jdks = new LinkedHashMap<>();
            String jdk17 = null, graalvm17 = null;
            String jdk17_info = null, graalvm17_info = null;
            for (String jdk : jdks) {
                int p = jdk.indexOf('-');
                if (p == -1) {
                    continue;
                }

                String ecs = jdk.substring(0, p);
                String[] ecs_jdk17s = graalvm17_jdks.get(ecs);
                if (ecs_jdk17s == null) {
                    ecs_jdk17s = new String[2];
                    graalvm17_jdks.put(ecs, ecs_jdk17s);
                }

                String jdkinfo = jdk.substring(p + 1);
                if (jdkinfo.startsWith("jdk-17.")) {
                    jdk17 = jdk;
                    jdk17_info = jdkinfo;
                    ecs_jdk17s[0] = jdk;
                } else if (jdkinfo.startsWith("graalvm-jdk-17.")) {
                    graalvm17 = jdk;
                    graalvm17_info = jdkinfo;
                    ecs_jdk17s[1] = jdk;
                }
            }

            if (jdk17 != null && graalvm17 != null) {
                out.println();
                out.println("### " + h1 + ".1 jdk17 vs graalvm17");
                out.println("|  ecs | library | " + jdk17_info + " | " + graalvm17_info + " | delta |");
                out.println("|-----|-----|-----|-----|-----|");
                for (LibResult libResult : benchmarkResult.libraryResults.values()) {
                    for (Map.Entry<String, String[]> entry : graalvm17_jdks.entrySet()) {
                        Double score_jdk17 = libResult.scores.get(entry.getValue()[0]);
                        Double score_graalvm17 = libResult.scores.get(entry.getValue()[1]);
                        double percent = (score_graalvm17 - score_jdk17) / score_jdk17;
                        out.println("|  " + entry.getKey() + " |  " + libResult.library + " | " + score_jdk17 + " | " + score_graalvm17 + " | " + new DecimalFormat("#,##0.##%").format(percent) + " |");
                    }
                }
            }
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

        public DocReader(File file) throws IOException {
            this.reader = new BufferedReader(new FileReader(file));
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }

        public Block readBlock() throws Exception {
            String line = reader.readLine();
            while (line != null && line.trim().isEmpty()) {
                line = reader.readLine();
            }

            if (line == null || !line.startsWith("#")) {
                return null;
            }

            Block block = new Block();
            block.jdk = line.substring(1).trim();

            line = reader.readLine();
            if (!line.startsWith("```")) {
                return null;
            }

            do {
                line = reader.readLine();
            } while (line.isEmpty());

            if (line.startsWith("```")) {
                return block;
            }

            if (!line.startsWith("Benchmark")) {
                return null;
            }

            while (true) {
                line = reader.readLine();
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
