package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderTest {
    @Test
    public void test() {
        FieldReader fieldReader = ObjectReaders.fieldReader("abc", Integer.class);
        assertNull(fieldReader.getItemClass());
        assertEquals(0, fieldReader.getItemClassHash());
    }

    @Test
    public void test1() {
        ObjectReader<Bean> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");

        JSONReader.Context context = JSONFactory.createReadContext();
        ObjectReader fieldObjectReader = fieldReader.getObjectReader(context);
        assertNotNull(fieldObjectReader);
        assertSame(fieldObjectReader, fieldReader.getObjectReader(context));
    }

    @Test
    public void test2() {
        ObjectReader<Bean> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");

        JSONReader jsonReader = JSONReader.of("{}");
        ObjectReader fieldObjectReader = fieldReader.getObjectReader(jsonReader);
        assertNotNull(fieldObjectReader);
        assertSame(fieldObjectReader, fieldReader.getObjectReader(jsonReader));

        assertNull(fieldReader.checkObjectAutoType(jsonReader));

        Bean bean = new Bean();

        fieldReader.acceptExtra(bean, null, null);
        fieldReader.processExtra(JSONReader.of("{}"), bean);

        assertThrows(Exception.class, () -> fieldReader.accept(bean, 'A'));
    }

    public static class Bean {
        private final AtomicInteger value = new AtomicInteger();

        public AtomicInteger getValue() {
            return value;
        }
    }

    @Test
    public void test3() {
        ObjectReaderBean objectReader = (ObjectReaderBean) ObjectReaderCreator.INSTANCE.createObjectReader(ExtendableBean3.class);
        FieldReader fieldReader = objectReader.extraFieldReader;
        assertNotNull(fieldReader);

        ExtendableBean3 bean = new ExtendableBean3();
        assertThrows(Throwable.class, () -> fieldReader.accept(bean, 1));
        assertThrows(Exception.class, () -> fieldReader.readFieldValue(JSONReader.of("1"), bean));
    }

    private static class ExtendableBean3 {
        private String name;

        private final Map<String, String> properties = new HashMap<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @JSONField(unwrapped = true)
        public void setProperty(String name, String value) {
            properties.put(name, value);
        }
    }

    @Test
    public void test4() {
        ObjectReader<Bean4> objectReader = ObjectReaders.objectReader(
                Bean4.class,
                Bean4::new,
                ObjectReaders.fieldReader("value", BigDecimal.class, Bean4::setValue)
        );

        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertEquals(new BigDecimal("12.34"), fieldReader.readFieldValue(JSONReader.of("12.34")));
    }

    public static class Bean4 {
        private BigDecimal value;

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }

    @Test
    public void test5() {
        ObjectReader<Bean5> objectReader = ObjectReaders.objectReader(
                Bean5.class,
                Bean5::new,
                ObjectReaders.fieldReader("value", BigInteger.class, Bean5::setValue)
        );

        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertEquals(new BigInteger("1234"), fieldReader.readFieldValue(JSONReader.of("1234")));
    }

    public static class Bean5 {
        private BigInteger value;

        public BigInteger getValue() {
            return value;
        }

        public void setValue(BigInteger value) {
            this.value = value;
        }
    }

    @Test
    public void test6() {
        ObjectReader<Bean6> objectReader = ObjectReaders.objectReader(
                Bean6.class,
                Bean6::new,
                ObjectReaders.fieldReaderDouble("value", Bean6::setValue)
        );

        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertEquals(12.34D, fieldReader.readFieldValue(JSONReader.of("12.34")));
    }

    public static class Bean6 {
        private double value;

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    @Test
    public void test7() {
        ObjectReader<Bean7> objectReader = ObjectReaders.objectReader(
                Bean7.class,
                Bean7::new,
                ObjectReaders.fieldReaderByte("value", Bean7::setValue)
        );

        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertEquals((byte) 12, fieldReader.readFieldValue(JSONReader.of("12")));
        Bean7 bean = new Bean7();
        fieldReader.accept(bean, (byte) 12);
        assertEquals(12, bean.value);
    }

    public static class Bean7 {
        private byte value;

        public byte getValue() {
            return value;
        }

        public void setValue(byte value) {
            this.value = value;
        }
    }

    @Test
    public void test8() {
        ObjectReader<Bean8> objectReader = ObjectReaders.of(
                Bean8.class,
                Bean8::new,
                ObjectReaders.fieldReaderList("values", Long.class, Bean8::setValues)
        );

        FieldReaderList fieldReader = (FieldReaderList) objectReader.getFieldReader("values");

        JSONReader.Context context = JSONFactory.createReadContext();

        assertNotNull(fieldReader.createList(context));

        Bean8 bean = new Bean8();
        fieldReader.accept(bean, new ArrayList<>());
        assertNotNull(bean.values);
    }

    public static class Bean8 {
        private List<Long> values;

        public List<Long> getValues() {
            return values;
        }

        public void setValues(List<Long> values) {
            this.values = values;
        }
    }

    @Test
    public void testRangeTemporal() {
        ObjectReader<RangeDateTime> objectReader = ObjectReaders.of(
                RangeDateTime.class,
                RangeDateTime::new,
                ObjectReaders.fieldReader(
                        "start",
                        LocalDateTime.class,
                        Temporal.class,
                        (AbstractRangeTemporal value, Temporal start) -> value.setStart(start)
                )
        );
        FieldReader start = objectReader.getFieldReader("start");
        RangeDateTime rangeDateTime = new RangeDateTime();
        LocalDateTime startTime = LocalDateTime.now();
        Assertions.assertDoesNotThrow(() -> start.acceptAny(rangeDateTime, startTime.toString(), 0));
        Assertions.assertEquals(startTime, rangeDateTime.start);
    }
    public static class RangeDateTime
            extends AbstractRangeTemporal<RangeDateTime, LocalDateTime> {
    }
    /**
     * @author 张治保
     * @since 2024/5/30
     */
    @Getter
    @ToString
    @EqualsAndHashCode
    public abstract static class AbstractRangeTemporal<S extends AbstractRangeTemporal<S, T>, T extends Temporal>
            implements Serializable {
        /**
         * 开始时间
         */
        protected T start;

        /**
         * 结束时间
         */
        protected T end;

        @SuppressWarnings("unchecked")
        public final S setStart(T start) {
            validateIt(start, end);
            this.start = start;
            return (S) this;
        }

        @SuppressWarnings("unchecked")
        public final S setEnd(T end) {
            validateIt(start, end);
            this.end = end;
            return (S) this;
        }

        /**
         * 开始时间结束时间转换成时间段
         *
         * @return 时间段
         */
        public final Duration toDuration() {
            if (start == null || end == null) {
                return null;
            }
            return Duration.between(start, end);
        }

        /**
         * 验证时间范围 设置是否正确
         *
         * @param start 开始时间
         * @param end   结束时间
         */
        private void validateIt(T start, T end) {
            if (start == null || end == null) {
                return;
            }
            if (isAfter(start, end)) {
                throw new TimeRangeException();
            }
        }

        /**
         * 判断开始时间是否在结束时间之后
         *
         * @param start 开始时间
         * @param end   结束时间
         * @return 是否在结束时间之后
         */
        protected final boolean isAfter(@NonNull T start, @NonNull T end) {
            return Duration.between(start, end).isNegative();
        }

        /**
         * 时间范围异常
         */
        public static final class TimeRangeException
                extends RuntimeException {
            public TimeRangeException() {
                super("start time cannot be greater than end time");
            }
        }
    }
}
