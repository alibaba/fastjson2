package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue742 {
    @Test
    public void test() {
        JSONArray result = (JSONArray) JSONPath.extract(str, "$.phoneNumbers[?(@.address.city == 'Nara1')]");
        assertEquals(1, result.size());
        for (Object item : result) {
            assertEquals("Nara1", ((JSONObject) item).getJSONObject("address").get("city"));
        }
    }

    @Test
    public void test1() {
        JSONObject jsonObject = JSON.parseObject(str);
        JSONArray result = (JSONArray) JSONPath.eval(jsonObject, "$.phoneNumbers[?(@.address.city == 'Nara1')]");
        assertEquals(1, result.size());
        for (Object item : result) {
            assertEquals("Nara1", ((JSONObject) item).getJSONObject("address").get("city"));
        }
    }

    @Test
    public void test2() {
        User user = JSON.parseObject(str, User.class);
        JSONArray result = (JSONArray) JSONPath.eval(user, "$.phoneNumbers[?(@.address.city == 'Nara1')]");
        assertEquals(1, result.size());
        for (Object item : result) {
            assertEquals("Nara1", ((PhoneNumber) item).address.city);
        }
    }

    @Test
    public void test3() {
        User user = JSON.parseObject(str, User.class);
        JSONObject object = JSONObject.of(
                "firstName", user.firstName,
                "lastName", user.lastName,
                "phoneNumbers", user.phoneNumbers
        );

        JSONArray result = (JSONArray) JSONPath.eval(object, "$.phoneNumbers[?(@.address.city == 'Nara1')]");
        assertEquals(1, result.size());
        for (Object item : result) {
            assertEquals("Nara1", ((PhoneNumber) item).address.city);
        }
    }

    public static class User {
        public String firstName;
        public String lastName;
        public int age;
        public List<PhoneNumber> phoneNumbers;
    }

    public static class Address {
        public String streetAddress;
        public String city;
        public String postalCode;
    }

    public static class PhoneNumber {
        public String type;
        public String number;
        public Address address;
    }

    static String str = "{ \"firstName\": \"John\", \"lastName\" : \"doe\", \"age\" : 26, \"address\" : { \"streetAddress\": \"naist street\", \"city\" : \"Nara\", \"postalCode\" : \"630-0192\" }, \"phoneNumbers\": [ { \"type\" : \"iPhone\", \"number\": \"0123-4567-8888\", \"address\" : { \"streetAddress\": \"naist street1\", \"city\" : \"Nara1\", \"postalCode\" : \"630-0192\" } }, { \"type\" : \"home\", \"number\": \"0123-4567-8910\", \"address\" : { \"streetAddress\": \"naist street2\", \"city\" : \"Nara2\", \"postalCode\" : \"630-0192\" } } ] }";
}
