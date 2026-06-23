package com.alibaba.fastjson2.internal.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
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
        assertEquals("{\"name\":\"Ada\"}", runSource(
                "Issue7660Bean",
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
                )
        ));
    }

    @Test
    public void jsonCompiledHonorsSerializeFalseOnGetter() throws Exception {
        assertEquals("{\"name\":\"Ada\"}", runSource(
                "Issue7660GetterBean",
                Arrays.asList(
                    "import com.alibaba.fastjson2.JSON;",
                    "import com.alibaba.fastjson2.annotation.JSONCompiled;",
                    "import com.alibaba.fastjson2.annotation.JSONField;",
                    "",
                    "public class Issue7660GetterBean {",
                    "    @JSONCompiled",
                    "    public static class Person {",
                    "        private String internalCode;",
                    "        private String name;",
                    "",
                    "        public Person() {",
                    "        }",
                    "",
                    "        public Person(String internalCode, String name) {",
                    "            this.internalCode = internalCode;",
                    "            this.name = name;",
                    "        }",
                    "",
                    "        @JSONField(serialize = false)",
                    "        public String getInternalCode() {",
                    "            return internalCode;",
                    "        }",
                    "",
                    "        public void setInternalCode(String internalCode) {",
                    "            this.internalCode = internalCode;",
                    "        }",
                    "",
                    "        public String getName() {",
                    "            return name;",
                    "        }",
                    "",
                    "        public void setName(String name) {",
                    "            this.name = name;",
                    "        }",
                    "    }",
                    "",
                    "    public static void main(String[] args) {",
                    "        System.out.println(JSON.toJSONString(new Person(\"secret\", \"Ada\")));",
                    "    }",
                    "}"
                )
        ));
    }

    @Test
    public void jsonCompiledKeepsChildAccessorDisableWhenParentFieldExists() throws Exception {
        assertEquals("{\"name\":\"Ada\"}", runSource(
                "Issue7660InheritedBean",
                Arrays.asList(
                    "import com.alibaba.fastjson2.JSON;",
                    "import com.alibaba.fastjson2.annotation.JSONCompiled;",
                    "import com.alibaba.fastjson2.annotation.JSONField;",
                    "",
                    "public class Issue7660InheritedBean {",
                    "    public static class BasePerson {",
                    "        public String internalCode;",
                    "        public String name;",
                    "",
                    "        public BasePerson() {",
                    "        }",
                    "",
                    "        public BasePerson(String internalCode, String name) {",
                    "            this.internalCode = internalCode;",
                    "            this.name = name;",
                    "        }",
                    "    }",
                    "",
                    "    @JSONCompiled",
                    "    public static class Person extends BasePerson {",
                    "        public Person() {",
                    "        }",
                    "",
                    "        public Person(String internalCode, String name) {",
                    "            super(internalCode, name);",
                    "        }",
                    "",
                    "        @JSONField(serialize = false)",
                    "        public String getInternalCode() {",
                    "            return internalCode;",
                    "        }",
                    "",
                    "        public String getName() {",
                    "            return name;",
                    "        }",
                    "    }",
                    "",
                    "    public static void main(String[] args) {",
                    "        System.out.println(JSON.toJSONString(new Person(\"secret\", \"Ada\")));",
                    "    }",
                    "}"
                )
        ));
    }

    @Test
    public void jsonCompiledHonorsDeserializeFalse() throws Exception {
        assertEquals("null:Ada", runSource(
                "Issue7660ReaderBean",
                Arrays.asList(
                    "import com.alibaba.fastjson2.JSON;",
                    "import com.alibaba.fastjson2.annotation.JSONCompiled;",
                    "import com.alibaba.fastjson2.annotation.JSONField;",
                    "",
                    "public class Issue7660ReaderBean {",
                    "    @JSONCompiled",
                    "    public static class Person {",
                    "        @JSONField(deserialize = false)",
                    "        public String internalCode;",
                    "        public String name;",
                    "",
                    "        public Person() {",
                    "        }",
                    "    }",
                    "",
                    "    public static void main(String[] args) {",
                    "        Person person = JSON.parseObject(\"{\\\"internalCode\\\":\\\"secret\\\",\\\"name\\\":\\\"Ada\\\"}\", Person.class);",
                    "        System.out.println(person.internalCode + \":\" + person.name);",
                    "    }",
                    "}"
                )
        ));
    }

    @Test
    public void jsonCompiledHonorsDeserializeFalseOnSetter() throws Exception {
        assertEquals("null:Ada", runSource(
                "Issue7660SetterReaderBean",
                Arrays.asList(
                    "import com.alibaba.fastjson2.JSON;",
                    "import com.alibaba.fastjson2.annotation.JSONCompiled;",
                    "import com.alibaba.fastjson2.annotation.JSONField;",
                    "",
                    "public class Issue7660SetterReaderBean {",
                    "    @JSONCompiled",
                    "    public static class Person {",
                    "        private String internalCode;",
                    "        private String name;",
                    "",
                    "        public Person() {",
                    "        }",
                    "",
                    "        public String getInternalCode() {",
                    "            return internalCode;",
                    "        }",
                    "",
                    "        @JSONField(deserialize = false)",
                    "        public void setInternalCode(String internalCode) {",
                    "            this.internalCode = internalCode;",
                    "        }",
                    "",
                    "        public String getName() {",
                    "            return name;",
                    "        }",
                    "",
                    "        public void setName(String name) {",
                    "            this.name = name;",
                    "        }",
                    "    }",
                    "",
                    "    public static void main(String[] args) {",
                    "        Person person = JSON.parseObject(\"{\\\"internalCode\\\":\\\"secret\\\",\\\"name\\\":\\\"Ada\\\"}\", Person.class);",
                    "        System.out.println(person.getInternalCode() + \":\" + person.getName());",
                    "    }",
                    "}"
                )
        ));
    }

    @Test
    public void jsonCompiledSerializeFalseStillAllowsDeserialization() throws Exception {
        assertEquals("secret:{\"name\":\"Ada\"}", runSource(
                "Issue7660RoundTripBean",
                Arrays.asList(
                    "import com.alibaba.fastjson2.JSON;",
                    "import com.alibaba.fastjson2.annotation.JSONCompiled;",
                    "import com.alibaba.fastjson2.annotation.JSONField;",
                    "",
                    "public class Issue7660RoundTripBean {",
                    "    @JSONCompiled",
                    "    public static class Person {",
                    "        @JSONField(serialize = false)",
                    "        public String internalCode;",
                    "        public String name;",
                    "",
                    "        public Person() {",
                    "        }",
                    "    }",
                    "",
                    "    public static void main(String[] args) {",
                    "        Person person = JSON.parseObject(\"{\\\"internalCode\\\":\\\"secret\\\",\\\"name\\\":\\\"Ada\\\"}\", Person.class);",
                    "        System.out.println(person.internalCode + \":\" + JSON.toJSONString(person));",
                    "    }",
                    "}"
                )
        ));
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = process.getInputStream().read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        byte[] output = baos.toByteArray();
        return new ProcessResult(process.waitFor(), new String(output, StandardCharsets.UTF_8));
    }

    private String runSource(String className, List<String> sourceLines) throws Exception {
        Path classes = tempDir.resolve(className + "-classes");
        Files.createDirectories(classes);

        Path source = tempDir.resolve(className + ".java");
        Files.write(source, sourceLines, StandardCharsets.UTF_8);

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
                className
        ));
        assertEquals(0, java.exitCode, java.output);
        return lastLine(java.output);
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
