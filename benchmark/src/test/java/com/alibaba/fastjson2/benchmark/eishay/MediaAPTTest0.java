package com.alibaba.fastjson2.benchmark.eishay;

import com.alibaba.fastjson2.internal.processor.JSONCompiledAnnotationProcessor;
import org.junit.jupiter.api.Test;

import javax.tools.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MediaAPTTest0 {
    @Test
    public void test() throws Exception {
        Path dir = new File("/Users/wenshao/Work/git/fastjson2/benchmark/target/generated-sources")
                .toPath();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(dir.toFile()));
        fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, Arrays.asList(dir.toFile()));

        File file = new File("src/main/java/com/alibaba/fastjson2/benchmark/eishay/vo/Media.java");
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
