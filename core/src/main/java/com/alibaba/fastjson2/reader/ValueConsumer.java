package com.alibaba.fastjson2.reader;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * ValueConsumer is a callback interface for processing JSON values during streaming
 * deserialization. It provides methods to accept different types of JSON values
 * (primitives, strings, objects, arrays) without fully parsing the entire document.
 *
 * <p>This interface is particularly useful for:
 * <ul>
 *   <li>Processing large JSON documents with minimal memory overhead</li>
 *   <li>Extracting specific values from JSON streams</li>
 *   <li>Custom deserialization logic for specific field types</li>
 *   <li>Building custom data structures from JSON</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Custom value consumer implementation
 * ValueConsumer consumer = new ValueConsumer() {
 *     @Override
 *     public void accept(String val) {
 *         System.out.println("String value: " + val);
 *     }
 *
 *     @Override
 *     public void accept(int val) {
 *         System.out.println("Int value: " + val);
 *     }
 * };
 * }</pre>
 *
 * @since 2.0.0
 */
public interface ValueConsumer {
    default void accept(byte[] bytes, int off, int len) {
        accept(new String(bytes, off, len, StandardCharsets.UTF_8));
    }

    default void acceptNull() {
    }

    default void accept(boolean val) {
    }

    default void accept(int val) {
        accept(Integer.valueOf(val));
    }

    default void accept(long val) {
        accept(Long.valueOf(val));
    }

    default void accept(Number val) {
    }

    default void accept(String val) {
    }

    default void accept(Map object) {
    }

    default void accept(List array) {
    }
}
