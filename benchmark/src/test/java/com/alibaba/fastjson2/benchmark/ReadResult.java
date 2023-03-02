package com.alibaba.fastjson2.benchmark;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReadResult {
    public static void main(String[] args) throws Exception {
        File file = new File("/Users/wenshao/Downloads/result_2.0.25.out");
        DocReader reader = new DocReader("OrangePi5", file);
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
                    if (line.startsWith("#") || line.startsWith("Error: ")) {
                        blockLines.add("```");
                        block = false;
                    }
                }

                if (line.startsWith("Benchmark ")) {
                    block = true;
                    String ends;
                    ends = "/jre/bin/java";
                    if (!vmInvoker.endsWith(ends)) {
                        ends = "/bin/java";
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
