package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

public class Issue2286 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.animal = new Cat();

        String str = JSON.toJSONString(bean);
        System.out.println(str);
    }

    public static class Bean {
        @JSONField(serializeUsing = AnimalSerDe.class)
        public Animal animal;
    }

    public static class Animal{
        String type;
    }

    public static class Cat
            extends Animal {
        public Cat() {
            this.type = "Cat";
        }

        public String color;
    }

    public static class AnimalSerDe
            implements ObjectWriter<Animal> {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            Animal animal = (Animal) object;
            jsonWriter.startObject();
            jsonWriter.writeNameValue("type", animal.type);
            jsonWriter.endObject();
        }
    }
}
