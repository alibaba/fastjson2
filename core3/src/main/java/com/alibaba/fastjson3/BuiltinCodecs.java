package com.alibaba.fastjson3;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.MonthDay;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;

/**
 * Built-in ObjectReader/ObjectWriter implementations for JDK types
 * that are not primitive but commonly used.
 *
 * <p>These are resolved once per type on first access and cached in ObjectMapper's
 * ConcurrentHashMap. Zero hot-path overhead for unrelated types.</p>
 */
final class BuiltinCodecs {
    private BuiltinCodecs() {
    }

    // ==================== ObjectReader factories ====================

    @SuppressWarnings("unchecked")
    static <T> ObjectReader<T> getReader(Class<T> type) {
        if (type == Optional.class) {
            return (ObjectReader<T>) OPTIONAL_READER;
        }
        if (type == OptionalInt.class) {
            return (ObjectReader<T>) OPTIONAL_INT_READER;
        }
        if (type == OptionalLong.class) {
            return (ObjectReader<T>) OPTIONAL_LONG_READER;
        }
        if (type == OptionalDouble.class) {
            return (ObjectReader<T>) OPTIONAL_DOUBLE_READER;
        }
        if (type == UUID.class) {
            return (ObjectReader<T>) UUID_READER;
        }
        if (type == Duration.class) {
            return (ObjectReader<T>) DURATION_READER;
        }
        if (type == Period.class) {
            return (ObjectReader<T>) PERIOD_READER;
        }
        if (type == Year.class) {
            return (ObjectReader<T>) YEAR_READER;
        }
        if (type == YearMonth.class) {
            return (ObjectReader<T>) YEAR_MONTH_READER;
        }
        if (type == MonthDay.class) {
            return (ObjectReader<T>) MONTH_DAY_READER;
        }
        if (type == URI.class) {
            return (ObjectReader<T>) URI_READER;
        }
        if (Path.class.isAssignableFrom(type)) {
            return (ObjectReader<T>) PATH_READER;
        }
        return null;
    }

    // ==================== ObjectWriter factories ====================

    @SuppressWarnings("unchecked")
    static <T> ObjectWriter<T> getWriter(Class<T> type) {
        if (Optional.class.isAssignableFrom(type)) {
            return (ObjectWriter<T>) OPTIONAL_WRITER;
        }
        if (type == OptionalInt.class) {
            return (ObjectWriter<T>) OPTIONAL_INT_WRITER;
        }
        if (type == OptionalLong.class) {
            return (ObjectWriter<T>) OPTIONAL_LONG_WRITER;
        }
        if (type == OptionalDouble.class) {
            return (ObjectWriter<T>) OPTIONAL_DOUBLE_WRITER;
        }
        if (type == UUID.class) {
            return (ObjectWriter<T>) UUID_WRITER;
        }
        if (type == Duration.class) {
            return (ObjectWriter<T>) DURATION_WRITER;
        }
        if (type == Period.class) {
            return (ObjectWriter<T>) PERIOD_WRITER;
        }
        if (type == Year.class) {
            return (ObjectWriter<T>) YEAR_WRITER;
        }
        if (type == YearMonth.class) {
            return (ObjectWriter<T>) YEAR_MONTH_WRITER;
        }
        if (type == MonthDay.class) {
            return (ObjectWriter<T>) MONTH_DAY_WRITER;
        }
        if (type == URI.class) {
            return (ObjectWriter<T>) URI_WRITER;
        }
        if (Path.class.isAssignableFrom(type)) {
            return (ObjectWriter<T>) PATH_WRITER;
        }
        return null;
    }

    // ==================== Optional ====================

