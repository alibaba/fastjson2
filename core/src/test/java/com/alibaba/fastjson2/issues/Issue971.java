package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue971 {
    @Test
    public void test() {
        Clazz t = new Clazz();
        t.setInt32(Misc.IntEnum.CLOSE);
        t.setInt64(Misc.LongEnum.HIGH);
        t.setAbc(ABC.A);

        String s = write(t);
        // System.out.println(s); // 期望输出{"abc":"A","int32":10,"int64":100}, 实际输出{"abc":"A","int32":"CLOSE","int64":"HIGH"}
        assertEquals("{\"abc\":\"A\",\"int32\":10,\"int64\":100}", s);
//
//        String json = "{\"abc\":\"A\", \"int32\":10,\"int64\":100}";
//        Clazz tt = read(json, Clazz.class);
//        System.out.println(tt.int32);
//        System.out.println(tt.int64);
//        System.out.println(tt.abc);
    }

    public <T> T read(String json, Type type) {
        ObjectReaderProvider readers = new ObjectReaderProvider();
        readers.mixIn(Misc.PersistentEnum.class, PersistentEnumMixin.class);
        JSONReader.Context context = new JSONReader.Context(readers);
        try (final JSONReader reader = JSONReader.of(context, json)) {
            ObjectReader<T> v = reader.getObjectReader(type);
            return v.readObject(reader, 0);
        }
    }

    public String write(Object value) {
        ObjectWriterProvider writers = new ObjectWriterProvider();
        writers.mixIn(Misc.PersistentEnum.class, PersistentEnumMixin.class);
        JSONWriter.Context context = new JSONWriter.Context(writers);
        try (final JSONWriter writer = JSONWriter.of(context)) {
            if (value == null) {
                writer.writeNull();
            } else {
                context.config(WriteEnumUsingToString);
                context.config(WriteNonStringKeyAsString);
                context.config(WriteNulls);
                final Class<?> clazz = value.getClass();
                ObjectWriter<?> v = writer.getObjectWriter(clazz, clazz);
                v.write(writer, value, null, null, 0);
            }
            return writer.toString();
        }
    }

    public enum ABC {
        A, B, C;
    }

    public static class Clazz {
        Misc.IntEnum int32;
        Misc.LongEnum int64;
        ABC abc;

        public Misc.IntEnum getInt32() {
            return int32;
        }

        public void setInt32(Misc.IntEnum int32) {
            this.int32 = int32;
        }

        public Misc.LongEnum getInt64() {
            return int64;
        }

        public void setInt64(Misc.LongEnum int64) {
            this.int64 = int64;
        }

        public ABC getAbc() {
            return abc;
        }

        public void setAbc(ABC abc) {
            this.abc = abc;
        }
    }

    public interface PersistentEnumMixin<T> {
        @JSONField(value = true)
        T getValue();
    }

    public static class Misc {
        public enum IntEnum
                implements PersistentEnum<Integer> {
            OPEN(6), CLOSE(10);

            int value;

            IntEnum(int value) {
                this.value = value;
            }

            @Override
            public Integer getValue() {
                return value;
            }

            @Override
            public Map<Integer, IntEnum> getAll() {
                Map<Integer, IntEnum> map = new HashMap<>();
                map.put(6, OPEN);
                map.put(10, CLOSE);
                return map;
            }

            public static IntEnum parse(int value) {
                if (value == OPEN.value) {
                    return OPEN;
                } else if (value == CLOSE.value) {
                    return CLOSE;
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        }

        public enum LongEnum
                implements PersistentEnum<Long> {
            HIGH(100L), LOW(200L);

            long value;

            LongEnum(long value) {
                this.value = value;
            }

            @Override
            public Long getValue() {
                return value;
            }

            @Override
            public Map<Long, LongEnum> getAll() {
                Map<Long, LongEnum> map = new HashMap<>();
                map.put(100L, HIGH);
                map.put(200L, LOW);
                return map;
            }

            public static LongEnum parse(int value) {
                if (value == HIGH.value) {
                    return HIGH;
                } else if (value == LOW.value) {
                    return LOW;
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        }

        public interface PersistentEnum<T> {
            T getValue();

            Map<T, ? extends PersistentEnum<T>> getAll();
        }
    }
}
