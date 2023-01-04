package com.alibaba.fastjson2.adapter.jackson.core.util;

import com.alibaba.fastjson2.adapter.jackson.core.JsonGenerator;
import com.alibaba.fastjson2.adapter.jackson.core.PrettyPrinter;

import java.io.IOException;

public class DefaultPrettyPrinter
        implements PrettyPrinter {
    protected Indenter objectIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;

    public DefaultPrettyPrinter withObjectIndenter(Indenter i) {
        if (i == null) {
            i = NopIndenter.instance;
        }
        if (objectIndenter == i) {
            return this;
        }
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter(this);
        pp.objectIndenter = i;
        return pp;
    }

    public DefaultPrettyPrinter() {
    }

    public DefaultPrettyPrinter(DefaultPrettyPrinter base) {
        this.objectIndenter = base.objectIndenter;
    }

    public interface Indenter {
        void writeIndentation(JsonGenerator g, int level) throws IOException;

        boolean isInline();
    }

    public static class NopIndenter
            implements Indenter, java.io.Serializable {
        public static final NopIndenter instance = new NopIndenter();

        @Override
        public void writeIndentation(JsonGenerator g, int level) throws IOException {
        }

        @Override
        public boolean isInline() {
            return true;
        }
    }
}
