package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.SymbolTable;
import com.alibaba.fastjson2.filter.ExtraProcessor;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class Issue3654 {
    @BeforeEach
    void setUp() {
        JSONFactory.setContextReaderCreator(null);
        JSONFactory.setContextObjectReaderProvider(null);
        JSONFactory.setContextWriterCreator(null);
        JSONFactory.setContextJSONPathCompiler(null);
    }

    @AfterEach
    void tearDown() {
        JSONFactory.setContextReaderCreator(null);
        JSONFactory.setContextObjectReaderProvider(null);
        JSONFactory.setContextWriterCreator(null);
        JSONFactory.setContextJSONPathCompiler(null);
    }

    @Test
    void testConfGetProperty() {
        String property = JSONFactory.Conf.getProperty("nonexistent.property");
        assertNull(property);
    }

    @Test
    void testGetProperty() {
        String property = JSONFactory.getProperty("nonexistent.property");
        assertNull(property);
    }

    @Test
    void testUseJacksonAnnotation() {
        boolean original = JSONFactory.isUseJacksonAnnotation();

        JSONFactory.setUseJacksonAnnotation(true);
        assertTrue(JSONFactory.isUseJacksonAnnotation());

        JSONFactory.setUseJacksonAnnotation(false);
        assertFalse(JSONFactory.isUseJacksonAnnotation());

        JSONFactory.setUseJacksonAnnotation(original);
    }

    @Test
    void testUseGsonAnnotation() {
        boolean original = JSONFactory.isUseGsonAnnotation();

        JSONFactory.setUseGsonAnnotation(true);
        assertTrue(JSONFactory.isUseGsonAnnotation());

        JSONFactory.setUseGsonAnnotation(false);
        assertFalse(JSONFactory.isUseGsonAnnotation());

        JSONFactory.setUseGsonAnnotation(original);
    }

    @Test
    void testJSONFieldDefaultValueCompatMode() {
        boolean original = JSONFactory.isJSONFieldDefaultValueCompatMode();

        JSONFactory.setJSONFieldDefaultValueCompatMode(true);
        assertTrue(JSONFactory.isJSONFieldDefaultValueCompatMode());

        JSONFactory.setJSONFieldDefaultValueCompatMode(false);
        assertFalse(JSONFactory.isJSONFieldDefaultValueCompatMode());

        JSONFactory.setJSONFieldDefaultValueCompatMode(original);
    }

    @Test
    void testDefaultObjectSupplier() {
        @SuppressWarnings("rawtypes")
        Supplier<Map> originalSupplier = JSONFactory.getDefaultObjectSupplier();

        @SuppressWarnings("rawtypes")
        Supplier<Map> testSupplier = HashMap::new;
        JSONFactory.setDefaultObjectSupplier(testSupplier);
        assertEquals(testSupplier, JSONFactory.getDefaultObjectSupplier());

        JSONFactory.setDefaultObjectSupplier(originalSupplier);
    }

    @Test
    void testDefaultArraySupplier() {
        @SuppressWarnings("rawtypes")
        Supplier<List> originalSupplier = JSONFactory.getDefaultArraySupplier();

        @SuppressWarnings("rawtypes")
        Supplier<List> testSupplier = ArrayList::new;
        JSONFactory.setDefaultArraySupplier(testSupplier);
        assertEquals(testSupplier, JSONFactory.getDefaultArraySupplier());

        JSONFactory.setDefaultArraySupplier(originalSupplier);
    }

    @Test
    void testCreateWriteContext() {
        JSONWriter.Context context = JSONFactory.createWriteContext();
        assertNotNull(context);

        JSONWriter.Context contextWithFeatures = JSONFactory.createWriteContext(JSONWriter.Feature.WriteNulls);
        assertNotNull(contextWithFeatures);

        ObjectWriterProvider provider = new ObjectWriterProvider();
        JSONWriter.Context contextWithProvider = JSONFactory.createWriteContext(provider, JSONWriter.Feature.WriteNulls);
        assertNotNull(contextWithProvider);
    }

    @Test
    void testCreateReadContext() {
        JSONReader.Context context = JSONFactory.createReadContext();
        assertNotNull(context);

        JSONReader.Context contextWithFeatures = JSONFactory.createReadContext(JSONReader.Feature.FieldBased);
        assertNotNull(contextWithFeatures);

        JSONReader.Context contextWithLongFeatures = JSONFactory.createReadContext(JSONReader.Feature.FieldBased.mask);
        assertNotNull(contextWithLongFeatures);

        ObjectReaderProvider provider = new ObjectReaderProvider();
        JSONReader.Context contextWithProvider = JSONFactory.createReadContext(provider, JSONReader.Feature.FieldBased);
        assertNotNull(contextWithProvider);
    }

    @Test
    void testCreateReadContextWithFilter() {
        ExtraProcessor filter = new ExtraProcessor() {
            @Override
            public void processExtra(Object object, String key, Object value) {
            }
        };

        JSONReader.Context context = JSONFactory.createReadContext(filter, JSONReader.Feature.FieldBased);
        assertNotNull(context);
    }

    @Test
    void testCreateReadContextWithSymbolTable() {
        SymbolTable symbolTable = new SymbolTable(new String[0]);
        JSONReader.Context context = JSONFactory.createReadContext(symbolTable);
        assertNotNull(context);

        JSONReader.Context contextWithFeatures = JSONFactory.createReadContext(symbolTable, JSONReader.Feature.FieldBased);
        assertNotNull(contextWithFeatures);
    }

    @Test
    void testCreateReadContextWithSuppliers() {
        @SuppressWarnings("rawtypes")
        Supplier<Map> objectSupplier = HashMap::new;
        JSONReader.Context context = JSONFactory.createReadContext(objectSupplier, JSONReader.Feature.FieldBased);
        assertNotNull(context);

        @SuppressWarnings("rawtypes")
        Supplier<List> arraySupplier = ArrayList::new;
        JSONReader.Context contextWithBoth = JSONFactory.createReadContext(objectSupplier, arraySupplier, JSONReader.Feature.FieldBased);
        assertNotNull(contextWithBoth);
    }

    @Test
    void testGetObjectReader() {
        Type type = String.class;
        @SuppressWarnings("rawtypes")
        ObjectReader reader = JSONFactory.getObjectReader(type, 0L);
        assertNotNull(reader);
    }

    @Test
    void testGetObjectWriter() {
        Type type = String.class;
        @SuppressWarnings("rawtypes")
        ObjectWriter writer = JSONFactory.getObjectWriter(type, 0L);
        assertNotNull(writer);
    }

    @Test
    void testGetDefaultProviders() {
        ObjectWriterProvider writerProvider = JSONFactory.getDefaultObjectWriterProvider();
        assertNotNull(writerProvider);

        ObjectReaderProvider readerProvider = JSONFactory.getDefaultObjectReaderProvider();
        assertNotNull(readerProvider);

        JSONFactory.JSONPathCompiler compiler = JSONFactory.getDefaultJSONPathCompiler();
        assertNotNull(compiler);
    }

    @Test
    void testContextCreators() {
        ObjectReaderCreator readerCreator = new ObjectReaderCreator() {
        };
        JSONFactory.setContextReaderCreator(readerCreator);
        assertEquals(readerCreator, JSONFactory.getContextReaderCreator());

        ObjectReaderProvider readerProvider = new ObjectReaderProvider();
        JSONFactory.setContextObjectReaderProvider(readerProvider);
        assertEquals(readerProvider, JSONFactory.getDefaultObjectReaderProvider());

        ObjectWriterCreator writerCreator = new ObjectWriterCreator() {
        };
        JSONFactory.setContextWriterCreator(writerCreator);
        assertEquals(writerCreator, JSONFactory.getContextWriterCreator());

        JSONFactory.JSONPathCompiler pathCompiler = (objectClass, path) -> path;
        JSONFactory.setContextJSONPathCompiler(pathCompiler);
        assertEquals(pathCompiler, JSONFactory.getDefaultJSONPathCompiler());
    }

    @Test
    void testDefaultFeatures() {
        long readerFeatures = JSONFactory.getDefaultReaderFeatures();
        assertTrue(readerFeatures >= 0);

        long writerFeatures = JSONFactory.getDefaultWriterFeatures();
        assertTrue(writerFeatures >= 0);
    }

    @Test
    void testDefaultFormats() {
        String readerFormat = JSONFactory.getDefaultReaderFormat();
        String writerFormat = JSONFactory.getDefaultWriterFormat();
    }

    @Test
    void testDefaultWriterAlphabetic() {
        boolean original = JSONFactory.isDefaultWriterAlphabetic();

        JSONFactory.setDefaultWriterAlphabetic(true);
        assertTrue(JSONFactory.isDefaultWriterAlphabetic());

        JSONFactory.setDefaultWriterAlphabetic(false);
        assertFalse(JSONFactory.isDefaultWriterAlphabetic());

        JSONFactory.setDefaultWriterAlphabetic(original);
    }

    @Test
    void testDisableFlags() {
        boolean disableReferenceDetect = JSONFactory.isDisableReferenceDetect();
        boolean disableAutoType = JSONFactory.isDisableAutoType();
        boolean disableJSONB = JSONFactory.isDisableJSONB();
        boolean disableArrayMapping = JSONFactory.isDisableArrayMapping();
        boolean disableSmartMatch = JSONFactory.isDisableSmartMatch();

        assertNotNull(disableReferenceDetect);
        assertNotNull(disableAutoType);
        assertNotNull(disableJSONB);
        assertNotNull(disableArrayMapping);
        assertNotNull(disableSmartMatch);
    }

    @Test
    void testSetDisableFlags() {
        JSONFactory.setDisableReferenceDetect(true);
        JSONFactory.setDisableArrayMapping(true);
        JSONFactory.setDisableJSONB(true);
        JSONFactory.setDisableAutoType(true);
        JSONFactory.setDisableSmartMatch(true);
    }

    @Test
    void testConstants() {
        assertNotNull(JSONFactory.CREATOR);
        assertNotNull(JSONFactory.PROPERTY_DENY_PROPERTY);
        assertNotNull(JSONFactory.PROPERTY_AUTO_TYPE_ACCEPT);
        assertNotNull(JSONFactory.PROPERTY_AUTO_TYPE_HANDLER);
        assertNotNull(JSONFactory.PROPERTY_AUTO_TYPE_BEFORE_HANDLER);
    }
}
