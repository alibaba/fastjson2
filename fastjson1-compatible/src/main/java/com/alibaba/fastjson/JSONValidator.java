package com.alibaba.fastjson;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

public class JSONValidator {
    public enum Type {
        Object, Array, Value
    }

    private JSONReader jsonReader;
    private Boolean validateResult;
    private Type type;
    private char firstChar;

    private JSONValidator(JSONReader jsonReader) {
        this.jsonReader = jsonReader;
    }

    public static JSONValidator fromUtf8(byte[] jsonBytes) {
        return new JSONValidator(JSONReader.of(jsonBytes));
    }

    public static JSONValidator from(String jsonStr) {
        return new JSONValidator(JSONReader.of(jsonStr));
    }

    public boolean validate() {
        if (validateResult != null) {
            return validateResult;
        }

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
