package com.alibaba.fastjson2.stream;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class JSONStreamReader
        extends StreamReader {
    public JSONStreamReader(Type[] types) {
        super(types);
    }

    public JSONStreamReader(ObjectReaderAdapter objectReader) {
        super(objectReader);
    }

    public static JSONStreamReader of(InputStream in) throws IOException {
        return of(in, StandardCharsets.UTF_8);
    }

    public static JSONStreamReader of(InputStream in, Type... types) throws IOException {
        return of(in, StandardCharsets.UTF_8, types);
    }

    public static JSONStreamReader of(InputStream in, Charset charset, Type... types) throws IOException {
        if (charset == StandardCharsets.UTF_16 || charset == StandardCharsets.UTF_16LE || charset == StandardCharsets.UTF_16BE) {
            return new JSONStreamReaderUTF16(new InputStreamReader(in, charset), types);
        }
        return new JSONStreamReaderUTF8(in, charset, types);
    }

    public static JSONStreamReader of(InputStream in, Class objectClass) {
        return of(in, StandardCharsets.UTF_8, objectClass);
    }

    public static JSONStreamReader of(InputStream in, Charset charset, Class objectClass) {
        JSONReader.Context context = JSONFactory.createReadContext();
        ObjectReaderAdapter objectReader = (ObjectReaderAdapter) context.getObjectReader(objectClass);

        if (charset == StandardCharsets.UTF_16 || charset == StandardCharsets.UTF_16LE || charset == StandardCharsets.UTF_16BE) {
            return new JSONStreamReaderUTF16(new InputStreamReader(in, charset), objectReader);
        }
        return new JSONStreamReaderUTF8(in, charset, objectReader);
    }
}
