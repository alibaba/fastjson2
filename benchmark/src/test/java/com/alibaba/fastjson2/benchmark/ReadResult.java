package com.alibaba.fastjson2.benchmark;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReadResult {
    public static void main(String[] args) throws Exception {
//        File file = new File("/Users/wenshao/Downloads/result_2.0.25.out");
        File file = new File("/Users/wenshao/Downloads/result_2.0.26_applem1pro.out");
//        DocReader reader = new DocReader("ecs.c7.xlarge", file);
//        DocReader reader = new DocReader("ecs.g8m.xlarge", file);
//        DocReader reader = new DocReader("OrangePi5", file);
        DocReader reader = new DocReader("AppleM1Pro", file);
        reader.read();
        for (String line : reader.blockLines) {
            System.out.println(line);
        }
    }

    static class DocReader
            implements Closeable {
        final String spec;
        BufferedReader reader;
        String vmVersion;
        String vmInvoker;
        boolean block;
        List<String> blockLines = new ArrayList<>();

        public DocReader(String spec, File file) throws IOException {
            this.spec = spec;
            this.reader = new BufferedReader(new FileReader(file));
        }

        public void read() throws IOException {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                if (line.startsWith("# VM version: ")) {
                    vmVersion = line;
                } else if (line.startsWith("# VM invoker: ")) {
                    vmInvoker = line;
                }

                if (block) {
                    if (line.startsWith("#") || line.startsWith("Error: ") || line.startsWith("./run")) {
                        blockLines.add("```");
                        block = false;
                    }
                }

                if (line.startsWith("Benchmark ")) {
                    block = true;
                    String ends = null;
                    String[] strings = new String[] {
                            "/Contents/Home/jre/bin/java",
                            "/Contents/Home/bin/java",
                            "/jre/bin/java",
                            "/bin/java"
                    };

                    for (String string : strings) {
                        if (vmInvoker.endsWith(string)) {
                            ends = string;
                            break;
                        }
                    }
                    if (ends == null) {
                        throw new IOException("not support VMInvoker");
                    }

                    int index = vmInvoker.lastIndexOf('/', vmInvoker.length() - ends.length() - 1);
                    String title = "# " + spec + "-" + vmInvoker.substring(index + 1, vmInvoker.length() - ends.length());
                    blockLines.add(title);
                    blockLines.add("```java");
                }

                if (block) {
                    blockLines.add(line);
                }
                // Benchmark                                                 Mode  Cnt     Score     Error   Units
            }

            if (block) {
                blockLines.add("```");
                block = false;
            }
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }
    }
}
