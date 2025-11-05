package com.alibaba.fastjson2.internal.mixin.spring;

import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;

/**
 * JSON mixin for Spring Security's SimpleGrantedAuthority class.
 * This mixin provides JSON serialization/deserialization configuration for
 * Spring Security's SimpleGrantedAuthority using fastjson2 annotations.
 *
 * <p>The mixin maps the "authority" JSON property to the constructor parameter,
 * enabling proper deserialization of Spring Security authorities.
 *
 * @see JSONCreator
 * @see JSONField
 */
public class SimpleGrantedAuthorityMixin {
    @JSONCreator
    public SimpleGrantedAuthorityMixin(@JSONField(name = "authority") String role) {
    }
}
