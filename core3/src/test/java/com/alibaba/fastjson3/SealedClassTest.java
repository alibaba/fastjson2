package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for sealed class polymorphic deserialization.
 */
class SealedClassTest {
    private final ObjectMapper mapper = ObjectMapper.shared();

    // ==================== Sealed interface with type discriminator ====================

    @Test
    void sealedDeserialize_cat() {
        String json = "{\"@type\":\"Cat\",\"name\":\"Whiskers\",\"indoor\":true}";
        Animal animal = mapper.readValue(json, Animal.class);
        assertInstanceOf(Cat.class, animal);
        Cat cat = (Cat) animal;
        assertEquals("Whiskers", cat.name);
        assertTrue(cat.indoor);
    }

    @Test
    void sealedDeserialize_dog() {
        String json = "{\"@type\":\"Dog\",\"name\":\"Rex\",\"breed\":\"Labrador\"}";
        Animal animal = mapper.readValue(json, Animal.class);
        assertInstanceOf(Dog.class, animal);
        Dog dog = (Dog) animal;
        assertEquals("Rex", dog.name);
        assertEquals("Labrador", dog.breed);
    }

    // ==================== Sealed with custom typeKey ====================

    @Test
    void sealedCustomTypeKey() {
        String json = "{\"kind\":\"Circle\",\"radius\":5.0}";
        Shape shape = mapper.readValue(json, Shape.class);
        assertInstanceOf(Circle.class, shape);
        assertEquals(5.0, ((Circle) shape).radius);
    }

    @Test
    void sealedCustomTypeKey_rect() {
        String json = "{\"kind\":\"Rect\",\"width\":3,\"height\":4}";
        Shape shape = mapper.readValue(json, Shape.class);
        assertInstanceOf(Rectangle.class, shape);
        assertEquals(3, ((Rectangle) shape).width);
        assertEquals(4, ((Rectangle) shape).height);
    }

    // ==================== Single permitted subtype ====================

    @Test
    void sealedSingleSubtype() {
        String json = "{\"message\":\"hello\"}";
        SingleParent result = mapper.readValue(json, SingleParent.class);
        assertInstanceOf(SingleChild.class, result);
        assertEquals("hello", ((SingleChild) result).message);
    }

    // ==================== Negative tests ====================

    @Test
    void sealedUnknownTypeName() {
        String json = "{\"@type\":\"Fish\",\"name\":\"Nemo\"}";
        assertThrows(JSONException.class, () -> mapper.readValue(json, Animal.class));
    }

    @Test
    void sealedMissingTypeKey_multipleSubtypes() {
        String json = "{\"name\":\"Unknown\"}";
        assertThrows(JSONException.class, () -> mapper.readValue(json, Animal.class));
    }

    @Test
    void sealedNull() {
        String json = "null";
        Animal animal = mapper.readValue(json, Animal.class);
        assertNull(animal);
    }

    // ==================== Test types ====================

    public sealed interface Animal permits Cat, Dog {
    }

    public static final class Cat implements Animal {
        public String name;
        public boolean indoor;
    }

    public static final class Dog implements Animal {
        public String name;
        public String breed;
    }

    @JSONType(typeKey = "kind")
    public sealed interface Shape permits Circle, Rectangle {
    }

    @JSONType(typeName = "Circle")
    public static final class Circle implements Shape {
        public double radius;
    }

    @JSONType(typeName = "Rect")
    public static final class Rectangle implements Shape {
        public int width;
        public int height;
    }

    public sealed interface SingleParent permits SingleChild {
    }

    public static final class SingleChild implements SingleParent {
        public String message;
    }
}
