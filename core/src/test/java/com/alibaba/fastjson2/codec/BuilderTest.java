package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaders;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BuilderTest {
    @Test
    public void test_build() throws Exception {
        ObjectReaderCreator creator = TestUtils.READER_CREATOR;
        Supplier instanceSupplier = creator.createInstanceSupplier(VOBuilder.class);
        FieldReader fieldReader = ObjectReaders.fieldReaderInt("id", VOBuilder::withId);
        Function<Object, Object> buildFunction = creator.createBuildFunction(VOBuilder.class.getMethod("build"));
        ObjectReader objectReader = creator.createObjectReader(VOBuilder.class, 0, instanceSupplier, buildFunction, fieldReader);

        {
            VO o = (VO) objectReader.readObject(JSONReader.of("{\"id\":123}"), 0);
            assertEquals(123, o.id);
        }
        {
            byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("id", 123));
            VO o = (VO) objectReader.readJSONBObject(JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length), null, null, 0);
            assertEquals(123, o.id);
        }
        {
            byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("id", 123));
            VO o = (VO) objectReader.readObject(JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length), null, null, 0);
            assertEquals(123, o.id);
        }
    }

    @Test
    public void test_build_2() throws Exception {
        ObjectReaderCreator creator = TestUtils.READER_CREATOR;
        Supplier instanceSupplier = creator.createInstanceSupplier(VOBuilder.class);

        FieldReader fieldReader1 = ObjectReaders.fieldReaderInt("id", VOBuilder::withId);
        FieldReader fieldReader2 = ObjectReaders.fieldReader("name", String.class, VOBuilder::withName);

        Function<Object, Object> buildFunction = creator.createBuildFunction(VOBuilder.class.getMethod("build"));
        ObjectReader objectReader = creator.createObjectReader(VOBuilder.class, 0, instanceSupplier, buildFunction, fieldReader1, fieldReader2);

        {
            VO o = (VO) objectReader.readObject(JSONReader.of("{\"id\":123,\"name\":\"bill\"}"), 0);
            assertEquals(123, o.id);
            assertEquals("bill", o.name);
        }

        Map map = new HashMap<>();
        map.put("id", 123);
        map.put("name", "bill");
        byte[] jsonbBytes = JSONB.toBytes(map);
        {
            VO o = (VO) objectReader.readJSONBObject(JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length), null, null, 0);
            assertEquals(123, o.id);
            assertEquals("bill", o.name);
        }
        {
            VO o = (VO) objectReader.readObject(JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length), null, null, 0);
            assertEquals(123, o.id);
            assertEquals("bill", o.name);
        }
    }

    @Test
    public void test_build_3() throws Exception {
        ObjectReaderCreator creator = ObjectReaderCreator.INSTANCE;
        Supplier instanceSupplier = creator.createInstanceSupplier(VOBuilder.class);

        FieldReader fieldReader1 = ObjectReaders.fieldReaderInt("id", VOBuilder::withId);
        FieldReader fieldReader2 = ObjectReaders.fieldReader("name", String.class, VOBuilder::withName);
        FieldReader fieldReader3 = ObjectReaders.fieldReaderShort("age", VOBuilder::withAge);

        Function<Object, Object> buildFunction = creator.createBuildFunction(VOBuilder.class.getMethod("build"));
        ObjectReader objectReader = creator.createObjectReader(
                VOBuilder.class,
                0, instanceSupplier,
                buildFunction,
                fieldReader1,
                fieldReader2,
                fieldReader3);

        {
            VO o = (VO) objectReader.readObject(JSONReader.of("{\"id\":123,\"name\":\"bill\",\"age\":56}"), 0);
            assertEquals(123, o.id);
            assertEquals("bill", o.name);
            assertEquals(56, o.age);
        }

        {
            Map map = new HashMap<>();
            map.put("id", 123);
            map.put("name", "bill");
            map.put("age", 56);
            byte[] jsonbBytes = JSONB.toBytes(map);
            {
                VO o = (VO) objectReader.readJSONBObject(JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length), null, null, 0);
                assertEquals(123, o.id);
                assertEquals("bill", o.name);
                assertEquals(56, o.age);
            }
            {
                VO o = (VO) objectReader.readObject(JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length), null, null, 0);
                assertEquals(123, o.id);
                assertEquals("bill", o.name);
                assertEquals(56, o.age);
            }
        }
    }

    @Test
    public void test_build_4() throws Exception {
        ObjectReaderCreator creator = ObjectReaderCreator.INSTANCE;
        Supplier instanceSupplier = creator.createInstanceSupplier(VOBuilder.class);

        FieldReader fieldReader1 = ObjectReaders.fieldReaderInt("id", VOBuilder::withId);
        FieldReader fieldReader2 = ObjectReaders.fieldReader("name", String.class, VOBuilder::withName);
        FieldReader fieldReader3 = ObjectReaders.fieldReaderShort("age", VOBuilder::withAge);
        FieldReader fieldReader4 = ObjectReaders.fieldReaderLong("tag1", VOBuilder::withTag1);

        Function<Object, Object> buildFunction = creator.createBuildFunction(VOBuilder.class.getMethod("build"));
        ObjectReader objectReader = creator.createObjectReader(
                VOBuilder.class,
                0,
                instanceSupplier,
                buildFunction,
                fieldReader1,
                fieldReader2,
                fieldReader3,
                fieldReader4
        );

        {
            VO o = (VO) objectReader.readObject(JSONReader.of("{\"id\":123,\"name\":\"bill\",\"age\":56,\"tag1\":987654321}"), 0);
            assertEquals(123, o.id);
            assertEquals("bill", o.name);
            assertEquals(56, o.age);
            assertEquals(987654321, o.tag1);
        }

        Map map = new HashMap<>();
        map.put("id", 123);
        map.put("name", "bill");
        map.put("age", 56);
        map.put("tag1", 987654321);
        byte[] jsonbBytes = JSONB.toBytes(map);
        {
            VO o = (VO) objectReader.readJSONBObject(JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length), null, null, 0);
            assertEquals(123, o.id);
            assertEquals("bill", o.name);
            assertEquals(56, o.age);
            assertEquals(987654321, o.tag1);
        }
        {
            VO o = (VO) objectReader.readObject(JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length), null, null, 0);
            assertEquals(123, o.id);
            assertEquals("bill", o.name);
            assertEquals(56, o.age);
            assertEquals(987654321, o.tag1);
        }
    }

    @Test
    public void test_build_5() throws Exception {
        ObjectReaderCreator creator = ObjectReaderCreator.INSTANCE;
        Supplier instanceSupplier = creator.createInstanceSupplier(VOBuilder.class);

        FieldReader fieldReader1 = ObjectReaders.fieldReaderInt("id", VOBuilder::withId);
        FieldReader fieldReader2 = ObjectReaders.fieldReader("name", String.class, VOBuilder::withName);
        FieldReader fieldReader3 = ObjectReaders.fieldReaderShort("age", VOBuilder::withAge);
        FieldReader fieldReader4 = ObjectReaders.fieldReaderLong("tag1", VOBuilder::withTag1);
        FieldReader fieldReader5 = ObjectReaders.fieldReader("tag2", Integer.class, VOBuilder::withTag2);
        FieldReader fieldReader6 = ObjectReaders.fieldReader("tag3", Long.class, VOBuilder::withTag3);

        Function<Object, Object> buildFunction = creator.createBuildFunction(VOBuilder.class.getMethod("build"));
        ObjectReader objectReader = creator.createObjectReader(
                VOBuilder.class,
                0, instanceSupplier,
                buildFunction,
                fieldReader1,
                fieldReader2,
                fieldReader3,
                fieldReader4,
                fieldReader5,
                fieldReader6
        );

        {
            VO o = (VO) objectReader.readObject(JSONReader.of("{\"id\":123,\"name\":\"bill\",\"age\":56,\"tag1\":987654321,\"tag2\":null,\"tag3\":101}"), 0);
            assertEquals(123, o.id);
            assertEquals("bill", o.name);
            assertEquals(56, o.age);
            assertEquals(987654321, o.tag1);
            assertNull(o.tag2);
            assertEquals(101L, o.tag3.longValue());
        }

        Map map = new HashMap<>();
        map.put("id", 123);
        map.put("name", "bill");
        map.put("age", 56);
        map.put("tag1", 987654321);
        map.put("tag2", null);
        map.put("tag3", 101);
        byte[] jsonbBytes = JSONB.toBytes(map);
        {
            VO o = (VO) objectReader.readJSONBObject(JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length), null, null, 0);
            assertEquals(123, o.id);
            assertEquals("bill", o.name);
            assertEquals(56, o.age);
            assertEquals(987654321, o.tag1);
            assertNull(o.tag2);
            assertEquals(101L, o.tag3.longValue());
        }
        {
            VO o = (VO) objectReader.readObject(JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length), null, null, 0);
            assertEquals(123, o.id);
            assertEquals("bill", o.name);
            assertEquals(56, o.age);
            assertEquals(987654321, o.tag1);
            assertNull(o.tag2);
            assertEquals(101L, o.tag3.longValue());
        }
    }

    public static class VO {
        int id;
        String name;
        short age;
        long tag1;
        Integer tag2;
        Long tag3;

        private VO() {
        }
    }

    public static class VOBuilder {
        VO vo = new VO();

        public VOBuilder withId(int id) {
            vo.id = id;
            return this;
        }

        public VOBuilder withName(String name) {
            vo.name = name;
            return this;
        }

        public VOBuilder withAge(short age) {
            vo.age = age;
            return this;
        }

        public VOBuilder withTag1(long tag1) {
            vo.tag1 = tag1;
            return this;
        }

        public VOBuilder withTag2(Integer tag2) {
            vo.tag2 = tag2;
            return this;
        }

        public VOBuilder withTag3(Long tag3) {
            vo.tag3 = tag3;
            return this;
        }

        public VO build() {
            return vo;
        }
    }
}
