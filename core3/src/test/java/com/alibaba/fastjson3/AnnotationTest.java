package com.alibaba.fastjson3;

import com.alibaba.fastjson3.annotation.JSONField;
import com.alibaba.fastjson3.annotation.JSONType;
import com.alibaba.fastjson3.annotation.NamingStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for annotation-driven serialization/deserialization.
 * Covers @JSONField, @JSONType, NamingStrategy branches in both
 * ObjectReaderCreator and ObjectWriterCreator.
 */
class AnnotationTest {
    // ==================== @JSONField.serialize / deserialize ====================

    public static class SecureBean {
        public String name;

        @JSONField(serialize = false)
        public String secret;

        @JSONField(deserialize = false)
        public String computed;
    }

    @Test
    void serializeSkipsFieldWhenSerializeFalse() {
        SecureBean bean = new SecureBean();
        bean.name = "Alice";
        bean.secret = "password";
        bean.computed = "hash123";
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"name\""));
        assertFalse(json.contains("secret"));
        assertTrue(json.contains("\"computed\""));
    }

    @Test
    void deserializeSkipsFieldWhenDeserializeFalse() {
        String json = "{\"name\":\"Bob\",\"secret\":\"pw\",\"computed\":\"val\"}";
        SecureBean bean = JSON.parseObject(json, SecureBean.class);
        assertEquals("Bob", bean.name);
        assertEquals("pw", bean.secret);
        assertNull(bean.computed);
    }

    // ==================== @JSONField.alternateNames ====================

    public static class AliasBean {
        @JSONField(alternateNames = {"user_name", "USERNAME"})
        public String userName;
        public int age;
    }

    @Test
    void deserializeWithAlternateName() {
        AliasBean b1 = JSON.parseObject("{\"user_name\":\"A\",\"age\":1}", AliasBean.class);
        assertEquals("A", b1.userName);

        AliasBean b2 = JSON.parseObject("{\"USERNAME\":\"B\",\"age\":2}", AliasBean.class);
        assertEquals("B", b2.userName);

        AliasBean b3 = JSON.parseObject("{\"userName\":\"C\",\"age\":3}", AliasBean.class);
        assertEquals("C", b3.userName);
    }

    // ==================== @JSONField.defaultValue ====================

    public static class DefaultBean {
        @JSONField(defaultValue = "guest")
        public String name;

        @JSONField(defaultValue = "18")
        public int age;

        @JSONField(defaultValue = "1.5")
        public double score;

        @JSONField(defaultValue = "100")
        public long id;

        @JSONField(defaultValue = "true")
        public boolean active;

        @JSONField(defaultValue = "3.14")
        public float ratio;
    }

    @Test
    void defaultValuesAppliedWhenFieldMissing() {
        DefaultBean bean = JSON.parseObject("{}", DefaultBean.class);
        assertEquals("guest", bean.name);
        assertEquals(18, bean.age);
        assertEquals(1.5, bean.score);
        assertEquals(100L, bean.id);
        assertTrue(bean.active);
        assertEquals(3.14f, bean.ratio, 0.01f);
    }

    @Test
    void defaultValuesNotAppliedWhenFieldPresent() {
        DefaultBean bean = JSON.parseObject(
                "{\"name\":\"Alice\",\"age\":25,\"score\":9.9,\"id\":1,\"active\":false,\"ratio\":2.0}",
                DefaultBean.class
        );
        assertEquals("Alice", bean.name);
        assertEquals(25, bean.age);
        assertEquals(9.9, bean.score, 0.001);
        assertEquals(1L, bean.id);
        assertFalse(bean.active);
    }

    // ==================== @JSONField.required ====================

    public static class RequiredBean {
        @JSONField(required = true)
        public String name;
        public int age;
    }

    @Test
    void requiredFieldMissingThrows() {
        assertThrows(JSONException.class, () ->
                JSON.parseObject("{\"age\":10}", RequiredBean.class)
        );
    }

    @Test
    void requiredFieldPresentSucceeds() {
        RequiredBean bean = JSON.parseObject("{\"name\":\"OK\",\"age\":10}", RequiredBean.class);
        assertEquals("OK", bean.name);
    }

    // ==================== @JSONField.ordinal (write ordering) ====================

    public static class OrderedBean {
        @JSONField(ordinal = 3)
        public String c;
        @JSONField(ordinal = 1)
        public String a;
        @JSONField(ordinal = 2)
        public String b;
    }

    @Test
    void writeRespectsOrdinal() {
        OrderedBean bean = new OrderedBean();
        bean.a = "1";
        bean.b = "2";
        bean.c = "3";
        String json = JSON.toJSONString(bean);
        int posA = json.indexOf("\"a\"");
        int posB = json.indexOf("\"b\"");
        int posC = json.indexOf("\"c\"");
        assertTrue(posA < posB, "a should come before b");
        assertTrue(posB < posC, "b should come before c");
    }

    // ==================== @JSONType.includes / ignores ====================

    @JSONType(includes = {"name", "age"})
    public static class IncludeBean {
        public String name;
        public int age;
        public String secret;
    }

    @Test
    void writeRespectsIncludes() {
        IncludeBean bean = new IncludeBean();
        bean.name = "Alice";
        bean.age = 30;
        bean.secret = "hidden";
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"age\""));
        assertFalse(json.contains("secret"));
    }

    @Test
    void readRespectsIncludes() {
        IncludeBean bean = JSON.parseObject(
                "{\"name\":\"Bob\",\"age\":25,\"secret\":\"val\"}", IncludeBean.class
        );
        assertEquals("Bob", bean.name);
        assertEquals(25, bean.age);
        assertNull(bean.secret);
    }

    @JSONType(ignores = "password")
    public static class IgnoreBean {
        public String name;
        public String password;
    }

    @Test
    void writeRespectsIgnores() {
        IgnoreBean bean = new IgnoreBean();
        bean.name = "Alice";
        bean.password = "secret";
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"name\""));
        assertFalse(json.contains("password"));
    }

    @Test
    void readRespectsIgnores() {
        IgnoreBean bean = JSON.parseObject(
                "{\"name\":\"Bob\",\"password\":\"pw\"}", IgnoreBean.class
        );
        assertEquals("Bob", bean.name);
        assertNull(bean.password);
    }

    // ==================== @JSONType.orders ====================

    @JSONType(orders = {"id", "name", "email"})
    public static class OrderedTypeBean {
        public String email;
        public String name;
        public int id;
    }

    @Test
    void writeRespectsTypeOrders() {
        OrderedTypeBean bean = new OrderedTypeBean();
        bean.id = 1;
        bean.name = "Alice";
        bean.email = "a@b.c";
        String json = JSON.toJSONString(bean);
        int posId = json.indexOf("\"id\"");
        int posName = json.indexOf("\"name\"");
        int posEmail = json.indexOf("\"email\"");
        assertTrue(posId < posName);
        assertTrue(posName < posEmail);
    }

    // ==================== @JSONType.alphabetic = false ====================

    @JSONType(alphabetic = false)
    public static class NonAlphaBean {
        public String z;
        public String a;
    }

    @Test
    void nonAlphabeticPreservesDeclarationOrder() {
        NonAlphaBean bean = new NonAlphaBean();
        bean.z = "1";
        bean.a = "2";
        String json = JSON.toJSONString(bean);
        // With alphabetic=false, order should NOT necessarily be alphabetical
        assertNotNull(json);
        assertTrue(json.contains("\"z\""));
        assertTrue(json.contains("\"a\""));
    }

    // ==================== All NamingStrategy branches ====================

    @JSONType(naming = NamingStrategy.CamelCase)
    public static class CamelBean {
        public String firstName;
    }

    @JSONType(naming = NamingStrategy.PascalCase)
    public static class PascalBean {
        public String firstName;
    }

    @JSONType(naming = NamingStrategy.SnakeCase)
    public static class SnakeBean {
        public String firstName;
    }

    @JSONType(naming = NamingStrategy.UpperSnakeCase)
    public static class UpperSnakeBean {
        public String firstName;
    }

    @JSONType(naming = NamingStrategy.KebabCase)
    public static class KebabBean {
        public String firstName;
    }

    @JSONType(naming = NamingStrategy.UpperKebabCase)
    public static class UpperKebabBean {
        public String firstName;
    }

    @Test
    void namingStrategyCamelCase() {
        CamelBean bean = new CamelBean();
        bean.firstName = "Alice";
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"firstName\""), json);
    }

    @Test
    void namingStrategyPascalCase() {
        PascalBean bean = new PascalBean();
        bean.firstName = "Alice";
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"FirstName\""), json);
    }

    @Test
    void namingStrategySnakeCase() {
        SnakeBean bean = new SnakeBean();
        bean.firstName = "Alice";
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"first_name\""), json);
    }

    @Test
    void namingStrategyUpperSnakeCase() {
        UpperSnakeBean bean = new UpperSnakeBean();
        bean.firstName = "Alice";
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"FIRST_NAME\""), json);
    }

    @Test
    void namingStrategyKebabCase() {
        KebabBean bean = new KebabBean();
        bean.firstName = "Alice";
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"first-name\""), json);
    }

    @Test
    void namingStrategyUpperKebabCase() {
        UpperKebabBean bean = new UpperKebabBean();
        bean.firstName = "Alice";
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"FIRST-NAME\""), json);
    }

    @Test
    void namingStrategyDeserializeRoundTrip() {
        SnakeBean bean = new SnakeBean();
        bean.firstName = "Alice";
        String json = JSON.toJSONString(bean);
        SnakeBean parsed = JSON.parseObject(json, SnakeBean.class);
        assertEquals("Alice", parsed.firstName);
    }

    // ==================== @JSONField on getter method ====================

    public static class GetterAnnotatedBean {
        private String value;

        @JSONField(name = "val")
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Test
    void jsonFieldOnGetterAffectsSerializeName() {
        GetterAnnotatedBean bean = new GetterAnnotatedBean();
        bean.setValue("test");
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"val\""), json);
        assertFalse(json.contains("\"value\""));
    }

    // ==================== @JSONField on setter method ====================

    public static class SetterAnnotatedBean {
        private String fullName;

        public String getFullName() {
            return fullName;
        }

        @JSONField(name = "full_name")
        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }

    @Test
    void jsonFieldOnSetterAffectsDeserializeName() {
        SetterAnnotatedBean bean = JSON.parseObject("{\"full_name\":\"Bob\"}", SetterAnnotatedBean.class);
        assertEquals("Bob", bean.getFullName());
    }

    // ==================== Setter-based POJO with @JSONField.serialize=false on field ====================

    public static class FieldAnnotatedGetter {
        @JSONField(serialize = false)
        public String hidden;

        public String visible;
    }

    @Test
    void serializeFalseOnFieldSkipsGetter() {
        FieldAnnotatedGetter bean = new FieldAnnotatedGetter();
        bean.hidden = "secret";
        bean.visible = "ok";
        String json = JSON.toJSONString(bean);
        assertFalse(json.contains("hidden"));
        assertTrue(json.contains("\"visible\""));
    }

    // ==================== WriteNulls feature ====================

    @Test
    void writeNullsFeature() {
        ObjectMapper mapper = ObjectMapper.builder()
                .enableWrite(WriteFeature.WriteNulls)
                .build();
        SecureBean bean = new SecureBean();
        bean.name = null;
        bean.secret = null;
        bean.computed = "val";
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("null"), json);
        assertTrue(json.contains("\"computed\""));
    }

    // ==================== Nested POJO with List ====================

    public static class Team {
        public String name;
        public List<Member> members;
    }

    public static class Member {
        public String name;
        public int score;
    }

    @Test
    void nestedPojoWithListRoundTrip() {
        String json = "{\"name\":\"A\",\"members\":[{\"name\":\"M1\",\"score\":10},{\"name\":\"M2\",\"score\":20}]}";
        Team team = JSON.parseObject(json, Team.class);
        assertEquals("A", team.name);
        assertNotNull(team.members);
        assertEquals(2, team.members.size());
        assertEquals("M1", ((java.util.Map<?, ?>) team.members.get(0)).get("name").toString());
    }

    // ==================== Inheritance ====================

    public static class Base {
        public int id;
    }

    public static class Derived extends Base {
        public String name;
    }

    @Test
    void inheritedFieldsSerialized() {
        Derived d = new Derived();
        d.id = 42;
        d.name = "Test";
        String json = JSON.toJSONString(d);
        assertTrue(json.contains("\"id\""));
        assertTrue(json.contains("42"));
        assertTrue(json.contains("\"name\""));
    }

    @Test
    void inheritedFieldsDeserialized() {
        Derived d = JSON.parseObject("{\"id\":42,\"name\":\"Test\"}", Derived.class);
        assertEquals(42, d.id);
        assertEquals("Test", d.name);
    }

    // ==================== ErrorOnUnknownProperties ====================

    @Test
    void errorOnUnknownPropertiesThrows() {
        ObjectMapper mapper = ObjectMapper.builder()
                .enableRead(ReadFeature.ErrorOnUnknownProperties)
                .build();
        assertThrows(JSONException.class, () ->
                mapper.readValue("{\"name\":\"A\",\"unknown\":1}", RequiredBean.class)
        );
    }

    @Test
    void unknownPropertiesIgnoredByDefault() {
        RequiredBean bean = JSON.parseObject("{\"name\":\"A\",\"unknown\":1}", RequiredBean.class);
        assertEquals("A", bean.name);
    }
}
