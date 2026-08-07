package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2269 {
    @Test
    public void test() {
        AppDto dto = new AppDto();
        dto.alarmStatus = AlarmStatus.RUNNING;
        String str = JSON.toJSONString(dto);
        assertEquals("{\"alarmStatus\":\"启用\"}", str);
        AppDto dto1 = JSON.parseObject(str, AppDto.class);
        assertEquals(dto.alarmStatus, dto1.alarmStatus);
    }

    @JSONType(serializer = DictSerializer.class, deserializer = DictDeserializer.class)
    public enum AlarmStatus {
        RUNNING("启用", 1),
        STOP("停止", 2);

        final String name;
        final int value;

        AlarmStatus(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    static class DictSerializer
            implements ObjectSerializer {
        @Override
        public void write(
                JSONSerializer serializer,
                Object object,
                Object fieldName,
                Type fieldType,
                int features) throws IOException {
            AlarmStatus status = (AlarmStatus) object;
            serializer.write(status.name);
        }
    }

    static class DictDeserializer
            implements ObjectDeserializer {
        @Override
        public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            JSONLexer lexer = parser.getLexer();
            lexer.nextToken();
            String str = lexer.stringVal();
            switch (str) {
                case "启用":
                    return (T) AlarmStatus.RUNNING;
                case "停止":
                    return (T) AlarmStatus.STOP;
                default:
                    return null;
            }
        }

        @Override
        public int getFastMatchToken() {
            return 0;
        }
    }

    public static class AppDto {
        public AlarmStatus alarmStatus;
    }
}
