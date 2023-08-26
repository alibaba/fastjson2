package com.alibaba.fastjson2.internal.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import java.lang.reflect.Method;
import java.util.Set;

import static com.alibaba.fastjson2.util.JDKUtils.JVM_VERSION;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
        "com.alibaba.fastjson2.annotation.JSONCompiled",
        "com.alibaba.fastjson2.annotation.JSONBuilder",
        "com.alibaba.fastjson2.annotation.JSONCreator",
        "com.alibaba.fastjson2.annotation.JSONField",
        "com.alibaba.fastjson2.annotation.JSONType"
})
public class JSONBaseAnnotationProcessor
        extends AbstractProcessor {
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        addOpensSinceJava9();
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    private static void addOpensSinceJava9() {
        if (JVM_VERSION >= 9) {
            Class<?> cModule = null;
            try {
                cModule = Class.forName("java.lang.Module");
            } catch (ClassNotFoundException e) {
                // just ignore
            }
            Object jdkCompilerModule = getJdkCompilerModule();
            Object ownModule = getOwnModule();
            String[] allPkgs = {
                    "com.sun.tools.javac.api",
                    "com.sun.tools.javac.code",
                    "com.sun.tools.javac.comp",
                    "com.sun.tools.javac.file",
                    "com.sun.tools.javac.main",
                    "com.sun.tools.javac.model",
                    "com.sun.tools.javac.parser",
                    "com.sun.tools.javac.processing",
                    "com.sun.tools.javac.tree",
                    "com.sun.tools.javac.util"
            };

            try {
                Method m = cModule.getDeclaredMethod("implAddOpens", String.class, cModule);
                long firstFieldOffset = UNSAFE.objectFieldOffset(Parent.class.getDeclaredField("first"));
                UNSAFE.putBooleanVolatile(m, firstFieldOffset, true);
                for (String p : allPkgs) {
                    m.invoke(jdkCompilerModule, p, ownModule);
                }
            } catch (Exception e) {
                return;
            }
        }
    }

    private static Object getJdkCompilerModule() {
        try {
            Class<?> moduleLayer = Class.forName("java.lang.ModuleLayer");
            Method boot = moduleLayer.getDeclaredMethod("boot");
            Object bootLayer = boot.invoke(null);
            Class<?> clazz = Class.forName("java.util.Optional");
            Method findModule = moduleLayer.getDeclaredMethod("findModule", String.class);
            Object compiler = findModule.invoke(bootLayer, "jdk.compiler");
            return clazz.getDeclaredMethod("get").invoke(compiler);
        } catch (Exception e) {
            return null;
        }
    }

    private static Object getOwnModule() {
        try {
            Class<Class> clazz = Class.class;
            Method getModule = clazz.getDeclaredMethod("getModule");
            return getModule.invoke(JSONBaseAnnotationProcessor.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static class Parent {
        boolean first;
    }
}
