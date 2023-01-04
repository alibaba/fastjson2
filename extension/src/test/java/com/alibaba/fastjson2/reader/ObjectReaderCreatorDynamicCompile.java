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
import java.lang.reflect.InvocationTargetException;
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
    public <T> ObjectReader<T> createObjectReader(Class<T> objectClass, Type objectType, boolean fieldBased, ObjectReaderProvider provider) {
        BeanInfo beanInfo = new BeanInfo();

        for (ObjectReaderModule module : provider.modules) {
            ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor != null) {
                annotationProcessor.getBeanInfo(beanInfo, objectClass);
            }
        }

        if (beanInfo.deserializer != null && ObjectReader.class.isAssignableFrom(beanInfo.deserializer)) {
            try {
                Constructor constructor = beanInfo.deserializer.getDeclaredConstructor();
                constructor.setAccessible(true);
                return (ObjectReader<T>) constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
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
        // 获取系统编译器实例
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        // 设置编译参数
        List<String> options = new ArrayList<>();
        options.add("-source");
        options.add("1.8");
        options.add("-target");
        options.add("1.8");
        // 获取标准的Java文件管理器实例
        StandardJavaFileManager manager = compiler.getStandardFileManager(DIAGNOSTIC_COLLECTOR, null, null);
        // 初始化自定义类加载器
        JdkDynamicCompileClassLoader classLoader = new JdkDynamicCompileClassLoader();
        // 初始化自定义Java文件管理器实例
        JdkDynamicCompileJavaFileManager fileManager = new JdkDynamicCompileJavaFileManager(manager, classLoader);
        String qualifiedName = packageName + "." + className;
        // 构建Java源文件实例
        CharSequenceJavaFileObject javaFileObject = new CharSequenceJavaFileObject(className, sourceCode);
        // 添加Java源文件实例到自定义Java文件管理器实例中
        fileManager.addJavaFileObject(
                StandardLocation.SOURCE_PATH,
                packageName,
                className + CharSequenceJavaFileObject.JAVA_EXTENSION,
                javaFileObject
        );
        // 初始化一个编译任务实例
        JavaCompiler.CompilationTask compilationTask = compiler.getTask(
                null,
                fileManager,
                DIAGNOSTIC_COLLECTOR,
                options,
                null,
                Collections.singleton(javaFileObject)
        );
        Boolean result = compilationTask.call();
//        System.out.println(String.format("编译[%s]结果:%s", qualifiedName, result));
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

        // 注意这个方法是编译结果回调的OutputStream，回调成功后就能通过下面的getByteCode()方法获取目标类编译后的字节码字节数组
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
         * 暂时存放编译的源文件对象,key为全类名的别名（非URI模式）,如club.throwable.compile.HelloService
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
         * 这里是编译器返回的同(源)Java文件对象,替换为CharSequenceJavaFileObject实现
         */
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            JavaFileObject javaFileObject = new CharSequenceJavaFileObject(className, kind);
            classLoader.addJavaFileObject(className, javaFileObject);
            return javaFileObject;
        }

        /**
         * 这里覆盖原来的类加载器
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
            // 这里要区分编译的Location以及编译的Kind
            if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
                // .class文件以及classPath下
                for (JavaFileObject file : javaFileObjectMap.values()) {
                    if (file.getKind() == JavaFileObject.Kind.CLASS && file.getName().startsWith(packageName)) {
                        result.add(file);
                    }
                }
                // 这里需要额外添加类加载器加载的所有Java文件对象
                result.addAll(classLoader.listJavaFileObject());
            } else if (location == StandardLocation.SOURCE_PATH && kinds.contains(JavaFileObject.Kind.SOURCE)) {
                // .java文件以及编译路径下
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
         * 自定义方法,用于添加和缓存待编译的源文件对象
         */
        public void addJavaFileObject(Location location, String packageName, String relativeName, JavaFileObject javaFileObject) {
            javaFileObjectMap.put(fromLocation(location, packageName, relativeName), javaFileObject);
        }
    }
}
