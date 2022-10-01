package com.alibaba.fastjson2.dubbo;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DubboTest4 {
    @Test
    public void test0() {
        MyException ex = new MyException();
        byte[] jsonbBytes = JSONB.toBytes(ex, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);
        MyException obj = (MyException) JSONB.parseObject(jsonbBytes,
                Object.class, JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);
        assertNull(obj.getMessage());
    }

    @Test
    public void test1() {
        String message = "hello world";
        MyException1 ex = new MyException1(message);
        byte[] jsonbBytes = JSONB.toBytes(ex, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);
        MyException1 obj = (MyException1) JSONB.parseObject(jsonbBytes,
                Object.class, JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);
        assertEquals(message, obj.getMessage());
    }

    @Test
    public void test2() {
        String message1 = "message1";
        String message2 = "message2";
        MyException1 ex1 = new MyException1(message1);
        MyException2 ex = new MyException2(message2, ex1);
        byte[] jsonbBytes = JSONB.toBytes(ex, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);
        MyException2 obj = (MyException2) JSONB.parseObject(jsonbBytes,
                Object.class, JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);
        assertEquals(message2, obj.getMessage());
        assertEquals(message1, obj.getCause().getMessage());
    }

    @Test
    public void test3() {
        String message = "hello world";
        MyException3 ex = new MyException3(message, null);
        byte[] jsonbBytes = JSONB.toBytes(ex, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);
        MyException3 obj = (MyException3) JSONB.parseObject(jsonbBytes,
                Object.class, JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);
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

        byte[] jsonbBytes = JSONB.toBytes(ex, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);

        MyException4 obj = (MyException4) JSONB.parseObject(jsonbBytes,
                Object.class, JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);

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
}
