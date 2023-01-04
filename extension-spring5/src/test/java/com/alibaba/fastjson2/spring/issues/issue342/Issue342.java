package com.alibaba.fastjson2.spring.issues.issue342;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Issue342 {
    private SearchPage<User> searchPage;

    public Issue342() {
        User user1 = new User(1, "test_1", 15);
        User user2 = new User(2, "test_2", 25);
        User user3 = new User(3, "test_3", 35);
        SearchHit<User> searchHit1 = new SearchHit("test1", "1", "test1", Float.NaN, new Object[0], Collections.<String, List<String>>emptyMap(), null, null, null, null, user1);
        SearchHit<User> searchHit2 = new SearchHit("test2", "2", "test2", Float.NaN, new Object[0], Collections.<String, List<String>>emptyMap(), null, null, null, null, user2);
        SearchHit<User> searchHit3 = new SearchHit("test3", "3", "test3", Float.NaN, new Object[0], Collections.<String, List<String>>emptyMap(), null, null, null, null, user3);
        List<SearchHit<User>> searchHitList = Arrays.asList(searchHit1, searchHit2, searchHit3);
        SearchHits<User> searchHits = new SearchHitsImpl(123, TotalHitsRelation.EQUAL_TO, Float.NaN, "123", searchHitList, null, null);
        Pageable pageable = PageRequest.of(10, 100, Sort.unsorted());
        searchPage = SearchHitSupport.searchPageFor(searchHits, pageable);
    }

    @Test
    public void test_fastjson1() {
        String jsonString = com.alibaba.fastjson.JSON.toJSONString(searchPage);
        System.out.println(jsonString);
    }

    @Test
    public void test_fastjson2() {
        String jsonString = JSON.toJSONString(searchPage, JSONWriter.Feature.ReferenceDetection);
        System.out.println(jsonString);
    }

    static class User {
        private Integer id;
        private String name;
        private Integer age;

        public User() {
        }

        public User(Integer id, String name, Integer age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }
}
