package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.util.TypeUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2329 {
    @Test
    public void test() {
        assertEquals(Type.Big, JSON.parseObject("\"L\"", Type.class));
        assertEquals(Type.Big, TypeUtils.cast("L", Type.class));
    }

    @JSONType(deserializer = TypeDeser.class)
    public enum Type {
        Big,
        Small
    }

    static class TypeDeser
            implements ObjectReader {
        public Object readObject(
                JSONReader jsonReader,
                java.lang.reflect.Type fieldType,
                Object fieldName,
                long features
        ) {
            String str = jsonReader.readString();
            switch (str) {
                case "Big":
                case "L":
                    return Type.Big;
                case "Small":
                case "S":
                    return Type.Small;
                default:
                    return null;
            }
        }
    }
}
