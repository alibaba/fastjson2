package com.alibaba.fastjson2.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JSONCompiled {
    boolean referenceDetect() default true;
    boolean smartMatch() default true;
}
