package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@Tag("autotype")
public class AutoTypeValidationTest {
    static final String PACKAGE_PREFIX = "com.alibaba.fastjson2.autoType.";
    static final String TEST_BEAN = "com.alibaba.fastjson2.autoType.AutoTypeValidationTest$TestBean";
    static final String TEST_CLASS_LOADER = "com.alibaba.fastjson2.autoType.AutoTypeValidationTest$TestClassLoader";
    static final String UNAUTHORIZED_TYPE = "com.example.Unauthorized";
    static final String TEST_DATA_SOURCE = "com.alibaba.fastjson2.autoType.AutoTypeValidationTest$TestDataSource";

    // =========================================================================
    // type name format validation
    // =========================================================================

    /**
     * Unresolvable rather than an error, matching a type name that simply fails to load. Whether the
     * caller sees an exception is left to {@code ErrorOnNotSupportAutoType}.
     */
    @Test
    public void testRejectColonInTypeName() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        assertNull(provider.checkAutoType("com.example.Bean:invalid", null, 0));
    }

    @Test
    public void testRejectExclamationInTypeName() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        assertNull(provider.checkAutoType("com.example.Bean!invalid", null, 0));
    }

    @Test
    public void testRejectColonWithSupportAutoType() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        long features = JSONReader.Feature.SupportAutoType.mask;
        assertNull(provider.checkAutoType("com.example.Bean:invalid", null, features));
    }

    /**
     * {@code @type} is a JSON-LD keyword whose value is an IRI, so it always carries {@code :}.
     * Rejecting the type name must leave the existing fall-back-to-map behaviour intact.
     */
    @Test
    public void testParseJsonLdFallsBackToMap() {
        String jsonLd = "{\"@type\":\"http://schema.org/Person\",\"name\":\"x\"}";

        Object object = JSON.parseObject(jsonLd, Object.class);
        assertInstanceOf(Map.class, object);
        assertEquals("x", ((Map<?, ?>) object).get("name"));

        assertInstanceOf(Map.class, JSON.parseArray("[" + jsonLd + "]", Object.class).get(0));

        assertThrows(JSONException.class, () -> JSON.parseObject(
                jsonLd, Object.class, JSONReader.Feature.ErrorOnNotSupportAutoType));
    }

    /**
     * A type name carrying {@code :} or {@code !} must not reach the class loader at all: a nested
     * jar URL such as {@code jar:http://host/x.jar!/} is resolvable by some class loaders and would
     * turn a type name into a remote class load. Uses a class loader that resolves any name, so the
     * assertion fails if the format check is removed.
     */
    @Test
    public void testLoadClassDoesNotReachClassLoader() {
        AtomicReference<String> requested = new AtomicReference<>();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader recording = new ClassLoader(contextClassLoader) {
            @Override
            public Class<?> loadClass(String name) {
                requested.set(name);
                return Integer.class;
            }
        };

        try {
            Thread.currentThread().setContextClassLoader(recording);

            assertNull(TypeUtils.loadClass("jar:http://127.0.0.1/evil.jar!/Evil"));
            assertNull(TypeUtils.loadClass("com.example.Bean:invalid"));
            assertNull(TypeUtils.loadClass("com.example.Bean!invalid"));
            assertNull(requested.get(), "type name must not reach the class loader");

            // control: an ordinary type name does reach the class loader
            assertEquals(Integer.class, TypeUtils.loadClass("com.example.Bean"));
            assertEquals("com.example.Bean", requested.get());
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Test
    public void testLoadClassNormalClass() {
        assertEquals(String.class, TypeUtils.loadClass("java.lang.String"));
    }

    @Test
    public void testContextHandlerRejectsColon() {
        ContextAutoTypeBeforeHandler handler = new ContextAutoTypeBeforeHandler(true);
        assertNull(handler.apply("com.example.Bean:invalid", null, 0));
    }

    @Test
    public void testContextHandlerRejectsExclamation() {
        ContextAutoTypeBeforeHandler handler = new ContextAutoTypeBeforeHandler(true);
        assertNull(handler.apply("com.example.Bean!invalid", null, 0));
    }

    @Test
    public void testContextHandlerAcceptsWhitelisted() {
        ContextAutoTypeBeforeHandler handler = new ContextAutoTypeBeforeHandler(String.class);
        assertEquals(String.class, handler.apply("java.lang.String", null, 0));
    }

    @Test
    public void testContextHandlerRejectsNonWhitelisted() {
        ContextAutoTypeBeforeHandler handler = new ContextAutoTypeBeforeHandler(String.class);
        assertNull(handler.apply("com.example.NotWhitelisted", null, 0));
    }

    // =========================================================================
    // whitelist text verification
    // =========================================================================

    /**
     * Without {@code SupportAutoType} the whitelist is the only way a type name resolves, so these
     * assertions can only pass through the rolling-hash accept path.
     */
    @Test
    public void testWhitelistPrefixAccept() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        assertNull(provider.checkAutoType(TEST_BEAN, null, 0));

        provider.addAutoTypeAccept(PACKAGE_PREFIX);
        assertEquals(TestBean.class, provider.checkAutoType(TEST_BEAN, null, 0));
    }

    /**
     * The rolling hash in {@code checkAutoType} normalizes {@code $} to {@code .}, so an accept
     * entry written with the binary name of a nested class has to be normalized the same way to
     * match.
     */
    @Test
    public void testWhitelistAcceptNestedClassBinaryName() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.addAutoTypeAccept(TEST_BEAN);
        assertEquals(TestBean.class, provider.checkAutoType(TEST_BEAN, null, 0));

        ObjectReaderProvider canonical = new ObjectReaderProvider();
        canonical.addAutoTypeAccept(TEST_BEAN.replace('$', '.'));
        assertEquals(TestBean.class, canonical.checkAutoType(TEST_BEAN, null, 0));
    }

    /**
     * A hash match alone must not whitelist a type name. Injects the hash of a {@code java.lang.}
     * prefix into {@code acceptHashCodes} without registering the text, which is what a rolling-hash
     * collision would look like, and asserts the type name is still rejected.
     */
    @Test
    public void testWhitelistHashMatchWithoutTextRejected() throws Exception {
        ObjectReaderProvider provider = new ObjectReaderProvider();

        Field field = ObjectReaderProvider.class.getDeclaredField("acceptHashCodes");
        field.setAccessible(true);
        long[] hashCodes = (long[]) field.get(provider);
        long[] injected = Arrays.copyOf(hashCodes, hashCodes.length + 1);
        injected[injected.length - 1] = Fnv.hashCode64("java.lang.");
        Arrays.sort(injected);
        field.set(provider, injected);

        assertNull(provider.checkAutoType("java.lang.Integer", null, 0));

        // control: the same lookup succeeds once the accept text is registered
        provider.addAutoTypeAccept("java.lang.");
        assertEquals(Integer.class, provider.checkAutoType("java.lang.Integer", null, 0));
    }

    /**
     * A collision on the full type name is the dangerous case: matching an accept entry in full is
     * what exempts a type from the gadget blacklist, so it must be backed by the accept text.
     */
    @Test
    public void testWhitelistFullNameHashMatchWithoutTextRejected() throws Exception {
        ObjectReaderProvider provider = new ObjectReaderProvider();

        Field field = ObjectReaderProvider.class.getDeclaredField("acceptHashCodes");
        field.setAccessible(true);
        long[] hashCodes = (long[]) field.get(provider);
        long[] injected = Arrays.copyOf(hashCodes, hashCodes.length + 1);
        injected[injected.length - 1] = Fnv.hashCode64(TEST_CLASS_LOADER.replace('$', '.'));
        Arrays.sort(injected);
        field.set(provider, injected);

        assertNull(provider.checkAutoType(TEST_CLASS_LOADER, null, 0));
        assertThrows(JSONException.class, () ->
                provider.checkAutoType(TEST_CLASS_LOADER, null, JSONReader.Feature.SupportAutoType.mask));
    }

    @Test
    public void testContextHandlerHashMatchWithoutTextRejected() throws Exception {
        ContextAutoTypeBeforeHandler handler = new ContextAutoTypeBeforeHandler("com.example.");

        Field field = ContextAutoTypeBeforeHandler.class.getDeclaredField("acceptHashCodes");
        field.setAccessible(true);
        long[] hashCodes = (long[]) field.get(handler);
        assertEquals(1, hashCodes.length);
        hashCodes[0] = Fnv.hashCode64(TEST_CLASS_LOADER.replace('$', '.'));

        assertNull(handler.apply(TEST_CLASS_LOADER, null, 0));
    }

    @Test
    public void testWhitelistNonAcceptedPrefix() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.addAutoTypeAccept("com.example.");

        long features = JSONReader.Feature.SupportAutoType.mask;
        assertNull(provider.checkAutoType("com.other.NotAccepted", null, features));
    }

    /**
     * {@code com.alibaba.fastjson.util.AntiCollisionHashMap} is always accepted through a hardcoded
     * hash. Text verification would silently kill that entry if its name were missing from
     * {@code acceptNameSet}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testAntiCollisionHashMapAlwaysAccepted() throws Exception {
        String name = "com.alibaba.fastjson.util.AntiCollisionHashMap";
        assertEquals(-6293031534589903644L, Fnv.hashCode64(name));

        ObjectReaderProvider provider = new ObjectReaderProvider();
        Field field = ObjectReaderProvider.class.getDeclaredField("acceptNameSet");
        field.setAccessible(true);
        assertTrue(((Set<String>) field.get(provider)).contains(name));
    }

    // =========================================================================
    // blacklist enforcement on whitelist matches
    // =========================================================================

    @Test
    public void testAcceptPrefixDoesNotAllowClassLoader() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.addAutoTypeAccept(PACKAGE_PREFIX);

        assertNull(provider.checkAutoType(TEST_CLASS_LOADER, null, 0));
        assertThrows(JSONException.class, () ->
                provider.checkAutoType(TEST_CLASS_LOADER, null, JSONReader.Feature.SupportAutoType.mask));

        // the accept prefix still resolves types that are not gadget base types
        assertEquals(TestBean.class, provider.checkAutoType(TEST_BEAN, null, 0));
    }

    /**
     * An accept entry naming the type in full is an explicit opt-in, and a shorter prefix entry
     * matching first must not preempt it.
     */
    @Test
    public void testExactAcceptNameAllowsClassLoader() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.addAutoTypeAccept(PACKAGE_PREFIX);
        provider.addAutoTypeAccept(TEST_CLASS_LOADER);

        assertEquals(TestClassLoader.class, provider.checkAutoType(TEST_CLASS_LOADER, null, 0));
        assertEquals(TestClassLoader.class, provider.checkAutoType(
                TEST_CLASS_LOADER, null, JSONReader.Feature.SupportAutoType.mask));
    }

    @Test
    public void testAcceptPrefixDoesNotAllowDataSource() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.addAutoTypeAccept(PACKAGE_PREFIX);

        assertNull(provider.checkAutoType(TEST_DATA_SOURCE, null, 0));
        assertThrows(JSONException.class, () ->
                provider.checkAutoType(TEST_DATA_SOURCE, null, JSONReader.Feature.SupportAutoType.mask));
    }

    @Test
    public void testContextHandlerAcceptPrefixDoesNotAllowClassLoader() {
        ContextAutoTypeBeforeHandler handler = new ContextAutoTypeBeforeHandler(PACKAGE_PREFIX);
        assertNull(handler.apply(TEST_CLASS_LOADER, null, 0));
        assertEquals(TestBean.class, handler.apply(TEST_BEAN, null, 0));
    }

    @Test
    public void testContextHandlerAcceptPrefixDoesNotAllowDataSource() {
        ContextAutoTypeBeforeHandler handler = new ContextAutoTypeBeforeHandler(PACKAGE_PREFIX);
        assertNull(handler.apply(TEST_DATA_SOURCE, null, 0));
    }

    /**
     * The blacklist runs after the class cache is consulted, so a cached class is checked too rather
     * than the check resting on nothing but a deny class never being cached.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testContextHandlerChecksCachedClass() throws Exception {
        ContextAutoTypeBeforeHandler handler = new ContextAutoTypeBeforeHandler(PACKAGE_PREFIX);

        Field field = ContextAutoTypeBeforeHandler.class.getDeclaredField("classCache");
        field.setAccessible(true);
        ((Map<Long, Class>) field.get(handler))
                .put(Fnv.hashCode64(TEST_CLASS_LOADER), TestClassLoader.class);

        assertNull(handler.apply(TEST_CLASS_LOADER, null, 0));
    }

    @Test
    public void testContextHandlerExactAcceptNameAllowsClassLoader() {
        ContextAutoTypeBeforeHandler handler = new ContextAutoTypeBeforeHandler(
                PACKAGE_PREFIX,
                TEST_CLASS_LOADER
        );
        assertEquals(TestClassLoader.class, handler.apply(TEST_CLASS_LOADER, null, 0));
    }

    /**
     * An unresolved {@code @type} falls back to a map unless {@code ErrorOnNotSupportAutoType} is
     * set, so what matters end to end is that no {@link ClassLoader} is ever instantiated.
     */
    @Test
    public void testParseRejectsClassLoaderUnderAcceptPrefix() {
        Object object = JSON.parseObject(
                "{\"@type\":\"" + TEST_CLASS_LOADER + "\"}",
                Object.class,
                JSONReader.autoTypeFilter(PACKAGE_PREFIX));
        assertFalse(object instanceof ClassLoader, "accept prefix must not resolve a ClassLoader");

        // control: the same accept prefix still deserializes an ordinary bean
        TestBean bean = (TestBean) JSON.parseObject(
                "{\"@type\":\"" + TEST_BEAN + "\",\"id\":123}",
                Object.class,
                JSONReader.autoTypeFilter(PACKAGE_PREFIX));
        assertEquals(123, bean.id);
    }

    @Test
    public void testParseWithAutoTypeFilterNormal() {
        String json = "{\"@type\":\"java.util.HashMap\",\"value\":1}";
        Object obj = JSON.parseObject(json, Object.class,
                JSONReader.autoTypeFilter("java.util.HashMap"));
        // not merely non-null, falling back to a JSONObject would satisfy that too
        assertEquals(HashMap.class, obj.getClass());
    }

    @Test
    public void testParseHashCacheDoesNotBypassTypeNameValidation() {
        String typeName = "jar:http://127.0.0.1/evil.jar!/Evil";
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.registerIfAbsent(
                Fnv.hashCode64(typeName),
                provider.getObjectReader(TestBean.class)
        );

        AtomicReference<String> requested = new AtomicReference<>();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader recording = new ClassLoader(contextClassLoader) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                requested.set(name);
                return super.loadClass(name);
            }
        };

        try {
            Thread.currentThread().setContextClassLoader(recording);
            Object object = JSON.parseObject(
                    "{\"@type\":\"" + typeName + "\",\"id\":123}",
                    Object.class,
                    JSONFactory.createReadContext(provider, JSONReader.Feature.SupportAutoType)
            );
            assertInstanceOf(Map.class, object);
            assertNull(requested.get(), "invalid type name must not reach the class loader");
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Test
    public void testParseHashCacheDoesNotBypassDenyClassCheck() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.registerIfAbsent(
                Fnv.hashCode64(TEST_CLASS_LOADER),
                provider.getObjectReader(TestClassLoader.class)
        );

        JSONReader.Context context = JSONFactory.createReadContext(
                provider,
                JSONReader.Feature.SupportAutoType
        );
        assertThrows(JSONException.class, () -> JSON.parseObject(
                "{\"@type\":\"" + TEST_CLASS_LOADER + "\"}",
                Object.class,
                context
        ));
    }

    @Test
    public void testParseHashCacheRequiresMatchingTypeName() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.registerIfAbsent(
                Fnv.hashCode64(TEST_BEAN),
                provider.getObjectReader(String.class)
        );

        TestBean bean = (TestBean) JSON.parseObject(
                "{\"@type\":\"" + TEST_BEAN + "\",\"id\":123}",
                Object.class,
                JSONFactory.createReadContext(provider, JSONReader.Feature.SupportAutoType)
        );
        assertEquals(123, bean.id);
    }

    @Test
    public void testParseJSONBHashCacheRequiresMatchingTypeName() {
        TestBean bean = new TestBean();
        bean.id = 123;
        byte[] jsonb = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);

        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.registerIfAbsent(
                Fnv.hashCode64(TEST_BEAN),
                provider.getObjectReader(String.class)
        );

        try (JSONReader jsonReader = JSONReader.ofJSONB(
                jsonb,
                JSONFactory.createReadContext(provider, JSONReader.Feature.SupportAutoType)
        )) {
            TestBean parsed = (TestBean) jsonReader.readAny();
            assertEquals(123, parsed.id);
        }
    }

    @Test
    public void testConcreteBeanHashCacheDoesNotAuthorizeReader() {
        SideEffect.created = false;
        ObjectReaderProvider provider = registerSideEffect(UNAUTHORIZED_TYPE);

        parseTextIgnoreError(UNAUTHORIZED_TYPE, OneFieldBean.class, provider);
        assertFalse(SideEffect.created, "concrete bean hash cache hit must not authorize reader");
    }

    @Test
    public void testInterfaceHashCacheDoesNotAuthorizeReader() {
        SideEffect.created = false;
        ObjectReaderProvider provider = registerSideEffect(UNAUTHORIZED_TYPE);

        parseTextIgnoreError(UNAUTHORIZED_TYPE, TestInterface.class, provider);
        assertFalse(SideEffect.created, "interface hash cache hit must not authorize reader");
    }

    @Test
    public void testNoDefaultConstructorHashCacheDoesNotAuthorizeReader() {
        SideEffect.created = false;
        ObjectReaderProvider provider = registerSideEffect(UNAUTHORIZED_TYPE);

        parseTextIgnoreError(UNAUTHORIZED_TYPE, NoDefaultBean.class, provider);
        assertFalse(SideEffect.created, "no-default-constructor hash cache hit must not authorize reader");
    }

    @Test
    public void testExceptionHashCacheDoesNotAuthorizeReader() {
        SideEffect.created = false;
        ObjectReaderProvider provider = registerSideEffect(UNAUTHORIZED_TYPE);

        parseTextIgnoreError(UNAUTHORIZED_TYPE, TestException.class, provider);
        assertFalse(SideEffect.created, "exception hash cache hit must not authorize reader");
    }

    @Test
    public void testJSONBEmbeddedHashCacheDoesNotAuthorizeReader() {
        SideEffect.created = false;
        ObjectReaderProvider provider = registerSideEffect(UNAUTHORIZED_TYPE);
        byte[] jsonb = embeddedTypedJsonb(UNAUTHORIZED_TYPE);

        try {
            JSONB.parseObject(jsonb, TestInterface.class, JSONFactory.createReadContext(provider));
        } catch (JSONException ignored) {
        }
        assertFalse(SideEffect.created, "JSONB embedded @type hash cache hit must not authorize reader");
    }

    @Test
    public void testDenyPrefixOnlyRejectionIsDistinguishable() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        long features = JSONReader.Feature.SupportAutoType.mask;

        // no accept entry covers it, the generic rejection
        JSONException plain = assertThrows(JSONException.class, () ->
                provider.checkAutoType(TEST_CLASS_LOADER, null, features));
        assertFalse(plain.getMessage().contains("accept"));

        // an accept prefix covers it but not in full, the actionable rejection
        provider.addAutoTypeAccept(PACKAGE_PREFIX);
        JSONException prefixOnly = assertThrows(JSONException.class, () ->
                provider.checkAutoType(TEST_CLASS_LOADER, null, features));
        assertTrue(prefixOnly.getMessage().contains("add the type name in full to accept"));
    }

    static ObjectReaderProvider registerSideEffect(String typeName) {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.registerIfAbsent(
                Fnv.hashCode64(typeName),
                provider.getObjectReader(SideEffect.class)
        );
        return provider;
    }

    static void parseTextIgnoreError(String typeName, Class<?> expectClass, ObjectReaderProvider provider) {
        try {
            JSON.parseObject(
                    "{\"@type\":\"" + typeName + "\",\"id\":1}",
                    expectClass,
                    JSONFactory.createReadContext(provider, JSONReader.Feature.SupportAutoType)
            );
        } catch (JSONException ignored) {
        }
    }

    static byte[] embeddedTypedJsonb(String typeName) {
        byte[] typeNameField = JSONB.toBytes("@type");
        byte[] typeNameValue = JSONB.toBytes(typeName);
        byte[] idField = JSONB.toBytes("id");
        byte[] bytes = new byte[1 + typeNameField.length + typeNameValue.length + idField.length + 5 + 1];
        int off = 0;
        bytes[off++] = JSONB.Constants.BC_OBJECT;
        System.arraycopy(typeNameField, 0, bytes, off, typeNameField.length);
        off += typeNameField.length;
        System.arraycopy(typeNameValue, 0, bytes, off, typeNameValue.length);
        off += typeNameValue.length;
        System.arraycopy(idField, 0, bytes, off, idField.length);
        off += idField.length;
        off = JSONB.IO.writeInt32(bytes, off, 1);
        bytes[off++] = JSONB.Constants.BC_OBJECT_END;
        return Arrays.copyOf(bytes, off);
    }

    public interface TestInterface {
    }

    public static class TestException
            extends RuntimeException {
    }

    public static class SideEffect {
        static volatile boolean created;

        public SideEffect() {
            created = true;
        }
    }

    public static class OneFieldBean {
        public int id;
    }

    public static class NoDefaultBean {
        public final int id;

        public NoDefaultBean(int id) {
            this.id = id;
        }
    }

    public static class TestBean {
        public int id;
    }

    public static class TestClassLoader
            extends ClassLoader {
    }

    /**
     * Abstract is enough, {@code checkAutoType} resolves the class without instantiating it.
     */
    public abstract static class TestDataSource
            implements DataSource {
    }
}
