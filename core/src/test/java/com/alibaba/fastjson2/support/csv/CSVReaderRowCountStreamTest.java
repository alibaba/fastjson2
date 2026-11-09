package com.alibaba.fastjson2.support.csv;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests for {@link CSVReader#rowCount(java.io.InputStream)}.
 * <p>
 * The row count must not depend on how the underlying stream splits the data into
 * read() chunks, so every short dataset is replayed through a stream that returns
 * at most 1, 2, 3, ... data.length + 1 bytes per read() call.
 */
@Tag("util")
public class CSVReaderRowCountStreamTest {
    /**
     * Size of the buffer used by {@link CSVReader#rowCount(java.io.InputStream)}.
     */
    static final int BUFFER_SIZE = 512 * 1024;

    @Test
    public void testCRLFAndBareCR() throws IOException {
        assertEachChunk("a\r\nb\r\n", 2);
        assertEachChunk("a\rb\r", 2);
        assertEachChunk("a\r\nb\rc\n", 3);
    }

    @Test
    public void testClosingQuoteBeforeTerminator() throws IOException {
        assertEachChunk("\"a\"\nb\n", 2);
        assertEachChunk("\"a\"\r\nb\r\n", 2);
        assertEachChunk("\"a\",\"b\"\nc\n", 2);
    }

    @Test
    public void testDoubledQuoteInsideQuotedField() throws IOException {
        assertEachChunk("\"a\"\"b\"\nc\n", 2);
        assertEachChunk("\"a\"\"b\"\r\nc\r\n", 2);
        assertEachChunk("x,\"a\"\"b\"\ny\n", 2);
    }

    @Test
    public void testQuotedFieldEndingWithEscapedQuote() throws IOException {
        assertEachChunk("\"a\"\"\"\nb\n", 2);
        assertEachChunk("\"a\"\"\"\r\nb\r\n", 2);
    }

    /**
     * Runs of 1, 2, 3 and 4 quotes, each of them must be counted the same way
     * no matter where the read() boundary falls inside the run.
     */
    @Test
    public void testQuotedRunsOfQuotes() throws IOException {
        // run of 1 quote: two simple quoted fields
        assertEachChunk("\"a\",\"b\"\nb\nc", 3);
        // run of 2 quotes: one escaped quote inside a quoted field
        assertEachChunk("\"a\"\"b\"\nb\nc", 3);
        // run of 3 quotes: escaped quote followed by the closing quote
        assertEachChunk("\"a\"\"\"\nb\nc", 3);
        // run of 4 quotes: two escaped quotes inside a quoted field
        assertEachChunk("\"a\"\"\"\"b\"\nb\nc", 3);
    }

    @Test
    public void testQuotedMultilineWithCRLF() throws IOException {
        assertEachChunk("\"a\r\nb\"\r\nc\r\n", 2);
        assertEachChunk("\"a\nb\"\nc\n", 2);
        assertEachChunk("\"a\r\nb\"\nc\r\n", 2);
    }

    @Test
    public void testEmptyRecords() throws IOException {
        assertEachChunk("\n", 1);
        assertEachChunk("a\n\nb\n", 3);
        assertEachChunk("\"a\"\n\n\"b\"\n", 3);
    }

    @Test
    public void testUnterminatedFinalRows() throws IOException {
        // unterminated final ordinary row
        assertEachChunk("a", 1);
        assertEachChunk("a\nb", 2);
        assertEachChunk("a\rb", 2);
        // unterminated final quoted row
        assertEachChunk("\"a\"\n\"b\"", 2);
        assertEachChunk("\"a\"\"b\"\nc", 2);
    }

    @Test
    public void testEmptyStream() throws IOException {
        assertEachChunk("", 0);
        assertEquals(0, CSVReader.rowCount(new ByteArrayInputStream(new byte[0])));
    }

    @Test
    public void testBufferBoundary() throws IOException {
        // CR as the last byte of the first read, LF as the first byte of the second read
        StringBuilder buf = new StringBuilder(BUFFER_SIZE + 16);
        repeat(buf, 'a', BUFFER_SIZE - 1);
        buf.append("\r\nb\n");
        assertEquals(2, CSVReader.rowCount(new ByteArrayInputStream(toUTF8(buf))));

        // closing quote as the last byte of the first read, LF as the first byte of the second read
        buf = new StringBuilder(BUFFER_SIZE + 16);
        buf.append('"');
        repeat(buf, 'a', BUFFER_SIZE - 2);
        buf.append("\"\nb\n");
        assertEquals(2, CSVReader.rowCount(new ByteArrayInputStream(toUTF8(buf))));

        // closing quote as the first byte of the second read
        buf = new StringBuilder(BUFFER_SIZE + 16);
        buf.append('"');
        repeat(buf, 'a', BUFFER_SIZE - 1);
        buf.append("\"\nb\n");
        assertEquals(2, CSVReader.rowCount(new ByteArrayInputStream(toUTF8(buf))));
    }

    /**
     * Asserts the expected row count for the default 512KiB buffer as well as for every
     * possible read() chunk size of the given data.
     */
    private static void assertEachChunk(String data, int expected) throws IOException {
        byte[] bytes = toUTF8(data);
        assertEquals(expected, CSVReader.rowCount(new ByteArrayInputStream(bytes)), describe("default", data));

        for (int chunk = 1; chunk <= bytes.length + 1; chunk++) {
            int actual = CSVReader.rowCount(new ChunkedInputStream(bytes, chunk));
            assertEquals(expected, actual, describe("chunk=" + chunk, data));
        }
    }

    private static String describe(String prefix, String data) {
        return prefix + ", data=\"" + data.replace("\r", "\\r").replace("\n", "\\n") + "\"";
    }

    private static byte[] toUTF8(CharSequence chars) {
        return chars.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void repeat(StringBuilder buf, char ch, int count) {
        for (int i = 0; i < count; i++) {
            buf.append(ch);
        }
    }

    /**
     * Stream returning at most {@code chunk} bytes per read() call, used to move the
     * read() boundaries into every position of the data.
     */
    static final class ChunkedInputStream
            extends ByteArrayInputStream {
        private final int chunk;

        ChunkedInputStream(byte[] buf, int chunk) {
            super(buf);
            this.chunk = chunk;
        }

        @Override
        public synchronized int read(byte[] b, int off, int len) {
            return super.read(b, off, Math.min(len, chunk));
        }
    }
}
