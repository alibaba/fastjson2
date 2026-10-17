package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSONFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("regression")
public class Issue7757 {
    // A fastjson2.properties value must be honored even when the matching -D
    // system property is absent. The helpers run only in the JSONFactory static
    // initializer, so they are exercised directly here for a deterministic result.
    // The property names are test-local on purpose: clearing a real production
    // key could race the JSONFactory class initialization and freeze the flags
    // with the user's -D overrides discarded; the helpers take the key as a
    // parameter, so the exercised code path is the same.

    private static boolean getPropertyBool(Properties properties, String name, boolean defaultValue) throws Exception {
        Method method = JSONFactory.class.getDeclaredMethod("getPropertyBool", Properties.class, String.class, boolean.class);
        method.setAccessible(true);
        return (boolean) method.invoke(null, properties, name, defaultValue);
    }

    private static int getPropertyInt(Properties properties, String name, int defaultValue) throws Exception {
        Method method = JSONFactory.class.getDeclaredMethod("getPropertyInt", Properties.class, String.class, int.class);
        method.setAccessible(true);
        return (int) method.invoke(null, properties, name, defaultValue);
    }

    private static String resolveProperty(Properties properties, String name) throws Exception {
        Method method = JSONFactory.class.getDeclaredMethod("resolveProperty", Properties.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, properties, name);
    }

