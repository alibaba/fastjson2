package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.filter.NameFilter;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2478 {
    @Test
    public void jsonFastSerializationTest0() {
        NameFilter namingStrategy = NameFilter.of(PropertyNamingStrategy.LowerCaseWithUnderScores);

        User user = new User(123L, "First", "Last");

        String userAsJsonString = JSON.toJSONString(user, namingStrategy);

        assertEquals("{\"first_name\":\"First\",\"id\":123,\"last_name\":\"Last\"}", userAsJsonString);
//        System.out.println(userAsJsonString); // {"first_name":"First","id":123,"last_name":"Last"} -> Ok
//
//        User deserializedUser = JSON.parseObject(userAsJsonString, User.class, namingStrategy);
//
//        System.out.println(deserializedUser); // UserTest{id=123, firstName='null', lastName='null'} -> Bug!!!
    }
//
//    @Test
//    public void jsonFastSerializationTest() {
//        Arrays.stream(PropertyNamingStrategy.values()).forEach(naming -> {
//            NameFilter namingStrategy = NameFilter.of(naming);
//            User user = new User(123L, "First", "Last");
//            String userAsJsonString = JSON.toJSONString(user, namingStrategy);
//            User deserializedUser = JSON.parseObject(userAsJsonString, User.class, namingStrategy);
//            assertEquals(user, deserializedUser);
//        });
//    }

    @Data
    @AllArgsConstructor
    public static class User {
        private Long id;
        private String firstName;
        private String lastName;
    }
}
