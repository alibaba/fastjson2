package com.alibaba.fastjson3.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a constructor or static factory method as the creator for deserialization.
 *
 * <pre>
 * public class Point {
 *     private final int x, y;
 *
 *     &#64;JSONCreator
 *     public Point(&#64;JSONField(name = "x") int x, &#64;JSONField(name = "y") int y) {
 *         this.x = x;
 *         this.y = y;
 *     }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface JSONCreator {
    /**
     * Parameter names (alternative to @JSONField on parameters).
     */
    String[] parameterNames() default {};
}
