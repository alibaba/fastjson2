package com.alibaba.fastjson2.writer;

import java.util.Arrays;

final class ObjectWriterImplStackTraceElement extends ObjectWriterAdapter {
    static final ObjectWriterImplStackTraceElement INSTANCE = new ObjectWriterImplStackTraceElement();

    ObjectWriterImplStackTraceElement() {
        super(StackTraceElement.class, Arrays.asList(
                new FieldWriter[]{
                        ObjectWriters.fieldWriter("fileName", String.class, StackTraceElement::getFileName),
                        ObjectWriters.fieldWriter("lineNumber", StackTraceElement::getLineNumber),
                        ObjectWriters.fieldWriter("className", String.class, StackTraceElement::getClassName),
                        ObjectWriters.fieldWriter("methodName", String.class, StackTraceElement::getMethodName),
                }
        ));
    }
}
