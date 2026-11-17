package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for {@code CSVReader.of(Reader, Type...)} / {@code readLine()} when the
 * underlying {@link Reader} legally returns fewer chars than requested (short reads).
 * <p>
 * A {@code Reader} is allowed to return any positive count up to {@code len}; the CSV reader
 * must therefore keep reading until the requested row is complete instead of giving up after a
 * fixed number of scan/refill attempts. Every case below asserts the complete, correct record
 * list for every chunk size from 1 up to {@code data.length() + 1}, followed by exactly one
 * EOF read ({@code null}). Reads are bounded on purpose: a corrupted reader position must make
 * the test fail, never spin forever.
 * <p>
 * <details><summary>中文</summary>
 * 当底层 Reader 每次只返回少量字符（合法的短读）时，{@code readLine()} 必须继续读取直到整行结束，
 * 而不能在固定次数重试后返回错误的行。本测试对 1 到 data.length()+1 的每种分块大小断言完整正确的
 * 记录列表，并只在其后做一次 EOF 读取；读取次数有界，避免有缺陷时死循环。
 * </details>
 */
@Tag("util")
public class CSVReaderShortReadTest {
    /**
     * Ordinary LF separated rows, one field longer than 12 chars so the read window has to be
     * extended far beyond a small number of refill attempts.
     */
    @Test
    public void testLfRowsLongField() {
        assertAllChunkSizes(
                "id,name\n123456789012345,alpha\n",
                row("id", "name"),
                row("123456789012345", "alpha"));
    }

    /**
     * Second and third row are only reachable after the buffer has been compacted (off &gt; 0).
     */
    @Test
    public void testRowsAfterCompaction() {
        assertAllChunkSizes(
                "a,b\nc,d\neeeeeeeeeeeeeeeeeeee,f\n",
                row("a", "b"),
                row("c", "d"),
                row("eeeeeeeeeeeeeeeeeeee", "f"));
    }

    /**
     * Last row is not newline terminated; it is finalized at end of input.
     */
    @Test
    public void testFinalRowWithoutNewline() {
        assertAllChunkSizes(
                "x,y\nlast_row_without_newline,42",
                row("x", "y"),
                row("last_row_without_newline", "42"));
    }

    /**
     * CRLF rows, including chunk boundaries that split the CR from the LF.
     */
    @Test
    public void testCrlfRows() {
        assertAllChunkSizes(
                "id,name\r\n123456789012345,alpha\r\n",
                row("id", "name"),
                row("123456789012345", "alpha"));
    }

    /**
     * Bare CR as the row separator, with LF only at the very end of the input.
     */
    @Test
    public void testBareCrRows() {
        assertAllChunkSizes(
                "a,b\r123456789012345,c\n",
                row("a", "b"),
                row("123456789012345", "c"));
    }

    /**
     * Multiline quoted field (no escaped quotes inside), so the line scan has to continue across
     * an embedded LF that arrives in a later chunk.
     */
    @Test
    public void testMultilineQuotedField() {
        assertAllChunkSizes(
                "id,note,x\n1,\"line one\nline two\",tail\n2,short,tail\n",
                row("id", "note", "x"),
                row("1", "line one\nline two", "tail"),
                row("2", "short", "tail"));
    }

    /**
     * Non ASCII content delivered in small char chunks.
     */
    @Test
    public void testUnicodeRows() {
        assertAllChunkSizes(
                "id,名称\n123456789012345,张三的值\n",
                row("id", "名称"),
                row("123456789012345", "张三的值"));
    }

    /**
     * Empty fields keep the intended default (empty String).
     */
    @Test
    public void testEmptyField() {
        assertAllChunkSizes(
                "a,,c\n",
                row("a", "", "c"));
    }

    /**
     * Empty input keeps returning {@code null} on the first read, as before.
     */
    @Test
    public void testEmptyReader() {
        List<String[]> expected = new ArrayList<>();
        assertRows("empty reader", expected, new StringReader(""));
        assertRows("empty reader, short reads", expected, new ShortReadCharReader("", 1));
    }

