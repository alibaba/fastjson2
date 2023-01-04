package com.alibaba.fastjson2.function;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.function.impl.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConvertTest {
    @Test
    public void to() {
        assertEquals((short) 0, new ToShort((short) 0).apply(null));
        assertNull(new ToString().apply(null));
        assertNull(new ToNumber(null).apply(null));
        assertNull(new ToLong(null).apply(null));
        assertNull(new ToInteger(null).apply(null));
        assertNull(new ToFloat(null).apply(null));
        assertNull(new ToDouble(null).apply(null));
        assertNull(new ToByte(null).apply(null));
        assertNull(new ToBigInteger().apply(null));
        assertNull(new ToBigDecimal().apply(null));
    }

    @Test
    public void error() {
        final Object object = new Object();
        assertThrows(JSONException.class, () -> new ToByte((byte) 0).apply(object));
        assertThrows(JSONException.class, () -> new ToShort((short) 0).apply(object));
        assertThrows(JSONException.class, () -> new ToInteger(0).apply(object));
        assertThrows(JSONException.class, () -> new ToLong(0L).apply(object));
        assertThrows(JSONException.class, () -> new ToNumber(0L).apply(object));
        assertThrows(JSONException.class, () -> new ToFloat((float) 0).apply(object));
        assertThrows(JSONException.class, () -> new ToDouble((double) 0).apply(object));
        assertThrows(JSONException.class, () -> new ToBigInteger().apply(object));
        assertThrows(JSONException.class, () -> new ToBigDecimal().apply(object));
        assertThrows(JSONException.class, () -> new StringToAny(Enum.class, null).apply("object"));
    }
}
