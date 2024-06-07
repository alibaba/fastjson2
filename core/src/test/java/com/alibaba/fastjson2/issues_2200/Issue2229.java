package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2229 {
    @Test
    void test() {
        String json = "{\"type\": \"A\", \"types\": [\"B\"]}";
        DTO dto = JSON.parseObject(json, DTO.class);
        assertEquals(Type.A, dto.getType());
        assertEquals(Type.B, dto.getTypes().get(0));
    }

    static class DTO {
        private Type type;
        private List<Type> types;

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public void setType(int type) {
            this.type = null;
        }

        public List<Type> getTypes() {
            return types;
        }

        public void setTypes(List<Type> types) {
            this.types = types;
        }

        public void setTypes(int types) {
            this.types = new ArrayList<>();
        }
    }

    enum Type {
        A,B;
    }
}
