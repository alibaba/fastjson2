package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectReaderImplListGenericTest {
    @Test
    public void inheritedCollectionItemType() {
        LevelCollection values = JSON.parseObject("[\"LOW\",\"HIGH\"]", LevelCollection.class);
        assertEquals(Arrays.asList(Level.LOW, Level.HIGH), new ArrayList<>(values));
    }

    @Test
    public void inheritedCollectionItemTypeJSONB() {
        byte[] jsonb = JSONB.toBytes(Arrays.asList("LOW", "HIGH"));
        LevelCollection values = JSONB.parseObject(jsonb, LevelCollection.class);
        assertEquals(Arrays.asList(Level.LOW, Level.HIGH), new ArrayList<>(values));
    }

    @Test
    public void inheritedLongCollectionItemType() {
        LongCollection values = JSON.parseObject("[1,2147483648]", LongCollection.class);
        assertEquals(Arrays.asList(1L, 2147483648L), new ArrayList<>(values));
    }

    @Test
    public void inheritedByteCollectionItemType() {
        ByteCollection values = JSON.parseObject("[-128,0,127]", ByteCollection.class);
        assertEquals(Arrays.asList((byte) -128, (byte) 0, (byte) 127), new ArrayList<>(values));
    }

    @Test
    public void inheritedFloatCollectionItemType() {
        FloatCollection values = JSON.parseObject("[-3.25,0,2.5]", FloatCollection.class);
        assertEquals(Arrays.asList(-3.25F, 0F, 2.5F), new ArrayList<>(values));
    }

    @Test
    public void inheritedCharacterCollectionItemType() {
        CharacterCollection values = JSON.parseObject("[\"a\",\"Z\"]", CharacterCollection.class);
        assertEquals(Arrays.asList('a', 'Z'), new ArrayList<>(values));
    }

    @Test
    public void objectCollectionRemainsHeterogeneous() {
        ObjectCollection values = JSON.parseObject("[1,\"two\"]", ObjectCollection.class);
        assertEquals(Arrays.asList(1, "two"), new ArrayList<>(values));
    }

    enum Level {
        LOW,
        HIGH
    }

    abstract static class DelegatingCollection<T>
            extends AbstractCollection<T> {
        private final List<T> values = new ArrayList<>();

        @Override
        public boolean add(T value) {
            return values.add(value);
        }

        @Override
        public Iterator<T> iterator() {
            return values.iterator();
        }

        @Override
        public int size() {
            return values.size();
        }
    }

    abstract static class AbstractLevelCollection
            extends DelegatingCollection<Level> {
        @Override
        public boolean add(Level value) {
            return super.add(value);
        }
    }

    static class LevelCollection
            extends AbstractLevelCollection {
    }

    abstract static class AbstractLongCollection
            extends DelegatingCollection<Long> {
        @Override
        public boolean add(Long value) {
            return super.add(value);
        }
    }

    static class LongCollection
            extends AbstractLongCollection {
    }

    abstract static class AbstractByteCollection
            extends DelegatingCollection<Byte> {
        @Override
        public boolean add(Byte value) {
            return super.add(value);
        }
    }

    static class ByteCollection
            extends AbstractByteCollection {
    }

    abstract static class AbstractFloatCollection
            extends DelegatingCollection<Float> {
        @Override
        public boolean add(Float value) {
            return super.add(value);
        }
    }

    static class FloatCollection
            extends AbstractFloatCollection {
    }

    abstract static class AbstractCharacterCollection
            extends DelegatingCollection<Character> {
        @Override
        public boolean add(Character value) {
            return super.add(value);
        }
    }

    static class CharacterCollection
            extends AbstractCharacterCollection {
    }

    static class ObjectCollection
            extends AbstractCollection<Object> {
        private final List<Object> values = new ArrayList<>();

        @Override
        public boolean add(Object value) {
            return values.add(value);
        }

        @Override
        public Iterator<Object> iterator() {
            return values.iterator();
        }

        @Override
        public int size() {
            return values.size();
        }
    }
}
