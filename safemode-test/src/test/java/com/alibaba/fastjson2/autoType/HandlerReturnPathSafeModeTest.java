package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * safeMode regression: a handler that resolves a ClassLoader or DataSource/RowSet subtype
 * must be rejected on the handler return path, matching the isAutoTypeDenyClass guard
 * already present on the whitelist and fallback branches after #7703. A handler that
 * resolves a business type must keep working, preserving Issue1503S.
 */
public class HandlerReturnPathSafeModeTest {
    static final String TEST_BEAN = "com.alibaba.fastjson2.autoType.HandlerReturnPathSafeModeTest$TestBean";
    static final String TEST_CLASS_LOADER = "com.alibaba.fastjson2.autoType.HandlerReturnPathSafeModeTest$TestClassLoader";
    static final String TEST_DATA_SOURCE = "com.alibaba.fastjson2.autoType.HandlerReturnPathSafeModeTest$TestDataSource";

    @Test
    public void testCheckAutoTypeHandlerRejectsClassLoader() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.setAutoTypeBeforeHandler((typeName, expectClass, features) -> {
            try {
                return Class.forName(typeName);
            } catch (ClassNotFoundException e) {
                return null;
            }
        });
        assertThrows(JSONException.class, () ->
                provider.checkAutoType(TEST_CLASS_LOADER, null, JSONReader.Feature.SupportAutoType.mask));
    }

    @Test
    public void testCheckAutoTypeHandlerRejectsDataSource() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.setAutoTypeBeforeHandler((typeName, expectClass, features) -> {
            try {
                return Class.forName(typeName);
            } catch (ClassNotFoundException e) {
                return null;
            }
        });
        assertThrows(JSONException.class, () ->
                provider.checkAutoType(TEST_DATA_SOURCE, null, JSONReader.Feature.SupportAutoType.mask));
    }

    @Test
    public void testParseHandlerRejectsClassLoader() {
        JSONReader.AutoTypeBeforeHandler handler = (typeName, expectClass, features) -> {
            try {
                return Class.forName(typeName);
            } catch (ClassNotFoundException e) {
                return null;
            }
        };
        assertThrows(JSONException.class, () ->
                JSON.parseObject("{\"@type\":\"" + TEST_CLASS_LOADER + "\"}", Object.class, handler));
    }

    @Test
    public void testParseHandlerRejectsDataSource() {
        JSONReader.AutoTypeBeforeHandler handler = (typeName, expectClass, features) -> {
            try {
                return Class.forName(typeName);
            } catch (ClassNotFoundException e) {
                return null;
            }
        };
        assertThrows(JSONException.class, () ->
                JSON.parseObject("{\"@type\":\"" + TEST_DATA_SOURCE + "\"}", Object.class, handler));
    }

    @Test
    public void testParseHandlerAcceptsBusinessType() {
        JSONReader.AutoTypeBeforeHandler handler = (typeName, expectClass, features) -> {
            try {
                return Class.forName(typeName);
            } catch (ClassNotFoundException e) {
                return null;
            }
        };
        TestBean bean = (TestBean) JSON.parseObject(
                "{\"@type\":\"" + TEST_BEAN + "\",\"id\":123}", Object.class, handler);
        assertEquals(123, bean.id);
    }

    public static class TestBean {
        public int id;
    }

    public static class TestClassLoader
            extends ClassLoader {
    }

    public abstract static class TestDataSource
            implements DataSource {
    }
}
