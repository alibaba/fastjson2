package com.alibaba.fastjson2;

/**
 * A utility class for validating JSON strings or byte arrays to check if they
 * represent valid JSON structures.
 *
 * <p>This class provides methods to validate JSON content and determine its type
 * (Object, Array, or Value). It can handle both UTF-8 encoded byte arrays and
 * String representations of JSON.</p>
 *
 * <p>Example usage:
 * <pre>
 * // Validate a JSON string
 * JSONValidator validator = JSONValidator.from("{\"name\":\"John\", \"age\":30}");
 * boolean isValid = validator.validate(); // returns true
 * JSONValidator.Type type = validator.getType(); // returns Type.Object
 *
 * // Validate a JSON byte array
 * byte[] jsonBytes = "[1, 2, 3]".getBytes(StandardCharsets.UTF_8);
 * boolean isValidArray = JSONValidator.fromUtf8(jsonBytes).validate(); // returns true
 * </pre>
 *
 *
 * @author wenshao[szujobs@hotmail.com]
 * @since 2.0.59
 */
public class JSONValidator {
    /**
     * An enumeration representing the type of JSON structure.
     *
     * <p>JSON can be one of three types:
     * <ul>
     *   <li>{@link #Object} - A JSON object, enclosed in curly braces {}</li>
     *   <li>{@link #Array} - A JSON array, enclosed in square brackets []</li>
     *   <li>{@link #Value} - A JSON value, which can be a string, number, boolean, or null</li>
     * </ul>
     *
     */
    public enum Type {
        /** Represents a JSON object structure (enclosed in curly braces {}) */
        Object,
        /** Represents a JSON array structure (enclosed in square brackets []) */
        Array,
        /** Represents a JSON value (string, number, boolean, or null) */
        Value
    }

    private final JSONReader jsonReader;
    private Boolean validateResult;
    private Type type;

    /**
     * Constructs a new JSONValidator with the specified JSONReader.
     *
     * <p>This constructor is protected and intended for internal use.
     * Use the static factory methods to create instances.</p>
     *
     * @param jsonReader the JSONReader to use for validation
     */
    protected JSONValidator(JSONReader jsonReader) {
        this.jsonReader = jsonReader;
    }

    /**
     * Creates a new JSONValidator for the specified UTF-8 encoded byte array.
     *
     * @param jsonBytes the UTF-8 encoded byte array containing JSON content
     * @return a new JSONValidator instance
     */
    public static JSONValidator fromUtf8(byte[] jsonBytes) {
        return new JSONValidator(JSONReader.of(jsonBytes));
    }

    /**
     * Creates a new JSONValidator for the specified JSON string.
     *
     * @param jsonStr the string containing JSON content
     * @return a new JSONValidator instance
     */
    public static JSONValidator from(String jsonStr) {
        return new JSONValidator(JSONReader.of(jsonStr));
    }

    /**
     * Creates a validator that uses the supplied reader at its current position.
     * Validation consumes input and closes this reader, including when validation fails.
     * <details><summary>中文</summary>
     * 使用指定读取器的当前位置进行校验；校验会消耗输入并关闭读取器，校验失败时也会关闭。
     * </details>
     *
     * @param jsonReader the JSONReader containing JSON content
     * @return a new JSONValidator instance
     */
    public static JSONValidator from(JSONReader jsonReader) {
        return new JSONValidator(jsonReader);
    }

    /**
     * Checks whether the reader can skip one value and then reaches the end of its input.
     *
     * <p>Accepted syntax follows the reader's configuration. The reader is closed after
     * this attempt. The result is cached, so subsequent calls do not consume more input.</p>
     * <details><summary>中文</summary>
     * 按读取器配置检查一个值后是否到达输入末尾；检查后关闭读取器，并缓存结果，后续调用不再读取输入。
     * </details>
     *
     * @return true if the content is valid JSON, false otherwise
     */
    public boolean validate() {
        if (validateResult != null) {
            return validateResult;
        }

        char firstChar;
        try {
            firstChar = jsonReader.current();
            jsonReader.skipValue();
        } catch (JSONException | ArrayIndexOutOfBoundsException error) {
            return validateResult = false;
        } finally {
            jsonReader.close();
        }

        if (firstChar == '{') {
            type = Type.Object;
        } else if (firstChar == '[') {
            type = Type.Array;
        } else {
            type = Type.Value;
        }

        return validateResult = jsonReader.isEnd();
    }

    /**
     * Returns the type of the first value successfully skipped during validation.
     *
     * <p>If the type has not yet been determined, this method will call {@link #validate()}
     * to parse the content and determine its type.</p>
     * <p>A non-null type does not imply successful validation: trailing content can cause
     * {@link #validate()} to return false after the first value's type has been determined.</p>
     * <details><summary>中文</summary>
     * 返回校验时成功跳过的首个值的类型；必要时触发校验。尾部存在其他内容时，校验失败但仍可能返回类型。
     * </details>
     *
     * @return Object, Array, or Value, or null if validation failed before determining a type
     */
    public Type getType() {
        if (type == null) {
            validate();
        }

        return type;
    }
}
