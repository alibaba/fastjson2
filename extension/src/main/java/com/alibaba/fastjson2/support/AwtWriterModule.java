package com.alibaba.fastjson2.support;

import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.awt.*;
import java.lang.reflect.Type;

import static com.alibaba.fastjson2.writer.ObjectWriters.fieldWriter;
import static com.alibaba.fastjson2.writer.ObjectWriters.objectWriter;

/**
 * ObjectWriterModule for AWT types (Color, Point, Font).
 * Provides custom serialization for common java.awt classes.
 *
 * <p><b>Note:</b> This module is not supported on Android.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * JSON.register(AwtWriterModule.INSTANCE);
 * String json = JSON.toJSONString(new Color(255, 0, 0, 128));
 * // Result: {"r":255,"g":0,"b":0,"alpha":128}
 * }</pre>
 *
 * @since 2.0.0
 */
public class AwtWriterModule
        implements ObjectWriterModule {
    /**
     * Singleton instance of AwtWriterModule.
     * <p><b>Note:</b> Not supported on Android platform.</p>
     */
    public static AwtWriterModule INSTANCE = new AwtWriterModule();

    /**
     * Gets the custom ObjectWriter for AWT types.
     *
     * @param objectType the type to be serialized
     * @param objectClass the class to be serialized
     * @return the ObjectWriter for the specified type, or null if not supported
     */
    @Override
    public ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        if (objectType == Color.class) {
            return objectWriter(Color.class,
                    fieldWriter("r", Color::getRed),
                    fieldWriter("g", Color::getGreen),
                    fieldWriter("b", Color::getBlue),
                    fieldWriter("alpha", Color::getAlpha)
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
