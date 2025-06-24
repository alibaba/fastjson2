package com.alibaba.fastjson2.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JSONCompiled {
    /**
     * whether to generate additional source file for Writer and Reader
     * @since 2.0.51
     */
    boolean debug() default false;
}
