package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("features")
public class SortFieldNamesRegistryTest {
    public static class Money {
        public long cents = 123;
    }

    static final class DollarWriter
            implements ObjectWriter<Money> {
        static final DollarWriter INSTANCE = new DollarWriter();

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            jsonWriter.writeString("$1.23");
        }
    }

    @Test
    public void registeredWriterHonoredUnderSortedContext() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.register(Money.class, DollarWriter.INSTANCE);
        Money money = new Money();
        assertEquals("\"$1.23\"", JSON.toJSONString(money, new JSONWriter.Context(provider)));
        assertEquals("\"$1.23\"", JSON.toJSONString(money,
                new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically)));

        provider.unregister(Money.class);
        assertEquals("{\"cents\":123}", JSON.toJSONString(money,
                new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically)));
    }

    @Test
    public void registerIfAbsentHonoredUnderSortedContext() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.registerIfAbsent(Money.class, DollarWriter.INSTANCE);
        assertEquals("\"$1.23\"", JSON.toJSONString(new Money(),
                new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically)));
    }

    @JSONType(alphabetic = false)
    public static class Transfer {
        public long to = 1001;
        public long from = 2002;
        public long amount = 300;
    }

    @Test
    public void perCallBeanToArrayKeepsPositionalOrder() {
        Transfer t = new Transfer();
        assertEquals("[1001,2002,300]", JSON.toJSONString(t, JSONWriter.Feature.BeanToArray));
        assertEquals("[1001,2002,300]", JSON.toJSONString(t,
                JSONWriter.Feature.BeanToArray, JSONWriter.Feature.SortFieldNamesAlphabetically));
    }

    public static class Account {
        public int a = 2;
        public int z = 1;
    }

    public static class AccountMixIn {
        @JSONField(name = "renamed")
        public int a;
        public int z;
    }

    @Test
    public void mixInAfterWarmupAppliesToSortedWriter() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        Account account = new Account();
        JSONWriter.Context natural = new JSONWriter.Context(provider);
        JSONWriter.Context sorted = new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically);

        assertEquals("{\"a\":2,\"z\":1}", JSON.toJSONString(account, natural));
        assertEquals("{\"a\":2,\"z\":1}", JSON.toJSONString(account, sorted));

        provider.mixIn(Account.class, AccountMixIn.class);
        // natural writes are also alphabetical on the default-alphabetic provider
        assertEquals("{\"renamed\":2,\"z\":1}", JSON.toJSONString(account, natural));
        assertEquals("{\"renamed\":2,\"z\":1}", JSON.toJSONString(account, sorted));
    }

    @Test
    public void cleanupClassEvictsSortedWriter() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        Account account = new Account();
        JSONWriter.Context sorted = new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically);
        assertEquals("{\"a\":2,\"z\":1}", JSON.toJSONString(account, sorted));

        provider.cleanup(Account.class);
        provider.mixIn(Account.class, AccountMixIn.class);
        assertEquals("{\"renamed\":2,\"z\":1}", JSON.toJSONString(account, sorted));
    }

    @Test
    public void clearEvictsSortedWriter() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        Account account = new Account();
        JSONWriter.Context sorted = new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically);
        assertEquals("{\"a\":2,\"z\":1}", JSON.toJSONString(account, sorted));

        provider.clear();
        provider.mixIn(Account.class, AccountMixIn.class);
        // mixIn alone does not re-create the writer here? clear() wiped both caches,
        // so the next write is created with the mixIn mapping visible
        assertEquals("{\"renamed\":2,\"z\":1}", JSON.toJSONString(account, sorted));
    }

    static class IsolatedLoader
            extends ClassLoader {
        IsolatedLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (name.equals(LeakBean.class.getName())) {
                // bypass parent delegation so this loader actually defines the class
                return findClass(name);
            }
            return super.loadClass(name);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (name.equals(LeakBean.class.getName())) {
                String path = name.replace('.', '/') + ".class";
                try (InputStream in = getParent().getResourceAsStream(path)) {
                    if (in == null) {
                        throw new ClassNotFoundException(name);
                    }
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] chunk = new byte[4096];
                    int n;
                    while ((n = in.read(chunk)) != -1) {
                        buffer.write(chunk, 0, n);
                    }
                    byte[] bytes = buffer.toByteArray();
                    return defineClass(name, bytes, 0, bytes.length);
                } catch (IOException e) {
                    throw new ClassNotFoundException(name, e);
                }
            }
            return super.findClass(name);
        }
    }

    public static class Secret {
        public String value = "sensitive";
    }

    public static class MaskWriter
            implements ObjectWriter<Secret> {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            jsonWriter.writeString("MASKED");
        }
    }

    public static class Holder {
        @JSONField(writeUsing = MaskWriter.class)
        public Secret secret = new Secret();
    }

    @Test
    public void writeUsingFieldWriterHonoredUnderSortedContext() {
        Holder holder = new Holder();
        assertEquals("{\"secret\":\"MASKED\"}", JSON.toJSONString(holder));
        assertEquals("{\"secret\":\"MASKED\"}",
                JSON.toJSONString(holder, JSONWriter.Feature.SortFieldNamesAlphabetically));
    }

    @JSONType(alphabetic = false)
    public static class Child {
        public int zebra = 3;
        public int apple = 1;
    }

    public static class ChildHolder {
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public Child child = new Child();
    }

    @Test
    public void beanToArrayAnnotatedFieldKeepsPositionalOrder() {
        ChildHolder holder = new ChildHolder();
        assertEquals("{\"child\":[3,1]}", JSON.toJSONString(holder));
        assertEquals("{\"child\":[3,1]}",
                JSON.toJSONString(holder, JSONWriter.Feature.SortFieldNamesAlphabetically));
    }

    public static class ChildListHolder {
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public java.util.List<Child> children = java.util.Collections.singletonList(new Child());
    }

    @Test
    public void beanToArrayAnnotatedListFieldKeepsNaturalShapeUnderSort() {
        // field-level BeanToArray semantics differ by creator: the ASM creator renders item
        // beans as objects in declaration order, the reflective one honors positional arrays.
        // In both cases the sorted-context output must be identical to the natural output.
        ChildListHolder holder = new ChildListHolder();
        String natural = JSON.toJSONString(holder);
        assertEquals(natural,
                JSON.toJSONString(holder, JSONWriter.Feature.SortFieldNamesAlphabetically));
        assertEquals("reflect".equals(System.getProperty("fastjson2.creator"))
                        ? "{\"children\":[[3,1]]}"
                        : "{\"children\":[{\"zebra\":3,\"apple\":1}]}",
                natural);
    }

    public static class DatesBean {
        @JSONField(format = "yyyy-MM-dd")
        public java.util.List<java.util.Date> dates = java.util.Collections.singletonList(new java.util.Date(0L));
    }

    @Test
    public void listItemFormatPreservedUnderSortedContext() {
        DatesBean bean = new DatesBean();
        assertEquals("{\"dates\":[\"1970-01-01\"]}", JSON.toJSONString(bean));
        assertEquals("{\"dates\":[\"1970-01-01\"]}",
                JSON.toJSONString(bean, JSONWriter.Feature.SortFieldNamesAlphabetically));
    }

    @Test
    public void cleanupClassLoaderReleasesLoader() throws Exception {
        ClassLoader parent = SortFieldNamesRegistryTest.class.getClassLoader();
        IsolatedLoader childLoader = new IsolatedLoader(parent);
        Class<?> loadedClass = childLoader.loadClass(LeakBean.class.getName());
        assert loadedClass != LeakBean.class;

        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.getObjectWriter(loadedClass, loadedClass, false);
        provider.getObjectWriter(loadedClass, loadedClass, JSONWriter.Feature.SortFieldNamesAlphabetically.mask);

        provider.cleanup(childLoader);

        WeakReference<Class<?>> weakClass = new WeakReference<>(loadedClass);
        WeakReference<ClassLoader> weakLoader = new WeakReference<>(childLoader);
        loadedClass = null;
        childLoader = null;
        System.gc();
        System.gc();
        Thread.sleep(20);
        assertNull(weakClass.get());
        assertNull(weakLoader.get());
    }

    public static class Inner {
        private int hidden = 42;
        private String name = "n";
    }

    public static class Inner$$EnhancerBySpringCGLIB$$abcdef extends Inner {
    }

    @Test
    public void fieldBasedProxyKeepsFieldBasedWriterVariant() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        JSONWriter.Context context = new JSONWriter.Context(provider, JSONWriter.Feature.FieldBased);

        Inner plain = new Inner();
        String plainJSON = JSON.toJSONString(plain, context);
        assertTrue(plainJSON.contains("hidden"), plainJSON);

        Inner proxy = new Inner$$EnhancerBySpringCGLIB$$abcdef();
        String proxyJSON = JSON.toJSONString(proxy, context);
        assertTrue(proxyJSON.contains("hidden"), proxyJSON);
        assertTrue(proxyJSON.contains("\"name\":\"n\""), proxyJSON);
    }
}
