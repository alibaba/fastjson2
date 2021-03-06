package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codegen.ObjectReaderGen;
import com.alibaba.fastjson2.modules.ObjectReaderAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.util.DynamicClassLoader;

import javax.tools.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectReaderCreatorDynamicCompile
        extends ObjectReaderCreator {
    // GraalVM not support
    // Android not support
    public static final ObjectReaderCreatorDynamicCompile INSTANCE = new ObjectReaderCreatorDynamicCompile();

    @Override
    public <T> ObjectReader<T> createObjectReader(Class<T> objectClass, Type objectType, boolean fieldBased, List<ObjectReaderModule> modules) {
        BeanInfo beanInfo = new BeanInfo();

        for (ObjectReaderModule module : modules) {
            ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor != null) {
                annotationProcessor.getBeanInfo(beanInfo, objectClass);
            }
        }

        if (beanInfo.deserializer != null && ObjectReader.class.isAssignableFrom(beanInfo.deserializer)) {
            try {
                return (ObjectReader<T>) beanInfo.deserializer.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new JSONException("create deserializer error", e);
            }
        }

        FieldReader[] fieldReaderArray = createFieldReaders(objectClass, objectType);

        StringBuilder out = new StringBuilder();
        ObjectReaderGen gen = new ObjectReaderGen(objectClass, out);
        gen.gen();

        String sourceCode = out.toString();

        ObjectReader objectReader;
        try {
            objectReader = compile(
                    gen.getPackageName(),
                    gen.getClassName(),
                    sourceCode,
                    new Class[]{FieldReader[].class},
                    new Object[]{fieldReaderArray}
            );
        } catch (Exception e) {
            throw new JSONException("compile error", e);
        }

        return objectReader;
    }

    static DiagnosticCollector<JavaFileObject> DIAGNOSTIC_COLLECTOR = new DiagnosticCollector<>();

    @SuppressWarnings("unchecked")
    public static <T> T compile(String packageName,
                                String className,
                                String sourceCode,
                                Class<?>[] constructorParamTypes,
                                Object[] constructorParams) throws Exception {
        // ???????????????????????????
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        // ??????????????????
        List<String> options = new ArrayList<>();
        options.add("-source");
        options.add("1.8");
        options.add("-target");
        options.add("1.8");
        // ???????????????Java?????????????????????
        StandardJavaFileManager manager = compiler.getStandardFileManager(DIAGNOSTIC_COLLECTOR, null, null);
        // ??????????????????????????????
        JdkDynamicCompileClassLoader classLoader = new JdkDynamicCompileClassLoader();
        // ??????????????????Java?????????????????????
        JdkDynamicCompileJavaFileManager fileManager = new JdkDynamicCompileJavaFileManager(manager, classLoader);
        String qualifiedName = packageName + "." + className;
        // ??????Java???????????????
        CharSequenceJavaFileObject javaFileObject = new CharSequenceJavaFileObject(className, sourceCode);
        // ??????Java???????????????????????????Java????????????????????????
        fileManager.addJavaFileObject(
                StandardLocation.SOURCE_PATH,
                packageName,
                className + CharSequenceJavaFileObject.JAVA_EXTENSION,
                javaFileObject
        );
        // ?????????????????????????????????
        JavaCompiler.CompilationTask compilationTask = compiler.getTask(
                null,
                fileManager,
                DIAGNOSTIC_COLLECTOR,
                options,
                null,
                Collections.singleton(javaFileObject)
        );
        Boolean result = compilationTask.call();
//        System.out.println(String.format("??????[%s]??????:%s", qualifiedName, result));
        if (!result) {
            Diagnostic<? extends JavaFileObject> diagnostic = DIAGNOSTIC_COLLECTOR.getDiagnostics().get(0);
            System.out.println(diagnostic);
            throw new JSONException("compile error. \n" + sourceCode);
        }
        Class<?> klass = classLoader.loadClass(qualifiedName);
        Constructor<?> constructor = klass.getDeclaredConstructor(constructorParamTypes);
        return (T) constructor.newInstance(constructorParams);
    }

    public static class CharSequenceJavaFileObject
            extends SimpleJavaFileObject {
        public static final String CLASS_EXTENSION = ".class";

        public static final String JAVA_EXTENSION = ".java";

        private static URI fromClassName(String className) {
            try {
                return new URI(className);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(className, e);
            }
        }

        private ByteArrayOutputStream byteCode;
        private final CharSequence sourceCode;

        public CharSequenceJavaFileObject(String className, CharSequence sourceCode) {
            super(fromClassName(className + JAVA_EXTENSION), Kind.SOURCE);
            this.sourceCode = sourceCode;
        }

        public CharSequenceJavaFileObject(String fullClassName, Kind kind) {
            super(fromClassName(fullClassName), kind);
            this.sourceCode = null;
        }

        public CharSequenceJavaFileObject(URI uri, Kind kind) {
            super(uri, kind);
            this.sourceCode = null;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return sourceCode;
        }

        @Override
        public InputStream openInputStream() {
            return new ByteArrayInputStream(getByteCode());
        }

        // ??????????????????????????????????????????OutputStream???????????????????????????????????????getByteCode()??????????????????????????????????????????????????????
        @Override
        public OutputStream openOutputStream() {
            return byteCode = new ByteArrayOutputStream();
        }

        public byte[] getByteCode() {
            return byteCode.toByteArray();
        }
    }

    public static class JdkDynamicCompileClassLoader
            extends DynamicClassLoader {
        public static final String CLASS_EXTENSION = ".class";

        private final Map<String, JavaFileObject> javaFileObjectMap = new ConcurrentHashMap<>();

        public JdkDynamicCompileClassLoader() {
            super();
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            JavaFileObject javaFileObject = javaFileObjectMap.get(name);
            if (null != javaFileObject) {
                CharSequenceJavaFileObject charSequenceJavaFileObject = (CharSequenceJavaFileObject) javaFileObject;
                byte[] byteCode = charSequenceJavaFileObject.getByteCode();
                return defineClass(name, byteCode, 0, byteCode.length);
            }
            return super.findClass(name);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            if (name.endsWith(CLASS_EXTENSION)) {
                String qualifiedClassName = name.substring(0, name.length() - CLASS_EXTENSION.length()).replace('/', '.');
                CharSequenceJavaFileObject javaFileObject = (CharSequenceJavaFileObject) javaFileObjectMap.get(qualifiedClassName);
                if (null != javaFileObject && null != javaFileObject.getByteCode()) {
                    return new ByteArrayInputStream(javaFileObject.getByteCode());
                }
            }
            return super.getResourceAsStream(name);
        }

        /**
         * ????????????????????????????????????,key???????????????????????????URI?????????,???club.throwable.compile.HelloService
         */
        void addJavaFileObject(String qualifiedClassName, JavaFileObject javaFileObject) {
            javaFileObjectMap.put(qualifiedClassName, javaFileObject);
        }

        Collection<JavaFileObject> listJavaFileObject() {
            return Collections.unmodifiableCollection(javaFileObjectMap.values());
        }
    }

    public static class JdkDynamicCompileJavaFileManager
            extends ForwardingJavaFileManager<JavaFileManager> {
        private final JdkDynamicCompileClassLoader classLoader;
        private final Map<URI, JavaFileObject> javaFileObjectMap = new ConcurrentHashMap<>();

        public JdkDynamicCompileJavaFileManager(JavaFileManager fileManager, JdkDynamicCompileClassLoader classLoader) {
            super(fileManager);
            this.classLoader = classLoader;
        }

        private static URI fromLocation(Location location, String packageName, String relativeName) {
            try {
                return new URI(location.getName() + '/' + packageName + '/' + relativeName);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
            JavaFileObject javaFileObject = javaFileObjectMap.get(fromLocation(location, packageName, relativeName));
            if (null != javaFileObject) {
                return javaFileObject;
            }
            return super.getFileForInput(location, packageName, relativeName);
        }

        /**
         * ??????????????????????????????(???)Java????????????,?????????CharSequenceJavaFileObject??????
         */
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            JavaFileObject javaFileObject = new CharSequenceJavaFileObject(className, kind);
            classLoader.addJavaFileObject(className, javaFileObject);
            return javaFileObject;
        }

        /**
         * ?????????????????????????????????
         */
        @Override
        public ClassLoader getClassLoader(Location location) {
            return classLoader;
        }

        @Override
        public String inferBinaryName(Location location, JavaFileObject file) {
            if (file instanceof CharSequenceJavaFileObject) {
                return file.getName();
            }
            return super.inferBinaryName(location, file);
        }

        @Override
        public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
            Iterable<JavaFileObject> superResult = super.list(location, packageName, kinds, recurse);
            List<JavaFileObject> result = new ArrayList();
            // ????????????????????????Location???????????????Kind
            if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
                // .class????????????classPath???
                for (JavaFileObject file : javaFileObjectMap.values()) {
                    if (file.getKind() == JavaFileObject.Kind.CLASS && file.getName().startsWith(packageName)) {
                        result.add(file);
                    }
                }
                // ???????????????????????????????????????????????????Java????????????
                result.addAll(classLoader.listJavaFileObject());
            } else if (location == StandardLocation.SOURCE_PATH && kinds.contains(JavaFileObject.Kind.SOURCE)) {
                // .java???????????????????????????
                for (JavaFileObject file : javaFileObjectMap.values()) {
                    if (file.getKind() == JavaFileObject.Kind.SOURCE && file.getName().startsWith(packageName)) {
                        result.add(file);
                    }
                }
            }
            for (JavaFileObject javaFileObject : superResult) {
                result.add(javaFileObject);
            }
            return result;
        }

        /**
         * ???????????????,????????????????????????????????????????????????
         */
        public void addJavaFileObject(Location location, String packageName, String relativeName, JavaFileObject javaFileObject) {
            javaFileObjectMap.put(fromLocation(location, packageName, relativeName), javaFileObject);
        }
    }
}
