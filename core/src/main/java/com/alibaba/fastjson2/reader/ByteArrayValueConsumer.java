package com.alibaba.fastjson2.reader;

import java.nio.charset.Charset;

public interface ByteArrayValueConsumer {
    default void beforeRow(int row) {
    }

    void accept(int row, int column, byte[] bytes, int off, int len, Charset charset);

    default void afterRow(int row) {
    }
}
