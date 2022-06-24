package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ObjectReader6Test {
    @Test
    public void test() {
        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean.class);
        assertNotNull(objectReader.getFieldReader("userId1"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId1")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid1")));

        assertNotNull(objectReader.getFieldReader("userId2"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId2")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid2")));

        assertNotNull(objectReader.getFieldReader("userId3"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId3")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid3")));

        assertNotNull(objectReader.getFieldReader("userId4"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId4")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid4")));

        assertNotNull(objectReader.getFieldReader("userId5"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId5")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid5")));

        assertNotNull(objectReader.getFieldReader("userId6"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId6")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid6")));

        assertNull(objectReader.getFieldReader("id"));
        assertNull(objectReader.getFieldReader(0));
        assertNull(objectReader.getFieldReaderLCase(0));
    }

    private class Bean {
        public int userId1;
        public int userId2;
        public int userId3;
        public int userId4;
        public int userId5;
        public int userId6;
    }
}
