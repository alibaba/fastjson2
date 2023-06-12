package com.alibaba.fastjson2.internal.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class APTTest0 {
    @TempDir
    Path tempDir;

    @Test
    public void test() throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(tempDir.toFile()));
        fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, Arrays.asList(tempDir.toFile()));

        File file = new File("src/test/java/com/alibaba/fastjson2/internal/processor/Bean.java");
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(file);

        List<String> options = Arrays.asList("-source", "1.8");
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
        task.setProcessors(Collections.singletonList(new JSONCompiledAnnotationProcessor()));
//        task.setProcessors(Collections.singletonList(new com.dslplatform.json.processor.CompiledJsonAnnotationProcessor()));

        // When
        boolean success = task.call();

        // Then
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            System.err.println(diagnostic);
        }
    }
}
