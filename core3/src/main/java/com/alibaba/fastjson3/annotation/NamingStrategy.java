package com.alibaba.fastjson3.annotation;

/**
 * Property naming strategies for automatic name conversion.
 *
 * <pre>
 * &#64;JSONType(naming = NamingStrategy.SnakeCase)
 * public class User {
 *     private String firstName;  // serialized as "first_name"
 * }
 * </pre>
 */
public enum NamingStrategy {
    /**
     * No transformation. Use original name.
     */
    NoneStrategy,

    /**
     * camelCase (Java default).
     */
    CamelCase,

    /**
     * PascalCase / UpperCamelCase.
     */
    PascalCase,

    /**
     * snake_case (e.g., user_name).
     */
    SnakeCase,

    /**
     * UPPER_SNAKE_CASE (e.g., USER_NAME).
     */
    UpperSnakeCase,

    /**
     * kebab-case (e.g., user-name).
     */
    KebabCase,

    /**
     * UPPER_KEBAB_CASE (e.g., USER-NAME).
     */
    UpperKebabCase
}