    private static boolean featureEnabled(String features, String feature) throws Exception {
        Method method = JSONFactory.class.getDeclaredMethod("featureEnabled", String.class, String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(null, features, feature);
    }

    private static int validMaxLevel(int maxLevel) throws Exception {
        Method method = JSONFactory.class.getDeclaredMethod("validMaxLevel", int.class);
        method.setAccessible(true);
        return (int) method.invoke(null, maxLevel);
    }

    @Test
    public void booleanFromPropertiesFile() throws Exception {
        String name = "fastjson2.test.issue7757.bool";
        String previous = System.clearProperty(name);
        try {
            // System property absent: the properties file value is honored.
            Properties properties = new Properties();
            properties.setProperty(name, "false");
            assertFalse(getPropertyBool(properties, name, true));
            properties.setProperty(name, "true");
            assertTrue(getPropertyBool(properties, name, false));

            // Absent from both system property and properties file -> default.
            Properties empty = new Properties();
            assertTrue(getPropertyBool(empty, name, true));
            assertFalse(getPropertyBool(empty, name, false));

            // A padded properties file value is trimmed before parsing.
            properties.setProperty(name, " false ");
            assertFalse(getPropertyBool(properties, name, true));

            // A non-empty -D system property wins over the properties file.
            properties.setProperty(name, "false");
            System.setProperty(name, " true ");
            assertTrue(getPropertyBool(properties, name, false));

            // An empty/blank -D system property still falls back to the file.
            System.setProperty(name, " ");
            assertFalse(getPropertyBool(properties, name, true));

            // An unrecognized file value keeps the default.
            properties.setProperty(name, "not-a-boolean");
            assertFalse(getPropertyBool(properties, name, false));
            assertTrue(getPropertyBool(properties, name, true));

            // An empty or blank file entry counts as unset: the default is used.
            properties.setProperty(name, "");
            assertTrue(getPropertyBool(properties, name, true));
            properties.setProperty(name, " ");
            assertFalse(getPropertyBool(properties, name, false));

            // An unrecognized -D value keeps the default as well.
            System.setProperty(name, "not-a-boolean");
            assertFalse(getPropertyBool(properties, name, false));
        } finally {
            if (previous != null) {
                System.setProperty(name, previous);
            } else {
                System.clearProperty(name);
            }
        }
    }

    @Test
    public void intFromPropertiesFile() throws Exception {
        String name = "fastjson2.test.issue7757.int";
        String previous = System.clearProperty(name);
        try {
            // System property absent: the properties file value is honored.
            Properties properties = new Properties();
            properties.setProperty(name, "512");
            assertEquals(512, getPropertyInt(properties, name, 2048));

            // A padded properties file value is trimmed before parsing
            // (Properties.load keeps trailing whitespace).
            properties.setProperty(name, "512 ");
            assertEquals(512, getPropertyInt(properties, name, 2048));

            // Absent from both -> default.
            Properties empty = new Properties();
            assertEquals(2048, getPropertyInt(empty, name, 2048));

            // A non-empty -D system property wins over the properties file.
            System.setProperty(name, " 4096 ");
            assertEquals(4096, getPropertyInt(properties, name, 2048));

            // An empty/blank -D system property still falls back to the file.
            System.setProperty(name, " ");
            assertEquals(512, getPropertyInt(properties, name, 2048));

            // A malformed file value falls back to the default.
            properties.setProperty(name, "not-a-number");
            assertEquals(2048, getPropertyInt(properties, name, 2048));

            // An empty file entry counts as unset: the default is used.
            properties.setProperty(name, "");
            assertEquals(2048, getPropertyInt(properties, name, 2048));
        } finally {
            if (previous != null) {
                System.setProperty(name, previous);
            } else {
                System.clearProperty(name);
            }
        }
    }

    @Test
    public void featureListTokensAreTrimmed() throws Exception {
        // fastjson2.features tokens are trimmed, so a comma-space list enables
        // every named flag; null, empty, blank and unknown tokens enable nothing.
        String features = "disableSmartMatch, disableAutoType";
        assertTrue(featureEnabled(features, "disableSmartMatch"));
        assertTrue(featureEnabled(features, "disableAutoType"));
        assertFalse(featureEnabled(features, "disableReferenceDetect"));

        assertTrue(featureEnabled(" disableSmartMatch , unknown ", "disableSmartMatch"));
        assertFalse(featureEnabled(" disableSmartMatch , unknown ", "disableAutoType"));

        assertFalse(featureEnabled(null, "disableAutoType"));
        assertFalse(featureEnabled("", "disableAutoType"));
        assertFalse(featureEnabled(" , ", "disableAutoType"));

        // Tokens match exactly: a longer token that contains or extends a real
        // feature name enables nothing.
        assertFalse(featureEnabled("disableAutoTypeExtra", "disableAutoType"));
        assertFalse(featureEnabled("disableSmartMatch2", "disableSmartMatch"));
        assertFalse(featureEnabled("disableAutoTypeExtra, disableJSONB", "disableAutoType"));
        assertTrue(featureEnabled("disableAutoTypeExtra, disableAutoType", "disableAutoType"));
    }

    @Test
    public void featuresResolvedFromPropertiesFile() throws Exception {
        String name = "fastjson2.test.issue7757.features";
        String previous = System.clearProperty(name);
        try {
            // A file entry keeps its trailing whitespace; resolution trims the
            // whole value and each token, so the spaced list enables both flags.
            Properties properties = new Properties();
            properties.setProperty(name, "disableSmartMatch, disableAutoType ");
            String features = resolveProperty(properties, name);
            assertTrue(featureEnabled(features, "disableSmartMatch"));
            assertTrue(featureEnabled(features, "disableAutoType"));

            // An empty/blank -D system property still falls back to the file.
            System.setProperty(name, " ");
            features = resolveProperty(properties, name);
            assertTrue(featureEnabled(features, "disableAutoType"));
        } finally {
            if (previous != null) {
                System.setProperty(name, previous);
            } else {
                System.clearProperty(name);
            }
        }
    }

    @Test
    public void emptyValuesCountAsUnset() throws Exception {
        String name = "fastjson2.test.issue7757.empty";
        String previous = System.clearProperty(name);
        try {
            // An empty or blank file entry resolves to null, like an absent
            // entry, instead of surfacing "" to the leaf helpers.
            Properties properties = new Properties();
            properties.setProperty(name, "");
            assertNull(resolveProperty(properties, name));
            properties.setProperty(name, " ");
            assertNull(resolveProperty(properties, name));

            // A blank -D system property with no file entry resolves to null too.
            System.setProperty(name, " ");
            assertNull(resolveProperty(new Properties(), name));
        } finally {
            if (previous != null) {
                System.setProperty(name, previous);
            } else {
                System.clearProperty(name);
            }
        }
    }

    @Test
    public void maxLevelIsValidated() throws Exception {
        // A parseable but non-positive maxLevel falls back to the 2048 default;
        // positive values pass through unchanged.
        assertEquals(2048, validMaxLevel(0));
        assertEquals(2048, validMaxLevel(-1));
        assertEquals(1, validMaxLevel(1));
        assertEquals(512, validMaxLevel(512));
    }
}
