package com.alibaba.fastjson2.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a constructor or static factory method as the creator to use for JSON deserialization.
 * When multiple constructors or factory methods exist, this annotation indicates which one
 * should be used to instantiate objects from JSON.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * public class User {
 *     private String name;
 *     private int age;
 *
 *     @JSONCreator
 *     public User(String name, int age) {
 *         this.name = name;
 *         this.age = age;
 *     }
 *
 *     // Static factory method example
 *     @JSONCreator(parameterNames = {"username", "userAge"})
 *     public static User create(String username, int userAge) {
 *         return new User(username, userAge);
 *     }
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE })
public @interface JSONCreator {
    /**
     * Specifies the names of the parameters for the creator method/constructor.
     * This is useful when parameter names cannot be obtained through reflection
     * or when you want to use different names than the actual parameter names.
     *
     * @return array of parameter names corresponding to the creator's parameters
     */
    String[] parameterNames() default {};
}
