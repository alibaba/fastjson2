package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriter;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue512 {
    @AfterEach
    public void tearDown() {
        JSONFactory.getDefaultObjectWriterProvider().unregister(LocalTime.class);
        JSONFactory.getDefaultObjectWriterProvider().unregister(LocalDate.class);
        JSONFactory.getDefaultObjectWriterProvider().unregister(LocalDateTime.class);

        JSON.mixIn(LocalTime.class, null);
        JSON.mixIn(LocalDate.class, null);
        JSON.mixIn(LocalDateTime.class, null);

        JSONFactory.getDefaultObjectWriterProvider().unregister(Model.class);
    }

    @Test
    public void testMixin() {
        Model model = new Model();
        model.setLocalDateTime(LocalDateTime.of(2022, 7, 1, 8, 47, 45, 514000000));
        model.setLocalDate(LocalDate.of(2022, 7, 1));
        model.setLocalTime(LocalTime.of(8, 47, 45, 515000000));

        JSON.mixIn(LocalTime.class, LocalTimeMixin.class);
        JSON.mixIn(LocalDateTime.class, LocalDateTimeMixin.class);
        assertEquals("{\"localDate\":\"2022-07-01\",\"localDateTime\":\"2022-07-01 08:47:45\",\"localTime\":\"08:47:45\"}", JSON.toJSONString(model));
        JSON.mixIn(LocalTime.class, null);
        JSON.mixIn(LocalDateTime.class, null);
    }

    @Test
    public void testRegister() {
        Model model = new Model();
        model.setLocalDateTime(LocalDateTime.of(2022, 7, 1, 8, 47, 45, 514000000));
        model.setLocalDate(LocalDate.of(2022, 7, 1));
        model.setLocalTime(LocalTime.of(8, 47, 45, 515000000));

        JSON.register(LocalTime.class, new LocalTimeWriter());
        JSON.register(LocalDate.class, new LocalDateWriter());
        JSON.register(LocalDateTime.class, new LocalDateTimeWriter());

        assertEquals("{\"localDate\":\"2022/07/01\",\"localDateTime\":\"2022-07-01 08:47:45\",\"localTime\":\"08:47:45\"}", JSON.toJSONString(model));
    }

    @Test
    public void test0() {
        Model model = new Model();
        model.setLocalDateTime(LocalDateTime.of(2022, 7, 1, 8, 47, 45, 514000000));
        model.setLocalDate(LocalDate.of(2022, 7, 1));
        model.setLocalTime(LocalTime.of(8, 47, 45, 515000000));
        assertEquals("{\"localDate\":\"2022-07-01\",\"localDateTime\":\"2022-07-01 08:47:45.514\",\"localTime\":\"08:47:45.515\"}", JSON.toJSONString(model));
    }

    @Getter
    @Setter
    public static class Model {
        private LocalDateTime localDateTime;
        private LocalDate localDate;
        private LocalTime localTime;
    }

    @JSONType(format = "HH:mm:ss")
    public static class LocalTimeMixin {
    }

    @JSONType(format = "yyyy-MM-dd HH:mm:ss")
    public static class LocalDateTimeMixin {
    }

    public static class LocalTimeWriter
            implements ObjectWriter {
        static DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            LocalTime localTime = (LocalTime) object;
            String str = FORMAT.format(localTime);
            jsonWriter.writeString(str);
        }
    }

    public static class LocalDateWriter
            implements ObjectWriter {
        static DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            LocalDate localDate = (LocalDate) object;
            String str = FORMAT.format(localDate);
            jsonWriter.writeString(str);
        }
    }

    public static class LocalDateTimeWriter
            implements ObjectWriter {
        static DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            LocalDateTime ldt = (LocalDateTime) object;
            String str = FORMAT.format(ldt);
            jsonWriter.writeString(str);
        }
    }
}
