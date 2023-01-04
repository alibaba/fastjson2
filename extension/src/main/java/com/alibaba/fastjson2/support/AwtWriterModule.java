package com.alibaba.fastjson2.support;

import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.awt.*;
import java.lang.reflect.Type;

import static com.alibaba.fastjson2.writer.ObjectWriters.fieldWriter;
import static com.alibaba.fastjson2.writer.ObjectWriters.objectWriter;

public class AwtWriterModule
        implements ObjectWriterModule {
    // Android not support
    public static AwtWriterModule INSTANCE = new AwtWriterModule();

    @Override
    public ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        if (objectType == Color.class) {
            return objectWriter(Color.class,
                    fieldWriter("rgb", Color::getRGB)
            );
        }

        if (objectType == Point.class) {
            return objectWriter(Point.class,
                    fieldWriter("x", (Point p) -> p.x),
                    fieldWriter("y", (Point p) -> p.y)
            );
        }

        if (objectType == Font.class) {
            return objectWriter(Font.class,
                    fieldWriter("name", Font::getName),
                    fieldWriter("style", Font::getStyle),
                    fieldWriter("size", Font::getSize)
            );
        }
        return null;
    }
}
