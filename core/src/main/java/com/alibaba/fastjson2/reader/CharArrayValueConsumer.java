package com.alibaba.fastjson2.reader;

public interface CharArrayValueConsumer<T> {
    default void start() {
    }

    default void beforeRow(int row) {
    }

    void accept(int row, int column, char[] chars, int off, int len);

    default void afterRow(int row) {
    }

    default void end() {
    }
}
