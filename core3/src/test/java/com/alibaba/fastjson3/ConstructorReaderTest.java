package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for constructor-with-parameters deserialization.
 * This validates support for Kotlin data classes and Java immutable classes.
 */
public class ConstructorReaderTest {
    static final ObjectMapper MAPPER = ObjectMapper.shared();

    // ==================== Java immutable class (simulates Kotlin data class) ====================

    public static class ImmutablePoint {
        private final int x;
        private final int y;

        public ImmutablePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    @Test
    public void testImmutableClassDeserialization() {
        ImmutablePoint point = MAPPER.readValue("{\"x\":10,\"y\":20}", ImmutablePoint.class);
        assertNotNull(point);
        assertEquals(10, point.getX());
        assertEquals(20, point.getY());
    }

    @Test
    public void testImmutableClassSerialization() {
        ImmutablePoint point = new ImmutablePoint(3, 4);
        String json = MAPPER.writeValueAsString(point);
        assertTrue(json.contains("\"x\":3"));
        assertTrue(json.contains("\"y\":4"));
    }

    @Test
    public void testImmutableClassRoundtrip() {
        ImmutablePoint original = new ImmutablePoint(100, 200);
        String json = MAPPER.writeValueAsString(original);
        ImmutablePoint parsed = MAPPER.readValue(json, ImmutablePoint.class);
        assertEquals(original.getX(), parsed.getX());
        assertEquals(original.getY(), parsed.getY());
    }

    // ==================== Immutable class with String fields ====================

    public static class ImmutableUser {
        private final String name;
        private final int age;
        private final String email;

        public ImmutableUser(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public String getEmail() {
            return email;
        }
    }

    @Test
    public void testImmutableUserDeserialization() {
        ImmutableUser user = MAPPER.readValue(
                "{\"name\":\"Alice\",\"age\":30,\"email\":\"alice@example.com\"}",
                ImmutableUser.class);
        assertNotNull(user);
        assertEquals("Alice", user.getName());
        assertEquals(30, user.getAge());
        assertEquals("alice@example.com", user.getEmail());
    }

    @Test
    public void testImmutableUserRoundtrip() {
        ImmutableUser original = new ImmutableUser("Bob", 25, "bob@test.com");
        String json = MAPPER.writeValueAsString(original);
        ImmutableUser parsed = MAPPER.readValue(json, ImmutableUser.class);
        assertEquals(original.getName(), parsed.getName());
        assertEquals(original.getAge(), parsed.getAge());
        assertEquals(original.getEmail(), parsed.getEmail());
    }

    // ==================== Null fields ====================

    @Test
    public void testImmutableClassWithNullField() {
        ImmutableUser user = MAPPER.readValue(
                "{\"name\":\"Alice\",\"age\":30}",
                ImmutableUser.class);
        assertNotNull(user);
        assertEquals("Alice", user.getName());
        assertEquals(30, user.getAge());
        assertNull(user.getEmail());
    }

    // ==================== Nested immutable class ====================

    public static class ImmutableLine {
        private final ImmutablePoint start;
        private final ImmutablePoint end;

        public ImmutableLine(ImmutablePoint start, ImmutablePoint end) {
            this.start = start;
            this.end = end;
        }

        public ImmutablePoint getStart() {
            return start;
        }

        public ImmutablePoint getEnd() {
            return end;
        }
    }

    @Test
    public void testNestedImmutableClassRoundtrip() {
        ImmutableLine original = new ImmutableLine(
                new ImmutablePoint(1, 2),
                new ImmutablePoint(3, 4));
        String json = MAPPER.writeValueAsString(original);
        ImmutableLine parsed = MAPPER.readValue(json, ImmutableLine.class);
        assertNotNull(parsed);
        assertEquals(1, parsed.getStart().getX());
        assertEquals(2, parsed.getStart().getY());
        assertEquals(3, parsed.getEnd().getX());
        assertEquals(4, parsed.getEnd().getY());
    }

    // ==================== With @JSONCreator on multi-param constructor ====================

    public static class AnnotatedImmutable {
        private final String id;
        private final String value;

        @com.alibaba.fastjson3.annotation.JSONCreator
        public AnnotatedImmutable(String id, String value) {
            this.id = id;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public String getValue() {
            return value;
        }
    }

    @Test
    public void testJSONCreatorWithParameters() {
        AnnotatedImmutable obj = MAPPER.readValue(
                "{\"id\":\"abc\",\"value\":\"xyz\"}",
                AnnotatedImmutable.class);
        assertNotNull(obj);
        assertEquals("abc", obj.getId());
        assertEquals("xyz", obj.getValue());
    }

    // ==================== List of immutable objects ====================

    @Test
    public void testListOfImmutableObjects() {
        List<ImmutablePoint> points = MAPPER.readList(
                "[{\"x\":1,\"y\":2},{\"x\":3,\"y\":4}]",
                ImmutablePoint.class);
        assertNotNull(points);
        assertEquals(2, points.size());
        assertEquals(1, points.get(0).getX());
        assertEquals(4, points.get(1).getY());
    }

    // ==================== Immutable class with inherited fields ====================

    public static class BaseEntity {
        private final String id;

        public BaseEntity(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    public static class NamedEntity extends BaseEntity {
        private final String name;

        public NamedEntity(String id, String name) {
            super(id);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Test
    public void testImmutableClassWithInheritance() {
        NamedEntity entity = MAPPER.readValue(
                "{\"id\":\"123\",\"name\":\"test\"}", NamedEntity.class);
        assertNotNull(entity);
        assertEquals("123", entity.getId());
        assertEquals("test", entity.getName());
    }

    // ==================== Class with default constructor should still use it ====================

    public static class MutableBean {
        private String name;

        public MutableBean() {
        }

        public MutableBean(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testClassWithBothConstructorsUsesNoArg() {
        // Should use no-arg constructor + setter, not the all-args constructor
        MutableBean bean = MAPPER.readValue("{\"name\":\"test\"}", MutableBean.class);
        assertNotNull(bean);
        assertEquals("test", bean.getName());
    }
}
