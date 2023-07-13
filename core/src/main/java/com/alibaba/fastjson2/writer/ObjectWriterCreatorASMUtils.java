package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.util.JDKUtils;

import static com.alibaba.fastjson2.internal.asm.ASMUtils.type;

final class ObjectWriterCreatorASMUtils {
    static final String TYPE_UNSAFE_UTILS = type(JDKUtils.class);
}
