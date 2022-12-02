package com.alibaba.fastjson2.adapter.jackson.core.util;

public class DefaultIndenter
        extends DefaultPrettyPrinter.NopIndenter {
    public static final String SYS_LF;

    static {
        String lf;
        try {
            lf = System.getProperty("line.separator");
        } catch (Throwable t) {
            lf = "\n"; // fallback when security manager denies access
        }
        SYS_LF = lf;
    }

    public static final DefaultIndenter SYSTEM_LINEFEED_INSTANCE = new DefaultIndenter("  ", SYS_LF);

    private static final int INDENT_LEVELS = 16;
    private final char[] indents;
    private final int charsPerLevel;
    private final String eol;

    public DefaultIndenter() {
        this("  ", SYS_LF);
    }

    public DefaultIndenter(String indent, String eol) {
        charsPerLevel = indent.length();

        indents = new char[indent.length() * INDENT_LEVELS];
        int offset = 0;
        for (int i = 0; i < INDENT_LEVELS; i++) {
            indent.getChars(0, indent.length(), indents, offset);
            offset += indent.length();
        }

        this.eol = eol;
    }

    public DefaultIndenter withLinefeed(String lf) {
        if (lf.equals(eol)) {
            return this;
        }
        return new DefaultIndenter(getIndent(), lf);
    }

    public String getIndent() {
        return new String(indents, 0, charsPerLevel);
    }
}
