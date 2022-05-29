package com.alibaba.fastjson2.v1issues.issue_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * <p>Title: Issue1612</p>
 * <p>Description: </p>
 *
 * @author Victor
 * @version 1.0
 * @since 2017/11/27
 */
public class Issue1612 {
    @Test
    public void test() {
        RegResponse<User> userRegResponse = testFastJson(User.class);
        User user = userRegResponse.getResult();
        assertNotNull(user);
    }

    public static <T> RegResponse<T> testFastJson(Class<T> clazz) {
        //把body解析成一个对象
        String body = "{\"retCode\":\"200\", \"result\":{\"name\":\"Zhangsan\",\"password\":\"123\"}}";

        return JSON.parseObject(body, new TypeReference<RegResponse<T>>(new Type[]{clazz}) {
        });
    }
}
