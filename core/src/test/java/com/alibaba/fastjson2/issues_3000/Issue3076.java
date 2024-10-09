package com.alibaba.fastjson2.issues_3000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ValueFilter;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author 张治保
 * @since 2024/10/9
 */
public class Issue3076 {
    @SneakyThrows
    @Test
    void test() {
        User user = new User("Alice", null, null);
        // 定义一个 ValueFilter，将 null 值修改为默认值
        ValueFilter filter = (object, name, value) -> {
            if (value == null) {
                return "N/A"; // 如果字段值为 null，改为 "N/A"
            }
            return value;
        };

        // 使用 ValueFilter 序列化
        String jsonString = JSON.toJSONString(user, filter, JSONWriter.Feature.WriteNulls);
        // 输出：{"name":"Alice","age":"N/A","active":"N/A"}
        JSONAssert.assertEquals(
                "{\"name\":\"Alice\",\"age\":\"N/A\",\"active\":\"N/A\"}",
                jsonString,
                true
        );
    }

    @Data
    static class User {
        private String name;
        private Integer age;
        private Boolean active;

        public User(String name, Integer age, Boolean active) {
            this.name = name;
            this.age = age;
            this.active = active;
        }
    }
}
