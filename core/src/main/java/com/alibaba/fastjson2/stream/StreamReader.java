package com.alibaba.fastjson2.stream;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.io.IOException;
import java.lang.reflect.Type;

public abstract class StreamReader {
    protected static final int SIZE_256K = 1024 * 256;

    protected ObjectReaderProvider provider;
    protected long features;

    protected Type[] types;
    protected ObjectReader[] typeReaders;
    protected ObjectReaderAdapter objectReader;

    protected int lineSize;
    protected int rowCount;

    protected int lineStart;
    protected int lineEnd;
    protected int lineNextStart;

    protected int end;
    protected int off;

    protected boolean inputEnd;
    protected boolean lineTerminated;

    public StreamReader() {
    }

    public StreamReader(Type[] types) {
        this.types = types;

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader[] readers = new ObjectReader[types.length];
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            if (type == String.class || type == Object.class) {
                readers[i] = null;
            } else {
                readers[i] = provider.getObjectReader(type);
            }
        }
        this.typeReaders = readers;
    }

    public StreamReader(ObjectReaderAdapter objectReader) {
        this.objectReader = objectReader;
    }

    protected abstract boolean seekLine() throws IOException;

    public abstract <T> T readLoneObject();

    public enum Feature {
        IgnoreEmptyLine(1);

        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }
    }
}
