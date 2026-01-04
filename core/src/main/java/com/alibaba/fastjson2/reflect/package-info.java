/**
 * Provides reflection-based property access functionality for fastjson2.
 * This package contains classes and interfaces for efficient property access
 * during JSON serialization and deserialization operations.
 *
 * <p>The core of this package is the {@link com.alibaba.fastjson2.reflect.PropertyAccessor}
 * interface, which provides a unified API for getting and setting object properties
 * regardless of the underlying access mechanism (field access, method calls, or functional interfaces).</p>
 *
 * <p>The {@link com.alibaba.fastjson2.reflect.PropertyAccessorFactory} class serves as
 * the main factory for creating optimized property accessors based on the property type
 * and access method, providing implementations for primitive types, wrapper classes,
 * and complex objects.</p>
 *
 * <p>Key features of this package include:</p>
 * <ul>
 *   <li>Optimized accessors for primitive types to avoid boxing/unboxing overhead</li>
 *   <li>Support for both field-based and method-based property access</li>
 *   <li>Functional interface-based accessors for maximum performance</li>
 *   <li>Automatic type conversion between compatible types</li>
 *   <li>Exception handling with detailed error messages</li>
 * </ul>
 *
 * @since 2.0
 * @author wenshao
 */
package com.alibaba.fastjson2.reflect;