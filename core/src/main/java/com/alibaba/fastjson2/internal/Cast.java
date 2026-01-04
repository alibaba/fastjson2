package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.JSONException;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Utility class for type casting operations.
 * Provides methods for converting values between different primitive types and objects.
 */
public class Cast {
    // region Byte Conversion Methods

    /**
     * Converts an Object to byte.
     * @param value the Object to convert
     * @return the converted byte value
     */
    public static byte toByteValue(Object value) {
        if (value instanceof Byte) {
            return (Byte) value;
        }

        return toByteEx(value);
    }

    public static Byte toByte(Object value) {
        if (value == null || value instanceof Byte) {
            return (Byte) value;
        }
        return toByteEx(value);
    }

    /**
     * Converts an Object to Short.
     * @param value the Object to convert
     * @return the converted Short value
     */
    public static Short toShort(Object value) {
        if (value == null || value instanceof Short) {
            return (Short) value;
        }
        return toShortEx(value);
    }

    /**
     * Converts an Object to Integer.
     * @param value the Object to convert
     * @return the converted Integer value
     */
    public static Integer toInteger(Object value) {
        if (value == null || value instanceof Integer) {
            return (Integer) value;
        }
        return toIntEx(value);
    }

    /**
     * Converts an Object to Long.
     * @param value the Object to convert
     * @return the converted Long value
     */
    public static Long toLong(Object value) {
        if (value == null || value instanceof Long) {
            return (Long) value;
        }
        return toLongEx(value);
    }

    /**
     * Converts an Object to Float.
     * @param value the Object to convert
     * @return the converted Float value
     */
    public static Float toFloat(Object value) {
        if (value == null || value instanceof Float) {
            return (Float) value;
        }
        return toFloatEx(value);
    }

    /**
     * Converts an Object to Double.
     * @param value the Object to convert
     * @return the converted Double value
     */
    public static Double toDouble(Object value) {
        if (value == null || value instanceof Double) {
            return (Double) value;
        }
        return toDoubleEx(value);
    }

    /**
     * Converts an Object to Character.
     * @param value the Object to convert
     * @return the converted Character value
     */
    public static Character toCharacter(Object value) {
        if (value == null || value instanceof Character) {
            return (Character) value;
        }
        return toCharEx(value);
    }

    /**
     * Converts an Object to Boolean.
     * @param value the Object to convert
     * @return the converted Boolean value
     */
    public static Boolean toBoolean(Object value) {
        if (value == null || value instanceof Boolean) {
            return (Boolean) value;
        }
        return toBooleanEx(value);
    }