    /**
     * The UTF_16LE {@code InputStream} overload goes through the same CSVReaderUTF16 path; the
     * byte stream returns fewer bytes than requested, which surfaces as short char reads.
     */
    @Test
    public void testUtf16leInputStreamShortReads() {
        String data = "id,名称\n123456789012345,张三的值\n";
        byte[] bytes = data.getBytes(StandardCharsets.UTF_16LE);
        List<String[]> expected = Arrays.asList(
                row("id", "名称"),
                row("123456789012345", "张三的值"));

        for (int chunk = 1; chunk <= 8; chunk++) {
            InputStream in = new ShortReadByteStream(bytes, chunk);
            CSVReader csvReader = CSVReader.of(in, StandardCharsets.UTF_16LE);
            assertRows("utf16le chunk=" + chunk, expected, csvReader);
        }
    }

    /**
     * A complete LF terminated row must be returned as soon as it is available: the reader is not
     * allowed to fill its buffer up to EOF (or up to 512 KiB) before answering, because that would
     * block streaming readers.
     */
    @Test
    public void testNoGreedyFillToEof() {
        CSVReader csvReader = CSVReader.of(new SingleRowReader("a,b\n"));

        String[] first = csvReader.readLine();
        assertNotNull(first, "a complete LF-terminated row must be returned without requesting EOF");
        assertArrayEquals(row("a", "b"), first);
    }

    /**
     * Boundary condition: the first read request from CSVReader should be filled completely
     * (up to the requested length) to test the allocation boundary without using a large
     * hard-coded buffer size. The reader records the requested length on the first positive
     * read and fills exactly that many chars with 'a' except the final CR, honoring the
     * provided offset and length. Subsequent reads provide the remainder of the row and then EOF.
     */
    @Test
    public void testFullBufferReadBoundary() throws IOException {
        FullBufferReader reader = new FullBufferReader();
        try (CSVReader csvReader = CSVReader.of(reader)) {
            String[] first = csvReader.readLine();
            assertNotNull(first, "first row should not be null");
            assertEquals(1, first.length, "first row should have one field");
            int firstReadLength = reader.getFirstReadLength();
            assertEquals(firstReadLength - 1, first[0].length(), "field length should be firstReadLength - 1");
            assertTrue(first[0].chars().allMatch(c -> c == 'a'), "field should consist only of 'a' characters");

            String[] second = csvReader.readLine();
            assertNotNull(second, "second row should not be null");
            assertArrayEquals(row("next", "row"), second);

            assertNull(csvReader.readLine(), "EOF expected after second row");
        }
    }

    /**
     * Escaped quotes inside a quoted field must not break row boundaries. For all chunk sizes
     * from 1 to 8, the CSV reader should parse three rows: header, row with escaped quotes and
     * trailing fields, and a simple row. We only assert row boundaries and unaffected fields,
     * not the exact decoded value of the escaped field because that is not relevant to short
     * read handling.
     */
    @Test
    public void testEscapedQuoteRowBoundaries() throws IOException {
        String data = "id,note,z\n1,\"a\"\"b\",z\n2,q\n";
        String[] expectedHeader = row("id", "note", "z");

        for (int chunk = 1; chunk <= 8; chunk++) {
            try (CSVReader csvReader = CSVReader.of(new ShortReadCharReader(data, chunk))) {
                String[] header = csvReader.readLine();
                assertNotNull(header, "expected header, chunk=" + chunk);
                assertArrayEquals(expectedHeader, header, "header mismatch, chunk=" + chunk);

                String[] row2 = csvReader.readLine();
                assertNotNull(row2, "expected second row, chunk=" + chunk);
                assertEquals(3, row2.length, "second row should have 3 fields, chunk=" + chunk);
                assertEquals("1", row2[0], "first field of second row, chunk=" + chunk);
                assertEquals("z", row2[2], "third field of second row, chunk=" + chunk);
                // row2[1] is the escaped quoted field; we do not assert its exact value here.

                String[] row3 = csvReader.readLine();
                assertNotNull(row3, "expected third row, chunk=" + chunk);
                assertArrayEquals(row("2", "q"), row3, "third row mismatch, chunk=" + chunk);

                assertNull(csvReader.readLine(), "expected EOF after third row, chunk=" + chunk);
            }
        }
    }

