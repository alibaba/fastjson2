package com.alibaba.fastjson2.internal.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.ToolProvider;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneratedExpressionRegressionTest {
    @TempDir
    Path directory;

    @Test
    public void compilesAndPreservesLiteralValues() throws Exception {
        ClassWriter writer = new ClassWriter(null, "GeneratedExpressions", null, new Class[0]);
        String value = "quote\" slash\\ newline\n tab\t return\r";
        writer.method(Modifier.PUBLIC, "text", String.class, new Class[0], new String[0]).ret(Opcodes.ldc(value));
        char[] chars = {'\'', '\\', '\n', '\r', '\t'};
        for (int i = 0; i < chars.length; i++) {
            writer.method(Modifier.PUBLIC, "character" + i, char.class, new Class[0], new String[0])
                    .ret(Opcodes.ldc(chars[i]));
        }
        writer.method(Modifier.PUBLIC, "different", boolean.class,
                new Class[]{int.class, int.class}, new String[]{"a", "b"})
                .ret(Opcodes.not(Opcodes.eq(Opcodes.var("a"), Opcodes.var("b"))));

        Path source = directory.resolve("GeneratedExpressions.java");
        Files.write(source, writer.toString().getBytes(StandardCharsets.UTF_8));
        assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null,
                "-proc:none", "-encoding", "UTF-8", "-d", directory.toString(), source.toString()));
        try (URLClassLoader loader = new URLClassLoader(new URL[]{directory.toUri().toURL()})) {
            Class<?> generated = loader.loadClass("GeneratedExpressions");
            Object instance = generated.getDeclaredConstructor().newInstance();
            assertEquals(value, generated.getMethod("text").invoke(instance));
            for (int i = 0; i < chars.length; i++) {
                assertEquals(chars[i], generated.getMethod("character" + i).invoke(instance));
            }
            assertEquals(false, generated.getMethod("different", int.class, int.class).invoke(instance, 1, 1));
            assertEquals(true, generated.getMethod("different", int.class, int.class).invoke(instance, 1, 2));
        }
    }
}
