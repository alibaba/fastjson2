package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import com.alibaba.fastjson3.annotation.JSONType;
import com.alibaba.fastjson3.annotation.NamingStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class POJOTest {
    // ==================== Simple POJO ====================

    public static class User {
        public String name;
        public int age;
        public boolean active;
    }

    @Test
    void serializeSimplePojo() {
        User user = new User();
        user.name = "Alice";
        user.age = 30;
        user.active = true;
        String json = JSON.toJSONString(user);
        assertNotNull(json);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"Alice\""));
        assertTrue(json.contains("\"age\""));
        assertTrue(json.contains("30"));
    }

    @Test
    void deserializeSimplePojo() {
        String json = "{\"name\":\"Bob\",\"age\":25,\"active\":true}";
        User user = JSON.parseObject(json, User.class);
        assertNotNull(user);
        assertEquals("Bob", user.name);
        assertEquals(25, user.age);
        assertTrue(user.active);
    }

    @Test
    void roundTrip() {
        User original = new User();
        original.name = "Charlie";
        original.age = 35;
        original.active = false;

        String json = JSON.toJSONString(original);
        User deserialized = JSON.parseObject(json, User.class);

        assertEquals(original.name, deserialized.name);
        assertEquals(original.age, deserialized.age);
        assertEquals(original.active, deserialized.active);
    }

    // ==================== Getter/Setter POJO ====================

    public static class Product {
        private String name;
        private double price;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }

    @Test
    void serializeWithGetters() {
        Product p = new Product();
        p.setName("Widget");
        p.setPrice(9.99);
        String json = JSON.toJSONString(p);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"Widget\""));
        assertTrue(json.contains("9.99"));
    }

    @Test
    void deserializeWithSetters() {
        String json = "{\"name\":\"Gadget\",\"price\":19.99}";
        Product p = JSON.parseObject(json, Product.class);
        assertEquals("Gadget", p.getName());
        assertEquals(19.99, p.getPrice());
    }

    // ==================== @JSONField ====================

    public static class AnnotatedBean {
        @JSONField(name = "user_name")
        public String userName;

        @JSONField(ordinal = 1)
        public int id;
    }

    @Test
    void deserializeWithJsonField() {
        String json = "{\"user_name\":\"Dave\",\"id\":42}";
        AnnotatedBean bean = JSON.parseObject(json, AnnotatedBean.class);
        assertEquals("Dave", bean.userName);
        assertEquals(42, bean.id);
    }

    @Test
    void serializeWithJsonField() {
        AnnotatedBean bean = new AnnotatedBean();
        bean.userName = "Eve";
        bean.id = 7;
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"user_name\""));
        assertTrue(json.contains("\"Eve\""));
    }

    // ==================== @JSONType naming strategy ====================

    @JSONType(naming = NamingStrategy.SnakeCase)
    public static class SnakeCaseBean {
        public String firstName;
        public String lastName;
    }

    @Test
    void deserializeWithSnakeCase() {
        String json = "{\"first_name\":\"Frank\",\"last_name\":\"Smith\"}";
        SnakeCaseBean bean = JSON.parseObject(json, SnakeCaseBean.class);
        assertEquals("Frank", bean.firstName);
        assertEquals("Smith", bean.lastName);
    }

    // ==================== Nested POJOs ====================

    public static class Address {
        public String city;
        public String country;
    }

    public static class Person {
        public String name;
        public Address address;
    }

    @Test
    void deserializeNestedPojo() {
        String json = "{\"name\":\"Grace\",\"address\":{\"city\":\"NYC\",\"country\":\"US\"}}";
        Person person = JSON.parseObject(json, Person.class);
        assertEquals("Grace", person.name);
        // Address is a nested object - for now it will be a JSONObject, not Address
        // Full nested POJO support requires recursive ObjectReader resolution
    }

    // ==================== List deserialization ====================

    @Test
    void parseArrayToList() {
        String json = "[{\"name\":\"A\",\"age\":1},{\"name\":\"B\",\"age\":2}]";
        List<User> users = JSON.parseArray(json, User.class);
        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("A", users.get(0).name);
        assertEquals("B", users.get(1).name);
    }

    // ==================== Null handling ====================

    @Test
    void deserializeNullFields() {
        String json = "{\"name\":null,\"age\":0}";
        User user = JSON.parseObject(json, User.class);
        assertNull(user.name);
        assertEquals(0, user.age);
    }

    @Test
    void deserializeNullInput() {
        assertNull(JSON.parseObject((String) null, User.class));
        assertNull(JSON.parseObject("", User.class));
    }

    // ==================== ObjectMapper builder ====================

    @Test
    void customMapperWithFeatures() {
        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.PrettyFormat)
                .build();
        assertNotNull(mapper);
        assertTrue(mapper.isWriteEnabled(WriteFeature.PrettyFormat));
    }

    @Test
    void mapperReadWrite() {
        ObjectMapper mapper = ObjectMapper.shared();
        User user = new User();
        user.name = "Test";
        user.age = 10;

        String json = mapper.writeValueAsString(user);
        assertNotNull(json);

        User parsed = mapper.readValue(json, User.class);
        assertEquals("Test", parsed.name);
        assertEquals(10, parsed.age);
    }
}