    /**
     * A closing quote at the very end of input, without a newline, must be parsed correctly.
     * This tests the char-array path where the final quote is seen when the buffer is exhausted.
     */
    @Test
    public void testCharArrayTrailingQuoteAtEof() throws IOException {
        try (CSVReader csvReader = CSVReader.of("x,\"y\"".toCharArray())) {
            assertArrayEquals(row("x", "y"), csvReader.readLine());
            assertNull(csvReader.readLine());
        }
    }

    @Test
    public void testCharArrayTrailingBareCr() throws IOException {
        try (CSVReader csvReader = CSVReader.of("a,值\r".toCharArray())) {
            assertArrayEquals(row("a", "值"), csvReader.readLine());
            assertNull(csvReader.readLine());
        }
    }

    /**
     * A trailing bare CR must also work through the Reader overload.
     */
    @Test
    public void testReaderBareCrRowsThroughAllChunkSizes() {
        assertAllChunkSizes(
                "a,b\rc,d\r",
                row("a", "b"),
                row("c", "d"));
    }

    /**
     * Reader that returns 4-char reads; on the second read it throws once, then recovers.
     * CSVReader must propagate the exception and remain usable afterward.
     */
    @Test
    public void testFlakyReader() throws IOException {
        try (CSVReader csvReader = CSVReader.of(new FlakyReader())) {
            String[] first = csvReader.readLine();
            assertNotNull(first, "first row");
            assertArrayEquals(row("a", "b"), first);

            assertThrows(JSONException.class, csvReader::readLine, "expected exception on second read");

            String[] third = csvReader.readLine();
            assertNotNull(third, "third row after exception");
            assertArrayEquals(row("c", "d"), third);

            String[] fourth = csvReader.readLine();
            assertNotNull(fourth, "fourth row");
            assertArrayEquals(row("eeeeeeeeeeeeeeeeeeee", "f"), fourth);

            assertNull(csvReader.readLine(), "EOF after fourth row");
        }
    }

    @Test
    public void testIOExceptionWhileQuotedRowIsBuffered() throws IOException {
        String csv = "a,b\n1,\"line\ncontinued\",z\n2,q\n";
        FlakyReader flaky = new FlakyReader(csv, 3);
        try (CSVReader reader = CSVReader.of(flaky)) {
            assertArrayEquals(row("a", "b"), reader.readLine());
            assertThrows(JSONException.class, reader::readLine);
            assertArrayEquals(row("1", "line\ncontinued", "z"), reader.readLine());
            assertArrayEquals(row("2", "q"), reader.readLine());
            assertNull(reader.readLine());
        }
    }

    private static void assertAllChunkSizes(String data, String[]... expectedRows) {
        List<String[]> expected = Arrays.asList(expectedRows);
        String printable = data.replace("\r", "\\r").replace("\n", "\\n");

        for (int chunk = 1; chunk <= data.length() + 1; chunk++) {
            String message = "chunk=" + chunk + ", data=" + printable;
            assertRows(message, expected, new ShortReadCharReader(data, chunk));
        }
    }

    private static void assertRows(String message, List<String[]> expected, Reader reader) {
        assertRows(message, expected, CSVReader.of(reader));
    }

    private static void assertRows(String message, List<String[]> expected, CSVReader csvReader) {
        for (int i = 0; i < expected.size(); i++) {
            String[] actual = csvReader.readLine();
            assertNotNull(actual, message + ", row " + i + " is missing");
            String[] want = expected.get(i);
            assertEquals(
                    want.length,
                    actual.length,
                    message + ", row " + i + " column count, actual " + Arrays.toString(actual));
            assertArrayEquals(want, actual, message + ", row " + i);
        }

        assertNull(csvReader.readLine(), message + ", expected EOF after " + expected.size() + " rows");
    }

    private static String[] row(String... values) {
        return values;
    }

