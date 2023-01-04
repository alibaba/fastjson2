package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class OptionalTest {
    @Test
    public void testOptionalInt() {
        assertEquals(123, JSON.parseObject("123", OptionalInt.class).getAsInt());
        assertEquals(123, JSON.parseObject("123.0", OptionalInt.class).getAsInt());
        assertEquals(123, JSON.parseObject("'123'", OptionalInt.class).getAsInt());
        assertEquals(123, JSON.parseObject("\"123\"", OptionalInt.class).getAsInt());
        assertFalse(JSON.parseObject("\"\"", OptionalInt.class).isPresent());
        assertFalse(JSON.parseObject("''", OptionalInt.class).isPresent());
        assertFalse(JSON.parseObject("null", OptionalInt.class).isPresent());
    }

    @Test
    public void testOptionalLong() {
        assertEquals(123, JSON.parseObject("123", OptionalLong.class).getAsLong());
        assertEquals(123, JSON.parseObject("123.0", OptionalLong.class).getAsLong());
        assertEquals(123, JSON.parseObject("'123'", OptionalLong.class).getAsLong());
        assertEquals(123, JSON.parseObject("\"123\"", OptionalLong.class).getAsLong());
        assertFalse(JSON.parseObject("\"\"", OptionalLong.class).isPresent());
        assertFalse(JSON.parseObject("''", OptionalLong.class).isPresent());
        assertFalse(JSON.parseObject("null", OptionalLong.class).isPresent());
    }

    @Test
    public void testOptionalDouble() {
        assertEquals(123, JSON.parseObject("123", OptionalDouble.class).getAsDouble());
        assertEquals(123, JSON.parseObject("123.0", OptionalDouble.class).getAsDouble());
        assertEquals(123, JSON.parseObject("'123'", OptionalDouble.class).getAsDouble());
        assertEquals(123, JSON.parseObject("\"123\"", OptionalDouble.class).getAsDouble());
        assertFalse(JSON.parseObject("\"\"", OptionalDouble.class).isPresent());
        assertFalse(JSON.parseObject("''", OptionalDouble.class).isPresent());
        assertFalse(JSON.parseObject("null", OptionalDouble.class).isPresent());
    }

    @Test
    public void testOptional_Integer() {
        assertEquals(Integer.valueOf(123),
                new TypeReference<Optional<Integer>>() {
                }
                        .parseObject("\"123\"")
                        .get()
        );

        assertEquals(Integer.valueOf(123),
                new TypeReference<Optional<Integer>>() {
                }
                        .parseObject("'123'")
                        .get()
        );

        assertEquals(Integer.valueOf(123),
                new TypeReference<Optional<Integer>>() {
                }
                        .parseObject("123")
                        .get()
        );

        assertEquals(Integer.valueOf(123),
                new TypeReference<Optional<Integer>>() {
                }
                        .parseObject("123.0")
                        .get()
        );

        assertFalse(new TypeReference<Optional<Integer>>() {
                }
                        .parseObject("null")
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<Integer>>() {
                }
                        .parseObject("\"\"")
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<Integer>>() {
                }
                        .parseObject("''")
                        .isPresent()
        );
    }

    @Test
    public void testOptional_Integer_utf8() {
        assertEquals(Integer.valueOf(123),
                new TypeReference<Optional<Integer>>() {
                }
                        .parseObject("\"123\"".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertEquals(Integer.valueOf(123),
                new TypeReference<Optional<Integer>>() {
                }
                        .parseObject("'123'".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertEquals(Integer.valueOf(123),
                new TypeReference<Optional<Integer>>() {
                }
                        .parseObject("123".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertEquals(Integer.valueOf(123),
                new TypeReference<Optional<Integer>>() {
                }
                        .parseObject("123.0".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertFalse(new TypeReference<Optional<Integer>>() {
                }
                        .parseObject("null".getBytes(StandardCharsets.UTF_8))
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<Integer>>() {
                }
                        .parseObject("\"\"".getBytes(StandardCharsets.UTF_8))
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<Integer>>() {
                }
                        .parseObject("''".getBytes(StandardCharsets.UTF_8))
                        .isPresent()
        );
    }

    @Test
    public void testOptional_Integer_ascii() {
        {
            byte[] asciiBytes = "\"123\"".getBytes(StandardCharsets.UTF_8);
            Optional<Integer> optional = JSON.parseObject(asciiBytes, 0, asciiBytes.length, StandardCharsets.US_ASCII, new TypeReference<Optional<Integer>>() {
            }.getType());
            assertEquals(Integer.valueOf(123), optional.get());
        }
        {
            byte[] asciiBytes = "'123'".getBytes(StandardCharsets.UTF_8);
            Optional<Integer> optional = JSON.parseObject(asciiBytes, 0, asciiBytes.length, StandardCharsets.US_ASCII, new TypeReference<Optional<Integer>>() {
            }.getType());
            assertEquals(Integer.valueOf(123), optional.get());
        }
        {
            byte[] asciiBytes = "123".getBytes(StandardCharsets.UTF_8);
            Optional<Integer> optional = JSON.parseObject(asciiBytes, 0, asciiBytes.length, StandardCharsets.US_ASCII, new TypeReference<Optional<Integer>>() {
            }.getType());
            assertEquals(Integer.valueOf(123), optional.get());
        }
        {
            byte[] asciiBytes = "''".getBytes(StandardCharsets.UTF_8);
            Optional<Integer> optional = JSON.parseObject(asciiBytes, 0, asciiBytes.length, StandardCharsets.US_ASCII, new TypeReference<Optional<Integer>>() {
            }.getType());
            assertFalse(optional.isPresent());
        }
        {
            byte[] asciiBytes = "\"\"".getBytes(StandardCharsets.UTF_8);
            Optional<Integer> optional = JSON.parseObject(asciiBytes, 0, asciiBytes.length, StandardCharsets.US_ASCII, new TypeReference<Optional<Integer>>() {
            }.getType());
            assertFalse(optional.isPresent());
        }
    }

    @Test
    public void testOptional_Long() {
        assertEquals(Long.valueOf(123),
                new TypeReference<Optional<Long>>() {
                }
                        .parseObject("\"123\"")
                        .get()
        );

        assertEquals(Long.valueOf(123),
                new TypeReference<Optional<Long>>() {
                }
                        .parseObject("'123'")
                        .get()
        );

        assertEquals(Long.valueOf(123),
                new TypeReference<Optional<Long>>() {
                }
                        .parseObject("123")
                        .get()
        );

        assertEquals(Long.valueOf(123),
                new TypeReference<Optional<Long>>() {
                }
                        .parseObject("123.0")
                        .get()
        );

        assertFalse(new TypeReference<Optional<Long>>() {
                }
                        .parseObject("null")
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<Long>>() {
                }
                        .parseObject("\"\"")
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<Long>>() {
                }
                        .parseObject("''")
                        .isPresent()
        );
    }

    @Test
    public void testOptional_Long_utf8() {
        assertEquals(Long.valueOf(123),
                new TypeReference<Optional<Long>>() {
                }
                        .parseObject("\"123\"".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertEquals(Long.valueOf(123),
                new TypeReference<Optional<Long>>() {
                }
                        .parseObject("'123'".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertEquals(Long.valueOf(123),
                new TypeReference<Optional<Long>>() {
                }
                        .parseObject("123".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertEquals(Long.valueOf(123),
                new TypeReference<Optional<Long>>() {
                }
                        .parseObject("123.0".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertFalse(new TypeReference<Optional<Long>>() {
                }
                        .parseObject("null".getBytes(StandardCharsets.UTF_8))
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<Long>>() {
                }
                        .parseObject("\"\"".getBytes(StandardCharsets.UTF_8))
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<Long>>() {
                }
                        .parseObject("''".getBytes(StandardCharsets.UTF_8))
                        .isPresent()
        );
    }

    @Test
    public void testOptional_Float() {
        assertEquals(Float.valueOf(123),
                new TypeReference<Optional<Float>>() {
                }
                        .parseObject("\"123\"")
                        .get()
        );

        assertEquals(Float.valueOf(123),
                new TypeReference<Optional<Float>>() {
                }
                        .parseObject("'123'")
                        .get()
        );

        assertEquals(Float.valueOf(123),
                new TypeReference<Optional<Float>>() {
                }
                        .parseObject("123")
                        .get()
        );

        assertEquals(Float.valueOf(123),
                new TypeReference<Optional<Float>>() {
                }
                        .parseObject("123.0")
                        .get()
        );

        assertFalse(new TypeReference<Optional<Float>>() {
                }
                        .parseObject("null")
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<Float>>() {
                }
                        .parseObject("\"\"")
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<Float>>() {
                }
                        .parseObject("''")
                        .isPresent()
        );
    }

    @Test
    public void testOptional_Float_utf8() {
        assertEquals(Float.valueOf(123),
                new TypeReference<Optional<Float>>() {
                }
                        .parseObject("\"123\"".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertEquals(Float.valueOf(123),
                new TypeReference<Optional<Float>>() {
                }
                        .parseObject("'123'".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertEquals(Float.valueOf(123),
                new TypeReference<Optional<Float>>() {
                }
                        .parseObject("123".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertEquals(Float.valueOf(123),
                new TypeReference<Optional<Float>>() {
                }
                        .parseObject("123.0".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertFalse(new TypeReference<Optional<Float>>() {
                }
                        .parseObject("null".getBytes(StandardCharsets.UTF_8))
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<Float>>() {
                }
                        .parseObject("\"\"".getBytes(StandardCharsets.UTF_8))
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<Float>>() {
                }
                        .parseObject("''".getBytes(StandardCharsets.UTF_8))
                        .isPresent()
        );
    }

    @Test
    public void testOptional_Double() {
        assertEquals(Double.valueOf(123),
                new TypeReference<Optional<Double>>() {
                }
                        .parseObject("\"123\"")
                        .get()
        );

        assertEquals(Double.valueOf(123),
                new TypeReference<Optional<Double>>() {
                }
                        .parseObject("'123'")
                        .get()
        );

        assertEquals(Double.valueOf(123),
                new TypeReference<Optional<Double>>() {
                }
                        .parseObject("123")
                        .get()
        );

        assertEquals(Double.valueOf(123),
                new TypeReference<Optional<Double>>() {
                }
                        .parseObject("123.0")
                        .get()
        );

        assertFalse(new TypeReference<Optional<Double>>() {
                }
                        .parseObject("null")
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<Double>>() {
                }
                        .parseObject("\"\"")
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<Double>>() {
                }
                        .parseObject("''")
                        .isPresent()
        );
    }

    @Test
    public void testOptional_Double_utf8() {
        assertEquals(Double.valueOf(123),
                new TypeReference<Optional<Double>>() {
                }
                        .parseObject("\"123\"".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertEquals(Double.valueOf(123),
                new TypeReference<Optional<Double>>() {
                }
                        .parseObject("'123'".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertEquals(Double.valueOf(123),
                new TypeReference<Optional<Double>>() {
                }
                        .parseObject("123".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertEquals(Double.valueOf(123),
                new TypeReference<Optional<Double>>() {
                }
                        .parseObject("123.0".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertFalse(new TypeReference<Optional<Double>>() {
                }
                        .parseObject("null".getBytes(StandardCharsets.UTF_8))
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<Double>>() {
                }
                        .parseObject("\"\"".getBytes(StandardCharsets.UTF_8))
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<Double>>() {
                }
                        .parseObject("''".getBytes(StandardCharsets.UTF_8))
                        .isPresent()
        );
    }

    @Test
    public void testOptional_BigDecimal() {
        assertEquals(BigDecimal.valueOf(123),
                new TypeReference<Optional<BigDecimal>>() {
                }
                        .parseObject("\"123\"")
                        .get()
        );

        assertEquals(BigDecimal.valueOf(123),
                new TypeReference<Optional<BigDecimal>>() {
                }
                        .parseObject("'123'")
                        .get()
        );

        assertEquals(BigDecimal.valueOf(123),
                new TypeReference<Optional<BigDecimal>>() {
                }
                        .parseObject("123")
                        .get()
        );

        assertEquals(new BigDecimal("123.0"),
                new TypeReference<Optional<BigDecimal>>() {
                }
                        .parseObject("123.0")
                        .get()
        );

        assertFalse(new TypeReference<Optional<BigDecimal>>() {
                }
                        .parseObject("null")
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<BigDecimal>>() {
                }
                        .parseObject("\"\"")
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<BigDecimal>>() {
                }
                        .parseObject("''")
                        .isPresent()
        );
    }

    @Test
    public void testOptional_BigDecimal_utf8() {
        assertEquals(BigDecimal.valueOf(123),
                new TypeReference<Optional<BigDecimal>>() {
                }
                        .parseObject("\"123\"".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertEquals(BigDecimal.valueOf(123),
                new TypeReference<Optional<BigDecimal>>() {
                }
                        .parseObject("'123'".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertEquals(BigDecimal.valueOf(123),
                new TypeReference<Optional<BigDecimal>>() {
                }
                        .parseObject("123".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertEquals(new BigDecimal("123.0"),
                new TypeReference<Optional<BigDecimal>>() {
                }
                        .parseObject("123.0".getBytes(StandardCharsets.UTF_8))
                        .get()
        );

        assertFalse(new TypeReference<Optional<BigDecimal>>() {
                }
                        .parseObject("null".getBytes(StandardCharsets.UTF_8))
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<BigDecimal>>() {
                }
                        .parseObject("\"\"".getBytes(StandardCharsets.UTF_8))
                        .isPresent()
        );

        assertFalse(new TypeReference<Optional<BigDecimal>>() {
                }
                        .parseObject("''".getBytes(StandardCharsets.UTF_8))
                        .isPresent()
        );
    }

    @Test
    public void testOptional_Integer_Field_utf8() {
        assertEquals(
                Integer.valueOf(123),
                JSON.parseObject(
                        "{\"value\":\"123\"}}".getBytes(StandardCharsets.UTF_8),
                        Bean_Integer.class,
                        JSONReader.Feature.IgnoreCheckClose
                ).value.get());
        assertEquals(
                Integer.valueOf(123),
                JSON.parseObject(
                        "{\"value\":'123'}}".getBytes(StandardCharsets.UTF_8),
                        Bean_Integer.class,
                        JSONReader.Feature.IgnoreCheckClose
                ).value.get()
        );
        assertEquals(
                Integer.valueOf(123),
                JSON.parseObject(
                        "{\"value\":123}}".getBytes(StandardCharsets.UTF_8),
                        Bean_Integer.class,
                        JSONReader.Feature.IgnoreCheckClose
                ).value.get()
        );
        assertEquals(
                Integer.valueOf(123),
                JSON.parseObject(
                        "{\"value\":123.}}".getBytes(StandardCharsets.UTF_8),
                        Bean_Integer.class,
                        JSONReader.Feature.IgnoreCheckClose
                ).value.get()
        );
        assertEquals(
                Integer.valueOf(123),
                JSON.parseObject(
                        "{\"value\":123.0}}".getBytes(StandardCharsets.UTF_8),
                        Bean_Integer.class,
                        JSONReader.Feature.IgnoreCheckClose
                ).value.get()
        );
        assertFalse(
                JSON.parseObject(
                        "{\"value\":\"\"}}".getBytes(StandardCharsets.UTF_8),
                        Bean_Integer.class,
                        JSONReader.Feature.IgnoreCheckClose
                ).value.isPresent()
        );
        assertFalse(
                JSON.parseObject(
                        "{\"value\":''}}".getBytes(StandardCharsets.UTF_8),
                        Bean_Integer.class,
                        JSONReader.Feature.IgnoreCheckClose
                ).value.isPresent()
        );
        assertFalse(
                JSON.parseObject(
                        "{\"value\":null}}".getBytes(StandardCharsets.UTF_8),
                        Bean_Integer.class,
                        JSONReader.Feature.IgnoreCheckClose
                ).value.isPresent()
        );
    }

    @Test
    public void testOptional_Integer_Field() {
        assertEquals(Integer.valueOf(123), JSON.parseObject("{\"value\":\"123\"}", Bean_Integer.class).value.get());
        assertEquals(Integer.valueOf(123), JSON.parseObject("{\"value\":'123'}", Bean_Integer.class).value.get());
        assertEquals(Integer.valueOf(123), JSON.parseObject("{\"value\":123}", Bean_Integer.class).value.get());
        assertEquals(Integer.valueOf(123), JSON.parseObject("{\"value\":123.}", Bean_Integer.class).value.get());
        assertEquals(Integer.valueOf(123), JSON.parseObject("{\"value\":123.0}", Bean_Integer.class).value.get());
        assertFalse(JSON.parseObject("{\"value\":\"\"}", Bean_Integer.class).value.isPresent());
        assertFalse(JSON.parseObject("{\"value\":''}", Bean_Integer.class).value.isPresent());
        assertFalse(JSON.parseObject("{\"value\":null}", Bean_Integer.class).value.isPresent());
    }

    public static class Bean_Integer {
        public Optional<Integer> value;
    }

    @Test
    public void testOptional_Long_Field_utf8() {
        assertEquals(Long.valueOf(123), JSON.parseObject("{\"value\":\"123\"}".getBytes(StandardCharsets.UTF_8), Bean_Long.class).value.get());
        assertEquals(Long.valueOf(123), JSON.parseObject("{\"value\":'123'}".getBytes(StandardCharsets.UTF_8), Bean_Long.class).value.get());
        assertEquals(Long.valueOf(123), JSON.parseObject("{\"value\":123}".getBytes(StandardCharsets.UTF_8), Bean_Long.class).value.get());
        assertEquals(Long.valueOf(123), JSON.parseObject("{\"value\":123.}".getBytes(StandardCharsets.UTF_8), Bean_Long.class).value.get());
        assertEquals(Long.valueOf(123), JSON.parseObject("{\"value\":123.0}".getBytes(StandardCharsets.UTF_8), Bean_Long.class).value.get());
        assertFalse(JSON.parseObject("{\"value\":\"\"}".getBytes(StandardCharsets.UTF_8), Bean_Long.class).value.isPresent());
        assertFalse(JSON.parseObject("{\"value\":''}".getBytes(StandardCharsets.UTF_8), Bean_Long.class).value.isPresent());
        assertFalse(JSON.parseObject("{\"value\":null}".getBytes(StandardCharsets.UTF_8), Bean_Long.class).value.isPresent());
    }

    @Test
    public void testOptional_Long_Field() {
        assertEquals(Long.valueOf(123), JSON.parseObject("{\"value\":\"123\"}", Bean_Long.class).value.get());
        assertEquals(Long.valueOf(123), JSON.parseObject("{\"value\":'123'}", Bean_Long.class).value.get());
        assertEquals(Long.valueOf(123), JSON.parseObject("{\"value\":123}", Bean_Long.class).value.get());
        assertEquals(Long.valueOf(123), JSON.parseObject("{\"value\":123.}", Bean_Long.class).value.get());
        assertEquals(Long.valueOf(123), JSON.parseObject("{\"value\":123.0}", Bean_Long.class).value.get());
        assertFalse(JSON.parseObject("{\"value\":\"\"}", Bean_Long.class).value.isPresent());
        assertFalse(JSON.parseObject("{\"value\":''}", Bean_Long.class).value.isPresent());
        assertFalse(JSON.parseObject("{\"value\":null}", Bean_Long.class).value.isPresent());
    }

    public static class Bean_Long {
        public Optional<Long> value;
    }

    @Test
    public void getObjectClass() {
        assertEquals(
                Optional.class,
                JSONFactory
                        .getDefaultObjectReaderProvider()
                        .getObjectReader(Optional.class)
                        .getObjectClass()
        );
    }
}
