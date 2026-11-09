package com.alibaba.fastjson2.support.csv;

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
}
