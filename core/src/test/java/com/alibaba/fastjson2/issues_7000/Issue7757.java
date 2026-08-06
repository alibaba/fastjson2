package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSONFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("regression")
public class Issue7757 {
    // A fastjson2.properties value must be honored even when the matching -D
    // system property is absent. The helpers run only in the JSONFactory static
    // initializer, so they are exercised directly here for a deterministic result.

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

    @Test
    public void booleanFromPropertiesFile() throws Exception {
        String name = "fastjson2.useJacksonAnnotation";
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

            // A non-empty -D system property wins over the properties file.
            properties.setProperty(name, "false");
            System.setProperty(name, "true");
            assertTrue(getPropertyBool(properties, name, false));

            // An empty/blank -D system property still falls back to the file.
            System.setProperty(name, " ");
            assertFalse(getPropertyBool(properties, name, true));
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
        String name = "fastjson2.writer.maxLevel";
        String previous = System.clearProperty(name);
        try {
            // System property absent: the properties file value is honored.
            Properties properties = new Properties();
            properties.setProperty(name, "512");
            assertEquals(512, getPropertyInt(properties, name, 2048));

            // Absent from both -> default.
            Properties empty = new Properties();
            assertEquals(2048, getPropertyInt(empty, name, 2048));

            // A non-empty -D system property wins over the properties file.
            System.setProperty(name, "4096");
            assertEquals(4096, getPropertyInt(properties, name, 2048));

            // An empty/blank -D system property still falls back to the file.
            System.setProperty(name, " ");
            assertEquals(512, getPropertyInt(properties, name, 2048));
        } finally {
            if (previous != null) {
                System.setProperty(name, previous);
            } else {
                System.clearProperty(name);
            }
        }
    }
}
