package com.alibaba.fastjson2.benchmark;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class GenReport {
    @Test
    public void gen() throws Exception {
        File file = new File("/Users/wenshao/Work/git/fastjson2/docs/benchmark/benchmark_2.0.21_raw.md");

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

        for (BenchmarkResult benchmarkResult : benchResults.values()) {
            if (benchmarkResult.libraryResults.size() == 4) {
                LibResult fastjson2 = benchmarkResult.libraryResults.get("fastjson2");
                LibResult fastjson1 = benchmarkResult.libraryResults.get("fastjson1");
                LibResult jackson = benchmarkResult.libraryResults.get("jackson");
                LibResult gson = benchmarkResult.libraryResults.get("gson");
                if (fastjson1 != null && fastjson2 != null && jackson != null && gson != null) {
                    benchmarkResult.libraryResults.clear();
                    benchmarkResult.libraryResults.put("fastjson2", fastjson2);
                    benchmarkResult.libraryResults.put("fastjson1", fastjson1);
                    benchmarkResult.libraryResults.put("jackson", jackson);
                    benchmarkResult.libraryResults.put("gson", gson);
                } else {
                    LibResult fastjson2JSONB = benchmarkResult.libraryResults.get("fastjson2JSONB");
                    LibResult fastjson1UTF8Bytes = benchmarkResult.libraryResults.get("fastjson1UTF8Bytes");
                    LibResult fastjson2UTF8Bytes = benchmarkResult.libraryResults.get("fastjson2UTF8Bytes");
                    LibResult kryo = benchmarkResult.libraryResults.get("kryo");
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
                }
            }

            System.out.println("## " + benchmarkResult.benchmarkCase);

            LibResult firLib = benchmarkResult.libraryResults.values().iterator().next();
            Set<String> jdks = firLib.scores.keySet();
            System.out.print("| aliyun ecs spec | jdk version ");
            for (LibResult libResult : benchmarkResult.libraryResults.values()) {
                System.out.print("\t|\t");
                System.out.print(libResult.library);
            }
            System.out.print(" |");
            System.out.println();

            System.out.print("|-----|");
            for (LibResult libResult : benchmarkResult.libraryResults.values()) {
                System.out.print("-----|-----");
            }
            System.out.print("|");
            System.out.println();

            for (String jdk : jdks) {
                double firstScore = benchmarkResult.libraryResults.values().iterator().next().scores.get(jdk);

                System.out.print("| ");

                int p = jdk.indexOf('-');
                if (p == -1) {
                    System.out.print(jdk);
                    System.out.print(" | ");
                } else {
                    String ecs = jdk.substring(0, p);
                    System.out.print(ecs);
                    System.out.print(" | ");
                    System.out.print(jdk.substring(p + 1));
                }

                int i = 0;
                for (LibResult libResult : benchmarkResult.libraryResults.values()) {
                    Double score = libResult.scores.get(jdk);
                    System.out.print("\t|\t");
                    System.out.print(score);
                    if (i != 0) {
                        double percent = score / firstScore;
                        System.out.print(" (" + new DecimalFormat("#,##0.##%").format(percent) + ")");
                    }
                    ++i;
                }
                System.out.print(" |");
                System.out.println();
            }

            System.out.println();
        }
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

//                System.out.println(line);
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
}
