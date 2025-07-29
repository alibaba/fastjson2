package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test20250729 {
    @Test
    public void test() {
        final User1 user1 = new User1();
        user1.setName2(Arrays.asList("name2_0", "name2_1"));

        final String json = JSON.toJSONString(user1);
        System.out.println(json);
        assertEquals("{\"name2\":[\"name2_0\",\"name2_1\"]}", json);

        final User2 user2 = JSON.parseObject(json, User2.class);
        assertEquals("{\"name2\":\"[\\\"name2_0\\\",\\\"name2_1\\\"]\"}", JSON.toJSONString(user2));
    }

    @Data
    public class User1 {
        private static final long serialVersionUID = -8383925194467683795L;

        private List<String> name2;
    }

    @Data
    public class User2 {
        private String name2;
    }
}
