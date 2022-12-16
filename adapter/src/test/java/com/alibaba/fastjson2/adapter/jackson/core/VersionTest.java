package com.alibaba.fastjson2.adapter.jackson.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class VersionTest {
    @Test
    public void test() {
        Version version0 = new Version(3, 2, 3, "a", "b", "c");
        Version version1 = new Version(3, 2, 3, "a", "b", "c");
        assertEquals(version0, version1);
        assertEquals(version0, version0);
        assertEquals(version0.hashCode(), version1.hashCode());
        assertEquals(version0.toString(), version1.toString());
        assertEquals(0, version0.compareTo(version1));
    }

    @Test
    public void test1() {
        Version version0 = new Version(3, 2, 3, "a");
        Version version1 = new Version(3, 2, 3, "a");
        assertEquals(version0, version1);
        assertEquals(version0.hashCode(), version1.hashCode());
        assertEquals(version0.toString(), version1.toString());
        assertEquals(0, version0.compareTo(version1));
    }

    @Test
    public void test2() {
        Version v = new Version(3, 2, 1, "a", "b", "c");
        assertEquals(3, v.getMajorVersion());
        assertEquals(2, v.getMinorVersion());
        assertEquals(1, v.getPatchLevel());
        assertEquals("b", v.getGroupId());
        assertEquals("c", v.getArtifactId());
        assertEquals("b/c/3.2.1-a", v.toFullString());
        assertFalse(v.isUnknownVersion());
        assertFalse(v.isUknownVersion());
    }
}
