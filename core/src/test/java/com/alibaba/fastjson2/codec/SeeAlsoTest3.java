package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeeAlsoTest3 {
    @Test
    public void test_seeAlso() throws Exception {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<Animal> objectReader = creator.createObjectReader(Animal.class);

            {
                Animal animal = objectReader.readObject(JSONReader.of("{\"type\":\"Cat\",\"catId\":1001}"), 0);
                assertEquals(Cat.class, animal.getClass());
                Cat cat = (Cat) animal;
                assertEquals(1001, cat.catId);
            }
            {
                Animal animal = objectReader.readObject(JSONReader.of("{\"type\":\"Pig\",\"pigId\":2001}"), 0);
                assertEquals(Pig.class, animal.getClass());
                Pig pig = (Pig) animal;
                assertEquals(2001, pig.pigId);
            }
            {
                Animal animal = objectReader.readObject(
                        JSONReader.of("{}"), 0);
                assertEquals(Animal.class, animal.getClass());
            }
            {
                Map map = new LinkedHashMap();
                map.put("type", "Cat");
                map.put("catId", 1001);
                JSONWriter jsonbWriter = JSONWriter.ofJSONB();
                jsonbWriter.writeAny(map);

                byte[] jsonbBytes = jsonbWriter.getBytes();
                Animal animal = objectReader.readObject(JSONReader.ofJSONB(jsonbBytes), 0);
                assertEquals(Cat.class, animal.getClass());
                Cat cat = (Cat) animal;
                assertEquals(1001, cat.catId);
            }
            {
                Map map = new LinkedHashMap();
                JSONWriter jsonbWriter = JSONWriter.ofJSONB();
                jsonbWriter.writeAny(map);

                byte[] jsonbBytes = jsonbWriter.getBytes();
                Animal animal = objectReader.readObject(JSONReader.ofJSONB(jsonbBytes), 0);
                assertEquals(Animal.class, animal.getClass());
            }
        }
    }

    @Test
    public void test_seeAlso_write() throws Exception {
        Cat cat = new Cat();
        cat.catId = 101;
        String str = JSON.toJSONString(cat);
        assertEquals("{\"catId\":101}", str);

        String str2 = JSON.toJSONString(cat, JSONWriter.Feature.WriteClassName);
        assertEquals("{\"type\":\"Cat\",\"catId\":101}", str2);
    }

    @JSONType(typeKey = "type", seeAlso = {Cat.class, Pig.class})
    public static class Animal {
    }

    public static class Cat
            extends Animal {
        public int catId;
    }

    public static class Pig
            extends Animal {
        public int pigId;
    }
}
