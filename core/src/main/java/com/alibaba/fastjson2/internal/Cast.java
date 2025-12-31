package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.JSONException;

public class Cast {
    public static byte toByte(Object value) {
        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }

        return toByteEx(value);
    }

    private static byte toByteEx(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? (byte) 1 : (byte) 0;
        } else if (value instanceof Character) {
            return (byte) ((Character) value).charValue();
        } else if (value == null) {
            return 0;
        }
        throw errorToByte(value);
    }

    private static JSONException errorToByte(Object value) {
        return new JSONException("Cannot convert " + value + " to byte");
    }

    public static byte toByte(short value) {
        return (byte) value;
    }

    public static byte toByte(int value) {
        return (byte) value;
    }

    public static byte toByte(long value) {
        return (byte) value;
    }

    public static byte toByte(char value) {
        return (byte) value;
    }

    public static byte toByte(boolean value) {
        return (byte) (value ? 1 : 0);
    }

    public static byte toByte(float value) {
        return (byte) value;
    }

    public static byte toByte(double value) {
        return (byte) value;
    }

    public static char toChar(Object value) {
        if (value instanceof Character) {
            return (Character) value;
        }
        return toCharEx(value);
    }

    private static char toCharEx(Object value) {
        if (value instanceof Short) {
            return toChar(((Short) value).shortValue());
        } else if (value instanceof Integer) {
            return toChar(((Integer) value).intValue());
        } else if (value instanceof Long) {
            return toChar(((Long) value).longValue());
        } else if (value instanceof Float) {
            return toChar(((Float) value).floatValue());
        } else if (value instanceof Double) {
            return toChar(((Double) value).doubleValue());
        } else if (value instanceof Boolean) {
            return (Boolean) value ? (char) 1 : (char) 0;
        } else if (value == null) {
            return 0;
        }
        throw errorToChar(value);
    }

    private static JSONException errorToChar(Object value) {
        return new JSONException("Cannot convert " + value + " to char");
    }

    public static char toChar(boolean value) {
        return value ? '1' : '0';
    }

    public static char toChar(short value) {
        return (char) value;
    }

    public static char toChar(int value) {
        return (char) value;
    }

    public static char toChar(long value) {
        return (char) value;
    }

    public static char toChar(float value) {
        return (char) value;
    }

    public static char toChar(double value) {
        return (char) value;
    }

    public static short toShort(Object value) {
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }

        return toShortEx(value);
    }

    private static short toShortEx(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? (short) 1 : (short) 0;
        } else if (value instanceof Character) {
            return (short) ((Character) value).charValue();
        } else if (value == null) {
            return 0;
        }
        throw errorToShort(value);
    }

    private static JSONException errorToShort(Object value) {
        return new JSONException("Cannot convert " + value + " to short");
    }

    public static short toShort(char value) {
        return (short) value;
    }

    public static short toShort(boolean value) {
        return (short) (value ? 1 : 0);
    }

    public static short toShort(int value) {
        return (short) value;
    }

    public static short toShort(long value) {
        return (short) value;
    }

    public static short toShort(float value) {
        return (short) value;
    }

    public static short toShort(double value) {
        return (short) value;
    }

    public static int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        return toIntEx(value);
    }

    private static int toIntEx(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        } else if (value instanceof Character) {
            return toInt(((Character) value).charValue());
        } else if (value == null) {
            return 0;
        }
        throw errorToInt(value);
    }

    private static JSONException errorToInt(Object value) {
        return new JSONException("Cannot convert " + value + " to int");
    }

    public static int toInt(char value) {
        if (value >= '0' && value <= '9') {
            return value - '0';
        }
        throw errorToInt(value);
    }

    public static int toInt(long value) {
        return (int) value;
    }

    public static int toInt(float value) {
        return (int) value;
    }

    public static int toInt(double value) {
        return (int) value;
    }

    public static long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        return toLongEx(value);
    }

    private static long toLongEx(Object value) {
        if (value instanceof Boolean) {
            return toLong(((Boolean) value).booleanValue());
        } else if (value instanceof Character) {
            return toLong(((Character) value).charValue());
        } else if (value == null) {
            return 0L;
        }
        throw errorToLong(value);
    }

    private static JSONException errorToLong(Object value) {
        return new JSONException("Cannot convert " + value + " to long");
    }

    public static long toLong(short value) {
        return value;
    }

    public static long toLong(char value) {
        return value;
    }

    public static long toLong(boolean value) {
        return value ? 1L : 0L;
    }

    public static long toLong(int value) {
        return value;
    }

    public static long toLong(float value) {
        return (long) value;
    }

    public static long toLong(double value) {
        return (long) value;
    }

    public static float toFloat(Object value) {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return toFloatEx(value);
    }

    private static float toFloatEx(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? 1.0f : 0.0f;
        } else if (value instanceof Character) {
            return toFloat(((Character) value).charValue());
        } else if (value == null) {
            return 0.0f;
        }
        throw errorToFloat(value);
    }

    private static JSONException errorToFloat(Object value) {
        return new JSONException("Cannot convert " + value + " to float");
    }

    public static float toFloat(short value) {
        return value;
    }

    public static float toFloat(char value) {
        return value;
    }

    public static float toFloat(int value) {
        return (float) value;
    }

    public static float toFloat(long value) {
        return (float) value;
    }

    public static float toFloat(double value) {
        return (float) value;
    }

    public static float toFloat(boolean value) {
        return value ? 1.0f : 0.0f;
    }

    public static double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        return toDoubleEx(value);
    }

    public static double toDouble(int value) {
        return value;
    }

    public static double toDouble(long value) {
        return value;
    }

    private static double toDoubleEx(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? 1.0 : 0.0;
        } else if (value instanceof Character) {
            return toDouble(((Character) value).charValue());
        } else if (value == null) {
            return 0.0;
        }
        throw errorToDouble(value);
    }

    private static JSONException errorToDouble(Object value) {
        return new JSONException("Cannot convert " + value + " to double");
    }

    public static double toDouble(boolean value) {
        return value ? 1.0D : 0.0D;
    }

    public static double toDouble(char value) {
        if (value >= '0' && value <= '9') {
            return value - '0';
        }
        throw errorToDouble(value);
    }

    public static boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        return toBooleanEx(value);
    }

    private static boolean toBooleanEx(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        } else if (value instanceof Character) {
            return ((Character) value).charValue() != 0;
        } else if (value == null) {
            return false;
        }
        throw errorToBoolean(value);
    }

    private static JSONException errorToBoolean(Object value) {
        return new JSONException("Cannot convert " + value + " to boolean");
    }

    public static boolean toBoolean(byte value) {
        return value != 0;
    }

    public static boolean toBoolean(short value) {
        return value != 0;
    }

    public static boolean toBoolean(char value) {
        return value != 0;
    }

    public static boolean toBoolean(int value) {
        return value != 0;
    }

    public static boolean toBoolean(long value) {
        return value != 0;
    }

    public static boolean toBoolean(float value) {
        return value != 0.0f;
    }

    public static boolean toBoolean(double value) {
        return value != 0.0;
    }
}
