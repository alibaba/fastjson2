package com.alibaba.fastjson2.support.csv;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Random;

public class CapacityTest {
    @Test
    public void writeString_utf16() throws Exception {
        try (CSVWriterUTF16 writer = (CSVWriterUTF16) CSVWriter.of(new NoopWriter())) {
            for (int i = 0; i < 100_000; i++) {
                writer.writeString("a");
            }
        }
        try (CSVWriterUTF16 writer = (CSVWriterUTF16) CSVWriter.of(new NoopWriter())) {
            for (int i = 0; i < 100_000; i++) {
                writer.writeString("\n");
                writer.writeString("\r");
                writer.writeString("\"");
            }
        }
        try (CSVWriterUTF16 writer = (CSVWriterUTF16) CSVWriter.of(new NoopWriter())) {
            writer.writeString(
                    repeat('x', 100_000));
            writer.writeString(
                    repeat(',', 100_000));
        }
        try (CSVWriterUTF16 writer = (CSVWriterUTF16) CSVWriter.of(new NoopWriter())) {
            Random r = new Random();
            for (int i = 0; i < 100_000; i++) {
                writer.writeString(
                        repeat('x', r.nextInt(1000)));
            }
        }
    }

    @Test
    public void writeString_utf8() throws Exception {
        try (CSVWriterUTF8 writer = (CSVWriterUTF8) CSVWriter.of(new NoopOutputStream())) {
            for (int i = 0; i < 100_000; i++) {
                writer.writeString("a");
            }
        }
        try (CSVWriterUTF8 writer = (CSVWriterUTF8) CSVWriter.of(new NoopOutputStream())) {
            for (int i = 0; i < 100_000; i++) {
                writer.writeString("\n");
                writer.writeString("\r");
                writer.writeString("\"");
            }
        }
        try (CSVWriterUTF8 writer = (CSVWriterUTF8) CSVWriter.of(new NoopOutputStream())) {
            writer.writeString(
                    repeat('x', 100_000));
            writer.writeString(
                    repeat(',', 100_000));
        }
        try (CSVWriterUTF8 writer = (CSVWriterUTF8) CSVWriter.of(new NoopOutputStream())) {
            Random r = new Random();
            for (int i = 0; i < 100_000; i++) {
                writer.writeString(
                        repeat('x', r.nextInt(1000)));
            }
        }
    }

    static String repeat(char c, int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, c);
        return new String(chars);
    }

    static class NoopWriter
            extends Writer {
        @Override
        public void write(@NotNull char[] cbuf, int off, int len) throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
    }

    static class NoopOutputStream
            extends java.io.OutputStream {
        @Override
        public void write(int b) throws IOException {
        }
    }
}
