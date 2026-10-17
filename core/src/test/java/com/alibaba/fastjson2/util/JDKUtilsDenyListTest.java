package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.util.JDKUtils.AUTO_TYPE_DENY_FQCN;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("util")
public class JDKUtilsDenyListTest {
    /**
     * Verifies PR-1: every FQCN in JDKUtils.AUTO_TYPE_DENY_FQCN is recognised by
     * {@link JDKUtils#isAutoTypeDenyClass(Class)} as a deny entry. Sibling classes in the same
     * package must NOT be blocked (the match is FQCN-exact, not isAssignableFrom, so legitimate
     * app classes inheriting from a safe type are not collateral damage).
     */
    @Test
    public void testIsAutoTypeDenyClass_ExactFQCN() throws Exception {
        for (String fqcn : AUTO_TYPE_DENY_FQCN) {
            Class<?> clazz;
            try {
                clazz = Class.forName(fqcn);
            } catch (ClassNotFoundException e) {
                // Optional module not on classpath; the FQCN string itself is the authoritative
                // gate, so a missing class does not fail this assertion.
                continue;
            }
            assertTrue(JDKUtils.isAutoTypeDenyClass(clazz),
                    "isAutoTypeDenyClass must return true for " + fqcn);
        }

        // A non-deny sibling must NOT be matched: Object is in java.lang but is not a gadget.
        assertFalse(JDKUtils.isAutoTypeDenyClass(Object.class),
                "java.lang.Object must NOT be on the deny list");
        assertFalse(JDKUtils.isAutoTypeDenyClass(String.class),
                "java.lang.String must NOT be on the deny list");
    }
}