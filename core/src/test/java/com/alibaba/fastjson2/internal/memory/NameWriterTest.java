package com.alibaba.fastjson2.internal.memory;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameWriterTest {
    @Test
    public void test_name() {
        List<String> names = new ArrayList<>();
        names.add("中国");
        names.add("\uD83D\uDE03");
        {
            char[] latinNameBuf = new char[64];
            latinNameBuf[0] = 'a';
            for (int i = 1; i < 64; i++) {
                latinNameBuf[i] = (char) ((i % 10) + '0');
            }

            for (int i = 1; i < 64; i++) {
                names.add(new String(latinNameBuf, 0, i));
            }

            char[] utf16NameBuf = new char[64];
            utf16NameBuf[0] = 'a';
            for (int i = 1; i < 64; i++) {
                utf16NameBuf[i] = (char) ((i % 10) + '①');
            }

            for (int i = 1; i < 64; i++) {
                names.add(new String(utf16NameBuf, 0, i));
            }
        }

        boolean[] quotes = new boolean[]{true, false};

        char[] chars = new char[1024];
        byte[] bytes = new byte[1024];
        for (String name : names) {
            for (boolean useSingleQuote : quotes) {
                char quote = useSingleQuote ? '\'' : '"';
                String expected = quote + name + quote + ":";
                byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);

                NameWriter nameWriter = NameWriter.of(name);
                assertEquals(name, nameWriter.toString());
                assertEquals(expectedBytes.length, nameWriter.utf8NameLength());
                assertEquals(expected.length(), nameWriter.utf16NameLength());

                Random r = new Random();
                int start = r.nextInt(100);
                {
                    int off = nameWriter.writeName(chars, start, useSingleQuote);
                    assertEquals(expected, new String(chars, start, off - start));
                }
                {
                    int off = nameWriter.writeName(bytes, start, useSingleQuote);
                    assertEquals(expected, new String(bytes, start, off - start));
                }
            }
        }
    }
}
