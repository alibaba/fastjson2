package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2_vo.*;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JSONReaderTest1 {
    @Test
    public void test() {
        String str = "{\"v0000\":+000.00}";
        {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertEquals(0, jsonReader.read(LongValue1.class).getV0000());
            }
        }
        {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertEquals(0, jsonReader.read(Long1.class).getV0000());
            }
        }

        {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertEquals(0, jsonReader.read(Int1.class).getV0000());
            }
        }
        {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertEquals(0, jsonReader.read(Integer1.class).getV0000());
            }
        }
        {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertEquals(0, jsonReader.read(FloatValue1.class).getV0000());
            }
        }
        {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertEquals(0, jsonReader.read(Float1.class).getV0000());
            }
        }
        {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertEquals(0, jsonReader.read(DoubleValue1.class).getV0000());
            }
        }
        {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertEquals(0, jsonReader.read(Double1.class).getV0000());
            }
        }
    }

    @Test
    public void testDecimal() {
        String str = "{\"id\":+000.00}";
        JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
        for (JSONReader jsonReader : jsonReaders) {
            assertEquals(new BigDecimal("0.00"), jsonReader.read(BigDecimal1.class).getId());
        }
    }

    @Test
    public void testInteger() {
        String str = "{\"id\":+000.00}";
        JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
        for (JSONReader jsonReader : jsonReaders) {
            assertEquals(BigInteger.ZERO, jsonReader.read(BigInteger1.class).getId());
        }
    }

    @Test
    public void testReadObject() {
        String str = "{\"v0000\":101}";
        Int1 vo = new Int1();
        JSONReader jsonReader = JSONReader.of(str);
        jsonReader.readObject(vo);
        assertEquals(101, vo.getV0000());
    }

    @Test
    public void testReadObject1() {
        String str = "{\"v0000\":101}";

        Map map = new TreeMap();
        JSONReader jsonReader = JSONReader.of(str);
        jsonReader.readObject(map);
        assertEquals(101, map.get("v0000"));
    }

    @Test
    public void testNextIfMatch3() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders("lax $.id")) {
            assertTrue(jsonReader.nextIfMatchIdent('l', 'a', 'x'));
            assertEquals('$', jsonReader.current());
            jsonReader.next();
            assertEquals('.', jsonReader.current());
            jsonReader.next();
            assertEquals("id", jsonReader.readFieldNameUnquote());
        }
    }

    @Test
    public void testNextIfMatch3_1() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("lax")) {
            assertTrue(jsonReader.nextIfMatchIdent('l', 'a', 'x'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("lax ")) {
            assertTrue(jsonReader.nextIfMatchIdent('l', 'a', 'x'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("lax (")) {
            assertTrue(jsonReader.nextIfMatchIdent('l', 'a', 'x'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("laxy")) {
            assertFalse(jsonReader.nextIfMatchIdent('l', 'a', 'x'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("la")) {
            assertFalse(jsonReader.nextIfMatchIdent('l', 'a', 'x'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("abc")) {
            assertFalse(jsonReader.nextIfMatchIdent('l', 'a', 'x'));
        }
    }

    @Test
    public void testNextIfMatch4() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("last")) {
            assertTrue(jsonReader.nextIfMatchIdent('l', 'a', 's', 't'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("last ")) {
            assertTrue(jsonReader.nextIfMatchIdent('l', 'a', 's', 't'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("last,")) {
            assertTrue(jsonReader.nextIfMatchIdent('l', 'a', 's', 't'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("last]")) {
            assertTrue(jsonReader.nextIfMatchIdent('l', 'a', 's', 't'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("lastest")) {
            assertFalse(jsonReader.nextIfMatchIdent('l', 'a', 's', 't'));
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("l")) {
            assertFalse(jsonReader.nextIfMatchIdent('l', 'a', 's', 't'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("la")) {
            assertFalse(jsonReader.nextIfMatchIdent('l', 'a', 's', 't'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("las")) {
            assertFalse(jsonReader.nextIfMatchIdent('l', 'a', 's', 't'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("abcd")) {
            assertFalse(jsonReader.nextIfMatchIdent('l', 'a', 's', 't'));
        }
    }

    @Test
    public void testNextIfMatch6() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders("strict $.id")) {
            assertTrue(jsonReader.nextIfMatchIdent('s', 't', 'r', 'i', 'c', 't'));
            assertEquals('$', jsonReader.current());
            jsonReader.next();
            assertEquals('.', jsonReader.current());
            jsonReader.next();
            assertEquals("id", jsonReader.readFieldNameUnquote());
        }
    }

    @Test
    public void testNextIfMatch6_1() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("strict")) {
            assertTrue(jsonReader.nextIfMatchIdent('s', 't', 'r', 'i', 'c', 't'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("strict ")) {
            assertTrue(jsonReader.nextIfMatchIdent('s', 't', 'r', 'i', 'c', 't'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("abc")) {
            assertFalse(jsonReader.nextIfMatchIdent('s', 't', 'r', 'i', 'c', 't'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("stricA")) {
            assertFalse(jsonReader.nextIfMatchIdent('s', 't', 'r', 'i', 'c', 't'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("striAt")) {
            assertFalse(jsonReader.nextIfMatchIdent('s', 't', 'r', 'i', 'c', 't'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("strAct")) {
            assertFalse(jsonReader.nextIfMatchIdent('s', 't', 'r', 'i', 'c', 't'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("stRict")) {
            assertFalse(jsonReader.nextIfMatchIdent('s', 't', 'r', 'i', 'c', 't'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("sTrict")) {
            assertFalse(jsonReader.nextIfMatchIdent('s', 't', 'r', 'i', 'c', 't'));
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("Strict")) {
            assertFalse(jsonReader.nextIfMatchIdent('s', 't', 'r', 'i', 'c', 't'));
        }
    }

    @Test
    public void testReadFieldNameHashCode() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"\\u0000\\u0001\\u0002\":")) {
            assertEquals(Fnv.hashCode64("\0\1\2"), jsonReader.readFieldNameHashCode());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders("'abc':123")) {
            assertEquals(Fnv.hashCode64("abc"), jsonReader.readFieldNameHashCode());
            assertEquals("abc", jsonReader.getFieldName());
            assertEquals("abc", jsonReader.getString());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"\\x00\\x01\\x02\":")) {
            assertEquals(Fnv.hashCode64("\0\1\2"), jsonReader.readFieldNameHashCode());
            assertEquals("\0\1\2", jsonReader.getFieldName());
            assertEquals("\0\1\2", jsonReader.getString());
        }
    }

    @Test
    public void testReadFieldNameHashCodeUnquote() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders("a\\u0000\\u0001\\u0002b:")) {
            assertEquals(Fnv.hashCode64("a\0\1\2b"), jsonReader.readFieldNameHashCodeUnquote());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders("a\\u0000\\u0001\\u0002b : ")) {
            assertEquals(Fnv.hashCode64("a\0\1\2b"), jsonReader.readFieldNameHashCodeUnquote());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders("a\\x00\\x01\\x02b : ")) {
            assertEquals(Fnv.hashCode64("a\0\1\2b"), jsonReader.readFieldNameHashCodeUnquote());
        }
    }

    @Test
    public void testNextIfMatch() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders(",中")) {
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals('中', jsonReader.ch);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders(", 中")) {
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals('中', jsonReader.ch);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders(", ®")) {
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals('®', jsonReader.ch);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(",A")) {
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals('A', jsonReader.ch);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(", A")) {
            assertTrue(jsonReader.nextIfMatch(','));
            assertEquals('A', jsonReader.ch);
        }
    }

    @Test
    public void testNextIfSet() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("Set[")) {
            assertTrue(jsonReader.nextIfSet());
            assertEquals('[', jsonReader.ch);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("Set [")) {
            assertTrue(jsonReader.nextIfSet());
            assertEquals('[', jsonReader.ch);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("Set  [")) {
            assertTrue(jsonReader.nextIfSet());
            assertEquals('[', jsonReader.ch);
        }
    }

    @Test
    public void testNextIfEmptyString() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\'\',1")) {
            assertTrue(jsonReader.nextIfNullOrEmptyString());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\'\' , 1")) {
            assertTrue(jsonReader.nextIfNullOrEmptyString());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("\'\' , 中")) {
            assertTrue(jsonReader.nextIfNullOrEmptyString());
            assertEquals('中', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders("\'\' , ®")) {
            assertTrue(jsonReader.nextIfNullOrEmptyString());
            assertEquals('®', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"\"")) {
            assertTrue(jsonReader.nextIfNullOrEmptyString());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"\",")) {
            assertTrue(jsonReader.nextIfNullOrEmptyString());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"\" ,")) {
            assertTrue(jsonReader.nextIfNullOrEmptyString());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"\" , ")) {
            assertTrue(jsonReader.nextIfNullOrEmptyString());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"\"  ")) {
            assertTrue(jsonReader.nextIfNullOrEmptyString());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"")) {
            assertFalse(jsonReader.nextIfNullOrEmptyString());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"a\"")) {
            assertFalse(jsonReader.nextIfNullOrEmptyString());
        }
    }

    @Test
    public void testNextIfNull() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("null")) {
            assertTrue(jsonReader.nextIfNull());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertFalse(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("null,  ")) {
            assertTrue(jsonReader.nextIfNull());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("null,  1")) {
            assertTrue(jsonReader.nextIfNull());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
//        for (JSONReader jsonReader : TestUtils.createJSONReaders("null,  中")) {
//            assertTrue(jsonReader.nextIfNull());
//            assertEquals('中', jsonReader.ch);
//            assertTrue(jsonReader.comma);
//        }
//        for (JSONReader jsonReader : TestUtils.createJSONReaders("null  ,  ®")) {
//            assertTrue(jsonReader.nextIfNull());
//            assertEquals('®', jsonReader.ch);
//            assertTrue(jsonReader.comma);
//        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("n")) {
            assertFalse(jsonReader.nextIfNull());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("nu")) {
            assertFalse(jsonReader.nextIfNull());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("nul")) {
            assertFalse(jsonReader.nextIfNull());
        }
    }

    @Test
    public void testReadUUID() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("null")) {
            assertNull(jsonReader.readUUID());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertFalse(jsonReader.comma);
        }

        UUID uuid = UUID.fromString("8c2fef2f-1c68-4075-8bf2-3fe66fd02297");
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"8c2fef2f-1c68-4075-8bf2-3fe66fd02297\"")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertFalse(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"8c2fef2f-1c68-4075-8bf2-3fe66fd02297\"   ")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertFalse(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"8c2fef2f-1c68-4075-8bf2-3fe66fd02297\" ,")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"8c2fef2f-1c68-4075-8bf2-3fe66fd02297\" , 1")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders("\"8c2fef2f-1c68-4075-8bf2-3fe66fd02297\" , ®")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals('®', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders("\"8c2fef2f-1c68-4075-8bf2-3fe66fd02297\" , 中")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals('中', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void testReadUUID1() {
        UUID uuid = UUID.fromString("8c2fef2f-1c68-4075-8bf2-3fe66fd02297");
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'8c2fef2f-1c68-4075-8bf2-3fe66fd02297'")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertFalse(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'8c2fef2f-1c68-4075-8bf2-3fe66fd02297'   ")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertFalse(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'8c2fef2f-1c68-4075-8bf2-3fe66fd02297' ,")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'8c2fef2f-1c68-4075-8bf2-3fe66fd02297' , 1")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders("'8c2fef2f-1c68-4075-8bf2-3fe66fd02297' , ®")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals('®', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders("'8c2fef2f-1c68-4075-8bf2-3fe66fd02297' , 中")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals('中', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void testReadUUID2() {
        UUID uuid = UUID.fromString("8c2fef2f-1c68-4075-8bf2-3fe66fd02297");
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'8c2fef2f1c6840758bf23fe66fd02297'")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertFalse(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'8c2fef2f1c6840758bf23fe66fd02297'   ")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertFalse(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'8c2fef2f1c6840758bf23fe66fd02297' ,")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'8c2fef2f1c6840758bf23fe66fd02297' , 1")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders("'8c2fef2f1c6840758bf23fe66fd02297' , ®")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals('®', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders("'8c2fef2f1c6840758bf23fe66fd02297' , 中")) {
            assertEquals(uuid, jsonReader.readUUID());
            assertEquals('中', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalDateTime16() {
        LocalDateTime ldt = LocalDateTime.of(2018, 7, 14, 12, 13, 0);
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018-07-14 12:13\"")) {
            assertEquals(ldt, jsonReader.readLocalDateTime16());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'2018-07-14 12:13',")) {
            assertEquals(ldt, jsonReader.readLocalDateTime16());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalDateTime17() {
        LocalDateTime ldt = LocalDateTime.of(2018, 7, 14, 12, 13, 0);
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018-07-14T12:13Z\" ")) {
            assertEquals(ldt, jsonReader.readLocalDateTime17());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'2018-07-14 12:13Z' , ")) {
            assertEquals(ldt, jsonReader.readLocalDateTime17());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalDateTime17_1() {
        LocalDateTime ldt = LocalDateTime.of(2018, 7, 3, 12, 13, 14);
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018-7-3 12:13:14\" ")) {
            assertEquals(ldt, jsonReader.readLocalDateTime17());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'2018-7-3T12:13:14' , ")) {
            assertEquals(ldt, jsonReader.readLocalDateTime17());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalDateTime17_2() {
        LocalDateTime ldt = LocalDateTime.of(2018, 7, 14, 0, 0, 0);
        String str = "\"2018年07月14日\" ";
        {
            JSONReader jsonReader = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
            assertEquals(ldt, jsonReader.readLocalDateTime17());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertFalse(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalDateTime18() {
        LocalDateTime ldt = LocalDateTime.of(2018, 7, 14, 12, 13, 14);
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018-7-14 12:13:14\" , ")) {
            assertEquals(ldt, jsonReader.readLocalDateTime18());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'2018-7-14T12:13:14' , ")) {
            assertEquals(ldt, jsonReader.readLocalDateTime18());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018-7-14T12:13:14\" , 1")) {
            assertEquals(ldt, jsonReader.readLocalDateTime18());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalDateTime18_1() {
        LocalDateTime ldt = LocalDateTime.of(2018, 10, 4, 12, 13, 14);
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018-10-4 12:13:14\" , ")) {
            assertEquals(ldt, jsonReader.readLocalDateTime18());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'2018-10-4T12:13:14' , ")) {
            assertEquals(ldt, jsonReader.readLocalDateTime18());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018-10-4T12:13:14\" , 1")) {
            assertEquals(ldt, jsonReader.readLocalDateTime18());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalDateTime18_2() {
        LocalDateTime ldt = LocalDateTime.of(2018, 10, 14, 2, 13, 14);
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018-10-14 2:13:14\" , ")) {
            assertEquals(ldt, jsonReader.readLocalDateTime18());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'2018-10-14T2:13:14' , ")) {
            assertEquals(ldt, jsonReader.readLocalDateTime18());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018-10-14T2:13:14\" , 1")) {
            assertEquals(ldt, jsonReader.readLocalDateTime18());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalDateTime18_3() {
        LocalDateTime ldt = LocalDateTime.of(2018, 10, 14, 12, 3, 14);
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018-10-14 12:3:14\" , ")) {
            assertEquals(ldt, jsonReader.readLocalDateTime18());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'2018-10-14T12:3:14' , ")) {
            assertEquals(ldt, jsonReader.readLocalDateTime18());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018-10-14T12:3:14\" , 1")) {
            assertEquals(ldt, jsonReader.readLocalDateTime18());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalDateTime18_4() {
        LocalDateTime ldt = LocalDateTime.of(2018, 10, 14, 12, 13, 4);
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018-10-14 12:13:4\" , ")) {
            assertEquals(ldt, jsonReader.readLocalDateTime18());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'2018-10-14T12:13:4' , ")) {
            assertEquals(ldt, jsonReader.readLocalDateTime18());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018-10-14T12:13:4\" , 1")) {
            assertEquals(ldt, jsonReader.readLocalDateTime18());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalTime8() {
        LocalTime localTime = LocalTime.of(12, 13, 14);
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"12:13:14\" , ")) {
            assertEquals(localTime, jsonReader.readLocalTime8());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'12:13:14' , 1")) {
            assertEquals(localTime, jsonReader.readLocalTime8());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalTime10() {
        LocalTime localTime = LocalTime.of(12, 13, 14, 100_000_000);
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"12:13:14.1\" , ")) {
            assertEquals(localTime, jsonReader.readLocalTime10());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'12:13:14.1' , 1")) {
            assertEquals(localTime, jsonReader.readLocalTime10());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalTime11() {
        LocalTime localTime = LocalTime.of(12, 13, 14, 10_000_000);
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"12:13:14.01\" , ")) {
            assertEquals(localTime, jsonReader.readLocalTime11());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'12:13:14.01' , 1")) {
            assertEquals(localTime, jsonReader.readLocalTime11());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalTime12() {
        LocalTime localTime = LocalTime.of(12, 13, 14, 1_000_000);
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"12:13:14.001\" , ")) {
            assertEquals(localTime, jsonReader.readLocalTime12());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'12:13:14.001' , 1")) {
            assertEquals(localTime, jsonReader.readLocalTime12());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalTime18() {
        LocalTime localTime = LocalTime.of(12, 13, 14, 123456789);
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"12:13:14.123456789\" , ")) {
            assertEquals(localTime, jsonReader.readLocalTime18());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'12:13:14.123456789' , 1")) {
            assertEquals(localTime, jsonReader.readLocalTime18());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalDate8() {
        LocalDate localTime = LocalDate.of(2018, 4, 1);
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018-4-1\" , ")) {
            assertEquals(localTime, jsonReader.readLocalDate8());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'2018-4-1' , 1")) {
            assertEquals(localTime, jsonReader.readLocalDate8());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'20180401'")) {
            assertEquals(localTime,
                    jsonReader
                            .readLocalDate8()
            );
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'20183301'")) {
            assertNull(jsonReader.readLocalDate8());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'20180241'")) {
            assertNull(jsonReader.readLocalDate8());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'20180231'")) {
            assertThrows(JSONException.class, () -> jsonReader.readLocalDate8());
        }
    }

    @Test
    public void test_readLocalDate9() {
        LocalDate localTime = LocalDate.of(2018, 4, 11);
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018-4-11\" , ")) {
            assertEquals(localTime, jsonReader.readLocalDate9());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'2018-4-11' , 1")) {
            assertEquals(localTime, jsonReader.readLocalDate9());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'2018-2-31'")) {
            assertThrows(JSONException.class, () -> jsonReader.readLocalDate9());
        }
    }

    @Test
    public void test_readLocalDate9_1() {
        LocalDate localTime = LocalDate.of(2018, 4, 1);
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("\"2018年4月1日\" , ")) {
            assertEquals(localTime, jsonReader.readLocalDate9());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("'2018年4月1日' , 1")) {
            assertEquals(localTime, jsonReader.readLocalDate9());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders2("\"2018년4월1일\" , ")) {
            assertEquals(localTime, jsonReader.readLocalDate9());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalDate10() {
        LocalDate localTime = LocalDate.of(2018, 11, 12);
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018-11-12\" , ")) {
            assertEquals(localTime, jsonReader.readLocalDate10());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'2018-11-12' , 1")) {
            assertEquals(localTime, jsonReader.readLocalDate10());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"2018/11/12\" , ")) {
            assertEquals(localTime, jsonReader.readLocalDate10());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"12.11.2018\" , ")) {
            assertEquals(localTime, jsonReader.readLocalDate10());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"12-11-2018\" , ")) {
            assertEquals(localTime, jsonReader.readLocalDate10());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'2018-02-31'")) {
            assertThrows(JSONException.class, () -> jsonReader.readLocalDate10());
        }
    }

    @Test
    public void test_readLocalDate10_1() {
        LocalDate localTime = LocalDate.of(2018, 1, 12);
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("\"2018年1月12日\" , ")) {
            assertEquals(localTime, jsonReader.readLocalDate10());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("'2018년1월12일' , 1")) {
            assertEquals(localTime, jsonReader.readLocalDate10());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalDate10_2() {
        LocalDate localTime = LocalDate.of(2018, 12, 1);
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("\"2018年12月1日\" , ")) {
            assertEquals(localTime, jsonReader.readLocalDate10());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("'2018년12월1일' , 1")) {
            assertEquals(localTime, jsonReader.readLocalDate10());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalDate11() {
        LocalDate localTime = LocalDate.of(2018, 12, 13);
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("\"2018年12月13日\" , ")) {
            assertEquals(localTime, jsonReader.readLocalDate11());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("'2018년12월13일' , 1")) {
            assertEquals(localTime, jsonReader.readLocalDate11());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalDateTime19() {
        String str = "2018-12-13 12:13:14";
        LocalDateTime localTime = LocalDateTime.of(2018, 12, 13, 12, 13, 14);
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("\"" + str + "\" , ")) {
            assertEquals(localTime, jsonReader.readLocalDateTime19());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("'" + str + "' , 1")) {
            assertEquals(localTime, jsonReader.readLocalDateTime19());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readLocalDateTimeX() {
        String str = "2018-12-13 12:13:14.0";
        LocalDateTime localTime = LocalDateTime.of(2018, 12, 13, 12, 13, 14);
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("\"" + str + "\" , ")) {
            assertEquals(localTime, jsonReader.readLocalDateTimeX(str.length()));
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("'" + str + "' , 1")) {
            assertEquals(localTime, jsonReader.readLocalDateTimeX(str.length()));
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readPattern() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("1 /abc/")) {
            jsonReader.next();
            assertEquals("abc", jsonReader.readPattern());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("1 /abc/ , ")) {
            jsonReader.next();
            assertEquals("abc", jsonReader.readPattern());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("1 /abc/ , 1")) {
            jsonReader.next();
            assertEquals("abc", jsonReader.readPattern());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readReference() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("{\"$ref\":\"$\"}")) {
            assertTrue(jsonReader.isReference());
            assertEquals("$", jsonReader.readReference());
            assertEquals(JSONReader.EOI, jsonReader.ch);
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("{\"$ref1\":\"$\"}")) {
            assertFalse(jsonReader.isReference());
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders2("{\"$ref\":\"$\"} , \"abc\":")) {
            assertTrue(jsonReader.isReference());
            assertEquals("$", jsonReader.readReference());
            assertEquals('"', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders2("{\"$ref\" : \"$\"} , \"abc\":")) {
            assertTrue(jsonReader.isReference());
            assertEquals("$", jsonReader.readReference());
            assertEquals('"', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_isReference() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("{")) {
            assertFalse(jsonReader.isReference());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("{\"")) {
            assertFalse(jsonReader.isReference());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("{\"$")) {
            assertFalse(jsonReader.isReference());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("{\"$r")) {
            assertFalse(jsonReader.isReference());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("{\"$re")) {
            assertFalse(jsonReader.isReference());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("{\"$ref")) {
            assertFalse(jsonReader.isReference());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("{\"$ref\"")) {
            assertFalse(jsonReader.isReference());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("{\"$ref\":")) {
            assertFalse(jsonReader.isReference());
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders2("{\"$ref\":\"")) {
            assertTrue(jsonReader.isReference());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders2("{\"$ref\" : \"")) {
            assertTrue(jsonReader.isReference());
        }
    }

    @Test
    public void test_nextIfMatch() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(",")) {
            assertTrue(jsonReader.nextIfMatch(','));
        }
    }

    @Test
    public void test_nextIfSet() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("Set")) {
            assertTrue(jsonReader.nextIfSet());
            assertTrue(jsonReader.isEnd());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("Set[")) {
            assertTrue(jsonReader.nextIfSet());
            assertEquals('[', jsonReader.ch);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("Set  [")) {
            assertTrue(jsonReader.nextIfSet());
            assertEquals('[', jsonReader.ch);
        }
    }

    @Test
    public void test_isBinary() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("1")) {
            assertFalse(jsonReader.isBinary());
        }
    }

    @Test
    public void test_readFieldNameUnquoted() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("id")) {
            assertEquals(Fnv.hashCode64("id"), jsonReader.readFieldNameHashCodeUnquote());
            assertEquals("id", jsonReader.getFieldName());
        }
    }

    @Test
    public void test_getNameHashCodeLCase() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders("\"A中国\":")) {
            assertEquals(Fnv.hashCode64("A中国"), jsonReader.readFieldNameHashCode());
            assertEquals(
                    Fnv.hashCode64("a中国"),
                    jsonReader.getNameHashCodeLCase()
            );
            assertEquals("A中国", jsonReader.getFieldName());
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("\"\\u0000\\u0001\\u0002\":")) {
            assertEquals(Fnv.hashCode64("\0\1\2"), jsonReader.readFieldNameHashCode());
            assertEquals(Fnv.hashCode64("\0\1\2"), jsonReader.getNameHashCodeLCase());
            assertEquals("\0\1\2", jsonReader.getFieldName());
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("\"\\x00\\x01\\x02\" : 1")) {
            assertEquals(Fnv.hashCode64("\0\1\2"), jsonReader.readFieldNameHashCode());
            assertEquals(Fnv.hashCode64("\0\1\2"), jsonReader.getNameHashCodeLCase());
            assertEquals("\0\1\2", jsonReader.getFieldName());
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("\"\\\\\\\"\":")) {
            assertEquals(Fnv.hashCode64("\\\""), jsonReader.readFieldNameHashCode());
            assertEquals(Fnv.hashCode64("\\\""), jsonReader.getNameHashCodeLCase());
            assertEquals("\\\"", jsonReader.getFieldName());
        }
    }

    @Test
    public void test_readFieldName() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders("\"A中国\":")) {
            assertEquals("A中国", jsonReader.readFieldName());
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("\"\\u0000\\u0001\\u0002\":")) {
            assertEquals("\0\1\2", jsonReader.readFieldName());
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("\"\\x00\\x01\\x02\" : 1")) {
            assertEquals("\0\1\2", jsonReader.readFieldName());
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("\"\\\\\\\"\":")) {
            assertEquals("\\\"", jsonReader.readFieldName());
        }
    }

    @Test
    public void test_readValueHashCode() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders("\"A中国\"")) {
            assertEquals(
                    Fnv.hashCode64("A中国"),
                    jsonReader.readValueHashCode()
            );
            assertEquals("A中国", jsonReader.getString());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"\\u0000\\u0001\\u0002\",")) {
            assertEquals(Fnv.hashCode64("\0\1\2"), jsonReader.readValueHashCode());
            assertEquals("\0\1\2", jsonReader.getString());
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"\\x00\\x01\\x02\" , \"abc\"")) {
            assertEquals(Fnv.hashCode64("\0\1\2"), jsonReader.readValueHashCode());
            assertEquals("\0\1\2", jsonReader.getString());
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"\\\\\\\"\"}")) {
            assertEquals(
                    Fnv.hashCode64("\\\""),
                    jsonReader.readValueHashCode()
            );
            assertEquals("\\\"", jsonReader.getString());
            assertFalse(jsonReader.comma);
        }
    }

    @Test
    public void test_readInt32() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("123")) {
            assertEquals(123, jsonReader.readInt32());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("+123")) {
            assertEquals(123, jsonReader.readInt32());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("-123")) {
            assertEquals(-123, jsonReader.readInt32());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"123\"")) {
            assertEquals(123, jsonReader.readInt32());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'123'")) {
            assertEquals(123, jsonReader.readInt32());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("123, ")) {
            assertEquals(123, jsonReader.readInt32());
            assertTrue(jsonReader.isEnd());
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("123 , 1")) {
            assertEquals(123, jsonReader.readInt32());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"\"")) {
            assertNull(jsonReader.readInt32());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'',")) {
            assertNull(jsonReader.readInt32());
            assertTrue(jsonReader.isEnd());
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readInt32Value() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("123")) {
            assertEquals(123, jsonReader.readInt32Value());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("+123")) {
            assertEquals(123, jsonReader.readInt32Value());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("-123")) {
            assertEquals(-123, jsonReader.readInt32Value());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"123\"")) {
            assertEquals(123, jsonReader.readInt32Value());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.wasNull);
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'123'")) {
            assertEquals(123, jsonReader.readInt32Value());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.wasNull);
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("123, ")) {
            assertEquals(123, jsonReader.readInt32Value());
            assertTrue(jsonReader.isEnd());
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("123 , 1")) {
            assertEquals(123, jsonReader.readInt32Value());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"\"")) {
            assertEquals(0, jsonReader.readInt32Value());
            assertTrue(jsonReader.wasNull);
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'',")) {
            assertEquals(0, jsonReader.readInt32Value());
            assertTrue(jsonReader.wasNull);
            assertTrue(jsonReader.isEnd());
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readInt64Value() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("123")) {
            assertEquals(123, jsonReader.readInt64Value());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("+123")) {
            assertEquals(123, jsonReader.readInt64Value());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("-123")) {
            assertEquals(-123, jsonReader.readInt64Value());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"123\"")) {
            assertEquals(123, jsonReader.readInt64Value());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.wasNull);
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'123'")) {
            assertEquals(123, jsonReader.readInt64Value());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.wasNull);
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("123, ")) {
            assertEquals(123, jsonReader.readInt64Value());
            assertTrue(jsonReader.isEnd());
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("123 , 1")) {
            assertEquals(123, jsonReader.readInt64Value());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"\"")) {
            assertEquals(0, jsonReader.readInt64Value());
            assertTrue(jsonReader.wasNull);
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'',")) {
            assertEquals(0, jsonReader.readInt64Value());
            assertTrue(jsonReader.wasNull);
            assertTrue(jsonReader.isEnd());
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readInt64() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("123")) {
            assertEquals(123L, jsonReader.readInt64());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("+123")) {
            assertEquals(123L, jsonReader.readInt64());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("-123")) {
            assertEquals(-123L, jsonReader.readInt64());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"123\"")) {
            assertEquals(123L, jsonReader.readInt64());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'123'")) {
            assertEquals(123L, jsonReader.readInt64());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("123, ")) {
            assertEquals(123L, jsonReader.readInt64());
            assertTrue(jsonReader.isEnd());
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("123 , 1")) {
            assertEquals(123L, jsonReader.readInt64());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("\"\"")) {
            assertNull(jsonReader.readInt64());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("'',")) {
            assertNull(jsonReader.readInt64());
            assertTrue(jsonReader.isEnd());
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test_readString() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders("\"A中国\"")) {
            assertEquals("A中国", jsonReader.readString());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders("'A中国',")) {
            assertEquals("A中国", jsonReader.readString());
            assertTrue(jsonReader.isEnd());
            assertTrue(jsonReader.comma);
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders("\"\\u0000\\u0001\\u0002\":")) {
            assertEquals("\0\1\2", jsonReader.readString());
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("\"\\x00\\x01\\x02\" , 1")) {
            assertEquals("\0\1\2", jsonReader.readString());
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("\"\\\\\\\"\",")) {
            assertEquals("\\\"", jsonReader.readString());
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("\"\\0\\1\\2\\3\\4\\5\\6\\7\\v\",")) {
            assertEquals("\0\1\2\3\4\5\6\7\u000B", jsonReader.readString());
        }
    }

    @Test
    public void test_readBinary() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders("'',")) {
            assertNull(jsonReader.readBinary());
            assertTrue(jsonReader.isEnd());
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("'abc',")) {
            assertThrows(JSONException.class, () -> jsonReader.readBinary());
        }
    }

    @Test
    public void test_readIfNull() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders("null")) {
            assertTrue(jsonReader.readIfNull());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("null  ")) {
            assertTrue(jsonReader.readIfNull());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("null,")) {
            assertTrue(jsonReader.readIfNull());
            assertTrue(jsonReader.isEnd());
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("null  ,  1")) {
            assertTrue(jsonReader.readIfNull());
            assertFalse(jsonReader.isEnd());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("nul_")) {
            assertFalse(jsonReader.readIfNull());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders("nu_l")) {
            assertFalse(jsonReader.readIfNull());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders("n_ll")) {
            assertFalse(jsonReader.readIfNull());
        }
        for (JSONReader jsonReader : TestUtils.createJSONReaders("_ull")) {
            assertFalse(jsonReader.readIfNull());
        }
    }

    @Test
    public void test_readNullOrNewDate() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders("null")) {
            assertNull(jsonReader.readNullOrNewDate());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("null  ")) {
            assertNull(jsonReader.readNullOrNewDate());
            assertTrue(jsonReader.isEnd());
            assertFalse(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("null,")) {
            assertNull(jsonReader.readNullOrNewDate());
            assertTrue(jsonReader.isEnd());
            assertTrue(jsonReader.comma);
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders("null  ,  1")) {
            assertNull(jsonReader.readNullOrNewDate());
            assertFalse(jsonReader.isEnd());
            assertEquals('1', jsonReader.ch);
            assertTrue(jsonReader.comma);
        }
    }

    @Test
    public void test1() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders("")) {
            assertEquals(-128, jsonReader.getType());
            assertFalse(jsonReader.isEnabled(JSONReader.Feature.IgnoreNoneSerializable));
            assertFalse(jsonReader.isSupportSmartMatch());
            assertNotNull(jsonReader.getZoneId());
            assertNull(jsonReader.checkAutoType(Object.class, 0L, 0));
            assertFalse(jsonReader.isReference());
            assertNull(jsonReader.readReference());
            assertThrows(JSONException.class, () -> jsonReader.nextIfMatch((byte) 0));
        }
    }

    @Test
    public void testReadArray() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders("[101,102,103]")) {
            List list = new ArrayList<>();
            jsonReader.readArray(list, Long.class);
            assertEquals(3, list.size());
            assertEquals(101L, list.get(0));
            assertEquals(102L, list.get(1));
            assertEquals(103L, list.get(2));
        }
    }

    @Test
    public void testOf() {
        assertThrows(JSONException.class, () -> JSONReader.of((InputStream) null, StandardCharsets.ISO_8859_1));
    }

    @Test
    public void testJSONB() {
        byte[] bytes = JSONB.toBytes("");
        {
            JSONReader jsonReader = JSONReader.ofJSONB(bytes, JSONFactory.createReadContext());
            assertThrows(JSONException.class, () -> jsonReader.readLocalDate11());
        }
        {
            JSONReader jsonReader = JSONReader.ofJSONB(JSONFactory.createReadContext(), bytes);
            assertThrows(JSONException.class, () -> jsonReader.readLocalDate11());
        }
    }

    @Test
    public void testResolveTaskToString() {
        JSONReader.ResolveTask resolveTask = new JSONReader.ResolveTask((FieldReader) null, new Object(), "", JSONPath.of("$"));
        assertEquals("$", resolveTask.toString());
    }

    @Test
    public void config() {
        JSONReader jsonReader = JSONReader.of("");
        JSONReader.AutoTypeBeforeHandler filter = JSONReader.autoTypeFilter("com.abc");
        jsonReader.getContext().config(new Filter[]{
                filter
        });
        assertSame(filter, jsonReader.getContext().getContextAutoTypeBeforeHandler());
    }

    @Test
    public void testDates() {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                .withZone(IOUtils.SHANGHAI_ZONE_ID);

        char[] chars = "\"1900-01-01 00:00:00\"".toCharArray();
        for (int year = 1900; year < 2200; year++) {
            IOUtils.getChars(year, 5, chars);

            for (int month = 1; month <= 12; month++) {
                chars[6] = '0';
                IOUtils.getChars(month, 8, chars);

                int dom = 31;
                switch (month) {
                    case 2:
                        dom = (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                        break;
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        dom = 30;
                        break;
                }

                for (int d = 1; d <= dom; d++) {
                    chars[9] = '0';
                    IOUtils.getChars(d, 11, chars);

                    for (int h = 1; h <= 12; h++) {
                        chars[12] = '0';
                        IOUtils.getChars(h, 14, chars);
                        String str = new String(chars, 1, 19);

                        {
                            JSONReader jsonReader = JSONReader.of(chars, 0, chars.length);
                            jsonReader.getContext().setZoneId(IOUtils.SHANGHAI_ZONE_ID);
                            long millis19 = jsonReader.readMillis19();

                            LocalDateTime ldt = LocalDateTime.parse(str, formatter);
                            ZonedDateTime zdt = ZonedDateTime.ofLocal(ldt, IOUtils.SHANGHAI_ZONE_ID, null);
                            assertEquals(zdt.toInstant().toEpochMilli(), millis19);
                        }

                        {
                            JSONReader jsonReader = JSONReader.of(chars, 0, chars.length);
                            jsonReader.getContext().setZoneId(ZoneOffset.UTC);
                            long millis19 = jsonReader.readMillis19();

                            LocalDateTime ldt = LocalDateTime.parse(str, formatter);
                            ZonedDateTime zdt = ZonedDateTime.ofLocal(ldt, ZoneOffset.UTC, null);
                            assertEquals(zdt.toInstant().toEpochMilli(), millis19);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void readNameHashCode() {
        String[] names = new String[]{
                "",
                "0",
                "01",
                "012",
                "0123",
                "01234",
                "012345",
                "0123456",
                "01234567",
                "012345678",
                "0123456789",
                "0123456789A"
        };

        for (String name : names) {
            String str = "{\"" + name + "\":123456789}";
            byte[] bytes = str.getBytes();
            JSONReaderASCII jsonReader = new JSONReaderASCII(JSONFactory.createReadContext(), str, bytes, 0, bytes.length);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(
                    Fnv.hashCode64(name),
                    jsonReader.readFieldNameHashCode()
            );
            assertEquals(name, jsonReader.getFieldName());
        }
    }

    @Test
    public void readNameHashCode_x2() {
        JSONReader.Context context = JSONFactory.createReadContext();

        byte[] bytes = "{\"01\":123456789}".getBytes();
        for (int i0 = 'a'; i0 <= 'z'; i0++) {
            for (int i1 = 'a'; i1 <= 'z'; i1++) {
                bytes[2] = (byte) i0;
                bytes[3] = (byte) i1;
                String name = new String(new char[]{(char) i0, (char) i1});
                JSONReaderASCII jsonReader = new JSONReaderASCII(context, null, bytes, 0, bytes.length);
                assertTrue(jsonReader.nextIfObjectStart());
                assertEquals(Fnv.hashCode64(name), jsonReader.readFieldNameHashCode());
            }
        }
    }

    @Test
    public void readNameHashCode_x3() {
        JSONReader.Context context = JSONFactory.createReadContext();

        byte[] bytes = "{\"012\":12}".getBytes();
        for (int i0 = 'a'; i0 <= 'z'; i0++) {
            for (int i1 = 'a'; i1 <= 'z'; i1++) {
                for (int i2 = 'a'; i2 <= 'z'; i2++) {
                    bytes[2] = (byte) i0;
                    bytes[3] = (byte) i1;
                    bytes[4] = (byte) i2;
                    String name = new String(new char[]{(char) i0, (char) i1, (char) i2});
                    JSONReaderASCII jsonReader = new JSONReaderASCII(context, null, bytes, 0, bytes.length);
                    assertTrue(jsonReader.nextIfObjectStart());
                    assertEquals(Fnv.hashCode64(name), jsonReader.readFieldNameHashCode());
                }
            }
        }

        bytes = "{\"012\":123456789}".getBytes();
        for (int i0 = 'a'; i0 <= 'z'; i0++) {
            for (int i1 = 'a'; i1 <= 'z'; i1++) {
                for (int i2 = 'a'; i2 <= 'z'; i2++) {
                    bytes[2] = (byte) i0;
                    bytes[3] = (byte) i1;
                    bytes[4] = (byte) i2;
                    String name = new String(new char[]{(char) i0, (char) i1, (char) i2});
                    JSONReaderASCII jsonReader = new JSONReaderASCII(context, null, bytes, 0, bytes.length);
                    assertTrue(jsonReader.nextIfObjectStart());
                    assertEquals(Fnv.hashCode64(name), jsonReader.readFieldNameHashCode());
                }
            }
        }
    }

    @Test
    public void readNameHashCode_x4() {
        JSONReader.Context context = JSONFactory.createReadContext();

        byte[] bytes = "{\"0123\":123456789}".getBytes();
        for (int i0 = 'a'; i0 <= 'z'; i0++) {
            for (int i1 = 'a'; i1 <= 'z'; i1++) {
                for (int i2 = 'a'; i2 <= 'z'; i2++) {
                    for (int i3 = 'a'; i3 <= 'z'; i3++) {
                        bytes[2] = (byte) i0;
                        bytes[3] = (byte) i1;
                        bytes[4] = (byte) i2;
                        bytes[5] = (byte) i3;

                        String name = new String(new char[]{(char) i0, (char) i1, (char) i2, (char) i3});
                        JSONReaderASCII jsonReader = new JSONReaderASCII(context, null, bytes, 0, bytes.length);
                        assertTrue(jsonReader.nextIfObjectStart());
                        assertEquals(Fnv.hashCode64(name), jsonReader.readFieldNameHashCode());
                    }
                }
            }
        }
    }

    @Test
    public void readName_1() {
        char[] chars = "{\"0\":0}".toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            bytes[i] = (byte) chars[i];
        }

        for (char c = 128; c < 255; ++c) {
            chars[2] = c;
            bytes[2] = (byte) c;

            JSONReader utf16Reader = JSONReader.of(chars);
            assertTrue(utf16Reader.nextIfObjectStart());
            String name0 = utf16Reader.readFieldName();

            JSONReaderASCII asciiReader = new JSONReaderASCII(JSONFactory.createReadContext(), null, bytes, 0, bytes.length);
            assertTrue(asciiReader.nextIfObjectStart());
            String name1 = asciiReader.readFieldName();

            assertEquals(name0, name1);
        }
    }

    @Test
    public void readName_2() {
        char[] chars = "{\"00\":0}".toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            bytes[i] = (byte) chars[i];
        }

        for (char c0 = 128; c0 < 129; ++c0) {
            chars[2] = c0;
            bytes[2] = (byte) c0;

            for (char c1 = 128; c1 < 255; ++c1) {
                chars[3] = c1;
                bytes[3] = (byte) c1;

                JSONReader utf16Reader = JSONReader.of(chars);
                assertTrue(utf16Reader.nextIfObjectStart());
                String name0 = utf16Reader.readFieldName();

                JSONReaderASCII asciiReader = new JSONReaderASCII(JSONFactory.createReadContext(), null, bytes, 0, bytes.length);
                assertTrue(asciiReader.nextIfObjectStart());
                String name1 = asciiReader.readFieldName();

                assertEquals(name0, name1);

                byte[] ut8Bytes = new String(chars).getBytes(StandardCharsets.UTF_8);
                JSONReaderUTF8 utf8Reader = new JSONReaderUTF8(JSONFactory.createReadContext(), ut8Bytes, 0, ut8Bytes.length);
                assertTrue(utf8Reader.nextIfObjectStart());
                String name2 = utf8Reader.readFieldName();
                assertEquals(name0, name2);
            }
        }
    }

    @Test
    public void asciitest() {
        char[] chars = new char[1024];
        chars[0] = '"';
        for (int i = 1; i < chars.length - 1; ++i) {
            chars[i] = 'A';
            chars[i + 1] = '"';
            byte[] ascii = new byte[i + 2];
            for (int j = 0; j < ascii.length; j++) {
                ascii[j] = (byte) chars[j];
            }
            String str0 = new String(ascii, 1, ascii.length - 2, StandardCharsets.US_ASCII);
            JSONReader jsonReader = JSONReader.of(ascii, 0, ascii.length, StandardCharsets.US_ASCII);
            String str1 = jsonReader.readString();
            assertEquals(str0, str1);
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void asciitest1() {
        char[] chars = new char[2048];
        chars[0] = '"';
        for (int i = 1; i < chars.length - 2; i += 2) {
            chars[i] = '\\';
            chars[i + 1] = '\\';
            chars[i + 2] = '"';
            byte[] ascii = new byte[i + 3];
            for (int j = 0; j < ascii.length; j++) {
                ascii[j] = (byte) chars[j];
            }

            byte[] unescape = new byte[i / 2 + 1];
            Arrays.fill(unescape, (byte) '\\');

            String str0 = new String(unescape);
            JSONReader jsonReader = JSONReader.of(ascii, 0, ascii.length, StandardCharsets.US_ASCII);
            String str1 = jsonReader.readString();
            assertEquals(str0, str1);
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void utf16test() {
        char[] chars = new char[1024];
        chars[0] = '"';
        for (int i = 1; i < chars.length - 1; ++i) {
            chars[i] = 'A';
            chars[i + 1] = '"';
            char[] utf16 = new char[i + 2];
            for (int j = 0; j < utf16.length; j++) {
                utf16[j] = chars[j];
            }
            String str0 = new String(utf16, 1, utf16.length - 2);
            JSONReader jsonReader = JSONReader.of(utf16, 0, utf16.length);
            String str1 = jsonReader.readString();
            assertEquals(str0, str1);
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void utf16test1() {
        char[] chars = new char[2048];
        chars[0] = '"';
        for (int i = 1; i < chars.length - 2; i += 2) {
            chars[i] = '\\';
            chars[i + 1] = '\\';
            chars[i + 2] = '"';
            char[] utf16 = new char[i + 3];
            for (int j = 0; j < utf16.length; j++) {
                utf16[j] = chars[j];
            }

            char[] unescape = new char[i / 2 + 1];
            Arrays.fill(unescape, '\\');

            String str0 = new String(unescape);
            JSONReader jsonReader = JSONReader.of(utf16, 0, utf16.length);
            String str1 = jsonReader.readString();
            assertEquals(str0, str1);
            assertTrue(jsonReader.isEnd());
        }
    }

    @Test
    public void testMillis_shanghai() {
        ZoneId zoneId = IOUtils.SHANGHAI_ZONE_ID;
        int[] years = {
                1992, 1991, 1990, 1989, 1988, 1987, 1986, 1985,
                1950, 1949, 1948, 1947, 1946, 1945, 1944, 1943, 1942, 1941, 1941, 1940,
                1939, 1938, 1937, 1920, 1919, 1902, 1901, 1900, 1899
        };
        int[] months = {12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        int[] days = {
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27, 28
        };
        int[] hours = {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                21, 22, 23
        };
        int[] minutes = {0, 1, 59};
        for (int year : years) {
            for (int month : months) {
                for (int dom : days) {
                    for (int hour : hours) {
                        for (int minute : minutes) {
                            validate(zoneId, year, month, dom, hour, minute);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testMillis_utc() {
        ZoneId[] zoneIds = {
                ZoneId.of("Asia/Macau"),
                ZoneOffset.UTC,
                ZoneId.of("Asia/Kuching"),
                ZoneId.of("Europe/London")
        };

        for (ZoneId zoneId : zoneIds) {
            int[] years = {
                    1992, 1991, 1990, 1989, 1988, 1987, 1986, 1985,
                    1950, 1949, 1948, 1947, 1946, 1945, 1944, 1943, 1942, 1941, 1941, 1940,
                    1939, 1938, 1937, 1920, 1919, 1902, 1901, 1900, 1899
            };
            int[] months = {12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
            int[] days = {
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                    11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                    21, 22, 23, 24, 25, 26, 27, 28
            };
            int[] hours = {
                    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                    11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                    21, 22, 23
            };

            int[] minutes = {0, 1, 59};
            for (int year : years) {
                for (int month : months) {
                    for (int dom : days) {
                        for (int hour : hours) {
                            for (int minute : minutes) {
                                validate(zoneId, year, month, dom, hour, minute);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void validate(ZoneId zoneId, int year, int month, int dom, int hour, int minute) {
        LocalDateTime ldt = LocalDateTime.of(year, month, dom, hour, minute, 0, 0);
        long epochMilli = ldt.atZone(zoneId).toInstant().toEpochMilli();

        long millis = DateUtils.millis(zoneId, year, month, dom, hour, minute, 0, 0);
        assertEquals(epochMilli, millis, ldt.toString());
    }
}
