package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import com.alibaba.fastjson2.writer.ObjectWriters;
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

    public static class Credentials {
        public long id = 7;
        public String name = "alice";
        public String password = "hunter2";
    }

    @Test
    public void registeredAdapterKeepsItsFieldWritersUnderSortedContext() {
        // a bean writer built with ObjectWriters.objectWriter(...) decides which fields are written;
        // the sorted variant may only change their order, never fall back to the class's own fields
        ObjectWriterProvider provider = new ObjectWriterProvider();
        JSONWriter.Context sorted = new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically);
        assertTrue(JSON.toJSONString(new Credentials(), sorted).contains("hunter2"));

        ObjectWriter writer = ObjectWriters.objectWriter(Credentials.class,
                ObjectWriters.fieldWriter("name", String.class, (Credentials c) -> c.name),
                ObjectWriters.fieldWriter("password", String.class, (Credentials c) -> "***"));
        provider.register(Credentials.class, writer);
        assertEquals("{\"name\":\"alice\",\"password\":\"***\"}",
                JSON.toJSONString(new Credentials(), new JSONWriter.Context(provider)));
        assertEquals("{\"name\":\"alice\",\"password\":\"***\"}", JSON.toJSONString(new Credentials(), sorted));

        ObjectWriterProvider provider2 = new ObjectWriterProvider();
        provider2.registerIfAbsent(Credentials.class, ObjectWriters.objectWriter(Credentials.class,
                ObjectWriters.fieldWriter("password", String.class, (Credentials c) -> "***"),
                ObjectWriters.fieldWriter("name", String.class, (Credentials c) -> c.name)));
        assertEquals("{\"name\":\"alice\",\"password\":\"***\"}", JSON.toJSONString(new Credentials(),
                new JSONWriter.Context(provider2, JSONWriter.Feature.SortFieldNamesAlphabetically)));

        assertTrue(provider.unregister(Credentials.class, writer));
        assertTrue(JSON.toJSONString(new Credentials(), sorted).contains("hunter2"));
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
    public void mixInAfterWarmupAppliesToFieldBasedVariantsToo() {
        // mixIn after the first write must evict all four writer cells, or FieldBased and
        // FieldBased|Sort keep serving the pre-mixin writer (including dropped fields)
        ObjectWriterProvider provider = new ObjectWriterProvider();
        Account account = new Account();
        JSONWriter.Context fieldBased = new JSONWriter.Context(provider, JSONWriter.Feature.FieldBased);
        JSONWriter.Context fieldBasedSorted = new JSONWriter.Context(provider,
                JSONWriter.Feature.FieldBased, JSONWriter.Feature.SortFieldNamesAlphabetically);

        assertEquals("{\"a\":2,\"z\":1}", JSON.toJSONString(account, fieldBased));
        assertEquals("{\"a\":2,\"z\":1}", JSON.toJSONString(account, fieldBasedSorted));

        provider.mixIn(Account.class, AccountMixIn.class);
        assertEquals("{\"renamed\":2,\"z\":1}", JSON.toJSONString(account, fieldBased));
        assertEquals("{\"renamed\":2,\"z\":1}", JSON.toJSONString(account, fieldBasedSorted));
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

    @Test
    public void beanToArrayAnnotatedFieldSortedFirstOnColdProvider() {
        // no natural writer of Child is cached yet when the first write on the provider is sorted;
        // the field's BeanToArray must still select the positional variant
        for (ObjectWriterProvider provider : new ObjectWriterProvider[]{
                new ObjectWriterProvider(), new ObjectWriterProvider(ObjectWriterCreator.INSTANCE)}) {
            assertEquals("{\"child\":[3,1]}", JSON.toJSONString(new ChildHolder(),
                    new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically)));
        }
        byte[] jsonb = JSONB.toBytes(new ChildHolder(),
                new JSONWriter.Context(new ObjectWriterProvider(), JSONWriter.Feature.SortFieldNamesAlphabetically));
        assertEquals("{\"child\":[3,1]}", JSON.toJSONString(JSONB.parse(jsonb)));
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
        // The creator is read back resolved (covers "reflect", "lambda", and the properties file),
        // not re-derived from one input channel.
        boolean asm = new ObjectWriterProvider().getCreator() instanceof ObjectWriterCreatorASM;
        ChildListHolder holder = new ChildListHolder();
        String natural = JSON.toJSONString(holder);
        assertEquals(natural,
                JSON.toJSONString(holder, JSONWriter.Feature.SortFieldNamesAlphabetically));
        assertEquals(asm
                        ? "{\"children\":[{\"zebra\":3,\"apple\":1}]}"
                        : "{\"children\":[[3,1]]}",
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

    public static class FormatAndSortedMapHolder {
        @JSONField(format = "yyyy-MM-dd", serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public java.util.Map<String, Natural> map = new java.util.LinkedHashMap<>(
                java.util.Collections.singletonMap("k", new Natural()));
    }

    @Test
    public void mapFieldWithFormatKeepsTheFieldLevelSort() {
        // a format attribute on a map field must not switch the field-level sort off: the format
        // arm of the sorted value-writer branch resolves with the merged word, format included
        assertEquals("{\"map\":{\"k\":{\"alpha\":2,\"zeta\":1}}}", JSON.toJSONString(new FormatAndSortedMapHolder()));
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

    @JSONType(alphabetic = false)
    public static class SharedChild {
        public int zebra = 3;
        public int apple = 1;
    }

    @JSONType(alphabetic = false)
    public static class SharedHolder {
        public int zulu = 9;
        public java.util.List<SharedChild> items = new java.util.ArrayList<>();

        public SharedHolder() {
            items.add(new SharedChild());
        }
    }

    @Test
    public void registeredVariantsIsolateListItemCaches() {
        // the natural and sorted registered variants share FieldWriter instances; a dynamically
        // cached list-item writer must not leak one variant's writer into the other
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.register(SharedHolder.class,
                new ObjectWriterCreator().createObjectWriter(SharedHolder.class));
        JSONWriter.Context natural = new JSONWriter.Context(provider);
        JSONWriter.Context sorted = new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically);

        String naturalExpected = "{\"zulu\":9,\"items\":[{\"zebra\":3,\"apple\":1}]}";
        String sortedExpected = "{\"items\":[{\"apple\":1,\"zebra\":3}],\"zulu\":9}";

        // natural first, then sorted, then natural again
        SharedHolder holder = new SharedHolder();
        assertEquals(naturalExpected, JSON.toJSONString(holder, natural));
        assertEquals(sortedExpected, JSON.toJSONString(holder, sorted));
        assertEquals(naturalExpected, JSON.toJSONString(holder, natural));

        // sorted first, then natural, then sorted again
        SharedHolder holder2 = new SharedHolder();
        assertEquals(sortedExpected, JSON.toJSONString(holder2, sorted));
        assertEquals(naturalExpected, JSON.toJSONString(holder2, natural));
        assertEquals(sortedExpected, JSON.toJSONString(holder2, sorted));
    }

    public static class DualCreds {
        public String password = "hunter2";
        public String name = "alice";
    }

    @Test
    public void filterUpdatesReachEveryRegisteredVariant() {
        ValueFilter mask = (object, name, value) -> "password".equals(name) ? "***" : value;
        String masked = "{\"name\":\"alice\",\"password\":\"***\"}";

        ObjectWriter writer = ObjectWriters.objectWriter(DualCreds.class,
                ObjectWriters.fieldWriter("password", String.class, (DualCreds s) -> s.password),
                ObjectWriters.fieldWriter("name", String.class, (DualCreds s) -> s.name));
        ObjectWriterProvider p1 = new ObjectWriterProvider();
        ObjectWriterProvider p2 = new ObjectWriterProvider();
        p1.register(DualCreds.class, writer);
        p2.register(DualCreds.class, writer);

        // updates made after both registrations must reach every live variant
        writer.setValueFilter(mask);
        assertEquals(masked, JSON.toJSONString(new DualCreds(),
                new JSONWriter.Context(p1, JSONWriter.Feature.SortFieldNamesAlphabetically)));
        assertEquals(masked, JSON.toJSONString(new DualCreds(),
                new JSONWriter.Context(p2, JSONWriter.Feature.SortFieldNamesAlphabetically)));

        // one provider, both cache kinds
        ObjectWriter writer2 = ObjectWriters.objectWriter(DualCreds.class,
                ObjectWriters.fieldWriter("password", String.class, (DualCreds s) -> s.password),
                ObjectWriters.fieldWriter("name", String.class, (DualCreds s) -> s.name));
        ObjectWriterProvider p3 = new ObjectWriterProvider();
        p3.register(DualCreds.class, writer2, false);
        p3.register(DualCreds.class, writer2, true);
        writer2.setValueFilter(mask);
        assertEquals(masked, JSON.toJSONString(new DualCreds(),
                new JSONWriter.Context(p3, JSONWriter.Feature.SortFieldNamesAlphabetically)));
        assertEquals(masked, JSON.toJSONString(new DualCreds(),
                new JSONWriter.Context(p3,
                        JSONWriter.Feature.FieldBased, JSONWriter.Feature.SortFieldNamesAlphabetically)));
    }

    @Test
    public void filtersVisibleOnConcurrentFirstSortedWrites() throws Exception {
        // the sorted writer must be fully initialized with the natural variant's filters
        // before any thread can take it from the cache
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            for (int round = 0; round < 20; round++) {
                ObjectWriterProvider provider = new ObjectWriterProvider();
                provider.getObjectWriter(Filtered.class).setFilter(
                        (ValueFilter) (object, name, value) -> "password".equals(name) ? "***" : value);
                JSONWriter.Context sorted = new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically);

                java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
                java.util.concurrent.Future<String> first = pool.submit(() -> {
                    start.await();
                    return JSON.toJSONString(new Filtered(), sorted);
                });
                java.util.concurrent.Future<String> second = pool.submit(() -> {
                    start.await();
                    return JSON.toJSONString(new Filtered(), sorted);
                });
                start.countDown();
                assertEquals("{\"id\":7,\"name\":\"alice\",\"password\":\"***\"}",
                        first.get(30, java.util.concurrent.TimeUnit.SECONDS));
                assertEquals("{\"id\":7,\"name\":\"alice\",\"password\":\"***\"}",
                        second.get(30, java.util.concurrent.TimeUnit.SECONDS));
            }
        } finally {
            pool.shutdownNow();
        }
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

    public static class Mixed {
        private String secret = "S";

        public String getZulu() {
            return "z";
        }

        public String getApple() {
            return "a";
        }
    }

    public static class Mixed$$EnhancerBySpringCGLIB$$1
            extends Mixed {
    }

    @Test
    public void fieldBasedSortedProxyKeepsFields() {
        // a proxy degrades to method-based discovery on both axes, so the natural and sorted
        // cells never disagree on the property set
        ObjectWriterProvider provider = new ObjectWriterProvider();
        Mixed proxy = new Mixed$$EnhancerBySpringCGLIB$$1();
        String fb = JSON.toJSONString(proxy, new JSONWriter.Context(provider, JSONWriter.Feature.FieldBased));
        String fbSorted = JSON.toJSONString(proxy, new JSONWriter.Context(provider,
                JSONWriter.Feature.FieldBased, JSONWriter.Feature.SortFieldNamesAlphabetically));
        assertEquals("{\"apple\":\"a\",\"zulu\":\"z\"}", fb);
        assertEquals(fb, fbSorted);
    }

    @Test
    public void fieldBasedSortedProxyWritesSortedFields() {
        // cold provider, no warm-up: the sorted cell is built from the same degraded discovery
        // as the natural cell, so a getterless proxy target answers the same empty property set
        // on both axes instead of leaking its declared fields into one of them
        ObjectWriterProvider provider = new ObjectWriterProvider();
        Inner proxy = new Inner$$EnhancerBySpringCGLIB$$abcdef();
        String fb = JSON.toJSONString(proxy, new JSONWriter.Context(provider, JSONWriter.Feature.FieldBased));
        String fbSorted = JSON.toJSONString(proxy, new JSONWriter.Context(provider,
                JSONWriter.Feature.FieldBased, JSONWriter.Feature.SortFieldNamesAlphabetically));
        assertEquals("{}", fb);
        assertEquals("{}", fbSorted);
    }

    public static class Filtered {
        public long id = 7;
        public String password = "hunter2";
        public String name = "alice";
    }

    @Test
    public void counterpartAndFilterAfterInitialLookupStillMaskLaterHits() throws Exception {
        // sorted created with the natural cell still empty; natural writer and its filter arrive
        // only during/after the sorted publication — every later hit must still be masked
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.getObjectWriter(Filtered.class, Filtered.class, JSONWriter.Feature.SortFieldNamesAlphabetically.mask);
        provider.getObjectWriter(Filtered.class).setFilter(
                (ValueFilter) (object, name, value) -> "password".equals(name) ? "***" : value);

        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(3);
        try {
            java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.Future<String> first = pool.submit(() -> {
                start.await();
                return JSON.toJSONString(new Filtered(),
                        new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically));
            });
            java.util.concurrent.Future<String> second = pool.submit(() -> {
                start.await();
                return JSON.toJSONString(new Filtered(),
                        new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically));
            });
            java.util.concurrent.Future<?> third = pool.submit(() -> {
                start.await();
                provider.getObjectWriter(Filtered.class).setFilter(
                        (ValueFilter) (object, name, value) -> "password".equals(name) ? "***" : value);
                return null;
            });
            start.countDown();
            third.get(30, java.util.concurrent.TimeUnit.SECONDS);
            assertEquals("{\"id\":7,\"name\":\"alice\",\"password\":\"***\"}",
                    first.get(30, java.util.concurrent.TimeUnit.SECONDS));
            assertEquals("{\"id\":7,\"name\":\"alice\",\"password\":\"***\"}",
                    second.get(30, java.util.concurrent.TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }
    }

    public static class FilteredWarm {
        public long id = 7;
        public String password = "hunter2";
        public String name = "alice";
    }

    public static class FilteredRegistered {
        public long id = 7;
        public String password = "hunter2";
        public String name = "alice";
    }

    @Test
    public void typeFiltersApplyToSortedVariant() {
        ValueFilter mask = (object, name, value) -> "password".equals(name) ? "***" : value;
        PropertyFilter drop = (object, name, value) -> !"password".equals(name);

        // a filter set on the writer the provider hands out (what JSON.register(Class, Filter) does)
        ObjectWriterProvider provider = new ObjectWriterProvider();
        JSONWriter.Context sorted = new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically);
        provider.getObjectWriter(Filtered.class).setFilter(mask);
        assertEquals("{\"id\":7,\"name\":\"alice\",\"password\":\"***\"}", JSON.toJSONString(new Filtered(), sorted));

        // the sorted variant already exists when the filter is set
        assertTrue(JSON.toJSONString(new FilteredWarm(), sorted).contains("hunter2"));
        provider.getObjectWriter(FilteredWarm.class).setFilter(drop);
        assertEquals("{\"id\":7,\"name\":\"alice\"}", JSON.toJSONString(new FilteredWarm(), sorted));

        // a registered adapter keeps its filters in the variant rebuilt for the feature, also ones set later
        ObjectWriter writer = ObjectWriters.objectWriter(FilteredRegistered.class,
                ObjectWriters.fieldWriter("password", String.class, (FilteredRegistered s) -> s.password),
                ObjectWriters.fieldWriter("name", String.class, (FilteredRegistered s) -> s.name));
        writer.setFilter(mask);
        provider.register(FilteredRegistered.class, writer);
        assertEquals("{\"name\":\"alice\",\"password\":\"***\"}", JSON.toJSONString(new FilteredRegistered(), sorted));
        writer.setFilter(drop);
        assertEquals("{\"name\":\"alice\"}", JSON.toJSONString(new FilteredRegistered(), sorted));
    }

    @JSONType(alphabetic = false)
    public static class SharedArrayHolder {
        public int zulu = 9;
        public SharedChild[] items = {new SharedChild()};
    }

    public static class AlphabeticArrayHolder {
        public int apple;
        public SharedChild[] items = {new SharedChild()};
    }

    @Test
    public void registeredVariantsIsolateArrayItemCaches() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.register(SharedArrayHolder.class, ObjectWriterCreator.INSTANCE.createObjectWriter(SharedArrayHolder.class));
        JSONWriter.Context natural = new JSONWriter.Context(provider);
        JSONWriter.Context sorted = new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically);
        String naturalExpected = "{\"zulu\":9,\"items\":[{\"zebra\":3,\"apple\":1}]}";
        String sortedExpected = "{\"items\":[{\"apple\":1,\"zebra\":3}],\"zulu\":9}";
        assertEquals(sortedExpected, JSON.toJSONString(new SharedArrayHolder(), sorted));
        assertEquals(naturalExpected, JSON.toJSONString(new SharedArrayHolder(), natural));
        assertEquals(sortedExpected, JSON.toJSONString(new SharedArrayHolder(), sorted));
        assertEquals(naturalExpected, JSON.toJSONString(new SharedArrayHolder(), natural));

        // already alphabetical: both cells hold the registered writer itself; a sorted write must not change
        // what a later write without the feature produces
        ObjectWriterProvider provider2 = new ObjectWriterProvider();
        provider2.register(AlphabeticArrayHolder.class, ObjectWriterCreator.INSTANCE.createObjectWriter(AlphabeticArrayHolder.class));
        assertEquals("{\"apple\":0,\"items\":[{\"apple\":1,\"zebra\":3}]}", JSON.toJSONString(new AlphabeticArrayHolder(),
                new JSONWriter.Context(provider2, JSONWriter.Feature.SortFieldNamesAlphabetically)));
        assertEquals("{\"apple\":0,\"items\":[{\"zebra\":3,\"apple\":1}]}", JSON.toJSONString(new AlphabeticArrayHolder(),
                new JSONWriter.Context(provider2)));
    }

    public static class ChildArrayHolder {
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public Child[] children = {new Child()};
    }

    @Test
    public void beanToArrayAnnotatedArrayFieldKeepsPositionalOrder() {
        // reflective creator: the ASM creator writes array items as objects for this annotation; a context
        // creator left on the thread by another test would override the provider's creator
        ObjectWriterCreator contextCreator = JSONFactory.getContextWriterCreator();
        JSONFactory.setContextWriterCreator(null);
        try {
            ObjectWriterProvider provider = new ObjectWriterProvider(ObjectWriterCreator.INSTANCE);
            JSONWriter.Context sorted = new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically);
            assertEquals("{\"children\":[[3,1]]}", JSON.toJSONString(new ChildArrayHolder(), sorted));
            assertEquals("{\"children\":[[3,1]]}", JSON.toJSONString(new ChildArrayHolder(), new JSONWriter.Context(provider)));
            assertEquals("{\"children\":[[3,1]]}", JSON.toJSONString(new ChildArrayHolder(), sorted));
        } finally {
            JSONFactory.setContextWriterCreator(contextCreator);
        }
    }

    public static class Ledger
            extends java.util.HashMap<String, Object> {
    }

    static final class LedgerWriter
            implements ObjectWriter<Ledger> {
        static final LedgerWriter INSTANCE = new LedgerWriter();

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            jsonWriter.writeString("ledger");
        }
    }

    public static class LedgerHolder {
        public java.util.Map<String, Object> asMap = new Ledger();
        public Ledger asLedger = new Ledger();
        public Object asObject = new Ledger();
    }

    @Test
    public void registeredMapWriterHonoredForMapDeclaredFields() {
        // a writer registered for the runtime Map class applies whatever the field's declared type, as before;
        // only the untyped built-in map writer is skipped for Map-declared fields
        ObjectWriterCreator contextCreator = JSONFactory.getContextWriterCreator();
        JSONFactory.setContextWriterCreator(null);
        try {
            for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
                ObjectWriterProvider provider = new ObjectWriterProvider(creator);
                provider.register(Ledger.class, LedgerWriter.INSTANCE);
                String expected = "{\"asLedger\":\"ledger\",\"asMap\":\"ledger\",\"asObject\":\"ledger\"}";
                assertEquals(expected, JSON.toJSONString(new LedgerHolder(), new JSONWriter.Context(provider)));
                assertEquals(expected, JSON.toJSONString(new LedgerHolder(),
                        new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically)));
            }
        } finally {
            JSONFactory.setContextWriterCreator(contextCreator);
        }
    }

    @JSONType(alphabetic = false)
    public static class Natural {
        public int zeta = 1;
        public int alpha = 2;
    }

    @Test
    public void generatedAndModuleWritersGetSortedVariants() {
        // a registered writer built by the ASM creator, and a plain writer returned by a module, are rebuilt in
        // field-name order for a sorted context, as a registered writer built by the reflective creator already was
        ObjectWriterCreator contextCreator = JSONFactory.getContextWriterCreator();
        JSONFactory.setContextWriterCreator(null);
        try {
            ObjectWriterProvider registered = new ObjectWriterProvider();
            registered.register(Natural.class, ObjectWriterCreatorASM.INSTANCE.createObjectWriter(Natural.class));
            ObjectWriter moduleWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Natural.class);
            ObjectWriterProvider fromModule = new ObjectWriterProvider();
            fromModule.register(new com.alibaba.fastjson2.modules.ObjectWriterModule() {
                @Override
                public ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
                    return objectClass == Natural.class ? moduleWriter : null;
                }
            });
            for (ObjectWriterProvider provider : new ObjectWriterProvider[]{registered, fromModule}) {
                assertEquals("{\"zeta\":1,\"alpha\":2}", JSON.toJSONString(new Natural(), new JSONWriter.Context(provider)));
                assertEquals("{\"alpha\":2,\"zeta\":1}", JSON.toJSONString(new Natural(),
                        new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically)));
            }
        } finally {
            JSONFactory.setContextWriterCreator(contextCreator);
        }
    }

    public static class TypedMapHolder {
        public java.util.Map<String, Natural> entries = new java.util.LinkedHashMap<>(java.util.Collections.singletonMap("k", new Natural()));
    }

    @Test
    public void mapFieldKeepsTypedWriterAfterTheRuntimeMapCellIsWarm() {
        // Map-declared fields consult the cache for registered writers, but the built-in untyped map writer cached for
        // the runtime class is still skipped: it would drop the declared value type and add a per-entry @type
        ObjectWriterCreator contextCreator = JSONFactory.getContextWriterCreator();
        JSONFactory.setContextWriterCreator(null);
        try {
            ObjectWriterProvider provider = new ObjectWriterProvider(ObjectWriterCreatorASM.INSTANCE);
            JSON.toJSONString(new java.util.LinkedHashMap<>(), new JSONWriter.Context(provider));
            String json = JSON.toJSONString(new TypedMapHolder(), new JSONWriter.Context(provider, JSONWriter.Feature.WriteClassName));
            assertEquals(-1, json.indexOf(Natural.class.getName()), json);
        } finally {
            JSONFactory.setContextWriterCreator(contextCreator);
        }
    }

    @JSONType(alphabetic = false)
    public static class Position {
        public int zulu = 7;
        public int alpha = 1;
    }

    @JSONType(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
    public static class SortedPositions {
        public java.util.List<Position> items = java.util.Collections.singletonList(new Position());
    }

    @Test
    public void annotationSortedListItemsKeepPositionalOrderWarmOrCold() {
        // positional JSONB of a list field's items keeps the declared order whether or not the item's natural writer
        // was cached first: the generated list writer must not bake either order into a sorted creation
        ObjectWriterCreator contextCreator = JSONFactory.getContextWriterCreator();
        JSONFactory.setContextWriterCreator(null);
        try {
            for (boolean warm : new boolean[]{false, true}) {
                ObjectWriterProvider provider = new ObjectWriterProvider(ObjectWriterCreatorASM.INSTANCE);
                if (warm) {
                    JSON.toJSONString(new Position(), new JSONWriter.Context(provider));
                }
                byte[] jsonb = JSONB.toBytes(new SortedPositions(), new JSONWriter.Context(provider, JSONWriter.Feature.BeanToArray));
                assertEquals("[[[7,1]]]", JSON.toJSONString(JSONB.parse(jsonb)), warm ? "warm" : "cold");
            }
        } finally {
            JSONFactory.setContextWriterCreator(contextCreator);
        }
    }

    @JSONType(alphabetic = false, serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
    public static class TypeLevelSorted {
        public int zulu = 9;
        public int apple = 1;
    }

    @Test
    public void typeLevelSortAnnotationOrdersOwnFields() {
        // the type-level annotation is the only sort source available where the global switch is
        // off: its writerFeatures word must reach the creation-time sort gate with either creator
        ObjectWriterCreator contextCreator = JSONFactory.getContextWriterCreator();
        JSONFactory.setContextWriterCreator(null);
        try {
            for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
                ObjectWriterProvider provider = new ObjectWriterProvider(creator);
                assertEquals("{\"apple\":1,\"zulu\":9}",
                        JSON.toJSONString(new TypeLevelSorted(), new JSONWriter.Context(provider)),
                        creator.getClass().getSimpleName());
            }
        } finally {
            JSONFactory.setContextWriterCreator(contextCreator);
        }
    }

    @JSONType(alphabetic = false)
    public static class TreeInner {
        public int zeta = 1;
        public int alpha = 2;
    }

    @JSONType(alphabetic = false)
    public static class TreeOuter {
        public int zulu = 9;
        public TreeInner inner = new TreeInner();
    }

    @Test
    public void sortedTreeConversionDoesNotReuseThePrimedNaturalFieldWriter() {
        // the variants of a registered adapter share FieldWriter instances; a natural write primes
        // the shared memo with the natural writer, and a sorted tree conversion afterwards must
        // still resolve the sorted variant for the nested bean instead of trusting the memo
        ObjectWriterCreator contextCreator = JSONFactory.getContextWriterCreator();
        JSONFactory.setContextWriterCreator(null);
        try {
            for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
                ObjectWriterProvider factory = new ObjectWriterProvider(creator);
                JSON.register(TreeOuter.class, factory.getObjectWriter(TreeOuter.class));
                try {
                    TreeOuter outer = new TreeOuter();
                    JSON.toJSONString(outer);
                    com.alibaba.fastjson2.JSONObject tree = com.alibaba.fastjson2.JSONObject.from(outer,
                            JSONWriter.Feature.SortFieldNamesAlphabetically);
                    assertEquals(new java.util.ArrayList<>(java.util.Arrays.asList("inner", "zulu")),
                            new java.util.ArrayList<>(tree.keySet()), creator.getClass().getSimpleName());
                    com.alibaba.fastjson2.JSONObject nested = tree.getJSONObject("inner");
                    assertEquals(new java.util.ArrayList<>(java.util.Arrays.asList("alpha", "zeta")),
                            new java.util.ArrayList<>(nested.keySet()), creator.getClass().getSimpleName());
                } finally {
                    JSONFactory.getDefaultObjectWriterProvider().unregister(TreeOuter.class);
                }
            }
        } finally {
            JSONFactory.setContextWriterCreator(contextCreator);
        }
    }
}
