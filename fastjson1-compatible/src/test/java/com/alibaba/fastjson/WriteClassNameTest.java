package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WriteClassNameTest {
    @BeforeEach
    public void setUp() throws Exception {
        ParserConfig.global.addAccept("com.alibaba.json.bvt.WriteClassNameTest.");
    }

    @Test
    public void test_0() throws Exception {
        Entity entity = new Entity(3, "jobs");
        String text = JSON.toJSONString(entity, SerializerFeature.WriteClassName);
        System.out.println(text);

        Entity entity2 = (Entity) JSON.parseObject(text, Object.class, Feature.SupportAutoType);

        assertEquals(entity.getId(), entity2.getId());
        assertEquals(entity.getName(), entity2.getName());
    }

    public static class Entity {
        private int id;
        private String name;

        public Entity() {
        }

        public Entity(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
