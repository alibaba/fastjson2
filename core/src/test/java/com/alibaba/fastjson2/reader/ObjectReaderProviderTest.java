package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ObjectReaderProviderTest {
    private String savedDenyProp;

    @BeforeEach
    public void saveSystemProperty() {
        savedDenyProp = System.getProperty("fastjson2.parser.deny");
        System.clearProperty("fastjson2.parser.deny");
    }

    @AfterEach
    public void restoreSystemProperty() {
        if (savedDenyProp == null) {
            System.clearProperty("fastjson2.parser.deny");
        } else {
            System.setProperty("fastjson2.parser.deny", savedDenyProp);
        }
    }

    /**
     * Verifies PR-3: ObjectReaderProvider.addAutoTypeDeny(String) now actually adds the type
     * to the deny table (it was previously a no-op @Deprecated stub in fastjson 2.x). After
     * adding, checkAutoType must throw JSONException for that name.
     */
    @Test
    public void testAddAutoTypeDenyEnforced() {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        provider.addAutoTypeDeny("com.acme.Gadget");
        long features = JSONReader.Feature.SupportAutoType.mask;
        assertThrows(
                JSONException.class,
                () -> provider.checkAutoType("com.acme.Gadget", null, features)
        );
    }

    /**
     * Verifies PR-3: the $ → . rewrite is normalised before the deny check, so a class with
     * an inner-type FQCN ("com.acme.Gadget$Inner") denied by name also blocks the dot-form
     * ("com.acme.Gadget.Inner") from sneaking through the rolling-hash allow-list.
     */
    @Test
    public void testDenyNormalisationDollarToDot() {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        provider.addAutoTypeDeny("com.acme.Gadget$Inner");
        long features = JSONReader.Feature.SupportAutoType.mask;
        assertThrows(
                JSONException.class,
                () -> provider.checkAutoType("com.acme.Gadget.Inner", null, features)
        );
    }

    /**
     * Verifies PR-3: setting the fastjson2.parser.deny system property at JVM start actually
     * seeds the deny list (previously the property was honoured only by fastjson 1.x's
     * ParserConfig; the 2.x Provider silently ignored it).
     */
    @Test
    public void testSystemPropertyDenySeed() {
        System.setProperty("fastjson2.parser.deny", "com.acme.X,com.acme.Y");
        ObjectReaderProvider provider = new ObjectReaderProvider();
        long features = JSONReader.Feature.SupportAutoType.mask;
        assertThrows(
                JSONException.class,
                () -> provider.checkAutoType("com.acme.X", null, features)
        );
        assertThrows(
                JSONException.class,
                () -> provider.checkAutoType("com.acme.Y", null, features)
        );
    }
}