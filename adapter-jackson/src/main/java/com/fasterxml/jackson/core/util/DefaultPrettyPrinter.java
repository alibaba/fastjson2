package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.PrettyPrinter;

public class DefaultPrettyPrinter
        implements PrettyPrinter {
    public void indentObjectsWith(Indenter i) {
    }

    public interface Indenter {
    }

    public static class NopIndenter
            implements Indenter, java.io.Serializable {
    }
}
