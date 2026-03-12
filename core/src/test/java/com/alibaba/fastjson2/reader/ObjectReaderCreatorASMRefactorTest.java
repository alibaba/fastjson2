package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PR #4021 refactoring of ObjectReaderCreatorASM.
 *
 * <p>Covers the following changes:
 * <ol>
 *   <li>TYPE_OBJECT_READERS array lookup (replaces 12-case switch)</li>
 *   <li>buildHashCode32Map utility (shared by getFieldReader, readObject, readJSONBObject)</li>
 *   <li>genMethodGetFieldReaderImpl (merged getFieldReader/getFieldReaderLCase)</li>
 *   <li>Removal of unused variables in ≤6 field branch</li>
 *   <li>Cached fieldNameCharLengthMin/Max in ObjectReadContext</li>
 *   <li>Enriched exception diagnostics</li>
 * </ol>
 */
public class ObjectReaderCreatorASMRefactorTest {
    // ============================================================
    // 1. TYPE_OBJECT_READERS array: super class selection
    //    Verify that classes with 1-12 fields use ObjectReaderN,
    //    and >12 fields use ObjectReaderAdapter.
    // ============================================================

    public static class Fields1 {
        public int f1;
    }

    public static class Fields6 {
        public int f1;
        public int f2;
        public int f3;
        public int f4;
        public int f5;
        public int f6;
    }

    public static class Fields12 {
        public int f01;
        public int f02;
        public int f03;
        public int f04;
        public int f05;
        public int f06;
        public int f07;
        public int f08;
        public int f09;
        public int f10;
        public int f11;
        public int f12;
    }

    public static class Fields13 {
        public int f01;
        public int f02;
        public int f03;
        public int f04;
        public int f05;
        public int f06;
        public int f07;
        public int f08;
        public int f09;
        public int f10;
        public int f11;
        public int f12;
        public int f13;
    }

    @Test
    public void testSuperClassSelection() {
        // 1 field -> ObjectReader1
        ObjectReader<?> reader1 = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Fields1.class);
        assertEquals("ObjectReader1", reader1.getClass().getSuperclass().getSimpleName());

