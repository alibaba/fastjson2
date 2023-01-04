package com.alibaba.fastjson2.support;

import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.util.Fnv;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;

import static com.alibaba.fastjson2.reader.ObjectReaders.fieldReader;

public class AwtRederModule
        implements ObjectReaderModule {
    // Android not support
    static final long HASH_X = Fnv.hashCode64("x");
    static final long HASH_Y = Fnv.hashCode64("y");
    static final long HASH_NAME = Fnv.hashCode64("name");
    static final long HASH_SIZE = Fnv.hashCode64("size");
    static final long HASH_STYLE = Fnv.hashCode64("style");
    public static AwtRederModule INSTANCE = new AwtRederModule();

    @Override
    public ObjectReader getObjectReader(ObjectReaderProvider provider, Type type) {
        if (type == Color.class) {
            return ObjectReaders.objectReader(
                    new ColorCreator(),
                    fieldReader("rgb", int.class),
                    fieldReader("r", int.class),
                    fieldReader("g", int.class),
                    fieldReader("b", int.class)
            );
        }

        if (type == Point.class) {
            return ObjectReaders.objectReader(
                    (values) -> new Point(
                            (Integer) values.get(HASH_X),
                            (Integer) values.get(HASH_Y)
                    ),
                    fieldReader("x", int.class),
                    fieldReader("y", int.class)
            );
        }

        if (type == Font.class) {
            return ObjectReaders.objectReader(
                    (values) -> new Font(
                            (String) values.get(HASH_NAME),
                            (Integer) values.get(HASH_STYLE),
                            (Integer) values.get(HASH_SIZE)
                    ),
                    fieldReader("name", String.class),
                    fieldReader("style", int.class),
                    fieldReader("size", int.class)
            );
        }
        return null;
    }

    static class ColorCreator
            implements Function<Map<Long, Object>, Color> {
        static final long HASH_RGB = Fnv.hashCode64("rgb");
        static final long HASH_R = Fnv.hashCode64("r");
        static final long HASH_G = Fnv.hashCode64("g");
        static final long HASH_B = Fnv.hashCode64("b");

        @Override
        public Color apply(Map<Long, Object> values) {
            Integer rgb = (Integer) values.get(HASH_RGB);
            if (rgb != null) {
                return new Color(rgb.intValue());
            }

            Integer r = (Integer) values.get(HASH_R);
            Integer g = (Integer) values.get(HASH_G);
            Integer b = (Integer) values.get(HASH_B);
            return new Color(r, g, b);
        }
    }
}
