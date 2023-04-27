package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Issue1413 {
    @Test
    public void test() {
        assertEquals("🦌", JSON.parse("\"🦌\"  "));
        assertEquals("🦌", JSON.parse("  \"🦌\""));
        assertEquals("🦌", JSON.parse("  \"🦌\","));
        assertEquals("🦌", JSON.parse("  \"🦌\"  "));
        assertEquals("🦌", JSON.parse("  \"🦌\"  \n"));
        assertEquals("🦌", JSON.parse("  \"🦌\"  \n,"));
        assertEquals("🦌", JSON.parse("  \"🦌\"  \r\n,  \r\n"));
        assertEquals("🦌", JSON.parse("\r\n  \"🦌\"  \r\n,  \r\n"));

        assertEquals("foo", JSON.parse("\"foo\"  "));
        assertEquals("foo", JSON.parse("  \"foo\""));
        assertEquals("foo", JSON.parse("  \"foo\","));
        assertEquals("foo", JSON.parse("  \"foo\"  "));
        assertEquals("foo", JSON.parse("  \"foo\"  \n"));
        assertEquals("foo", JSON.parse("  \"foo\"  \n,"));
        assertEquals("foo", JSON.parse("  \"foo\"  \r\n,  \r\n"));
        assertEquals("foo", JSON.parse("\r\n  \"foo\"  \r\n,  \r\n"));
    }
}
