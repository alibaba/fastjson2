package com.alibaba.fastjson.util;

import com.alibaba.fastjson.parser.ParserConfig;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TypeUtilsTest {
    @Test
    public void cast() {
        assertNull(TypeUtils.cast("null", HashMap.class, ParserConfig.global));
        assertNull(TypeUtils.cast("null", (Type) HashMap.class, ParserConfig.global));
        assertNull(TypeUtils.cast("NULL", (Type) HashMap.class, ParserConfig.global));
        assertNull(TypeUtils.cast("", (Type) HashMap.class, ParserConfig.global));
        assertNull(TypeUtils.cast(null, (Type) HashMap.class, ParserConfig.global));

        assertThrows(
                Exception.class,
                () -> TypeUtils.cast(new Object(), (Type) HashMap.class, ParserConfig.global)
        );
    }

    @Test
    public void castToString() {
        assertNull(TypeUtils.castToString(null));
        assertEquals("123", TypeUtils.castToString("123"));
    }

    @Test
    public void fnv1a_64_lower() {
        assertEquals(-1792535898324117685L, TypeUtils.fnv1a_64_lower("abc"));
        assertEquals(-1792535898324117685L, TypeUtils.fnv1a_64_lower("ABC"));
        assertEquals(-1792535898324117685L, TypeUtils.fnv1a_64("abc"));
    }
}
