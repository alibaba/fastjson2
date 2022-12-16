package com.alibaba.fastjson2.adapter.jackson.core;

import com.alibaba.fastjson2.JSONException;

public enum JsonToken {
    START_OBJECT("{", JsonTokenId.ID_START_OBJECT),
    END_OBJECT("}", JsonTokenId.ID_END_OBJECT),
    START_ARRAY("[", JsonTokenId.ID_START_ARRAY),
    END_ARRAY("]", JsonTokenId.ID_END_ARRAY),
    FIELD_NAME(null, JsonTokenId.ID_FIELD_NAME),
    VALUE_STRING(null, JsonTokenId.ID_STRING),
    VALUE_NUMBER_INT(null, JsonTokenId.ID_NUMBER_INT),
    VALUE_NUMBER_FLOAT(null, JsonTokenId.ID_NUMBER_FLOAT),
    VALUE_TRUE("true", JsonTokenId.ID_TRUE),
    VALUE_FALSE("false", JsonTokenId.ID_FALSE),
    VALUE_NULL("null", JsonTokenId.ID_NULL);

    JsonToken(String token, int id) {
        // TODO isScalarValue
    }

    public final boolean isScalarValue() {
        // TODO isScalarValue
        throw new JSONException("TODO");
    }
}