    private static final ObjectReader<Optional<?>> OPTIONAL_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return Optional.empty();
                }
                // Resolve the element type T from Optional<T>
                Class<?> elemClass = null;
                if (fieldType instanceof ParameterizedType pt) {
                    Type[] args = pt.getActualTypeArguments();
                    if (args.length == 1 && args[0] instanceof Class<?> c) {
                        elemClass = c;
                    }
                }
                if (elemClass != null && elemClass != Object.class) {
                    // Use ObjectReader for T if available (e.g., Optional<User>)
                    ObjectReader<?> elemReader = ObjectMapper.shared().getObjectReader(elemClass);
                    if (elemReader != null) {
                        return Optional.of(elemReader.readObject(parser, elemClass, fieldName, features));
                    }
                }
                // Fallback: auto-detect type
                Object value = parser.readAny();
                return Optional.ofNullable(value);
            };

    private static final ObjectWriter<Optional<?>> OPTIONAL_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                Optional<?> opt = (Optional<?>) object;
                if (opt == null || opt.isEmpty()) {
                    generator.writeNull();
                } else {
                    generator.writeAny(opt.get());
                }
            };

    private static final ObjectReader<OptionalInt> OPTIONAL_INT_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return OptionalInt.empty();
                }
                return OptionalInt.of(parser.readInt());
            };

    private static final ObjectWriter<OptionalInt> OPTIONAL_INT_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                OptionalInt opt = (OptionalInt) object;
                if (opt == null || opt.isEmpty()) {
                    generator.writeNull();
                } else {
                    generator.writeInt32(opt.getAsInt());
                }
            };

    private static final ObjectReader<OptionalLong> OPTIONAL_LONG_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return OptionalLong.empty();
                }
                return OptionalLong.of(parser.readLong());
            };

    private static final ObjectWriter<OptionalLong> OPTIONAL_LONG_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                OptionalLong opt = (OptionalLong) object;
                if (opt == null || opt.isEmpty()) {
                    generator.writeNull();
                } else {
                    generator.writeInt64(opt.getAsLong());
                }
            };

    private static final ObjectReader<OptionalDouble> OPTIONAL_DOUBLE_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return OptionalDouble.empty();
                }
                return OptionalDouble.of(parser.readDouble());
            };

    private static final ObjectWriter<OptionalDouble> OPTIONAL_DOUBLE_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                OptionalDouble opt = (OptionalDouble) object;
                if (opt == null || opt.isEmpty()) {
                    generator.writeNull();
                } else {
                    generator.writeDouble(opt.getAsDouble());
                }
            };

    // ==================== UUID ====================

    private static final ObjectReader<UUID> UUID_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readString();
                return str == null ? null : UUID.fromString(str);
            };

    private static final ObjectWriter<UUID> UUID_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeString(((UUID) object).toString());
            };

    // ==================== Duration / Period ====================

    private static final ObjectReader<Duration> DURATION_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readString();
                return str == null ? null : Duration.parse(str);
            };

    private static final ObjectWriter<Duration> DURATION_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeString(((Duration) object).toString());
            };

    private static final ObjectReader<Period> PERIOD_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readString();
                return str == null ? null : Period.parse(str);
            };

    private static final ObjectWriter<Period> PERIOD_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeString(((Period) object).toString());
            };

    // ==================== Year / YearMonth / MonthDay ====================

    private static final ObjectReader<Year> YEAR_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                return Year.of(parser.readInt());
            };

    private static final ObjectWriter<Year> YEAR_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeInt32(((Year) object).getValue());
            };

    private static final ObjectReader<YearMonth> YEAR_MONTH_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readString();
                return str == null ? null : YearMonth.parse(str);
            };

    private static final ObjectWriter<YearMonth> YEAR_MONTH_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeString(((YearMonth) object).toString());
            };

    private static final ObjectReader<MonthDay> MONTH_DAY_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readString();
                return str == null ? null : MonthDay.parse(str);
            };

    private static final ObjectWriter<MonthDay> MONTH_DAY_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeString(((MonthDay) object).toString());
            };

    // ==================== URI / Path ====================

    private static final ObjectReader<URI> URI_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readString();
                return str == null ? null : URI.create(str);
            };

    private static final ObjectWriter<URI> URI_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeString(((URI) object).toString());
            };

    private static final ObjectReader<Path> PATH_READER =
            (parser, fieldType, fieldName, features) -> {
                String str = parser.readString();
                return str == null ? null : Path.of(str);
            };

    private static final ObjectWriter<Path> PATH_WRITER =
            (generator, object, fieldName, fieldType, features) -> {
                generator.writeString(((Path) object).toString());
            };
}
