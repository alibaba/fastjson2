package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1488 {
    @JSONType(seeAlso = {Dog.class, Cat.class}, typeKey = "aniType")
    public static class Animal {
        public int aniType;
    }

    @JSONType(typeName = "1")
    public static class Dog
            extends Animal {
        public String dogName;

        public Dog() {
            this.aniType = 1;
        }
    }

    @JSONType(typeName = "2")
    public static class Cat
            extends Animal {
        public String catName;

        public Cat() {
            this.aniType = 2;
        }
    }

    @Test
    public void test() {
        Dog dog = new Dog();
        dog.dogName = "dog1001";

        String text = JSON.toJSONString(dog);
        Dog dog1 = (Dog) JSON.parseObject(text, Animal.class);
        assertNotNull(dog1);
        assertEquals(1, dog1.aniType);
    }

    @Test
    public void test1() {
        Cat cat = new Cat();
        cat.catName = "cat1001";

        String text = JSON.toJSONString(cat);
        Cat cat1 = (Cat) JSON.parseObject(text, Animal.class);
        assertNotNull(cat1);
        assertEquals(2, cat1.aniType);
    }
}
