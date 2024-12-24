package com.alibaba.fastjson2.issues_2100.issue2185;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 张治保
 * @since 2024/10/18
 */
public class Issue2185 {
    @Test
    public void testUTF16() {
        User user = new User()
                .setName("A")
                .setAge(24)
                .setFather(new User().setName("A的爸爸").setAge(48))
                .setChildren(
                        new ArrayList<User>() {{
                            add(new User().setName("A的儿子").setAge(1));
                            add(new User().setName("A的女儿").setAge(1));
                        }}
                )
                .setHobbies(
                        new ArrayList<String>() {
                            {
                                add("游戏");
                                add("编程");
                                add("旅游");
                            }
                        }
                );

        String jsonString = JSON.toJSONString(user, JSONWriter.Feature.PrettyFormatWithSpace);
        String ideaJson;
        try (InputStream is = Issue2185.class.getClassLoader().getResourceAsStream("issues/2185/idea.json")) {
            Assertions.assertNotNull(is);
            ideaJson = IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(
                ideaJson.trim(),
                jsonString
        );
//        System.out.println(jsonString);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    static class User {
        private String name;
        private Integer age;
        private User father;
        private List<User> children;
        private List<String> hobbies;
    }
}
