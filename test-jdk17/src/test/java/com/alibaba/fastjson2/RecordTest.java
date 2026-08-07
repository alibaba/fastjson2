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

        byte[] jsonb = JSONB.toBytes(item, JSONWriter.Feature.UseSingleQuotes);
        Item item3 = JSONB.parseObject(jsonb, Item.class);
        assertEquals(item.value, item2.value);
    }

    public record Item(int value) {
    }

    @Test
    public void test1() {
        Item1 item = new Item1(Arrays.asList("abc"));
        {
            String str = JSON.toJSONString(item);
            Item1 item2 = JSON.parseObject(str, Item1.class);
            assertEquals(item.value, item2.value);
        }
        {
            String str = JSON.toJSONString(item, JSONWriter.Feature.UseSingleQuotes);
            Item1 item2 = JSON.parseObject(str, Item1.class);
            assertEquals(item.value, item2.value);
        }
        {
            byte[] jsonb = JSONB.toBytes(item, JSONWriter.Feature.UseSingleQuotes);
            Item1 item2 = JSONB.parseObject(jsonb, Item1.class);
            assertEquals(item.value, item2.value);
        }
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

        byte[] jsonb = JSONB.toBytes(item, JSONWriter.Feature.UseSingleQuotes);
        Item2 item3 = JSONB.parseObject(jsonb, Item2.class);
        assertEquals(item.value.size(), item3.value.size());
        assertEquals(item.value.get(0).value, item3.value.get(0).value);
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

    public record User(boolean default_profile, String screen_name) {
    }

    public record Item3(User user) implements Serializable {
    }

    @Test
    public void test4() {
        Item4 item = new Item4(123);
        String str = JSON.toJSONString(item);
        String str1 = JSON.toJSONString(item, JSONWriter.Feature.FieldBased);
        assertEquals(str, str1);

        JSONObject object = JSON.parseObject(str);
        assertEquals(item.value, object.get("value"));

        Item4 item2 = JSON.parseObject(str, Item4.class);
        assertEquals(item.value, item2.value);
    }

    private record Item4(int value) {
    }

    @Test
    public void test5() {
        Item5 item = new Item5(new User5(true, "abc"));
        String str = JSON.toJSONString(item);
        Item5 item2 = JSON.parseObject(str, Item5.class);
        assertEquals(item.user.default_profile, item2.user.default_profile);
        assertEquals(item.user.screen_name, item2.user.screen_name);

        byte[] jsonb = JSONB.toBytes(item);
        Item5 item3 = JSONB.parseObject(jsonb, Item5.class);
        assertEquals(item.user.default_profile, item3.user.default_profile);
        assertEquals(item.user.screen_name, item3.user.screen_name);
    }

    private record User5(boolean default_profile, String screen_name) {
    }

    private record Item5(User5 user) implements Serializable {
    }
}
