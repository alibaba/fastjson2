package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONCompiledRecordTest {
    @TempDir
    public Path tempDir;

    @Test
    public void record() throws Exception {
        Path sourceDir = tempDir.resolve("src");
        Path classesDir = tempDir.resolve("classes");
        Files.createDirectories(sourceDir);
        Files.createDirectories(classesDir);

        Path source = sourceDir.resolve("Person.java");
        Files.write(source, Arrays.asList(
                "import com.alibaba.fastjson2.annotation.JSONCompiled;",
                "",
                "@JSONCompiled",
                "public record Person(int id, String name) {",
                "}"
        ), StandardCharsets.UTF_8);

        List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "javac").toString());
        command.add("-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED");
        command.add("-J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED");
        command.add("-J--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED");
        command.add("-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED");
        command.add("-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED");
        command.add("-J--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED");
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add("-processor");
        command.add(JSONCompiledAnnotationProcessor.class.getName());
        command.add("-source");
        command.add("17");
        command.add("-target");
        command.add("17");
        command.add("-d");
        command.add(classesDir.toString());
        command.add("-s");
        command.add(classesDir.toString());
        command.add(source.toString());

        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, process.waitFor(), output);

        try (URLClassLoader classLoader = new URLClassLoader(
                new URL[]{classesDir.toUri().toURL()},
                Thread.currentThread().getContextClassLoader())) {
            Class<?> personClass = Class.forName("Person", true, classLoader);
            Object person = JSON.parseObject("{\"name\":\"DataWorks\",\"id\":101}", personClass);
            assertEquals(101, personClass.getMethod("id").invoke(person));
            assertEquals("DataWorks", personClass.getMethod("name").invoke(person));
        }
    }
}
