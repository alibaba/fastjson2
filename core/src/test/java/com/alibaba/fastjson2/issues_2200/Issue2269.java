package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.Test;

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
            implements ObjectWriter {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            AlarmStatus status = (AlarmStatus) object;
            jsonWriter.writeString(status.name);
        }
    }

    static class DictDeserializer
            implements ObjectReader<AlarmStatus> {
        @Override
        public AlarmStatus readObject(JSONReader reader, Type type, Object name, long features) {
            if (type == null) {
                return null;
            }
            String str = reader.readString();
            switch (str) {
                case "启用":
                    return AlarmStatus.RUNNING;
                case "停止":
                    return AlarmStatus.STOP;
                default:
                    return null;
            }
        }
    }

    public static class AppDto {
        public AlarmStatus alarmStatus;
    }
}
