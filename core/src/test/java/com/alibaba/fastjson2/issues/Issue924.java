package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue924 {
    @Test
    public void test() {
        Entity entity = new Entity(101L);
        String json = JSON.toJSONString(entity);
        assertEquals("{\"C\":101}", json);

        Entity entity1 = JSON.parseObject(json, Entity.class);
        assertEquals(entity.C, entity1.C);

        Entity entity2 = JSON.parseObject("{\"c\":101}", Entity.class);
        assertEquals(entity.C, entity2.C);
    }

    @Test
    public void test1() {
        Entity1 entity = new Entity1(101L);
        String json = JSON.toJSONString(entity);
        assertEquals("{\"C\":101}", json);

        Entity1 entity1 = JSON.parseObject(json, Entity1.class);
        assertEquals(entity.C, entity1.C);

        Entity1 entity2 = JSON.parseObject("{\"c\":101}", Entity1.class);
        assertEquals(entity.C, entity2.C);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Entity {
        private Long C;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private static class Entity1 {
        private Long C;
    }

    @Test
    public void test2() {
        Entity2 entity = new Entity2();
        entity.ID = 101L;
        String json = JSON.toJSONString(entity);
        assertEquals("{\"ID\":101}", json);

        Entity2 entity1 = JSON.parseObject(json, Entity2.class);
        assertEquals(entity.ID, entity1.ID);

        Entity2 entity2 = JSON.parseObject("{\"id\":101}", Entity2.class);
        assertEquals(entity.ID, entity2.ID);
    }

    public static class Entity2 {
        private Long ID;

        public Entity2() {
        }

        public Long getID() {
            return ID;
        }

        public void setID(Long id) {
            this.ID = id;
        }
    }

    @Test
    public void test3() {
        Entity3 entity = new Entity3();
        entity.ID = 101L;
        String json = JSON.toJSONString(entity);
        assertEquals("{\"ID\":101}", json);

        Entity3 entity1 = JSON.parseObject(json, Entity3.class);
        assertEquals(entity.ID, entity1.ID);

        Entity3 entity2 = JSON.parseObject("{\"id\":101}", Entity3.class);
        assertEquals(entity.ID, entity2.ID);
    }

    private static class Entity3 {
        private Long ID;

        public Entity3() {
        }

        public Long getID() {
            return ID;
        }

        public void setID(Long id) {
            this.ID = id;
        }
    }
}