    /**
     * Helper method for converting non-Number objects to byte.
     * @param value the Object to convert
     * @return the converted byte value
     */
    private static byte toByteEx(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? (byte) 1 : (byte) 0;
        } else if (value instanceof Character) {
            return (byte) ((Character) value).charValue();
        } else if (value instanceof String) {
            return toByteValue((String) value);
        } else if (value instanceof Number) {
            return toByteValue(((Number) value).doubleValue());
        } else if (value == null) {
            return 0;
        }
        throw errorToByte(value);
    }

    /**
     * Creates a JSONException for byte conversion errors.
     * @param value the value that caused the error
     * @return the JSONException
     */
    private static JSONException errorToByte(Object value) {
        return new JSONException("Cannot convert " + value + " to byte");
    }

    /**
     * Converts a short to byte.
     * @param value the short to convert
     * @return the converted byte value
     */
    public static byte toByteValue(short value) {
        return (byte) value;
    }

    /**
     * Converts an int to byte.
     * @param value the int to convert
     * @return the converted byte value
     */
    public static byte toByteValue(int value) {
        return (byte) value;
    }

    /**
     * Converts a long to byte.
     * @param value the long to convert
     * @return the converted byte value
     */
    public static byte toByteValue(long value) {
        return (byte) value;
    }

    /**
     * Converts a char to byte.
     * @param value the char to convert
     * @return the converted byte value
     */
    public static byte toByteValue(char value) {
        return (byte) value;
    }

    /**
     * Converts a boolean to byte.
     * @param value the boolean to convert
     * @return the converted byte value
     */
    public static byte toByteValue(boolean value) {
        return (byte) (value ? 1 : 0);
    }

    /**
     * Converts a float to byte.
     * @param value the float to convert
     * @return the converted byte value
     */
    public static byte toByteValue(float value) {
        return (byte) value;
    }

    /**
     * Converts a double to byte.
     * @param value the double to convert
     * @return the converted byte value
     */
    public static byte toByteValue(double value) {
        return (byte) value;
    }

    /**
     * Converts a String to byte.
     * @param value the String to convert
     * @return the converted byte value
     */
    public static byte toByteValue(String value) {
        if (value == null) {
            return 0;
        }
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException e) {
            throw errorToByte(value);
        }
    }

    /**
     * Converts a BigInteger to byte.
     * @param value the BigInteger to convert
     * @return the converted byte value
     */
    public static byte toByteValue(BigInteger value) {
        return value.byteValue();
    }

    /**
     * Converts a BigDecimal to byte.
     * @param value the BigDecimal to convert
     * @return the converted byte value
     */
    public static byte toByteValue(BigDecimal value) {
        return value.byteValue();
    }

    // endregion

    // region Char Conversion Methods

    /**
     * Converts an Object to char.
     * @param value the Object to convert
     * @return the converted char value
     */
    public static char toCharValue(Object value) {
        if (value instanceof Character) {
            return (Character) value;
        }
        return toCharEx(value);
    }

    /**
     * Helper method for converting non-Character objects to char.
     * @param value the Object to convert
     * @return the converted char value
     */
    private static char toCharEx(Object value) {
        if (value instanceof Short) {
            return toCharValue(((Short) value).shortValue());
        } else if (value instanceof Integer) {
            return toCharValue(((Integer) value).intValue());
        } else if (value instanceof Long) {
            return toCharValue(((Long) value).longValue());
        } else if (value instanceof Float) {
            return toCharValue(((Float) value).floatValue());
        } else if (value instanceof Double) {
            return toCharValue(((Double) value).doubleValue());
        } else if (value instanceof Boolean) {
            return (Boolean) value ? (char) 1 : (char) 0;
        } else if (value instanceof String) {
            return toCharValue((String) value);
        } else if (value instanceof BigInteger) {
            return toCharValue((BigInteger) value);
        } else if (value instanceof BigDecimal) {
            return toCharValue((BigDecimal) value);
        } else if (value == null) {
            return 0;
        }
        throw errorToChar(value);
    }

    /**
     * Creates a JSONException for char conversion errors.
     * @param value the value that caused the error
     * @return the JSONException
     */
    private static JSONException errorToChar(Object value) {
        return new JSONException("Cannot convert " + value + " to char");
    }

    /**
     * Converts a boolean to char.
     * @param value the boolean to convert
     * @return the converted char value
     */
    public static char toCharValue(boolean value) {
        return value ? '1' : '0';
    }

    /**
     * Converts a short to char.
     * @param value the short to convert
     * @return the converted char value
     */
    public static char toCharValue(short value) {
        return (char) value;
    }

    /**
     * Converts an int to char.
     * @param value the int to convert
     * @return the converted char value
     */
    public static char toCharValue(int value) {
        return (char) value;
    }

    /**
     * Converts a long to char.
     * @param value the long to convert
     * @return the converted char value
     */
    public static char toCharValue(long value) {
        return (char) value;
    }

    /**
     * Converts a float to char.
     * @param value the float to convert
     * @return the converted char value
     */
    public static char toCharValue(float value) {
        return (char) value;
    }

    /**
     * Converts a double to char.
     * @param value the double to convert
     * @return the converted char value
     */
    public static char toCharValue(double value) {
        return (char) value;
    }

    /**
     * Converts a String to char.
     * @param value the String to convert
     * @return the converted char value
     */
    public static char toCharValue(String value) {
        if (value == null) {
            return '\0';
        }
        if (value.length() == 1) {
            return value.charAt(0);
        }
        try {
            return (char) Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw errorToChar(value);
        }
    }

    /**
     * Converts a BigInteger to char.
     * @param value the BigInteger to convert
     * @return the converted char value
     */
    public static char toCharValue(BigInteger value) {
        return (char) value.intValue();
    }

    /**
     * Converts a BigDecimal to char.
     * @param value the BigDecimal to convert
     * @return the converted char value
     */
    public static char toCharValue(BigDecimal value) {
        return (char) value.intValue();
    }

    // endregion

    // region Short Conversion Methods

    /**
     * Converts an Object to short.
     * @param value the Object to convert
     * @return the converted short value
     */
    public static short toShortValue(Object value) {
        if (value instanceof Short) {
            return (Short) value;
        }

        return toShortEx(value);
    }

    /**
     * Helper method for converting non-Number objects to short.
     * @param value the Object to convert
     * @return the converted short value
     */
    private static short toShortEx(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? (short) 1 : (short) 0;
        } else if (value instanceof Character) {
            return (short) ((Character) value).charValue();
        } else if (value instanceof String) {
            return toShortValue((String) value);
        } else if (value instanceof BigInteger) {
            return toShortValue((BigInteger) value);
        } else if (value instanceof BigDecimal) {
            return toShortValue((BigDecimal) value);
        } else if (value instanceof Number) {
            return toShortValue(((Number) value).shortValue());
        } else if (value == null) {
            return 0;
        }
        throw errorToShort(value);
    }

    /**
     * Creates a JSONException for short conversion errors.
     * @param value the value that caused the error
     * @return the JSONException
     */
    private static JSONException errorToShort(Object value) {
        return new JSONException("Cannot convert " + value + " to short");
    }

    /**
     * Converts a char to short.
     * @param value the char to convert
     * @return the converted short value
     */
    public static short toShortValue(char value) {
        return (short) value;
    }

    /**
     * Converts a boolean to short.
     * @param value the boolean to convert
     * @return the converted short value
     */
    public static short toShortValue(boolean value) {
        return (short) (value ? 1 : 0);
    }

    /**
     * Converts an int to short.
     * @param value the int to convert
     * @return the converted short value
     */
    public static short toShortValue(int value) {
        return (short) value;
    }

    /**
     * Converts a long to short.
     * @param value the long to convert
     * @return the converted short value
     */
    public static short toShortValue(long value) {
        return (short) value;
    }

    /**
     * Converts a float to short.
     * @param value the float to convert
     * @return the converted short value
     */
    public static short toShortValue(float value) {
        return (short) value;
    }

    /**
     * Converts a double to short.
     * @param value the double to convert
     * @return the converted short value
     */
    public static short toShortValue(double value) {
        return (short) value;
    }

    /**
     * Converts a String to short.
     * @param value the String to convert
     * @return the converted short value
     */
    public static short toShortValue(String value) {
        if (value == null) {
            return 0;
        }
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            throw errorToShort(value);
        }
    }

    /**
     * Converts a BigInteger to short.
     * @param value the BigInteger to convert
     * @return the converted short value
     */
    public static short toShortValue(BigInteger value) {
        return value.shortValue();
    }

    /**
     * Converts a BigDecimal to short.
     * @param value the BigDecimal to convert
     * @return the converted short value
     */
    public static short toShortValue(BigDecimal value) {
        return value.shortValue();
    }

    // endregion

    // region Int Conversion Methods

    /**
     * Converts an Object to int.
     * @param value the Object to convert
     * @return the converted int value
     */
    public static int toIntValue(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }

        return toIntEx(value);
    }

    /**
     * Helper method for converting non-Number objects to int.
     * @param value the Object to convert
     * @return the converted int value
     */
    private static int toIntEx(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        } else if (value instanceof Character) {
            return toIntValue(((Character) value).charValue());
        } else if (value instanceof String) {
            return toIntValue((String) value);
        } else if (value instanceof BigInteger) {
            return toIntValue((BigInteger) value);
        } else if (value instanceof BigDecimal) {
            return toIntValue((BigDecimal) value);
        } else if (value instanceof Number) {
            return toIntValue(((Number) value).intValue());
        } else if (value == null) {
            return 0;
        }
        throw errorToInt(value);
    }

    /**
     * Creates a JSONException for int conversion errors.
     * @param value the value that caused the error
     * @return the JSONException
     */
    private static JSONException errorToInt(Object value) {
        return new JSONException("Cannot convert " + value + " to int");
    }

    /**
     * Converts a char to int.
     * @param value the char to convert
     * @return the converted int value
     */
    public static int toIntValue(char value) {
        if (value >= '0' && value <= '9') {
            return value - '0';
        }
        throw errorToInt(value);
    }

    /**
     * Converts a long to int.
     * @param value the long to convert
     * @return the converted int value
     */
    public static int toIntValue(long value) {
        return (int) value;
    }

    /**
     * Converts a float to int.
     * @param value the float to convert
     * @return the converted int value
     */
    public static int toIntValue(float value) {
        return (int) value;
    }

    /**
     * Converts a double to int.
     * @param value the double to convert
     * @return the converted int value
     */
    public static int toIntValue(double value) {
        return (int) value;
    }

    /**
     * Converts a String to int.
     * @param value the String to convert
     * @return the converted int value
     */
    public static int toIntValue(String value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw errorToInt(value);
        }
    }

    /**
     * Converts a BigInteger to int.
     * @param value the BigInteger to convert
     * @return the converted int value
     */
    public static int toIntValue(BigInteger value) {
        return value.intValue();
    }

    /**
     * Converts a BigDecimal to int.
     * @param value the BigDecimal to convert
     * @return the converted int value
     */
    public static int toIntValue(BigDecimal value) {
        return value.intValue();
    }

    // endregion

    // region Long Conversion Methods

    /**
     * Converts an Object to long.
     * @param value the Object to convert
     * @return the converted long value
     */
    public static long toLongValue(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        }

        return toLongEx(value);
    }

    /**
     * Helper method for converting non-Number objects to long.
     * @param value the Object to convert
     * @return the converted long value
     */
    private static long toLongEx(Object value) {
        if (value instanceof Boolean) {
            return toLongValue(((Boolean) value).booleanValue());
        } else if (value instanceof Character) {
            return toLongValue(((Character) value).charValue());
        } else if (value instanceof String) {
            return toLongValue((String) value);
        } else if (value instanceof BigInteger) {
            return toLongValue((BigInteger) value);
        } else if (value instanceof BigDecimal) {
            return toLongValue((BigDecimal) value);
        } else if (value instanceof Number) {
            return toLongValue(((Number) value).longValue());
        } else if (value == null) {
            return 0L;
        }
        throw errorToLong(value);
    }

    /**
     * Creates a JSONException for long conversion errors.
     * @param value the value that caused the error
     * @return the JSONException
     */
    private static JSONException errorToLong(Object value) {
        return new JSONException("Cannot convert " + value + " to long");
    }

    /**
     * Converts a short to long.
     * @param value the short to convert
     * @return the converted long value
     */
    public static long toLongValue(short value) {
        return value;
    }

    /**
     * Converts a char to long.
     * @param value the char to convert
     * @return the converted long value
     */
    public static long toLongValue(char value) {
        return value;
    }

    /**
     * Converts a boolean to long.
     * @param value the boolean to convert
     * @return the converted long value
     */
    public static long toLongValue(boolean value) {
        return value ? 1L : 0L;
    }

    /**
     * Converts an int to long.
     * @param value the int to convert
     * @return the converted long value
     */
    public static long toLongValue(int value) {
        return value;
    }

    /**
     * Converts a float to long.
     * @param value the float to convert
     * @return the converted long value
     */
    public static long toLongValue(float value) {
        return (long) value;
    }

    /**
     * Converts a double to long.
     * @param value the double to convert
     * @return the converted long value
     */
    public static long toLongValue(double value) {
        return (long) value;
    }

    /**
     * Converts a String to long.
     * @param value the String to convert
     * @return the converted long value
     */
    public static long toLongValue(String value) {
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw errorToLong(value);
        }
    }

    /**
     * Converts a BigInteger to long.
     * @param value the BigInteger to convert
     * @return the converted long value
     */
    public static long toLongValue(BigInteger value) {
        return value.longValue();
    }

    /**
     * Converts a BigDecimal to long.
     * @param value the BigDecimal to convert
     * @return the converted long value
     */
    public static long toLongValue(BigDecimal value) {
        return value.longValue();
    }

    // endregion

    // region Float Conversion Methods

    /**
     * Converts an Object to float.
     * @param value the Object to convert
     * @return the converted float value
     */
    public static float toFloatValue(Object value) {
        if (value instanceof Float) {
            return (Float) value;
        }
        return toFloatEx(value);
    }

    /**
     * Helper method for converting non-Number objects to float.
     * @param value the Object to convert
     * @return the converted float value
     */
    private static float toFloatEx(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? 1.0f : 0.0f;
        } else if (value instanceof Character) {
            return toFloatValue(((Character) value).charValue());
        } else if (value instanceof String) {
            return toFloatValue((String) value);
        } else if (value instanceof BigInteger) {
            return toFloatValue((BigInteger) value);
        } else if (value instanceof BigDecimal) {
            return toFloatValue((BigDecimal) value);
        } else if (value instanceof Number) {
            return ((Number) value).floatValue();
        } else if (value == null) {
            return 0.0f;
        }
        throw errorToFloat(value);
    }

    /**
     * Creates a JSONException for float conversion errors.
     * @param value the value that caused the error
     * @return the JSONException
     */
    private static JSONException errorToFloat(Object value) {
        return new JSONException("Cannot convert " + value + " to float");
    }

    /**
     * Converts a short to float.
     * @param value the short to convert
     * @return the converted float value
     */
    public static float toFloatValue(short value) {
        return value;
    }

    /**
     * Converts a char to float.
     * @param value the char to convert
     * @return the converted float value
     */
    public static float toFloatValue(char value) {
        return value;
    }

    /**
     * Converts an int to float.
     * @param value the int to convert
     * @return the converted float value
     */
    public static float toFloatValue(int value) {
        return (float) value;
    }

    /**
     * Converts a long to float.
     * @param value the long to convert
     * @return the converted float value
     */
    public static float toFloatValue(long value) {
        return (float) value;
    }

    /**
     * Converts a double to float.
     * @param value the double to convert
     * @return the converted float value
     */
    public static float toFloatValue(double value) {
        return (float) value;
    }

    /**
     * Converts a boolean to float.
     * @param value the boolean to convert
     * @return the converted float value
     */
    public static float toFloatValue(boolean value) {
        return value ? 1.0f : 0.0f;
    }

    /**
     * Converts a String to float.
     * @param value the String to convert
     * @return the converted float value
     */
    public static float toFloatValue(String value) {
        if (value == null) {
            return 0.0f;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw errorToFloat(value);
        }
    }

    /**
     * Converts a BigInteger to float.
     * @param value the BigInteger to convert
     * @return the converted float value
     */
    public static float toFloatValue(BigInteger value) {
        return value.floatValue();
    }

    /**
     * Converts a BigDecimal to float.
     * @param value the BigDecimal to convert
     * @return the converted float value
     */
    public static float toFloatValue(BigDecimal value) {
        return value.floatValue();
    }

    // endregion

    // region Double Conversion Methods

    /**
     * Converts an Object to double.
     * @param value the Object to convert
     * @return the converted double value
     */
    public static double toDoubleValue(Object value) {
        if (value instanceof Double) {
            return (Double) value;
        }

        return toDoubleEx(value);
    }

    /**
     * Converts an int to double.
     * @param value the int to convert
     * @return the converted double value
     */
    public static double toDoubleValue(int value) {
        return value;
    }

    /**
     * Converts a long to double.
     * @param value the long to convert
     * @return the converted double value
     */
    public static double toDoubleValue(long value) {
        return value;
    }

    /**
     * Helper method for converting non-Number objects to double.
     * @param value the Object to convert
     * @return the converted double value
     */
    private static double toDoubleEx(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? 1.0 : 0.0;
        } else if (value instanceof Character) {
            return toDoubleValue(((Character) value).charValue());
        } else if (value instanceof String) {
            return toDoubleValue((String) value);
        } else if (value instanceof BigInteger) {
            return toDoubleValue((BigInteger) value);
        } else if (value instanceof BigDecimal) {
            return toDoubleValue((BigDecimal) value);
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value == null) {
            return 0.0;
        }
        throw errorToDouble(value);
    }

    /**
     * Creates a JSONException for double conversion errors.
     * @param value the value that caused the error
     * @return the JSONException
     */
    private static JSONException errorToDouble(Object value) {
        return new JSONException("Cannot convert " + value + " to double");
    }

    /**
     * Converts a boolean to double.
     * @param value the boolean to convert
     * @return the converted double value
     */
    public static double toDoubleValue(boolean value) {
        return value ? 1.0D : 0.0D;
    }

    /**
     * Converts a char to double.
     * @param value the char to convert
     * @return the converted double value
     */
    public static double toDoubleValue(char value) {
        if (value >= '0' && value <= '9') {
            return value - '0';
        }
        throw errorToDouble(value);
    }

    /**
     * Converts a String to double.
     * @param value the String to convert
     * @return the converted double value
     */
    public static double toDoubleValue(String value) {
        if (value == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw errorToDouble(value);
        }
    }

    /**
     * Converts a BigInteger to double.
     * @param value the BigInteger to convert
     * @return the converted double value
     */
    public static double toDoubleValue(BigInteger value) {
        return value.doubleValue();
    }

    /**
     * Converts a BigDecimal to double.
     * @param value the BigDecimal to convert
     * @return the converted double value
     */
    public static double toDoubleValue(BigDecimal value) {
        return value.doubleValue();
    }

    // endregion

    // region Boolean Conversion Methods

    /**
     * Converts an Object to boolean.
     * @param value the Object to convert
     * @return the converted boolean value
     */
    public static boolean toBooleanValue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        return toBooleanEx(value);
    }

    /**
     * Helper method for converting non-Boolean objects to boolean.
     * @param value the Object to convert
     * @return the converted boolean value
     */
    private static boolean toBooleanEx(Object value) {
        if (value instanceof BigInteger) {
            return toBooleanValue((BigInteger) value);
        } else if (value instanceof BigDecimal) {
            return toBooleanValue((BigDecimal) value);
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        } else if (value instanceof Character) {
            return (Character) value != 0;
        }
        else if (value instanceof String) {
            return toBooleanValue((String) value);
        }
        else if (value == null) {
            return false;
        }
        throw errorToBoolean(value);
    }

    /**
     * Creates a JSONException for boolean conversion errors.
     * @param value the value that caused the error
     * @return the JSONException
     */
    private static JSONException errorToBoolean(Object value) {
        return new JSONException("Cannot convert " + value + " to boolean");
    }

    /**
     * Converts a byte to boolean.
     * @param value the byte to convert
     * @return the converted boolean value
     */
    public static boolean toBooleanValue(byte value) {
        return value != 0;
    }

    /**
     * Converts a short to boolean.
     * @param value the short to convert
     * @return the converted boolean value
     */
    public static boolean toBooleanValue(short value) {
        return value != 0;
    }

    /**
     * Converts a char to boolean.
     * @param value the char to convert
     * @return the converted boolean value
     */
    public static boolean toBooleanValue(char value) {
        return value == '1' || value == 't' || value == 'T';
    }

    /**
     * Converts an int to boolean.
     * @param value the int to convert
     * @return the converted boolean value
     */
    public static boolean toBooleanValue(int value) {
        return value != 0;
    }

    /**
     * Converts a long to boolean.
     * @param value the long to convert
     * @return the converted boolean value
     */
    public static boolean toBooleanValue(long value) {
        return value != 0;
    }

    /**
     * Converts a float to boolean.
     * @param value the float to convert
     * @return the converted boolean value
     */
    public static boolean toBooleanValue(float value) {
        return value != 0.0f;
    }

    /**
     * Converts a double to boolean.
     * @param value the double to convert
     * @return the converted boolean value
     */
    public static boolean toBooleanValue(double value) {
        return value != 0.0;
    }

    /**
     * Converts a String to boolean.
     * @param value the String to convert
     * @return the converted boolean value
     */
    public static boolean toBooleanValue(String value) {
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Converts a BigInteger to boolean.
     * @param value the BigInteger to convert
     * @return the converted boolean value
     */
    public static boolean toBooleanValue(BigInteger value) {
        return !value.equals(BigInteger.ZERO);
    }

    /**
     * Converts a BigDecimal to boolean.
     * @param value the BigDecimal to convert
     * @return the converted boolean value
     */
    public static boolean toBooleanValue(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) != 0;
    }

    // endregion

    // region BigInteger Conversion Methods

    /**
     * Converts an Object to BigInteger.
     * This implementation is optimized for the Java optimizer by keeping
     * the main method small and delegating complex logic to toBigIntegerEx.
     */
    public static BigInteger toBigInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }
        return toBigIntegerEx(value);
    }

    /**
     * Helper method that contains the more complex conversion logic.
     * Separating this logic allows the main toBigInteger method to remain
     * small and more amenable to JVM optimizations like inlining.
     */
    private static BigInteger toBigIntegerEx(Object value) {
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toBigInteger();
        } else if (value instanceof Number) {
            return BigInteger.valueOf(((Number) value).longValue());
        } else if (value instanceof Boolean) {
            return BigInteger.valueOf((Boolean) value ? 1 : 0);
        } else if (value instanceof Character) {
            return BigInteger.valueOf((Character) value);
        } else if (value instanceof String) {
            return new BigInteger((String) value);
        }
        throw errorToBigInteger(value);
    }

    /**
     * Creates a JSONException for BigInteger conversion errors.
     * @param value the value that caused the error
     * @return the JSONException
     */
    private static JSONException errorToBigInteger(Object value) {
        return new JSONException("Cannot convert " + value + " to BigInteger");
    }

    /**
     * Converts a byte to BigInteger.
     * @param value the byte to convert
     * @return the converted BigInteger value
     */
    public static BigInteger toBigInteger(byte value) {
        return BigInteger.valueOf(value);
    }

    /**
     * Converts a short to BigInteger.
     * @param value the short to convert
     * @return the converted BigInteger value
     */
    public static BigInteger toBigInteger(short value) {
        return BigInteger.valueOf(value);
    }

    /**
     * Converts a char to BigInteger.
     * @param value the char to convert
     * @return the converted BigInteger value
     */
    public static BigInteger toBigInteger(char value) {
        return BigInteger.valueOf(value);
    }

    /**
     * Converts an int to BigInteger.
     * @param value the int to convert
     * @return the converted BigInteger value
     */
    public static BigInteger toBigInteger(int value) {
        return BigInteger.valueOf(value);
    }

    /**
     * Converts a long to BigInteger.
     * @param value the long to convert
     * @return the converted BigInteger value
     */
    public static BigInteger toBigInteger(long value) {
        return BigInteger.valueOf(value);
    }

    /**
     * Converts a float to BigInteger.
     * @param value the float to convert
     * @return the converted BigInteger value
     */
    public static BigInteger toBigInteger(float value) {
        return BigInteger.valueOf((long) value);
    }

    /**
     * Converts a double to BigInteger.
     * @param value the double to convert
     * @return the converted BigInteger value
     */
    public static BigInteger toBigInteger(double value) {
        return BigInteger.valueOf((long) value);
    }

    /**
     * Converts a boolean to BigInteger.
     * @param value the boolean to convert
     * @return the converted BigInteger value
     */
    public static BigInteger toBigInteger(boolean value) {
        return value ? BigInteger.ONE : BigInteger.ZERO;
    }

    /**
     * Converts a String to BigInteger.
     * @param value the String to convert
     * @return the converted BigInteger value
     */
    public static BigInteger toBigInteger(String value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigInteger(value);
        } catch (NumberFormatException e) {
            throw errorToBigInteger(value);
        }
    }

    // endregion

    // region BigDecimal Conversion Methods

    /**
     * Converts an Object to BigDecimal.
     * This implementation is optimized for the Java optimizer by keeping
     * the main method small and delegating complex logic to toBigDecimalEx.
     */
    public static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return toBigDecimalEx(value);
    }

    /**
     * Helper method that contains the more complex conversion logic.
     * Separating this logic allows the main toBigDecimal method to remain
     * small and more amenable to JVM optimizations like inlining.
     */
    private static BigDecimal toBigDecimalEx(Object value) {
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        } else if (value instanceof Number) {
            if (value instanceof Float || value instanceof Double) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            } else {
                return new BigDecimal(value.toString());
            }
        } else if (value instanceof Boolean) {
            return BigDecimal.valueOf((Boolean) value ? 1 : 0);
        } else if (value instanceof Character) {
            return BigDecimal.valueOf((Character) value);
        } else if (value instanceof String) {
            return new BigDecimal((String) value);
        } else if (value == null) {
            return BigDecimal.ZERO;
        }
        throw errorToBigDecimal(value);
    }

    /**
     * Creates a JSONException for BigDecimal conversion errors.
     * @param value the value that caused the error
     * @return the JSONException
     */
    private static JSONException errorToBigDecimal(Object value) {
        return new JSONException("Cannot convert " + value + " to BigDecimal");
    }

    /**
     * Converts a byte to BigDecimal.
     * @param value the byte to convert
     * @return the converted BigDecimal value
     */
    public static BigDecimal toBigDecimal(byte value) {
        return BigDecimal.valueOf(value);
    }

    /**
     * Converts a short to BigDecimal.
     * @param value the short to convert
     * @return the converted BigDecimal value
     */
    public static BigDecimal toBigDecimal(short value) {
        return BigDecimal.valueOf(value);
    }

    /**
     * Converts a char to BigDecimal.
     * @param value the char to convert
     * @return the converted BigDecimal value
     */
    public static BigDecimal toBigDecimal(char value) {
        return BigDecimal.valueOf(value);
    }

    /**
     * Converts an int to BigDecimal.
     * @param value the int to convert
     * @return the converted BigDecimal value
     */
    public static BigDecimal toBigDecimal(int value) {
        return BigDecimal.valueOf(value);
    }

    /**
     * Converts a long to BigDecimal.
     * @param value the long to convert
     * @return the converted BigDecimal value
     */
    public static BigDecimal toBigDecimal(long value) {
        return BigDecimal.valueOf(value);
    }

    /**
     * Converts a float to BigDecimal.
     * @param value the float to convert
     * @return the converted BigDecimal value
     */
    public static BigDecimal toBigDecimal(float value) {
        return BigDecimal.valueOf(value);
    }

    /**
     * Converts a double to BigDecimal.
     * @param value the double to convert
     * @return the converted BigDecimal value
     */
    public static BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value);
    }

    /**
     * Converts a boolean to BigDecimal.
     * @param value the boolean to convert
     * @return the converted BigDecimal value
     */
    public static BigDecimal toBigDecimal(boolean value) {
        return value ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /**
     * Converts a String to BigDecimal.
     * @param value the String to convert
     * @return the converted BigDecimal value
     */
    public static BigDecimal toBigDecimal(String value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw errorToBigDecimal(value);
        }
    }

    // endregion

    // region String Conversion Methods

    /**
     * Converts an Object to String.
     * @param value the Object to convert
     * @return the converted String value
     */
    public static String toString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * Converts a byte to String.
     * @param value the byte to convert
     * @return the converted String value
     */
    public static String toString(byte value) {
        return String.valueOf(value);
    }

    /**
     * Converts a short to String.
     * @param value the short to convert
     * @return the converted String value
     */
    public static String toString(short value) {
        return String.valueOf(value);
    }

    /**
     * Converts a char to String.
     * @param value the char to convert
     * @return the converted String value
     */
    public static String toString(char value) {
        return String.valueOf(value);
    }

    /**
     * Converts an int to String.
     * @param value the int to convert
     * @return the converted String value
     */
    public static String toString(int value) {
        return String.valueOf(value);
    }

    /**
     * Converts a long to String.
     * @param value the long to convert
     * @return the converted String value
     */
    public static String toString(long value) {
        return String.valueOf(value);
    }

    /**
     * Converts a float to String.
     * @param value the float to convert
     * @return the converted String value
     */
    public static String toString(float value) {
        return String.valueOf(value);
    }

    /**
     * Converts a double to String.
     * @param value the double to convert
     * @return the converted String value
     */
    public static String toString(double value) {
        return String.valueOf(value);
    }

    /**
     * Converts a boolean to String.
     * @param value the boolean to convert
     * @return the converted String value
     */
    public static String toString(boolean value) {
        return String.valueOf(value);
    }

    /**
     * Converts a BigInteger to String.
     * @param value the BigInteger to convert
     * @return the converted String value
     */
    public static String toString(BigInteger value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * Converts a BigDecimal to String.
     * @param value the BigDecimal to convert
     * @return the converted String value
     */
    public static String toString(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    // endregion

    // region Number Conversion Methods

    /**
     * Converts an Object to Number.
     * @param value the Object to convert
     * @return the converted Number value
     */
    public static Number toNumber(Object value) {
        if (value == null || value instanceof Number) {
            return (Number) value;
        }
        return toNumberEx(value);
    }

    /**
     * Helper method for converting non-Number objects to Number.
     * @param value the Object to convert
     * @return the converted Number value
     */
    private static Number toNumberEx(Object value) {
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        } else if (value instanceof Character) {
            return (double) ((Character) value).charValue();
        } else if (value instanceof String) {
            String str = (String) value;
            if (str == null) {
                return null;
            }
            // Try to parse as integer first, then as double
            try {
                // Check if it contains decimal point or scientific notation
                if (str.contains(".") || str.toLowerCase().contains("e")) {
                    return Double.parseDouble(str);
                } else {
                    // Try long first to handle larger integers
                    return Long.parseLong(str);
                }
            } catch (NumberFormatException e) {
                throw errorToNumber(value);
            }
        } else if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof BigInteger) {
            return (BigInteger) value;
        } else if (value == null) {
            return null;
        }
        throw errorToNumber(value);
    }

    /**
     * Creates a JSONException for Number conversion errors.
     * @param value the value that caused the error
     * @return the JSONException
     */
    private static JSONException errorToNumber(Object value) {
        return new JSONException("Cannot convert " + value + " to Number");
    }

    /**
     * Converts a byte to Number.
     * @param value the byte to convert
     * @return the converted Number value
     */
    public static Number toNumber(byte value) {
        return value;
    }

    /**
     * Converts a short to Number.
     * @param value the short to convert
     * @return the converted Number value
     */
    public static Number toNumber(short value) {
        return value;
    }

    /**
     * Converts a char to Number.
     * @param value the char to convert
     * @return the converted Number value
     */
    public static Number toNumber(char value) {
        return (int) value;
    }

    /**
     * Converts an int to Number.
     * @param value the int to convert
     * @return the converted Number value
     */
    public static Number toNumber(int value) {
        return value;
    }

    /**
     * Converts a long to Number.
     * @param value the long to convert
     * @return the converted Number value
     */
    public static Number toNumber(long value) {
        return value;
    }

    /**
     * Converts a float to Number.
     * @param value the float to convert
     * @return the converted Number value
     */
    public static Number toNumber(float value) {
        return value;
    }

    /**
     * Converts a double to Number.
     * @param value the double to convert
     * @return the converted Number value
     */
    public static Number toNumber(double value) {
        return value;
    }

    /**
     * Converts a boolean to Number.
     * @param value the boolean to convert
     * @return the converted Number value
     */
    public static Number toNumber(boolean value) {
        return value ? 1 : 0;
    }

    /**
     * Converts a String to Number.
     * @param value the String to convert
     * @return the converted Number value
     */
    public static Number toNumber(String value) {
        if (value == null) {
            return null;
        }
        try {
            // Check if it contains decimal point or scientific notation
            if (value.contains(".") || value.toLowerCase().contains("e")) {
                return Double.parseDouble(value);
            } else {
                // Try long first to handle larger integers
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            throw errorToNumber(value);
        }
    }

    /**
     * Converts a BigInteger to Number.
     * @param value the BigInteger to convert
     * @return the converted Number value
     */
    public static Number toNumber(BigInteger value) {
        return value;
    }

    /**
     * Converts a BigDecimal to Number.
     * @param value the BigDecimal to convert
     * @return the converted Number value
     */
    public static Number toNumber(BigDecimal value) {
        return value;
    }

    // endregion
}
