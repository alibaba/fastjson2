package com.alibaba.fastjson2.reader;

/**
 * CharArrayValueConsumer is a callback interface for processing tabular JSON data
 * with character array representation. It provides lifecycle methods for handling rows
 * and columns of data during streaming deserialization.
 *
 * <p>This interface is particularly useful for:
 * <ul>
 *   <li>Processing CSV-like JSON data with character-based operations</li>
 *   <li>Handling large tabular datasets efficiently</li>
 *   <li>Direct character array processing for better performance</li>
 *   <li>Custom text data processing and transformation</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * CharArrayValueConsumer<User> consumer = new CharArrayValueConsumer<User>() {
 *     @Override
 *     public void accept(int row, int column, char[] chars, int off, int len) {
 *         String value = new String(chars, off, len);
 *         System.out.println("Row " + row + ", Column " + column + ": " + value);
 *     }
 *
 *     @Override
 *     public void start() {
 *         System.out.println("Starting data processing");
 *     }
 *
 *     @Override
 *     public void end() {
 *         System.out.println("Finished data processing");
 *     }
 * };
 * }</pre>
 *
 * @param <T> the type of object being constructed
 * @since 2.0.0
 */
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
