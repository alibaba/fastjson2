package com.alibaba.fastjson2.reader;

import java.nio.charset.Charset;

/**
 * ByteArrayValueConsumer is a callback interface for processing tabular JSON data
 * with byte array representation. It provides lifecycle methods for handling rows
 * and columns of data during streaming deserialization.
 *
 * <p>This interface is particularly useful for:
 * <ul>
 *   <li>Processing CSV-like JSON data</li>
 *   <li>Handling large tabular datasets with minimal memory overhead</li>
 *   <li>Direct byte array processing without string conversion overhead</li>
 *   <li>Custom data import/export operations</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * ByteArrayValueConsumer consumer = new ByteArrayValueConsumer() {
 *     @Override
 *     public void accept(int row, int column, byte[] bytes, int off, int len, Charset charset) {
 *         String value = new String(bytes, off, len, charset);
 *         System.out.println("Row " + row + ", Column " + column + ": " + value);
 *     }
 *
 *     @Override
 *     public void beforeRow(int row) {
 *         System.out.println("Starting row: " + row);
 *     }
 * };
 * }</pre>
 *
 * @since 2.0.0
 */
public interface ByteArrayValueConsumer {
    default void start() {
    }

    default void beforeRow(int row) {
    }

    void accept(int row, int column, byte[] bytes, int off, int len, Charset charset);

    default void afterRow(int row) {
    }

    default void end() {
    }
}
