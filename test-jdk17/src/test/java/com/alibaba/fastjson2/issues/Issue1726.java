package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1726 {
    @Test
    public void test() {
        Person person = Person.builder()
                .id("1")
                .name("jin")
                .books(List.of(
                        Book.builder()
                                .id("1")
                                .name("西游记")
                                .build(),
                        Book.builder()
                                .id("2")
                                .name("水浒传")
                                .build()
                ))
                .build();

        JSONObject jsonObject = JSONObject.from(person);
        jsonObject.put("name", "修改用户名");
        jsonObject.getJSONArray("books").getJSONObject(1).put("name", "修改书名");
        assertEquals(
                "修改书名",
                jsonObject.getJSONArray("books").getJSONObject(1).getString("name")
        );
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Person {
        private String id;
        private String name;
        private List<Book> books;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Book {
        private String id;
        private String name;
    }
}
