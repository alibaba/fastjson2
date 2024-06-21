package com.alibaba.fastjson2.issues_2700;

import com.alibaba.fastjson2.annotation.JSONType;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.lang.reflect.Type;
import java.util.Objects;

import static com.alibaba.fastjson2.issues_2700.Issue2726.ElementType.DIV;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2726 {
    @Test
    public void test() {
        String json = "[{\"elementType\":1}]";
        JSONArray jsonArray = JSONArray.parseArray(json);
        assertEquals(DIV, ((JSONObject) jsonArray.get(0)).to(Bean.class).getElementType());
        assertEquals(DIV, jsonArray.toJavaList(Bean.class).get(0).getElementType());
    }

    @Getter
    @Setter
    static class Bean {
        private ElementType elementType;
    }

    @JSONType(writeEnumAsJavaBean = true, deserializer = FastJsonEnumDeserializer.class)
    @Getter
    enum ElementType {
        HTML("HTML"),
        DIV("DIV");

        final String name;

        ElementType(String name) {
            this.name = name;
        }
    }

    @SuppressWarnings("rawtypes")
    static class FastJsonEnumDeserializer
            implements ObjectReader<Object> {
        @Override
        public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            if (jsonReader.nextIfNull()) {
                return null;
            }
            Class fieldClass = (Class) fieldType;
            Object value = jsonReader.readAny();
            if (fieldClass.isEnum()) {
                Enum[] enumConstants = (Enum[]) fieldClass.getEnumConstants();
                if (value instanceof Integer) {
                    for (Enum enumConstant : enumConstants) {
                        if (Objects.equals(enumConstant.ordinal(), value)) {
                            return enumConstant;
                        }
                    }
                } else {
                    for (Enum enumConstant : enumConstants) {
                        if (Objects.equals(enumConstant.name(), value)) {
                            return enumConstant;
                        }
                    }
                }
            }
            return null;
        }
    }
}

