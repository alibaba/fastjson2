package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for PropertyFilter, ValueFilter, and NameFilter.
 */
class FilterTest {
    public static class User {
        public String name;
        public String password;
        public int age;

        public User() {
        }

        public User(String name, String password, int age) {
            this.name = name;
            this.password = password;
            this.age = age;
        }
    }

    @Test
    void testPropertyFilterExcludeField() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addPropertyFilter((source, name, value) -> !"password".equals(name))
                .build();

        User user = new User("Alice", "secret123", 30);
        String json = mapper.writeValueAsString(user);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"Alice\""));
        assertFalse(json.contains("password"));
        assertFalse(json.contains("secret123"));
        assertTrue(json.contains("\"age\""));
    }

    @Test
    void testValueFilterMaskField() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addValueFilter((source, name, value) -> {
                    if ("password".equals(name)) {
                        return "***";
                    }
                    return value;
                })
                .build();

        User user = new User("Alice", "secret123", 30);
        String json = mapper.writeValueAsString(user);
        assertTrue(json.contains("\"Alice\""));
        assertTrue(json.contains("\"***\""));
        assertFalse(json.contains("secret123"));
    }

    @Test
    void testNameFilterUpperCase() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addNameFilter((source, name, value) -> name.toUpperCase())
                .build();

        User user = new User("Alice", "secret", 30);
        String json = mapper.writeValueAsString(user);
        assertTrue(json.contains("\"NAME\""));
        assertTrue(json.contains("\"PASSWORD\""));
        assertTrue(json.contains("\"AGE\""));
    }

    @Test
    void testCombinedFilters() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addPropertyFilter((source, name, value) -> !"password".equals(name))
                .addNameFilter((source, name, value) -> name.toUpperCase())
                .build();

        User user = new User("Alice", "secret", 30);
        String json = mapper.writeValueAsString(user);
        assertTrue(json.contains("\"NAME\""));
        assertFalse(json.contains("password"));
        assertFalse(json.contains("PASSWORD"));
        assertTrue(json.contains("\"AGE\""));
    }

    @Test
    void testNoFilterPerformancePath() {
        // Shared mapper has no filters — should use fast path
        ObjectMapper mapper = ObjectMapper.shared();
        User user = new User("Alice", "secret", 30);
        String json = mapper.writeValueAsString(user);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"password\""));
        assertTrue(json.contains("\"age\""));
    }

    @Test
    void testFilterWithBytes() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addPropertyFilter((source, name, value) -> !"password".equals(name))
                .build();

        User user = new User("Alice", "secret", 30);
        byte[] bytes = mapper.writeValueAsBytes(user);
        String json = new String(bytes);
        assertFalse(json.contains("password"));
        assertTrue(json.contains("\"name\""));
    }
}
