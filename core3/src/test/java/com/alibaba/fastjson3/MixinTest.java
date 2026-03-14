package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MixinTest {
    // ==================== Target classes (no annotations) ====================

    public static class User {
        private String firstName;
        private String lastName;
        private int age;

        public User() {
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    // ==================== Mixin classes ====================

    public interface UserMixIn {
        @JSONField(name = "first_name")
        String getFirstName();

        @JSONField(name = "first_name")
        void setFirstName(String firstName);

        @JSONField(name = "last_name")
        String getLastName();

        @JSONField(name = "last_name")
        void setLastName(String lastName);
    }

    // ==================== Tests: Serialization ====================

    @Test
    public void testMixInSerializationRename() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addMixIn(User.class, UserMixIn.class)
                .build();

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setAge(30);

        String json = mapper.writeValueAsString(user);
        assertTrue(json.contains("\"first_name\":\"John\""), json);
        assertTrue(json.contains("\"last_name\":\"Doe\""), json);
        assertTrue(json.contains("\"age\":30"), json);
    }

    @Test
    public void testWithoutMixInNoRename() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setAge(30);

        String json = JSON.toJSONString(user);
        assertTrue(json.contains("\"firstName\":\"John\""), json);
        assertTrue(json.contains("\"lastName\":\"Doe\""), json);
    }

    // ==================== Tests: Deserialization ====================

    @Test
    public void testMixInDeserialization() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addMixIn(User.class, UserMixIn.class)
                .build();

        String json = "{\"first_name\":\"Jane\",\"last_name\":\"Smith\",\"age\":25}";
        User user = mapper.readValue(json, User.class);

        assertNotNull(user);
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals(25, user.getAge());
    }

    // ==================== Tests: Field-based mixin ====================

    public static class Point {
        public int x;
        public int y;
    }

    public static class PointMixIn {
        @JSONField(name = "posX")
        public int x;
        @JSONField(name = "posY")
        public int y;
    }

    @Test
    public void testFieldBasedMixInSerialization() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addMixIn(Point.class, PointMixIn.class)
                .build();

        Point p = new Point();
        p.x = 10;
        p.y = 20;

        String json = mapper.writeValueAsString(p);
        assertTrue(json.contains("\"posX\":10"), json);
        assertTrue(json.contains("\"posY\":20"), json);
    }

    @Test
    public void testFieldBasedMixInDeserialization() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addMixIn(Point.class, PointMixIn.class)
                .build();

        String json = "{\"posX\":10,\"posY\":20}";
        Point p = mapper.readValue(json, Point.class);

        assertNotNull(p);
        assertEquals(10, p.x);
        assertEquals(20, p.y);
    }

    // ==================== Tests: serialize=false via mixin ====================

    public static class Secret {
        public String name;
        public String password;
    }

    public static class SecretMixIn {
        @JSONField(serialize = false)
        public String password;
    }

    @Test
    public void testMixInHideField() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addMixIn(Secret.class, SecretMixIn.class)
                .build();

        Secret s = new Secret();
        s.name = "admin";
        s.password = "secret123";

        String json = mapper.writeValueAsString(s);
        assertTrue(json.contains("\"name\":\"admin\""), json);
        assertFalse(json.contains("password"), json);
    }

    // ==================== Tests: Mapper isolation ====================

    @Test
    public void testMixInIsolatedBetweenMappers() {
        ObjectMapper mapperWithMixin = ObjectMapper.builder()
                .addMixIn(User.class, UserMixIn.class)
                .build();

        ObjectMapper mapperWithout = ObjectMapper.builder().build();

        User user = new User();
        user.setFirstName("Alice");

        String withMixin = mapperWithMixin.writeValueAsString(user);
        String withoutMixin = mapperWithout.writeValueAsString(user);

        assertTrue(withMixin.contains("first_name"), withMixin);
        assertTrue(withoutMixin.contains("firstName"), withoutMixin);
    }

    // ==================== Tests: Roundtrip ====================

    @Test
    public void testMixInRoundtrip() {
        ObjectMapper mapper = ObjectMapper.builder()
                .addMixIn(User.class, UserMixIn.class)
                .build();

        User original = new User();
        original.setFirstName("Bob");
        original.setLastName("Jones");
        original.setAge(42);

        String json = mapper.writeValueAsString(original);
        User parsed = mapper.readValue(json, User.class);

        assertEquals(original.getFirstName(), parsed.getFirstName());
        assertEquals(original.getLastName(), parsed.getLastName());
        assertEquals(original.getAge(), parsed.getAge());
    }

    // ==================== Tests: rebuild preserves mixins ====================

    @Test
    public void testRebuildPreservesMixIn() {
        ObjectMapper mapper1 = ObjectMapper.builder()
                .addMixIn(User.class, UserMixIn.class)
                .build();

        ObjectMapper mapper2 = mapper1.rebuild().build();

        User user = new User();
        user.setFirstName("Test");

        String json = mapper2.writeValueAsString(user);
        assertTrue(json.contains("first_name"), json);
    }
}
