package com.alibaba.fastjson2.internal.memory;

import java.nio.charset.StandardCharsets;

import static com.alibaba.fastjson2.internal.Conf.BYTES;

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

        if (bytes.length == chars.length) {
            switch (bytes.length) {
                case 4:
                    return new N4(name, bytes, chars);
                case 5:
                    return new N5(name, bytes, chars);
                case 6:
                    return new N6(name, bytes, chars);
                case 7:
                    return new N7(name, bytes, chars);
                case 8:
                    return new N8(name, bytes, chars);
                case 9:
                    return new N9(name, bytes, chars);
                case 10:
                    return new N10(name, bytes, chars);
                case 11:
                    return new N11(name, bytes, chars);
                case 12:
                    return new N12(name, bytes, chars);
                case 13:
                    return new N13(name, bytes, chars);
                case 14:
                    return new N14(name, bytes, chars);
                default:
                    break;
            }
        }

        return new NameWriter(name, bytes, chars);
    }

    private static final class N4 extends NameWriter {
        final int utf8Name0;
        final int utf8Name0SingleQuote;
        final long utf16Name0;
        final long utf16Name0SingleQuote;

        private N4(String name, byte[] nameUTF8, char[] nameUTF16) {
            super(name, nameUTF8, nameUTF16);
            this.utf8Name0 = BYTES.getIntUnaligned(nameUTF8, 0);
            this.utf16Name0 = BYTES.getLongUnaligned(nameUTF16, 0);
            this.utf8Name0SingleQuote = BYTES.getIntUnaligned(nameUTF8SingleQuote, 0);
            this.utf16Name0SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 0);
        }

        @Override
        public int utf8NameLength() {
            return 4;
        }

        @Override
        public int utf16NameLength() {
            return 4;
        }

        @Override
        public int writeName(byte[] bytes, int off, boolean singleQuote) {
            BYTES.putIntUnaligned(bytes, off, singleQuote ? utf8Name0SingleQuote : utf8Name0);
            return off + 4;
        }

        @Override
        public int writeName(char[] chars, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(chars, off, singleQuote ? utf16Name0SingleQuote : utf16Name0);
            return off + 4;
        }
    }

    private static final class N5 extends NameWriter {
        final int utf8Name0;
        final int utf8Name0SingleQuote;
        final long utf16Name0;
        final long utf16Name0SingleQuote;

        private N5(String name, byte[] nameUTF8, char[] nameUTF16) {
            super(name, nameUTF8, nameUTF16);
            this.utf8Name0 = BYTES.getIntUnaligned(nameUTF8, 0);
            this.utf16Name0 = BYTES.getLongUnaligned(nameUTF16, 0);
            this.utf8Name0SingleQuote = BYTES.getIntUnaligned(nameUTF8SingleQuote, 0);
            this.utf16Name0SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 0);
        }

        @Override
        public int utf8NameLength() {
            return 5;
        }

        @Override
        public int utf16NameLength() {
            return 5;
        }

        @Override
        public int writeName(byte[] bytes, int off, boolean singleQuote) {
            BYTES.putIntUnaligned(bytes, off, singleQuote ? utf8Name0SingleQuote : utf8Name0);
            BYTES.putByte(bytes, off + 4, (byte) ':');
            return off + 5;
        }

        @Override
        public int writeName(char[] chars, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(chars, off, singleQuote ? utf16Name0SingleQuote : utf16Name0);
            BYTES.putChar(chars, off + 4, ':');
            return off + 5;
        }
    }

    private static final short UTF8_SINGLE_QUOTE_COLON, UTF8_DOUBLE_QUOTE_COLON;
    private static final int UTF16_SINGLE_QUOTE_COLON, UTF16_DOUBLE_QUOTE_COLON;
    static {
        byte[] bytes = {'"', ':'};
        UTF8_DOUBLE_QUOTE_COLON = BYTES.getShortUnaligned(bytes, 0);
        bytes[0] = '\'';
        UTF8_SINGLE_QUOTE_COLON = BYTES.getShortUnaligned(bytes, 0);

        char[] chars = {'"', ':'};
        UTF16_DOUBLE_QUOTE_COLON = BYTES.getIntUnaligned(chars, 0);
        chars[0] = '\'';
        UTF16_SINGLE_QUOTE_COLON = BYTES.getIntUnaligned(chars, 0);
    }

    private static final class N6 extends NameWriter {
        final int utf8Name0;
        final int utf8Name0SingleQuote;
        final long utf16Name0;
        final long utf16Name0SingleQuote;

        private N6(String name, byte[] nameUTF8, char[] nameUTF16) {
            super(name, nameUTF8, nameUTF16);
            this.utf8Name0 = BYTES.getIntUnaligned(nameUTF8, 0);
            this.utf16Name0 = BYTES.getLongUnaligned(nameUTF16, 0);
            this.utf8Name0SingleQuote = BYTES.getIntUnaligned(nameUTF8SingleQuote, 0);
            this.utf16Name0SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 0);
        }

        @Override
        public int utf8NameLength() {
            return 6;
        }

        @Override
        public int utf16NameLength() {
            return 6;
        }

        @Override
        public int writeName(byte[] bytes, int off, boolean singleQuote) {
            BYTES.putIntUnaligned(bytes, off, singleQuote ? utf8Name0SingleQuote : utf8Name0);
            BYTES.putShortUnaligned(bytes, off + 4, singleQuote ? UTF8_SINGLE_QUOTE_COLON : UTF8_DOUBLE_QUOTE_COLON);
            return off + 6;
        }

        @Override
        public int writeName(char[] chars, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(chars, off, singleQuote ? utf16Name0SingleQuote : utf16Name0);
            BYTES.putIntUnaligned(chars, off + 4, singleQuote ? UTF16_SINGLE_QUOTE_COLON : UTF16_DOUBLE_QUOTE_COLON);
            return off + 6;
        }
    }

    private static final class N7 extends NameWriter {
        final int utf8Name0;
        final int utf8Name0SingleQuote;
        final short utf8Name1;
        final short utf8Name1SingleQuote;

        final long utf16Name0;
        final long utf16Name0SingleQuote;

        final int utf16Name1;
        final int utf16Name1SingleQuote;

        private N7(String name, byte[] nameUTF8, char[] nameUTF16) {
            super(name, nameUTF8, nameUTF16);
            this.utf8Name0 = BYTES.getIntUnaligned(nameUTF8, 0);
            this.utf16Name0 = BYTES.getLongUnaligned(nameUTF16, 0);
            this.utf8Name0SingleQuote = BYTES.getIntUnaligned(nameUTF8SingleQuote, 0);
            this.utf16Name0SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 0);
            this.utf8Name1 = BYTES.getShortUnaligned(nameUTF8, 4);
            this.utf16Name1 = BYTES.getIntUnaligned(nameUTF16, 4);
            this.utf8Name1SingleQuote = BYTES.getShortUnaligned(nameUTF8SingleQuote, 4);
            this.utf16Name1SingleQuote = BYTES.getIntUnaligned(nameUTF16SingleQuote, 4);
        }

        @Override
        public int utf8NameLength() {
            return 7;
        }

        @Override
        public int utf16NameLength() {
            return 7;
        }

        @Override
        public int writeName(byte[] bytes, int off, boolean singleQuote) {
            BYTES.putIntUnaligned(bytes, off, singleQuote ? utf8Name0SingleQuote : utf8Name0);
            BYTES.putShortUnaligned(bytes, off + 4, singleQuote ? utf8Name1SingleQuote : utf8Name1);
            bytes[off + 6] = ':';
            return off + 7;
        }

        @Override
        public int writeName(char[] chars, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(chars, off, singleQuote ? utf16Name0SingleQuote : utf16Name0);
            BYTES.putIntUnaligned(chars, off + 4, singleQuote ? utf16Name1SingleQuote : utf16Name1);
            chars[off + 6] = ':';
            return off + 7;
        }
    }

    private static final class N8 extends NameWriter {
        final long utf8Name0;
        final long utf8Name0SingleQuote;

        final long utf16Name0;
        final long utf16Name0SingleQuote;

        final long utf16Name1;
        final long utf16Name1SingleQuote;

        private N8(String name, byte[] nameUTF8, char[] nameUTF16) {
            super(name, nameUTF8, nameUTF16);
            this.utf8Name0 = BYTES.getLongUnaligned(nameUTF8, 0);
            this.utf8Name0SingleQuote = BYTES.getLongUnaligned(nameUTF8SingleQuote, 0);

            this.utf16Name0 = BYTES.getLongUnaligned(nameUTF16, 0);
            this.utf16Name0SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 0);
            this.utf16Name1 = BYTES.getLongUnaligned(nameUTF16, 4);
            this.utf16Name1SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 4);
        }

        @Override
        public int utf8NameLength() {
            return 8;
        }

        @Override
        public int utf16NameLength() {
            return 8;
        }

        @Override
        public int writeName(byte[] bytes, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(bytes, off, singleQuote ? utf8Name0SingleQuote : utf8Name0);
            return off + 8;
        }

        @Override
        public int writeName(char[] chars, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(chars, off, singleQuote ? utf16Name0SingleQuote : utf16Name0);
            BYTES.putLongUnaligned(chars, off + 4, singleQuote ? utf16Name1SingleQuote : utf16Name1);
            return off + 8;
        }
    }

    private static final class N9 extends NameWriter {
        final long utf8Name0;
        final long utf8Name0SingleQuote;

        final long utf16Name0;
        final long utf16Name0SingleQuote;

        final long utf16Name1;
        final long utf16Name1SingleQuote;

        private N9(String name, byte[] nameUTF8, char[] nameUTF16) {
            super(name, nameUTF8, nameUTF16);
            this.utf8Name0 = BYTES.getLongUnaligned(nameUTF8, 0);
            this.utf8Name0SingleQuote = BYTES.getLongUnaligned(nameUTF8SingleQuote, 0);

            this.utf16Name0 = BYTES.getLongUnaligned(nameUTF16, 0);
            this.utf16Name0SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 0);
            this.utf16Name1 = BYTES.getLongUnaligned(nameUTF16, 4);
            this.utf16Name1SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 4);
        }

        @Override
        public int utf8NameLength() {
            return 9;
        }

        @Override
        public int utf16NameLength() {
            return 9;
        }

        @Override
        public int writeName(byte[] bytes, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(bytes, off, singleQuote ? utf8Name0SingleQuote : utf8Name0);
            bytes[off + 8] = ':';
            return off + 9;
        }

        @Override
        public int writeName(char[] chars, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(chars, off, singleQuote ? utf16Name0SingleQuote : utf16Name0);
            BYTES.putLongUnaligned(chars, off + 4, singleQuote ? utf16Name1SingleQuote : utf16Name1);
            chars[off + 8] = ':';
            return off + 9;
        }
    }

    private static final class N10 extends NameWriter {
        final long utf8Name0;
        final long utf8Name0SingleQuote;

        final long utf16Name0;
        final long utf16Name0SingleQuote;

        final long utf16Name1;
        final long utf16Name1SingleQuote;

        private N10(String name, byte[] nameUTF8, char[] nameUTF16) {
            super(name, nameUTF8, nameUTF16);
            this.utf8Name0 = BYTES.getLongUnaligned(nameUTF8, 0);
            this.utf8Name0SingleQuote = BYTES.getLongUnaligned(nameUTF8SingleQuote, 0);

            this.utf16Name0 = BYTES.getLongUnaligned(nameUTF16, 0);
            this.utf16Name0SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 0);
            this.utf16Name1 = BYTES.getLongUnaligned(nameUTF16, 4);
            this.utf16Name1SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 4);
        }

        @Override
        public int utf8NameLength() {
            return 10;
        }

        @Override
        public int utf16NameLength() {
            return 10;
        }

        @Override
        public int writeName(byte[] bytes, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(bytes, off, singleQuote ? utf8Name0SingleQuote : utf8Name0);
            BYTES.putShortUnaligned(bytes, off + 8, singleQuote ? UTF8_SINGLE_QUOTE_COLON : UTF8_DOUBLE_QUOTE_COLON);
            return off + 10;
        }

        @Override
        public int writeName(char[] chars, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(chars, off, singleQuote ? utf16Name0SingleQuote : utf16Name0);
            BYTES.putLongUnaligned(chars, off + 4, singleQuote ? utf16Name1SingleQuote : utf16Name1);
            BYTES.putIntUnaligned(chars, off + 8, singleQuote ? UTF16_SINGLE_QUOTE_COLON : UTF16_DOUBLE_QUOTE_COLON);
            return off + 10;
        }
    }

    private static final class N11 extends NameWriter {
        final long utf8Name0;
        final long utf8Name0SingleQuote;
        final short utf8Name1;
        final short utf8Name1SingleQuote;

        final long utf16Name0;
        final long utf16Name0SingleQuote;
        final long utf16Name1;
        final long utf16Name1SingleQuote;
        final int utf16Name2;
        final int utf16Name2SingleQuote;

        private N11(String name, byte[] nameUTF8, char[] nameUTF16) {
            super(name, nameUTF8, nameUTF16);
            this.utf8Name0 = BYTES.getLongUnaligned(nameUTF8, 0);
            this.utf8Name0SingleQuote = BYTES.getLongUnaligned(nameUTF8SingleQuote, 0);
            this.utf16Name0 = BYTES.getLongUnaligned(nameUTF16, 0);
            this.utf16Name0SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 0);
            this.utf16Name1 = BYTES.getLongUnaligned(nameUTF16, 4);
            this.utf16Name1SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 4);
            this.utf16Name2 = BYTES.getIntUnaligned(nameUTF16, 8);
            this.utf16Name2SingleQuote = BYTES.getIntUnaligned(nameUTF16SingleQuote, 8);
            this.utf8Name1 = BYTES.getShortUnaligned(nameUTF8, 8);
            this.utf8Name1SingleQuote = BYTES.getShortUnaligned(nameUTF8SingleQuote, 8);
        }

        @Override
        public int utf8NameLength() {
            return 11;
        }

        @Override
        public int utf16NameLength() {
            return 11;
        }

        @Override
        public int writeName(byte[] bytes, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(bytes, off, singleQuote ? utf8Name0SingleQuote : utf8Name0);
            BYTES.putShortUnaligned(bytes, off + 8, singleQuote ? utf8Name1SingleQuote : utf8Name1);
            bytes[off + 10] = ':';
            return off + 11;
        }

        @Override
        public int writeName(char[] chars, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(chars, off, singleQuote ? utf16Name0SingleQuote : utf16Name0);
            BYTES.putLongUnaligned(chars, off + 4, singleQuote ? utf16Name1SingleQuote : utf16Name1);
            BYTES.putIntUnaligned(chars, off + 8, singleQuote ? utf16Name2SingleQuote : utf16Name2);
            chars[off + 10] = ':';
            return off + 11;
        }
    }

    private static final class N12 extends NameWriter {
        final long utf8Name0;
        final long utf8Name0SingleQuote;
        final int utf8Name1;
        final int utf8Name1SingleQuote;

        final long utf16Name0;
        final long utf16Name0SingleQuote;
        final long utf16Name1;
        final long utf16Name1SingleQuote;
        final long utf16Name2;
        final long utf16Name2SingleQuote;

        private N12(String name, byte[] nameUTF8, char[] nameUTF16) {
            super(name, nameUTF8, nameUTF16);
            this.utf8Name0 = BYTES.getLongUnaligned(nameUTF8, 0);
            this.utf8Name0SingleQuote = BYTES.getLongUnaligned(nameUTF8SingleQuote, 0);
            this.utf16Name0 = BYTES.getLongUnaligned(nameUTF16, 0);
            this.utf16Name0SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 0);
            this.utf16Name1 = BYTES.getLongUnaligned(nameUTF16, 4);
            this.utf16Name1SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 4);
            this.utf16Name2 = BYTES.getLongUnaligned(nameUTF16, 8);
            this.utf16Name2SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 8);
            this.utf8Name1 = BYTES.getIntUnaligned(nameUTF8, 8);
            this.utf8Name1SingleQuote = BYTES.getIntUnaligned(nameUTF8SingleQuote, 8);
        }

        @Override
        public int utf8NameLength() {
            return 12;
        }

        @Override
        public int utf16NameLength() {
            return 12;
        }

        @Override
        public int writeName(byte[] bytes, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(bytes, off, singleQuote ? utf8Name0SingleQuote : utf8Name0);
            BYTES.putIntUnaligned(bytes, off + 8, singleQuote ? utf8Name1SingleQuote : utf8Name1);
            return off + 12;
        }

        @Override
        public int writeName(char[] chars, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(chars, off, singleQuote ? utf16Name0SingleQuote : utf16Name0);
            BYTES.putLongUnaligned(chars, off + 4, singleQuote ? utf16Name1SingleQuote : utf16Name1);
            BYTES.putLongUnaligned(chars, off + 8, singleQuote ? utf16Name2SingleQuote : utf16Name2);
            return off + 12;
        }
    }

    private static final class N13 extends NameWriter {
        final long utf8Name0;
        final long utf8Name0SingleQuote;
        final int utf8Name1;
        final int utf8Name1SingleQuote;

        final long utf16Name0;
        final long utf16Name0SingleQuote;
        final long utf16Name1;
        final long utf16Name1SingleQuote;
        final long utf16Name2;
        final long utf16Name2SingleQuote;

        private N13(String name, byte[] nameUTF8, char[] nameUTF16) {
            super(name, nameUTF8, nameUTF16);
            this.utf8Name0 = BYTES.getLongUnaligned(nameUTF8, 0);
            this.utf8Name0SingleQuote = BYTES.getLongUnaligned(nameUTF8SingleQuote, 0);
            this.utf16Name0 = BYTES.getLongUnaligned(nameUTF16, 0);
            this.utf16Name0SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 0);
            this.utf16Name1 = BYTES.getLongUnaligned(nameUTF16, 4);
            this.utf16Name1SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 4);
            this.utf16Name2 = BYTES.getLongUnaligned(nameUTF16, 8);
            this.utf16Name2SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 8);
            this.utf8Name1 = BYTES.getIntUnaligned(nameUTF8, 8);
            this.utf8Name1SingleQuote = BYTES.getIntUnaligned(nameUTF8SingleQuote, 8);
        }

        @Override
        public int utf8NameLength() {
            return 13;
        }

        @Override
        public int utf16NameLength() {
            return 13;
        }

        @Override
        public int writeName(byte[] bytes, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(bytes, off, singleQuote ? utf8Name0SingleQuote : utf8Name0);
            BYTES.putIntUnaligned(bytes, off + 8, singleQuote ? utf8Name1SingleQuote : utf8Name1);
            bytes[off + 12] = ':';
            return off + 13;
        }

        @Override
        public int writeName(char[] chars, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(chars, off, singleQuote ? utf16Name0SingleQuote : utf16Name0);
            BYTES.putLongUnaligned(chars, off + 4, singleQuote ? utf16Name1SingleQuote : utf16Name1);
            BYTES.putLongUnaligned(chars, off + 8, singleQuote ? utf16Name2SingleQuote : utf16Name2);
            chars[off + 12] = ':';
            return off + 13;
        }
    }

    private static final class N14 extends NameWriter {
        final long utf8Name0;
        final long utf8Name0SingleQuote;
        final int utf8Name1;
        final int utf8Name1SingleQuote;

        final long utf16Name0;
        final long utf16Name0SingleQuote;
        final long utf16Name1;
        final long utf16Name1SingleQuote;
        final long utf16Name2;
        final long utf16Name2SingleQuote;

        private N14(String name, byte[] nameUTF8, char[] nameUTF16) {
            super(name, nameUTF8, nameUTF16);
            this.utf8Name0 = BYTES.getLongUnaligned(nameUTF8, 0);
            this.utf8Name0SingleQuote = BYTES.getLongUnaligned(nameUTF8SingleQuote, 0);
            this.utf16Name0 = BYTES.getLongUnaligned(nameUTF16, 0);
            this.utf16Name0SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 0);
            this.utf16Name1 = BYTES.getLongUnaligned(nameUTF16, 4);
            this.utf16Name1SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 4);
            this.utf16Name2 = BYTES.getLongUnaligned(nameUTF16, 8);
            this.utf16Name2SingleQuote = BYTES.getLongUnaligned(nameUTF16SingleQuote, 8);
            this.utf8Name1 = BYTES.getIntUnaligned(nameUTF8, 8);
            this.utf8Name1SingleQuote = BYTES.getIntUnaligned(nameUTF8SingleQuote, 8);
        }

        @Override
        public int utf8NameLength() {
            return 14;
        }

        @Override
        public int utf16NameLength() {
            return 14;
        }

        @Override
        public int writeName(byte[] bytes, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(bytes, off, singleQuote ? utf8Name0SingleQuote : utf8Name0);
            BYTES.putIntUnaligned(bytes, off + 8, singleQuote ? utf8Name1SingleQuote : utf8Name1);
            BYTES.putShortUnaligned(bytes, off + 12, singleQuote ? UTF8_SINGLE_QUOTE_COLON : UTF8_DOUBLE_QUOTE_COLON);
            return off + 14;
        }

        @Override
        public int writeName(char[] chars, int off, boolean singleQuote) {
            BYTES.putLongUnaligned(chars, off, singleQuote ? utf16Name0SingleQuote : utf16Name0);
            BYTES.putLongUnaligned(chars, off + 4, singleQuote ? utf16Name1SingleQuote : utf16Name1);
            BYTES.putLongUnaligned(chars, off + 8, singleQuote ? utf16Name2SingleQuote : utf16Name2);
            BYTES.putIntUnaligned(chars, off + 12, singleQuote ? UTF16_SINGLE_QUOTE_COLON : UTF16_DOUBLE_QUOTE_COLON);
            return off + 14;
        }
    }
}
