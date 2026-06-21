package com.alibaba.fastjson2.internal.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue7660Test {
    @TempDir
    public Path tempDir;

    @Test
    public void jsonCompiledHonorsSerializeFalse() throws Exception {
        Path classes = tempDir.resolve("classes");
        Files.createDirectories(classes);

        Path source = tempDir.resolve("Issue7660Bean.java");
        Files.write(
                source,
                Arrays.asList(
                        "import com.alibaba.fastjson2.JSON;",
                        "import com.alibaba.fastjson2.annotation.JSONCompiled;",
                        "import com.alibaba.fastjson2.annotation.JSONField;",
                        "",
                        "public class Issue7660Bean {",
                        "    @JSONCompiled",
                        "    public static class Person {",
                        "        @JSONField(serialize = false)",
                        "        public String internalCode;",
                        "        public String name;",
                        "",
                        "        public Person() {",
                        "        }",
                        "",
                        "        public Person(String internalCode, String name) {",
                        "            this.internalCode = internalCode;",
                        "            this.name = name;",
                        "        }",
                        "    }",
                        "",
                        "    public static void main(String[] args) {",
                        "        System.out.println(JSON.toJSONString(new Person(\"secret\", \"Ada\")));",
                        "    }",
                        "}"
                ),
                StandardCharsets.UTF_8
        );

        String javaClassPath = System.getProperty("java.class.path");
        ProcessResult javac = run(command(
                javac(),
                "-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
                "-J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
                "-J--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
                "-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
                "-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
                "-J--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
                "-cp",
                javaClassPath,
                "-processorpath",
                javaClassPath,
                "-processor",
                JSONCompiledAnnotationProcessor.class.getName(),
                "-source",
                "1.8",
                "-target",
                "1.8",
                "-d",
                classes.toString(),
                source.toString()
        ));
        assertEquals(0, javac.exitCode, javac.output);

        ProcessResult java = run(command(
                java(),
                "-cp",
                classes + File.pathSeparator + javaClassPath,
                "Issue7660Bean"
        ));
        assertEquals(0, java.exitCode, java.output);
        assertEquals("{\"name\":\"Ada\"}", lastLine(java.output));
    }

    private static List<String> command(String first, String... rest) {
        List<String> command = new ArrayList<>();
        command.add(first);
        command.addAll(Arrays.asList(rest));
        return command;
    }

    private static ProcessResult run(List<String> command) throws Exception {
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        byte[] output = process.getInputStream().readAllBytes();
        return new ProcessResult(process.waitFor(), new String(output, StandardCharsets.UTF_8));
    }

    private static String javac() {
        return executable("javac");
    }

    private static String java() {
        return executable("java");
    }

    private static String executable(String name) {
        return System.getProperty("java.home") + File.separator + "bin" + File.separator + name;
    }

    private static String lastLine(String output) {
        String[] lines = output.trim().split("\\R");
        return lines[lines.length - 1];
    }

    private static final class ProcessResult {
        final int exitCode;
        final String output;

        ProcessResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}
