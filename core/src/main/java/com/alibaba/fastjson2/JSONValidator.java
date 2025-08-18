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
 * </p>
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
     * </p>
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
     * Creates a new JSONValidator for the specified JSONReader.
     *
     * @param jsonReader the JSONReader containing JSON content
     * @return a new JSONValidator instance
     */
    public static JSONValidator from(JSONReader jsonReader) {
        return new JSONValidator(jsonReader);
    }

    /**
     * Validates the JSON content and returns true if it is valid JSON.
     *
     * <p>This method parses the JSON content to check its validity. The result is cached,
     * so subsequent calls will return the same result without re-parsing.</p>
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
     * Returns the type of the JSON content.
     *
     * <p>If the type has not yet been determined, this method will call {@link #validate()}
     * to parse the content and determine its type.</p>
     *
     * @return the Type of the JSON content (Object, Array, or Value)
     */
    public Type getType() {
        if (type == null) {
            validate();
        }

        return type;
    }
}
