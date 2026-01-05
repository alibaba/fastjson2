package com.alibaba.fastjson2.internal.memory;

import java.nio.charset.StandardCharsets;

public class NameWriter {
    /**
     * The original field name string
     */
    protected final String name;

    /**
     * UTF-8 encoded field name, including quotes and colon ("name":)
     */
    protected final byte[] nameUTF8;
    protected final byte[] nameUTF8SingleQuote;

    /**
     * UTF-16 encoded field name, including quotes and colon ("name":)
     */
    protected final char[] nameUTF16;
    protected final char[] nameUTF16SingleQuote;

    /**
     * Constructor to create a NameWriter instance
     *
     * @param name The original field name
     * @param nameUTF8 UTF-8 encoded field name (including quotes and colon)
     * @param nameUTF16 UTF-16 encoded field name (including quotes and colon)
     */
    private NameWriter(String name, byte[] nameUTF8, char[] nameUTF16) {
        this.name = name;
        this.nameUTF8 = nameUTF8;
        this.nameUTF16 = nameUTF16;

        byte[] nameUTF8SingleQuote = nameUTF8.clone();
        nameUTF8SingleQuote[0] = '\'';
        nameUTF8SingleQuote[nameUTF8SingleQuote.length - 2] = '\'';
        this.nameUTF8SingleQuote = nameUTF8SingleQuote;

        char[] nameUTF16SingleQuote = nameUTF16.clone();
        nameUTF16SingleQuote[0] = '\'';
        nameUTF16SingleQuote[nameUTF16SingleQuote.length - 2] = '\'';
        this.nameUTF16SingleQuote = nameUTF16SingleQuote;
    }

    public int utf8NameLength() {
        return nameUTF8.length;
    }

    public int utf16NameLength() {
        return nameUTF16.length;
    }

    public int writeName(byte[] bytes, int off, boolean singleQuote) {
        byte[] nameUTF8 = singleQuote ? nameUTF8SingleQuote : this.nameUTF8;
        System.arraycopy(nameUTF8, 0, bytes, off, nameUTF8.length);
        return off + nameUTF8.length;
    }

    public int writeName(char[] chars, int off, boolean singleQuote) {
        char[] nameUTF16 = singleQuote ? nameUTF16SingleQuote : this.nameUTF16;
        System.arraycopy(nameUTF16, 0, chars, off, nameUTF16.length);
        return off + nameUTF16.length;
    }

    public String toString() {
        return name;
    }

    public static NameWriter of(String name) {
        byte[] utf8 = name.getBytes(StandardCharsets.UTF_8);
        int nameLength = name.length();

        // Create UTF-8 encoded byte array
        byte[] bytes = new byte[utf8.length + 3];
        System.arraycopy(utf8, 0, bytes, 1, utf8.length);
        bytes[0] = '"';
        bytes[bytes.length - 2] = '"';
        bytes[bytes.length - 1] = ':';

        // Create UTF-16 encoded character array
        char[] chars = new char[nameLength + 3];
        chars[0] = '"';  // Opening quote
        name.getChars(0, name.length(), chars, 1);  // Field name
        chars[chars.length - 2] = '"';  // Closing quote
        chars[chars.length - 1] = ':';  // Colon

        return new NameWriter(name, bytes, chars);
    }
}
