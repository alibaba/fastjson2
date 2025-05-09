package com.alibaba.fastjson2.issues_3500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3540 {
    @Test
    public void exceptUseSeeAlsoDefault() {
        final String json = "                {\n" +
                "                    \"name\": \"tom\",\n" +
                "                    \"age\": 18\n" +
                "                }";
        IAnimal animal = JSON.parseObject(json, IAnimal.class);
        assertEquals(Dog.class, animal.getClass());
    }

    @Test
    public void exceptUseSeeAlsoDefault2() {
        final String json = "                {\n" +
                "                    \"@type\": \"\",\n" +
                "                    \"name\": \"tom\",\n" +
                "                    \"age\": 18\n" +
                "                }";
        IAnimal animal = JSON.parseObject(json, IAnimal.class);
        assertEquals(Dog.class, animal.getClass());
    }

    @Test
    public void useTypeForSeeAlso() {
        final String json = "                {\n" +
                "                    \"@type\": \"Dog\",\n" +
                "                    \"name\": \"tom\",\n" +
                "                    \"age\": 18\n" +
                "                }";
        IAnimal animal = JSON.parseObject(json, IAnimal.class);
        assertEquals(Dog.class, animal.getClass());
    }

    @JSONType(seeAlso = Dog.class, seeAlsoDefault = Dog.class)
    public interface IAnimal {
        String getName();

        int getAge();
    }

    @JSONType(typeName = "Dog")
    public static class Dog
            implements IAnimal {
        private String name;
        private int age;

        public void setName(String name) {
            this.name = name;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public int getAge() {
            return 0;
        }
    }
}