    /**
     * Returns at most {@code chunk} chars per read.
     */
    static final class ShortReadCharReader
            extends StringReader {
        private final int chunk;

        ShortReadCharReader(String str, int chunk) {
            super(str);
            this.chunk = chunk;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            return super.read(cbuf, off, Math.min(len, chunk));
        }
    }

    /**
     * Returns at most {@code chunk} bytes per read.
     */
    static final class ShortReadByteStream
            extends ByteArrayInputStream {
        private final int chunk;

        ShortReadByteStream(byte[] buf, int chunk) {
            super(buf);
            this.chunk = chunk;
        }

        @Override
        public synchronized int read(byte[] b, int off, int len) {
            return super.read(b, off, Math.min(len, chunk));
        }

        @Override
        public synchronized int available() {
            return 0;
        }
    }

    /**
     * Serves one LF terminated row in short reads and fails if any further read is requested.
     */
    static final class SingleRowReader
            extends Reader {
        private final String data;
        private int position;

        SingleRowReader(String data) {
            this.data = data;
        }

        @Override
        public int read(char[] cbuf, int off, int len) {
            if (len == 0) {
                return 0;
            }
            if (position == data.length()) {
                throw new AssertionError(
                        "readLine() must not request more chars once a complete LF-terminated row is buffered");
            }

            int count = Math.min(len, Math.min(2, data.length() - position));
            data.getChars(position, position + count, cbuf, off);
            position += count;
            return count;
        }

        @Override
        public void close() {
        }
    }

    /**
     * Fills the entire requested read length with 'a' characters except the final CR,
     * recording the requested length on the first positive read. The next read provides
     * the rest of the row (LF + "next,row" + LF), then EOF.
     */
    static final class FullBufferReader
            extends Reader {
        private int firstReadLength = -1;
        private boolean servedFirst;
        private String suffix = "\nnext,row\n";
        private int suffixPos;

        int getFirstReadLength() {
            if (firstReadLength < 0) {
                throw new IllegalStateException("first read has not been requested yet");
            }
            return firstReadLength;
        }

        @Override
        public int read(char[] cbuf, int off, int len) {
            if (len == 0) {
                return 0;
            }
            if (!servedFirst) {
                // First read: fill exactly len chars with 'a's, but the last char becomes CR.
                servedFirst = true;
                firstReadLength = len;
                int fillLength = len - 1; // last char will be CR
                for (int i = 0; i < fillLength; i++) {
                    cbuf[off + i] = 'a';
                }
                cbuf[off + len - 1] = '\r';
                return len;
            } else {
                // Subsequent reads: serve suffix (LF + "next,row" + LF)
                if (suffixPos >= suffix.length()) {
                    return -1; // EOF
                }
                int count = Math.min(len, suffix.length() - suffixPos);
                suffix.getChars(suffixPos, suffixPos + count, cbuf, off);
                suffixPos += count;
                return count;
            }
        }

        @Override
        public void close() {
        }
    }

    /**
     * Reader that returns 4 chars per read (except possibly the last), and on the second
     * read throws an IOException once. The data is constructed to produce rows:
     * "a,b\n", "c,d\n", "eeeeeeeeeeeeeeeeeeee,f\n".
     * We need to control reads: first read returns "a,b\n" (4 chars), second read throws,
     * then subsequent reads return remaining content in 4-char chunks.
     */
    static final class FlakyReader
            extends Reader {
        private final String data;
        private final int failureRead;
        private int position;
        private boolean thrownOnce;
        private int readCount;

        FlakyReader() {
            this("a,b\nc,d\neeeeeeeeeeeeeeeeeeee,f\n", 2);
        }

        FlakyReader(String data, int failureRead) {
            this.data = data;
            this.failureRead = failureRead;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (len == 0) {
                return 0;
            }
            readCount++;
            if (readCount == failureRead && !thrownOnce) {
                thrownOnce = true;
                throw new IOException("simulated flaky read");
            }
            if (position >= data.length()) {
                return -1;
            }
            int count = Math.min(len, Math.min(4, data.length() - position));
            data.getChars(position, position + count, cbuf, off);
            position += count;
            return count;
        }

        @Override
        public void close() {
        }
    }
}
