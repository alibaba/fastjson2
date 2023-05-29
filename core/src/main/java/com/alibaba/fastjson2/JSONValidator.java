package com.alibaba.fastjson2;

public class JSONValidator {
    public enum Type {
        Object, Array, Value
    }

    private final JSONReader jsonReader;
    private Boolean validateResult;
    private Type type;

    protected JSONValidator(JSONReader jsonReader) {
        this.jsonReader = jsonReader;
    }

    public static JSONValidator fromUtf8(byte[] jsonBytes) {
        return new JSONValidator(JSONReader.of(jsonBytes));
    }

    public static JSONValidator from(String jsonStr) {
        return new JSONValidator(JSONReader.of(jsonStr));
    }

    public static JSONValidator from(JSONReader jsonReader) {
        return new JSONValidator(jsonReader);
    }

    public boolean validate() {
        if (validateResult != null) {
            return validateResult;
        }

        char firstChar;
        try {
            firstChar = jsonReader.current();
            jsonReader.skipValue();
        } catch (JSONException error) {
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

    public Type getType() {
        if (type == null) {
            validate();
        }

        return type;
    }
}
