package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

public class Issue1729 {
    @Test
    public void test() {
        JSONObject object = new JSONObject();
        object.put("demo", 1);
        Demo compRule = JSONObject.toJavaObject(object, Demo.class);
        System.out.println(JSON.toJSONString(compRule));
    }

    public static class EnumJsonHandler
            implements ObjectSerializer, ObjectDeserializer {
        @Override
        public long getFeatures() {
            return ObjectSerializer.super.getFeatures();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object deserialze(DefaultJSONParser parser, Type type, Object o) {
            if (Objects.isNull(type)) {
                throw new RuntimeException("类型为空");
            }
            if (type instanceof ParameterizedType) {
                //    规则实现
            } else {
                //    规则实现
            }
            return null;
        }

        @Override
        public int getFastMatchToken() {
            return 0;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void write(JSONSerializer jsonSerializer, Object o, Object o1, Type type, int i) throws IOException {
        }
    }

    @Getter
    @Setter
    @ToString
    public static class Demo {
        @JSONField(serializeUsing = EnumJsonHandler.class, deserializeUsing = EnumJsonHandler.class)
        private EnumDemo demo;

        enum EnumDemo {
            A,
            B,
        }
    }
}
