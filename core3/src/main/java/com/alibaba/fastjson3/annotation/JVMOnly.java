package com.alibaba.fastjson3.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as JVM-only. Classes with this annotation are excluded
 * from the Android build ({@code mvn package -Pandroid}).
 *
 * <p>Typical use: ASM bytecode generation classes that require
 * {@code ClassLoader.defineClass()} — not available on Android (Dalvik/ART).</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface JVMOnly {
}
