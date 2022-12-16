package com.alibaba.fastjson2.dubbo;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DubboTest4 {
    static final JSONWriter.Feature[] writerFeatures = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.WriteNameAsSymbol
    };

    static final JSONReader.Feature[] readerFeatures = {
            JSONReader.Feature.SupportAutoType,
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.UseNativeObject,
            JSONReader.Feature.FieldBased
    };

    @Test
    public void test0() {
        MyException ex = new MyException();
        byte[] jsonbBytes = JSONB.toBytes(ex, writerFeatures);
        MyException obj = (MyException) JSONB.parseObject(jsonbBytes, Object.class, readerFeatures);
        assertNull(obj.getMessage());
    }

    @Test
    public void test1() {
        String message = "hello world";
        MyException1 ex = new MyException1(message);
        byte[] jsonbBytes = JSONB.toBytes(ex, writerFeatures);
        MyException1 obj = (MyException1) JSONB.parseObject(jsonbBytes, Object.class, readerFeatures);
        assertEquals(message, obj.getMessage());
    }

    @Test
    public void test2() {
        String message1 = "message1";
        String message2 = "message2";
        MyException1 ex1 = new MyException1(message1);
        MyException2 ex = new MyException2(message2, ex1);
        byte[] jsonbBytes = JSONB.toBytes(ex, writerFeatures);
        MyException2 obj = (MyException2) JSONB.parseObject(jsonbBytes, Object.class, readerFeatures);
        assertEquals(message2, obj.getMessage());
        assertEquals(message1, obj.getCause().getMessage());
    }

    @Test
    public void test3() {
        String message = "hello world";
        MyException3 ex = new MyException3(message, null);
        byte[] jsonbBytes = JSONB.toBytes(ex, writerFeatures);
        MyException3 obj = (MyException3) JSONB.parseObject(jsonbBytes,
                Object.class, readerFeatures);
        assertEquals(message, obj.getMessage());
    }

    public static class MyException
            extends Exception {
        public MyException() {
        }
    }

    public static class MyException1
            extends Exception {
        public MyException1() {
        }

        public MyException1(String message) {
            super(message);
        }
    }

    public static class MyException2
            extends Exception {
        public MyException2() {
        }

        public MyException2(String message) {
            super(message);
        }

        public MyException2(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class MyException3
            extends Exception {
        public MyException3() {
        }

        public MyException3(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @Test
    public void test4() {
        String message = "hello world";
        MyException4 ex = new MyException4(message);
        ex.setCode(101);

        byte[] jsonbBytes = JSONB.toBytes(ex, writerFeatures);
        MyException4 obj = (MyException4) JSONB.parseObject(jsonbBytes, Object.class, readerFeatures);

        assertEquals(message, obj.getMessage());
        assertEquals(ex.code, obj.code);
    }

    public static class MyException4
            extends Exception {
        private long code;

        public MyException4() {
            super();
            this.code = 0;
        }

        public MyException4(String message, Throwable cause) {
            super(message, cause);
            this.code = 0;
        }

        public MyException4(String message) {
            super(message);
            this.code = 0;
        }

        public MyException4(Throwable cause) {
            super(cause);
            this.code = 0;
        }

        public MyException4(long code) {
            super();
            this.code = code;
        }

        public MyException4(long code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
        }

        public MyException4(long code, String message) {
            super(message);
            this.code = code;
        }

        public MyException4(long code, Throwable cause) {
            super(cause);
            this.code = code;
        }

        public void setCode(long code) {
            this.code = code;
        }

        public long getCode() {
            return code;
        }

        @Override
        public String toString() {
            return "MyException{" +
                    "code=" + code +
                    '}';
        }
    }

    @Test
    public void test5() {
        MyException5 ex = new MyException5(101);

        byte[] jsonbBytes = JSONB.toBytes(ex, writerFeatures);
        MyException5 ex1 = (MyException5) JSONB.parseObject(jsonbBytes, Object.class, readerFeatures);

        assertEquals(ex.getMessage(), ex1.getMessage());
        assertEquals(ex.code, ex1.code);
    }

    public static class MyException5
            extends Exception {
        private final long code;

        public MyException5(long code) {
            this.code = code;
        }
    }

    @Test
    public void testDateTimeParseException() {
        DateTimeParseException ex = new DateTimeParseException("msg", "input", 0);

        byte[] jsonbBytes = JSONB.toBytes(ex, writerFeatures);
        DateTimeParseException ex1 = (DateTimeParseException) JSONB.parseObject(jsonbBytes, Object.class, readerFeatures);

        assertEquals(ex.getMessage(), ex1.getMessage());
        assertEquals(ex.getErrorIndex(), ex1.getErrorIndex());
    }

    @Test
    public void test6() {
        UncheckedIOException ex = new UncheckedIOException(null, new IOException("xxx"));

        byte[] jsonbBytes = JSONB.toBytes(ex, writerFeatures);
        UncheckedIOException ex1 = (UncheckedIOException) JSONB.parseObject(jsonbBytes, Object.class, readerFeatures);

        assertEquals(ex.getMessage(), ex1.getMessage());
        assertEquals(ex.getCause().getMessage(), ex1.getCause().getMessage());
    }

    @Test
    public void test7() {
        Exception ex = new Exception(null, new IOException("xxx"));

        byte[] jsonbBytes = JSONB.toBytes(ex, writerFeatures);
        Exception ex1 = (Exception) JSONB.parseObject(jsonbBytes, Object.class, readerFeatures);

        assertEquals(ex.getCause().getMessage(), ex1.getCause().getMessage());
    }

    @Test
    public void test8() {
        MyException8 ex = new MyException8(null);

        byte[] jsonbBytes = JSONB.toBytes(ex, writerFeatures);
        MyException8 ex1 = (MyException8) JSONB.parseObject(jsonbBytes, Object.class, readerFeatures);

        assertEquals(ex.getMessage(), ex1.getMessage());
    }

    public static class MyException8
            extends Exception {
        public MyException8(String message) {
            super(message);
        }
    }

    @Test
    public void test9() {
        MyException9 ex = new MyException9(null);

        byte[] jsonbBytes = JSONB.toBytes(ex, writerFeatures);
        MyException9 ex1 = (MyException9) JSONB.parseObject(jsonbBytes, Object.class, readerFeatures);

        assertEquals(ex.getMessage(), ex1.getMessage());
        assertEquals(ex.getCause(), ex1.getCause());
    }

    public static class MyException9
            extends Exception {
        public MyException9(Throwable cause) {
            super(cause);
        }
    }

    @Test
    public void test10() {
        MyException10 ex = new MyException10(null, null);

        byte[] jsonbBytes = JSONB.toBytes(ex, writerFeatures);
        MyException10 ex1 = (MyException10) JSONB.parseObject(jsonbBytes, Object.class, readerFeatures);

        assertEquals(ex.getMessage(), ex1.getMessage());
        assertEquals(ex.getCause(), ex1.getCause());
    }

    public static class MyException10
            extends Exception {
        public MyException10(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
