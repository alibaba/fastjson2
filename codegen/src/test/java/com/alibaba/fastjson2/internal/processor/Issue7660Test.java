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
        assertEquals("visible:Ada:{\"internalCode\":\"visible\",\"name\":\"Ada\"}", runSource(
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
                    "        person.internalCode = \"visible\";",
                    "        System.out.println(person.internalCode + \":\" + person.name + \":\" + JSON.toJSONString(person));",
                    "    }",
                    "}"
                )
        ));
    }

    @Test
    public void jsonCompiledHonorsSerializeFalseWithFieldBasedDeserializeFeature() throws Exception {
        assertEquals("secret:{\"name\":\"Ada\"}", runSource(
                "Issue7660FieldBasedBean",
                Arrays.asList(
                    "import com.alibaba.fastjson2.JSON;",
                    "import com.alibaba.fastjson2.JSONReader;",
                    "import com.alibaba.fastjson2.annotation.JSONCompiled;",
                    "import com.alibaba.fastjson2.annotation.JSONField;",
                    "",
                    "public class Issue7660FieldBasedBean {",
                    "    @JSONCompiled",
                    "    public static class Person {",
                    "        @JSONField(serialize = false, deserializeFeatures = {JSONReader.Feature.FieldBased})",
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

    @Test
    public void jsonCompiledHonorsSerializeFalseAndDeserializeFalseTogether() throws Exception {
        assertEquals("visible:{\"name\":\"Ada\"}", runSource(
                "Issue7660ReadWriteIgnoredBean",
                Arrays.asList(
                    "import com.alibaba.fastjson2.JSON;",
                    "import com.alibaba.fastjson2.annotation.JSONCompiled;",
                    "import com.alibaba.fastjson2.annotation.JSONField;",
                    "",
                    "public class Issue7660ReadWriteIgnoredBean {",
                    "    @JSONCompiled",
                    "    public static class Person {",
                    "        @JSONField(serialize = false, deserialize = false)",
                    "        public String internalCode;",
                    "        public String name;",
                    "",
                    "        public Person() {",
                    "        }",
                    "    }",
                    "",
                    "    public static void main(String[] args) {",
                    "        Person person = JSON.parseObject(\"{\\\"internalCode\\\":\\\"secret\\\",\\\"name\\\":\\\"Ada\\\"}\", Person.class);",
                    "        person.internalCode = \"visible\";",
                    "        System.out.println(person.internalCode + \":\" + JSON.toJSONString(person));",
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
        List<String> javacCommand = command(javac());
        if (isJdk9OrLater()) {
            javacCommand.addAll(Arrays.asList(
                    "-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
                    "-J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
                    "-J--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
                    "-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
                    "-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
                    "-J--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED"
            ));
        }
        javacCommand.addAll(Arrays.asList(
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
        ProcessResult javac = run(javacCommand);
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

    private static boolean isJdk9OrLater() {
        return !System.getProperty("java.specification.version").startsWith("1.");
    }

    private static String executable(String name) {
        File javaHome = new File(System.getProperty("java.home"));
        File executable = executable(new File(javaHome, "bin"), name);
        if (executable.isFile()) {
            return executable.getAbsolutePath();
        }

        File parent = javaHome.getParentFile();
        if (parent != null) {
            File parentExecutable = executable(new File(parent, "bin"), name);
            if (parentExecutable.isFile()) {
                return parentExecutable.getAbsolutePath();
            }
        }

        return executable.getAbsolutePath();
    }

    private static File executable(File directory, String name) {
        File executable = new File(directory, name);
        if (executable.isFile()) {
            return executable;
        }
        return new File(directory, name + ".exe");
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
