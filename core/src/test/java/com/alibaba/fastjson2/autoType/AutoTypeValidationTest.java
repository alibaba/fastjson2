package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.TypeUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("autotype")
public class AutoTypeValidationTest {
    @Test
    public void testRejectColonInTypeName() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        assertThrows(JSONException.class, () ->
                provider.checkAutoType("com.example.Bean:invalid", null, 0));
    }

    @Test
    public void testRejectExclamationInTypeName() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        assertThrows(JSONException.class, () ->
                provider.checkAutoType("com.example.Bean!invalid", null, 0));
    }

    @Test
    public void testRejectColonWithSupportAutoType() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        long features = JSONReader.Feature.SupportAutoType.mask;
        assertThrows(JSONException.class, () ->
                provider.checkAutoType("com.example.Bean:invalid", null, features));
    }

    @Test
    public void testLoadClassRejectsColon() {
        assertNull(TypeUtils.loadClass("com.example.Bean:invalid"));
    }

    @Test
    public void testLoadClassRejectsExclamation() {
        assertNull(TypeUtils.loadClass("com.example.Bean!invalid"));
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
        Class<?> clazz = handler.apply("java.lang.String", null, 0);
        assertEquals(String.class, clazz);
    }

    @Test
    public void testContextHandlerRejectsNonWhitelisted() {
        ContextAutoTypeBeforeHandler handler = new ContextAutoTypeBeforeHandler(String.class);
        assertNull(handler.apply("com.example.NotWhitelisted", null, 0));
    }

    @Test
    public void testWhitelistWithAddAccept() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.addAutoTypeAccept("com.alibaba.fastjson2.autoType.AutoTypeValidationTest$");

        long features = JSONReader.Feature.SupportAutoType.mask;
        Class<?> clazz = provider.checkAutoType(
                "com.alibaba.fastjson2.autoType.AutoTypeValidationTest$TestBean", null, features);
        assertEquals(TestBean.class, clazz);
    }

    @Test
    public void testWhitelistTextVerification() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.addAutoTypeAccept("com.example.");

        long features = JSONReader.Feature.SupportAutoType.mask;
        Class<?> clazz = provider.checkAutoType("com.other.NotAccepted", null, features);
        assertNull(clazz);
    }

    @Test
    public void testParseWithAutoTypeFilterRejectsColon() {
        String json = "{\"@type\":\"com.example.Bean:invalid\",\"value\":1}";
        assertThrows(JSONException.class, () ->
                JSON.parseObject(json, Object.class,
                        JSONReader.autoTypeFilter(String.class)));
    }

    @Test
    public void testParseWithAutoTypeFilterNormal() {
        String json = "{\"@type\":\"java.util.HashMap\",\"value\":1}";
        Object obj = JSON.parseObject(json, Object.class,
                JSONReader.autoTypeFilter("java.util.HashMap"));
        assertNotNull(obj);
    }

    public static class TestBean {
        public int id;
    }
}
