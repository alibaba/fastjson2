package com.alibaba.fastjson2.support.jaxrs.jakarta.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author 张治保
 * @since 2024/10/16
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class User {
    private String name;
    private int age;
}
