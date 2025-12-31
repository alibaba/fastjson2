package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.JSONException;

public class Cast {
    public static byte toByte(Object value) {
        if (value instanceof Number) {
            return ((Number) value).byteValue();
        } else if (value instanceof Boolean) {
            return (Boolean) value ? (byte) 1 : (byte) 0;
        } else if (value instanceof Character) {
            return (byte) ((Character) value).charValue();
        } else if (value == null) {
            return 0;
        }
        throw new JSONException("Cannot convert " + value + " to byte");
    }

    public static char toChar(Object value) {
        if (value instanceof Character) {
            return (Character) value;
        } else if (value instanceof Number) {
            return (char) ((Number) value).intValue();
        } else if (value instanceof Boolean) {
            return (Boolean) value ? (char) 1 : (char) 0;
        } else if (value == null) {
            return 0;
        }
        throw new JSONException("Cannot convert " + value + " to char");
    }

    public static short toShort(Object value) {
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        } else if (value instanceof Boolean) {
            return (Boolean) value ? (short) 1 : (short) 0;
        } else if (value instanceof Character) {
            return (short) ((Character) value).charValue();
        } else if (value == null) {
            return 0;
        }
        throw new JSONException("Cannot convert " + value + " to short");
    }

    public static int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        } else if (value instanceof Character) {
            return (int) ((Character) value).charValue();
        } else if (value == null) {
            return 0;
        }
        throw new JSONException("Cannot convert " + value + " to int");
    }

    public static long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof Boolean) {
            return (Boolean) value ? 1L : 0L;
        } else if (value instanceof Character) {
            return (long) ((Character) value).charValue();
        } else if (value == null) {
            return 0L;
        }
        throw new JSONException("Cannot convert " + value + " to long");
    }

    public static float toFloat(Object value) {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        } else if (value instanceof Boolean) {
            return (Boolean) value ? 1.0f : 0.0f;
        } else if (value instanceof Character) {
            return (float) ((Character) value).charValue();
        } else if (value == null) {
            return 0.0f;
        }
        throw new JSONException("Cannot convert " + value + " to float");
    }

    public static double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof Boolean) {
            return (Boolean) value ? 1.0 : 0.0;
        } else if (value instanceof Character) {
            return (double) ((Character) value).charValue();
        } else if (value == null) {
            return 0.0;
        }
        throw new JSONException("Cannot convert " + value + " to double");
    }

    public static boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        } else if (value instanceof Character) {
            return ((Character) value).charValue() != 0;
        } else if (value == null) {
            return false;
        }
        throw new JSONException("Cannot convert " + value + " to boolean");
    }
}
