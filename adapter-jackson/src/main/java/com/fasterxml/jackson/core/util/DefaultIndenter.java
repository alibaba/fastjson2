package com.fasterxml.jackson.core.util;

public class DefaultIndenter
        extends DefaultPrettyPrinter.NopIndenter {
    private static final int INDENT_LEVELS = 16;
    private final char[] indents;
    private final int charsPerLevel;
    private final String eol;

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
}
