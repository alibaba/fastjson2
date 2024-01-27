package com.alibaba.fastjson2.function;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.function.impl.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ConvertTest {
    @Test
    public void to() {
        assertEquals((short) 0, new ToAny(Short.class, (short) 0).apply(null));
        assertNull(new ToAny(String.class).apply(null));
        assertNull(new ToAny(Number.class, null).apply(null));
        assertNull(new ToAny(Long.class).apply(null));
        assertNull(new ToAny(Integer.class).apply(null));
        assertNull(new ToAny(Float.class).apply(null));
        assertNull(new ToAny(Double.class).apply(null));
        assertNull(new ToAny(Byte.class).apply(null));
        assertNull(new ToAny(BigInteger.class).apply(null));
        assertNull(new ToAny(BigDecimal.class).apply(null));
    }

    @Test
    public void error() {
        final Object object = new Object();
        assertThrows(JSONException.class, () -> new ToAny(Byte.class, (byte) 0).apply(object));
        assertThrows(JSONException.class, () -> new ToAny(Short.class, (short) 0).apply(object));
        assertThrows(JSONException.class, () -> new ToAny(Integer.class, 0).apply(object));
        assertThrows(JSONException.class, () -> new ToAny(Long.class, 0L).apply(object));
        assertThrows(JSONException.class, () -> new ToAny(Number.class, 0L).apply(object));
        assertThrows(JSONException.class, () -> new ToAny(Float.class, (float) 0).apply(object));
        assertThrows(JSONException.class, () -> new ToAny(Double.class, (double) 0).apply(object));
        assertThrows(JSONException.class, () -> new ToAny(BigInteger.class).apply(object));
        assertThrows(JSONException.class, () -> new ToAny(BigDecimal.class).apply(object));
        assertThrows(JSONException.class, () -> new StringToAny(Enum.class, null).apply("object"));
    }
}
