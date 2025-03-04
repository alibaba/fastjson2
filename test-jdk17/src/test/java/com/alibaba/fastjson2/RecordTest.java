package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecordTest {
    @Test
    public void test() {
        Item item = new Item(123);
        String str = JSON.toJSONString(item);
        String str1 = JSON.toJSONString(item, JSONWriter.Feature.FieldBased);
        assertEquals(str, str1);

        JSONObject object = JSON.parseObject(str);
        assertEquals(item.value, object.get("value"));

        Item item2 = JSON.parseObject(str, Item.class);
        assertEquals(item.value, item2.value);
    }

    public record Item(int value) {
    }

    @Test
    public void test1() {
        Item1 item = new Item1(Arrays.asList("abc"));
        String str = JSON.toJSONString(item);
        Item1 item2 = JSON.parseObject(str, Item1.class);
        assertEquals(item.value, item2.value);
    }

    public record Item1(List<String> value) {
    }

    @Test
    public void test2() {
        Item2 item = new Item2(Arrays.asList(new Item(123)));
        String str = JSON.toJSONString(item);
        Item2 item2 = JSON.parseObject(str, Item2.class);
        assertEquals(item.value.size(), item2.value.size());
        assertEquals(item.value.get(0).value, item2.value.get(0).value);
    }

    public record Item2(List<Item> value) {
    }

    @Test
    public void test3() {
        Item3 item = new Item3(new User(true, "abc"));
        String str = JSON.toJSONString(item);
        Item3 item2 = JSON.parseObject(str, Item3.class);
        assertEquals(item.user.default_profile, item2.user.default_profile);
        assertEquals(item.user.screen_name, item2.user.screen_name);
    }

    public record User(boolean default_profile, String screen_name) implements Serializable {
    }

    public record Item3(User user) implements Serializable {
    }
}
