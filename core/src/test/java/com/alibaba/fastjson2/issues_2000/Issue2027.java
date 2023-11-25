package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2027 {
    @Test
    public void test() {
        JSON.register(DescEnum.class, new DescEnumWriter());
        assertEquals(
                "{\"test\":\"bcccc\"}",
                JSON.toJSONString(new Bean().setTest(TestEnum.BB))
        );
    }

    @Getter
    @RequiredArgsConstructor
    public enum TestEnum
            implements DescEnum {
        AA("asss"),
        BB("bcccc");

        private final String desc;
    }

    @JSONType(enableSerialize = true)
    public interface DescEnum {
        String getDesc();
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Bean {
        private TestEnum test;
    }

    public static class DescEnumWriter
            implements ObjectWriter<DescEnum> {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            DescEnum descEnum = (DescEnum) object;
            jsonWriter.writeString(descEnum.getDesc());
        }
    }
}
