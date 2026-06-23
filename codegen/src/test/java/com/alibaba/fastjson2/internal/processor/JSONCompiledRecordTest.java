package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class JSONCompiledRecordTest {
    @TempDir
    public Path tempDir;

    @Test
    public void record() throws Exception {
        try (CompiledClass compiled = compile("Person",
                "import com.alibaba.fastjson2.annotation.JSONCompiled;",
                "",
                "@JSONCompiled",
                "public record Person(int id, String name) {",
                "}"
        )) {
            Class<?> personClass = compiled.clazz;
            Object person = JSON.parseObject("{\"name\":\"DataWorks\",\"id\":101}", personClass);
            assertEquals(101, personClass.getMethod("id").invoke(person));
            assertEquals("DataWorks", personClass.getMethod("name").invoke(person));
        }
    }

    @Test
    public void recordCreateInstanceUsesDefaultValues() throws Exception {
        try (CompiledClass compiled = compile("Person",
                "import com.alibaba.fastjson2.annotation.JSONCompiled;",
                "",
                "@JSONCompiled",
                "public record Person(int id, String name, boolean active, long score) {",
                "}"
        )) {
            Class<?> personClass = compiled.clazz;
            ObjectReader<?> objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(personClass);
            Object person = objectReader.createInstance(0L);
            assertEquals(0, personClass.getMethod("id").invoke(person));
            assertNull(personClass.getMethod("name").invoke(person));
            assertEquals(false, personClass.getMethod("active").invoke(person));
            assertEquals(0L, personClass.getMethod("score").invoke(person));
        }
    }

    @Test
    public void recordJsonbSerializationAndCollections() throws Exception {
        try (CompiledClass compiled = compile("Person",
                "import com.alibaba.fastjson2.annotation.JSONCompiled;",
                "import java.util.List;",
                "import java.util.Map;",
                "",
                "@JSONCompiled",
                "public record Person(int id, String name, List<String> tags, Map<String, Integer> scores) {",
                "}"
        )) {
            Class<?> personClass = compiled.clazz;
            Object person = JSON.parseObject(
                    "{\"id\":7,\"name\":\"Flink\",\"tags\":[\"stream\",\"sql\"],\"scores\":{\"core\":99}}",
                    personClass
            );
            assertEquals("{\"id\":7,\"name\":\"Flink\",\"tags\":[\"stream\",\"sql\"],\"scores\":{\"core\":99}}",
                    JSON.toJSONString(person));

            byte[] jsonbBytes = JSONB.toBytes(person);
            Object jsonbPerson = JSONB.parseObject(jsonbBytes, personClass);
            assertEquals(7, personClass.getMethod("id").invoke(jsonbPerson));
            assertEquals("Flink", personClass.getMethod("name").invoke(jsonbPerson));
            assertEquals(Arrays.asList("stream", "sql"), personClass.getMethod("tags").invoke(jsonbPerson));
            assertEquals(Integer.valueOf(99), ((java.util.Map<?, ?>) personClass.getMethod("scores").invoke(jsonbPerson)).get("core"));
        }
    }

    @Test
    public void recordJsonbNullListFieldDoesNotCorruptFollowingFields() throws Exception {
        try (CompiledClass compiled = compile("Person",
                "import com.alibaba.fastjson2.annotation.JSONCompiled;",
                "import java.util.List;",
                "",
                "@JSONCompiled",
                "public record Person(int id, List<String> tags, String name) {",
                "}"
        )) {
            Class<?> personClass = compiled.clazz;
            byte[] jsonbBytes = JSONB.toBytes(
                    JSON.parseObject("{\"id\":7,\"tags\":null,\"name\":\"Flink\"}"),
                    JSONWriter.Feature.WriteNulls
            );
            Object person = JSONB.parseObject(jsonbBytes, personClass);

            assertEquals(7, personClass.getMethod("id").invoke(person));
            assertNull(personClass.getMethod("tags").invoke(person));
            assertEquals("Flink", personClass.getMethod("name").invoke(person));
        }
    }

    @Test
    public void recordListFieldResolvesReferences() throws Exception {
        try (CompiledClass compiled = compile("Person",
                "import com.alibaba.fastjson2.annotation.JSONCompiled;",
                "import java.util.List;",
                "",
                "@JSONCompiled",
                "public record Person(List<Item> first, List<Item> second) {",
                "    public record Item(String name) {",
                "    }",
                "}"
        )) {
            Class<?> personClass = compiled.clazz;
            Object person = JSON.parseObject(
                    "{\"first\":[{\"name\":\"Flink\"}],\"second\":[{\"$ref\":\"$.first[0]\"}]}",
                    personClass
            );

            List<?> first = (List<?>) personClass.getMethod("first").invoke(person);
            List<?> second = (List<?>) personClass.getMethod("second").invoke(person);
            assertEquals("Flink", first.get(0).getClass().getMethod("name").invoke(first.get(0)));
            assertSame(first.get(0), second.get(0));
        }
    }

    private CompiledClass compile(String className, String... sourceLines) throws Exception {
        assumeTrue(isJdk17OrLater());

        Path sourceDir = tempDir.resolve(className + "-src-" + System.nanoTime());
        Path classesDir = tempDir.resolve(className + "-classes-" + System.nanoTime());
        Files.createDirectories(sourceDir);
        Files.createDirectories(classesDir);

        Path source = sourceDir.resolve(className + ".java");
        Files.write(source, Arrays.asList(sourceLines), StandardCharsets.UTF_8);

        List<String> command = new ArrayList<>();
        command.add(javacExecutable());
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

        ProcessResult result = run(command);
        assertEquals(0, result.exitCode, result.output);

        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{classesDir.toUri().toURL()},
                Thread.currentThread().getContextClassLoader()
        );
        return new CompiledClass(classLoader, Class.forName(className, true, classLoader));
    }

    private static boolean isJdk17OrLater() {
        String version = System.getProperty("java.specification.version");
        if (version.startsWith("1.")) {
            version = version.substring(2);
        }
        return Integer.parseInt(version) >= 17;
    }

    private static String javacExecutable() {
        return new File(new File(System.getProperty("java.home"), "bin"), "javac").toString();
    }

    private static ProcessResult run(List<String> command) throws Exception {
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while ((n = process.getInputStream().read(buffer)) != -1) {
            baos.write(buffer, 0, n);
        }
        return new ProcessResult(process.waitFor(), baos.toString(StandardCharsets.UTF_8.name()));
    }

    private static final class CompiledClass
            implements AutoCloseable {
        private final URLClassLoader classLoader;
        private final Class<?> clazz;

        private CompiledClass(URLClassLoader classLoader, Class<?> clazz) {
            this.classLoader = classLoader;
            this.clazz = clazz;
        }

        @Override
        public void close() throws Exception {
            classLoader.close();
        }
    }

    private static final class ProcessResult {
        private final int exitCode;
        private final String output;

        private ProcessResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}