        // 6 fields -> ObjectReader6
        ObjectReader<?> reader6 = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Fields6.class);
        assertEquals("ObjectReader6", reader6.getClass().getSuperclass().getSimpleName());

        // 12 fields -> ObjectReader12
        ObjectReader<?> reader12 = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Fields12.class);
        assertEquals("ObjectReader12", reader12.getClass().getSuperclass().getSimpleName());

        // 13 fields -> ObjectReaderAdapter (exceeds ObjectReader12)
        ObjectReader<?> reader13 = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Fields13.class);
        assertEquals("ObjectReaderAdapter", reader13.getClass().getSuperclass().getSimpleName());
    }

    @Test
    public void testSuperClassSelectionRoundTrip() {
        // 12 fields: boundary case for ObjectReader12
        Fields12 bean12 = new Fields12();
        bean12.f01 = 1;
        bean12.f06 = 6;
        bean12.f12 = 12;
        String json12 = JSON.toJSONString(bean12);
        Fields12 parsed12 = JSON.parseObject(json12, Fields12.class);
        assertEquals(1, parsed12.f01);
        assertEquals(6, parsed12.f06);
        assertEquals(12, parsed12.f12);

        // 13 fields: first case using ObjectReaderAdapter
        Fields13 bean13 = new Fields13();
        bean13.f01 = 1;
        bean13.f13 = 13;
        String json13 = JSON.toJSONString(bean13);
        Fields13 parsed13 = JSON.parseObject(json13, Fields13.class);
        assertEquals(1, parsed13.f01);
        assertEquals(13, parsed13.f13);
    }

    // ============================================================
    // 2. buildHashCode32Map + 3. genMethodGetFieldReaderImpl
    //    The >6 fields path uses buildHashCode32Map in both
    //    getFieldReader and readObject/readJSONBObject.
    //    Test with 8 fields (>6 threshold) for all paths.
    // ============================================================

    public static class EightFields {
        public String alpha;
        public String bravo;
        public String charlie;
        public String delta;
        public String echo;
        public String foxtrot;
        public String golf;
        public String hotel;
    }

    @Test
    public void testGetFieldReaderGt6() {
        ObjectReader<?> reader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(EightFields.class);

        // getFieldReader (case-sensitive hash64)
        for (String name : new String[]{"alpha", "bravo", "charlie", "delta", "echo", "foxtrot", "golf", "hotel"}) {
            long hash = Fnv.hashCode64(name);
            assertNotNull(reader.getFieldReader(hash), "getFieldReader should find: " + name);
        }

        // getFieldReaderLCase (case-insensitive hash64)
        for (String name : new String[]{"alpha", "bravo", "charlie", "delta", "echo", "foxtrot", "golf", "hotel"}) {
            long hashLCase = Fnv.hashCode64LCase(name);
            assertNotNull(reader.getFieldReaderLCase(hashLCase), "getFieldReaderLCase should find: " + name);
        }

        // Non-existent field
        assertNull(reader.getFieldReader(Fnv.hashCode64("nonexistent")));
        assertNull(reader.getFieldReaderLCase(Fnv.hashCode64LCase("nonexistent")));
        assertNull(reader.getFieldReader(0L));
        assertNull(reader.getFieldReaderLCase(0L));
    }

    @Test
    public void testReadObjectGt6() {
        // readObject path: exercises buildHashCode32Map in genMethodReadObject
        String json = "{\"alpha\":\"A\",\"bravo\":\"B\",\"charlie\":\"C\",\"delta\":\"D\","
                + "\"echo\":\"E\",\"foxtrot\":\"F\",\"golf\":\"G\",\"hotel\":\"H\"}";
        EightFields bean = JSON.parseObject(json, EightFields.class);
        assertEquals("A", bean.alpha);
        assertEquals("D", bean.delta);
        assertEquals("H", bean.hotel);
    }

    @Test
    public void testReadObjectGt6UTF8() {
        String json = "{\"alpha\":\"A\",\"bravo\":\"B\",\"charlie\":\"C\",\"delta\":\"D\","
                + "\"echo\":\"E\",\"foxtrot\":\"F\",\"golf\":\"G\",\"hotel\":\"H\"}";
        EightFields bean = JSON.parseObject(json.getBytes(), EightFields.class);
        assertEquals("A", bean.alpha);
        assertEquals("H", bean.hotel);
    }

    @Test
    public void testReadJSONBObjectGt6() {
        // readJSONBObject path: exercises buildHashCode32Map in genMethodReadJSONBObject
        EightFields bean = new EightFields();
        bean.alpha = "A";
        bean.bravo = "B";
        bean.charlie = "C";
        bean.delta = "D";
        bean.echo = "E";
        bean.foxtrot = "F";
        bean.golf = "G";
        bean.hotel = "H";
        byte[] jsonb = JSONB.toBytes(bean);
        EightFields parsed = JSONB.parseObject(jsonb, EightFields.class);
        assertEquals("A", parsed.alpha);
        assertEquals("D", parsed.delta);
        assertEquals("H", parsed.hotel);
    }

    @Test
    public void testSmartMatchGt6() {
        // SmartMatch uses getFieldReaderLCase internally
        String json = "{\"ALPHA\":\"A\",\"BRAVO\":\"B\",\"CHARLIE\":\"C\",\"DELTA\":\"D\","
                + "\"ECHO\":\"E\",\"FOXTROT\":\"F\",\"GOLF\":\"G\",\"HOTEL\":\"H\"}";
        EightFields bean = JSON.parseObject(json, EightFields.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals("A", bean.alpha);
        assertEquals("H", bean.hotel);
    }

    // ============================================================
    // 4. Removed unused variables in ≤6 field branch
    //    Verify ≤6 fields still work correctly after removing
    //    the get_ Label and fieldName String variables.
    // ============================================================

    public static class ThreeFields {
        public int x;
        public String y;
        public double z;
    }

    @Test
    public void testLe6FieldsGetFieldReader() {
        ObjectReader<?> reader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(ThreeFields.class);

        // ≤6 path: linear scan with hashCode64
        assertNotNull(reader.getFieldReader(Fnv.hashCode64("x")));
        assertNotNull(reader.getFieldReader(Fnv.hashCode64("y")));
        assertNotNull(reader.getFieldReader(Fnv.hashCode64("z")));
        assertNull(reader.getFieldReader(Fnv.hashCode64("w")));

        assertNotNull(reader.getFieldReaderLCase(Fnv.hashCode64LCase("x")));
        assertNotNull(reader.getFieldReaderLCase(Fnv.hashCode64LCase("y")));
        assertNotNull(reader.getFieldReaderLCase(Fnv.hashCode64LCase("z")));
        assertNull(reader.getFieldReaderLCase(Fnv.hashCode64LCase("w")));
    }

    @Test
    public void testLe6FieldsReadObject() {
        ThreeFields bean = JSON.parseObject("{\"x\":1,\"y\":\"hello\",\"z\":3.14}", ThreeFields.class);
        assertEquals(1, bean.x);
        assertEquals("hello", bean.y);
        assertEquals(3.14, bean.z, 0.001);
    }

    @Test
    public void testLe6FieldsJSONB() {
        ThreeFields bean = new ThreeFields();
        bean.x = 42;
        bean.y = "test";
        bean.z = 2.71;
        byte[] jsonb = JSONB.toBytes(bean);
        ThreeFields parsed = JSONB.parseObject(jsonb, ThreeFields.class);
        assertEquals(42, parsed.x);
        assertEquals("test", parsed.y);
        assertEquals(2.71, parsed.z, 0.001);
    }

    // ============================================================
    // 5. Cached fieldNameCharLengthMin/Max
    //    These are passed to readFieldNameHashCode(typeKeyLen, min, max).
    //    Test with varied field name lengths to exercise the bounds.
    // ============================================================

    // Mixed lengths: "id"(2) to "registered"(10) — simulates Client
    public static class MixedLengths {
        public int id;
        public int age;
        public String name;
        public String email;
        public String address;
        public double latitude;
        public double longitude;
        public String registered;
    }

    @Test
    public void testCharLengthMinMaxReadObject() {
        // This exercises readFieldNameHashCode(typeKeyLen, min=2, max=10)
        String json = "{\"id\":1,\"age\":25,\"name\":\"test\",\"email\":\"a@b.c\","
                + "\"address\":\"123 St\",\"latitude\":1.0,\"longitude\":2.0,\"registered\":\"2024-01-01\"}";
        MixedLengths bean = JSON.parseObject(json, MixedLengths.class);
        assertEquals(1, bean.id);
        assertEquals(25, bean.age);
        assertEquals("test", bean.name);
        assertEquals("a@b.c", bean.email);
        assertEquals("123 St", bean.address);
        assertEquals(1.0, bean.latitude, 0.001);
        assertEquals(2.0, bean.longitude, 0.001);
        assertEquals("2024-01-01", bean.registered);
    }

    @Test
    public void testCharLengthMinMaxReadObjectUTF8() {
        String json = "{\"id\":1,\"age\":25,\"name\":\"test\",\"email\":\"a@b.c\","
                + "\"address\":\"123 St\",\"latitude\":1.0,\"longitude\":2.0,\"registered\":\"2024-01-01\"}";
        MixedLengths bean = JSON.parseObject(json.getBytes(), MixedLengths.class);
        assertEquals(1, bean.id);
        assertEquals("2024-01-01", bean.registered);
    }

    @Test
    public void testCharLengthMinMaxJSONB() {
        MixedLengths bean = new MixedLengths();
        bean.id = 1;
        bean.age = 25;
        bean.name = "test";
        bean.registered = "2024-01-01";
        byte[] jsonb = JSONB.toBytes(bean);
        MixedLengths parsed = JSONB.parseObject(jsonb, MixedLengths.class);
        assertEquals(1, parsed.id);
        assertEquals(25, parsed.age);
        assertEquals("test", parsed.name);
        assertEquals("2024-01-01", parsed.registered);
    }

    // Short uniform names: all 1-char, min==max==1
    public static class ShortNames {
        public int a;
        public int b;
        public int c;
        public int d;
        public int e;
        public int f;
        public int g;
        public int h;
    }

    @Test
    public void testCharLengthMinEqMax() {
        // All field names length 1: min==max==1
        String json = "{\"a\":1,\"b\":2,\"c\":3,\"d\":4,\"e\":5,\"f\":6,\"g\":7,\"h\":8}";
        ShortNames bean = JSON.parseObject(json, ShortNames.class);
        assertEquals(1, bean.a);
        assertEquals(8, bean.h);
    }

    // ============================================================
    // 6. Enriched exception diagnostics
    //    Verify error messages contain field count, super, external info.
    //    (Indirect: hard to trigger jitObjectReader failure in test.
    //     Instead, verify that normal creation succeeds for boundary cases.)
    // ============================================================

    // 0 fields: fieldCount=0 → objectReaderSuper=TYPE_OBJECT_READER_ADAPTER
    public static class EmptyBean {
    }

    @Test
    public void testZeroFieldsAdapter() {
        ObjectReader<?> reader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(EmptyBean.class);
        assertNotNull(reader);
        EmptyBean bean = JSON.parseObject("{}", EmptyBean.class);
        assertNotNull(bean);
    }

    // ============================================================
    // Comprehensive: benchmark-like Client model
    //    Exercises all refactored paths together: 20 fields (>12 → Adapter),
    //    genRead243 fast path, getFieldReader lookupswitch,
    //    fieldNameCharLengthMin/Max, mixed field types.
    // ============================================================

    public enum EyeColor {
        BROWN, BLUE, GREEN
    }

    public static class Partner {
        public long id;
        public String name;
        public OffsetDateTime since;
    }

    public static class Client {
        public long id;
        public int index;
        public UUID guid;
        public boolean isActive;
        public BigDecimal balance;
        public String picture;
        public int age;
        public EyeColor eyeColor;
        public String name;
        public String gender;
        public String company;
        public String[] emails;
        public long[] phones;
        public String address;
        public String about;
        public LocalDate registered;
        public double latitude;
        public double longitude;
        public List<String> tags;
        public List<Partner> partners;
    }

    @Test
    public void testClientSuperClass() {
        ObjectReader<?> reader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Client.class);
        // 20 fields -> ObjectReaderAdapter
        assertEquals("ObjectReaderAdapter", reader.getClass().getSuperclass().getSimpleName());
    }

    @Test
    public void testClientGetFieldReader() {
        ObjectReader<?> reader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Client.class);

        // Spot-check getFieldReader (case-sensitive)
        assertNotNull(reader.getFieldReader(Fnv.hashCode64("id")));
        assertNotNull(reader.getFieldReader(Fnv.hashCode64("guid")));
        assertNotNull(reader.getFieldReader(Fnv.hashCode64("eyeColor")));
        assertNotNull(reader.getFieldReader(Fnv.hashCode64("registered")));
        assertNotNull(reader.getFieldReader(Fnv.hashCode64("partners")));
        assertNull(reader.getFieldReader(Fnv.hashCode64("nonexistent")));

        // Spot-check getFieldReaderLCase (case-insensitive)
        assertNotNull(reader.getFieldReaderLCase(Fnv.hashCode64LCase("id")));
        assertNotNull(reader.getFieldReaderLCase(Fnv.hashCode64LCase("eyeColor")));
        assertNotNull(reader.getFieldReaderLCase(Fnv.hashCode64LCase("registered")));
        assertNull(reader.getFieldReaderLCase(Fnv.hashCode64LCase("nonexistent")));
    }

    @Test
    public void testClientReadObject() {
        String json = "{"
                + "\"id\":100,\"index\":1,\"guid\":\"550e8400-e29b-41d4-a716-446655440000\","
                + "\"isActive\":true,\"balance\":99.99,\"picture\":\"pic.jpg\","
                + "\"age\":30,\"eyeColor\":\"BLUE\",\"name\":\"Alice\","
                + "\"gender\":\"F\",\"company\":\"ACME\","
                + "\"emails\":[\"a@b.c\"],\"phones\":[1234567890],"
                + "\"address\":\"123 Main St\",\"about\":\"test\","
                + "\"registered\":\"2024-01-15\","
                + "\"latitude\":40.7128,\"longitude\":-74.0060,"
                + "\"tags\":[\"dev\",\"test\"],"
                + "\"partners\":[{\"id\":1,\"name\":\"Bob\",\"since\":\"2020-01-01T00:00:00Z\"}]"
                + "}";

        Client client = JSON.parseObject(json, Client.class);
        assertEquals(100L, client.id);
        assertEquals(1, client.index);
        assertEquals(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), client.guid);
        assertTrue(client.isActive);
        assertEquals(new BigDecimal("99.99"), client.balance);
        assertEquals("pic.jpg", client.picture);
        assertEquals(30, client.age);
        assertEquals(EyeColor.BLUE, client.eyeColor);
        assertEquals("Alice", client.name);
        assertEquals("F", client.gender);
        assertEquals("ACME", client.company);
        assertArrayEquals(new String[]{"a@b.c"}, client.emails);
        assertArrayEquals(new long[]{1234567890L}, client.phones);
        assertEquals("123 Main St", client.address);
        assertEquals("test", client.about);
        assertEquals(LocalDate.of(2024, 1, 15), client.registered);
        assertEquals(40.7128, client.latitude, 0.0001);
        assertEquals(-74.0060, client.longitude, 0.0001);
        assertEquals(2, client.tags.size());
        assertEquals("dev", client.tags.get(0));
        assertEquals(1, client.partners.size());
        assertEquals("Bob", client.partners.get(0).name);
    }

    @Test
    public void testClientReadObjectUTF8() {
        String json = "{"
                + "\"id\":200,\"index\":2,\"age\":40,\"name\":\"Charlie\","
                + "\"isActive\":false,\"balance\":0,"
                + "\"eyeColor\":\"GREEN\","
                + "\"latitude\":0,\"longitude\":0,"
                + "\"tags\":[],\"partners\":[]"
                + "}";
        Client client = JSON.parseObject(json.getBytes(), Client.class);
        assertEquals(200L, client.id);
        assertEquals(2, client.index);
        assertEquals(40, client.age);
        assertEquals("Charlie", client.name);
        assertFalse(client.isActive);
        assertEquals(EyeColor.GREEN, client.eyeColor);
    }

    @Test
    public void testClientJSONB() {
        Client client = new Client();
        client.id = 300;
        client.index = 3;
        client.age = 50;
        client.name = "Dave";
        client.isActive = true;
        client.eyeColor = EyeColor.BROWN;
        client.latitude = 51.5074;
        client.longitude = -0.1278;
        client.balance = new BigDecimal("123.45");

        byte[] jsonb = JSONB.toBytes(client);
        Client parsed = JSONB.parseObject(jsonb, Client.class);
        assertEquals(300L, parsed.id);
        assertEquals(3, parsed.index);
        assertEquals(50, parsed.age);
        assertEquals("Dave", parsed.name);
        assertTrue(parsed.isActive);
        assertEquals(EyeColor.BROWN, parsed.eyeColor);
        assertEquals(51.5074, parsed.latitude, 0.0001);
        assertEquals(new BigDecimal("123.45"), parsed.balance);
    }

    @Test
    public void testClientSmartMatch() {
        // Upper case field names → exercises getFieldReaderLCase path
        String json = "{\"ID\":1,\"INDEX\":2,\"AGE\":30,\"NAME\":\"Eve\","
                + "\"ISACTIVE\":true,\"EYECOLOR\":\"BLUE\","
                + "\"LATITUDE\":0,\"LONGITUDE\":0,"
                + "\"TAGS\":[],\"PARTNERS\":[]}";
        Client client = JSON.parseObject(json, Client.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(1L, client.id);
        assertEquals(2, client.index);
        assertEquals(30, client.age);
        assertEquals("Eve", client.name);
        assertTrue(client.isActive);
        assertEquals(EyeColor.BLUE, client.eyeColor);
    }

    // ============================================================
    // Partner: 3 fields (≤6), exercises the merged
    // genMethodGetFieldReaderImpl in ≤6 linear-scan branch
    // ============================================================

    @Test
    public void testPartnerLe6() {
        ObjectReader<?> reader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Partner.class);
        assertEquals("ObjectReader3", reader.getClass().getSuperclass().getSimpleName());

        assertNotNull(reader.getFieldReader(Fnv.hashCode64("id")));
        assertNotNull(reader.getFieldReader(Fnv.hashCode64("name")));
        assertNotNull(reader.getFieldReader(Fnv.hashCode64("since")));
        assertNull(reader.getFieldReader(Fnv.hashCode64("xyz")));

        assertNotNull(reader.getFieldReaderLCase(Fnv.hashCode64LCase("id")));
        assertNotNull(reader.getFieldReaderLCase(Fnv.hashCode64LCase("name")));
        assertNotNull(reader.getFieldReaderLCase(Fnv.hashCode64LCase("since")));
        assertNull(reader.getFieldReaderLCase(Fnv.hashCode64LCase("xyz")));
    }

    @Test
    public void testPartnerReadObject() {
        String json = "{\"id\":42,\"name\":\"Partner1\",\"since\":\"2020-06-15T10:30:00+08:00\"}";
        Partner p = JSON.parseObject(json, Partner.class);
        assertEquals(42L, p.id);
        assertEquals("Partner1", p.name);
        assertNotNull(p.since);
        assertEquals(2020, p.since.getYear());
    }

    @Test
    public void testPartnerJSONB() {
        Partner p = new Partner();
        p.id = 99;
        p.name = "PartnerJSONB";
        p.since = OffsetDateTime.parse("2023-03-01T12:00:00Z");
        byte[] jsonb = JSONB.toBytes(p);
        Partner parsed = JSONB.parseObject(jsonb, Partner.class);
        assertEquals(99L, parsed.id);
        assertEquals("PartnerJSONB", parsed.name);
        assertEquals(2023, parsed.since.getYear());
    }

    // ============================================================
    // Exactly 7 fields: the <=6 / >6 boundary
    // getFieldReader uses lookupswitch (>6), but readObject may
    // still use genRead243 fast path.
    // ============================================================

    public static class SevenFields {
        public String alpha;
        public String bravo;
        public String charlie;
        public String delta;
        public String echo;
        public String foxtrot;
        public String golf;
    }

    @Test
    public void testSevenFieldsBoundary() {
        ObjectReader<?> reader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(SevenFields.class);

        // getFieldReader uses lookupswitch path (>6)
        for (String name : new String[]{"alpha", "bravo", "charlie", "delta", "echo", "foxtrot", "golf"}) {
            assertNotNull(reader.getFieldReader(Fnv.hashCode64(name)), name);
            assertNotNull(reader.getFieldReaderLCase(Fnv.hashCode64LCase(name)), name + " LCase");
        }
        assertNull(reader.getFieldReader(0L));
        assertNull(reader.getFieldReaderLCase(0L));
    }

    @Test
    public void testSevenFieldsReadObject() {
        String json = "{\"alpha\":\"1\",\"bravo\":\"2\",\"charlie\":\"3\",\"delta\":\"4\","
                + "\"echo\":\"5\",\"foxtrot\":\"6\",\"golf\":\"7\"}";
        SevenFields bean = JSON.parseObject(json, SevenFields.class);
        assertEquals("1", bean.alpha);
        assertEquals("7", bean.golf);
    }

    @Test
    public void testSevenFieldsSmartMatch() {
        String json = "{\"ALPHA\":\"1\",\"BRAVO\":\"2\",\"CHARLIE\":\"3\",\"DELTA\":\"4\","
                + "\"ECHO\":\"5\",\"FOXTROT\":\"6\",\"GOLF\":\"7\"}";
        SevenFields bean = JSON.parseObject(json, SevenFields.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals("1", bean.alpha);
        assertEquals("7", bean.golf);
    }

    // ============================================================
    // Extra fields in JSON (unknown fields): exercises processExtra
    // path after lookupswitch miss in readObject/readJSONBObject
    // ============================================================

    @Test
    public void testExtraFieldsGt6() {
        String json = "{\"alpha\":\"1\",\"bravo\":\"2\",\"charlie\":\"3\",\"delta\":\"4\","
                + "\"echo\":\"5\",\"foxtrot\":\"6\",\"golf\":\"7\",\"hotel\":\"8\","
                + "\"unknownField\":\"ignored\"}";
        EightFields bean = JSON.parseObject(json, EightFields.class);
        assertEquals("1", bean.alpha);
        assertEquals("8", bean.hotel);
    }

    @Test
    public void testExtraFieldsLe6() {
        String json = "{\"x\":10,\"y\":\"hello\",\"z\":3.14,\"extra\":\"ignored\"}";
        ThreeFields bean = JSON.parseObject(json, ThreeFields.class);
        assertEquals(10, bean.x);
        assertEquals("hello", bean.y);
        assertEquals(3.14, bean.z, 0.001);
    }

    // ============================================================
    // Exactly 6 fields: the upper boundary of ≤6 path
    // getFieldReader uses linear scan (≤6)
    // ============================================================

    @Test
    public void testSixFieldsBoundary() {
        ObjectReader<?> reader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Fields6.class);

        for (int i = 1; i <= 6; i++) {
            String name = "f" + i;
            assertNotNull(reader.getFieldReader(Fnv.hashCode64(name)), name);
            assertNotNull(reader.getFieldReaderLCase(Fnv.hashCode64LCase(name)), name + " LCase");
        }
        assertNull(reader.getFieldReader(Fnv.hashCode64("f7")));
        assertNull(reader.getFieldReaderLCase(Fnv.hashCode64LCase("f7")));
    }

    @Test
    public void testSixFieldsRoundTrip() {
        Fields6 bean = new Fields6();
        bean.f1 = 1;
        bean.f2 = 2;
        bean.f3 = 3;
        bean.f4 = 4;
        bean.f5 = 5;
        bean.f6 = 6;
        String json = JSON.toJSONString(bean);
        Fields6 parsed = JSON.parseObject(json, Fields6.class);
        assertEquals(1, parsed.f1);
        assertEquals(6, parsed.f6);

        // JSONB round-trip
        byte[] jsonb = JSONB.toBytes(bean);
        Fields6 parsedJSONB = JSONB.parseObject(jsonb, Fields6.class);
        assertEquals(1, parsedJSONB.f1);
        assertEquals(6, parsedJSONB.f6);
    }

    // ============================================================
    // Array-to-bean mapping: exercises readArrayMappingJSONBObject
    // for different field counts
    // ============================================================

    @Test
    public void testArrayMappingGt6() {
        // 8 fields via array mapping
        com.alibaba.fastjson2.JSONArray arr = new com.alibaba.fastjson2.JSONArray();
        arr.add("A");
        arr.add("B");
        arr.add("C");
        arr.add("D");
        arr.add("E");
        arr.add("F");
        arr.add("G");
        arr.add("H");
        byte[] jsonb = arr.toJSONBBytes();
        EightFields bean = JSONB.parseObject(jsonb, EightFields.class, JSONReader.Feature.SupportArrayToBean);
        assertEquals("A", bean.alpha);
        assertEquals("H", bean.hotel);
    }

    @Test
    public void testArrayMappingLe6() {
        com.alibaba.fastjson2.JSONArray arr = new com.alibaba.fastjson2.JSONArray();
        arr.add(42);
        arr.add("hello");
        arr.add(3.14);
        byte[] jsonb = arr.toJSONBBytes();
        ThreeFields bean = JSONB.parseObject(jsonb, ThreeFields.class, JSONReader.Feature.SupportArrayToBean);
        assertEquals(42, bean.x);
        assertEquals("hello", bean.y);
        assertEquals(3.14, bean.z, 0.001);
    }
}
