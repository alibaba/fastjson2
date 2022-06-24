package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ObjectReader2Test {
    @Test
    public void test() {
        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean.class);
        assertNotNull(objectReader.getFieldReader("userId1"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId1")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid1")));

        assertNotNull(objectReader.getFieldReader("userId2"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId2")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid2")));

        assertNull(objectReader.getFieldReader("id"));
        assertNull(objectReader.getFieldReader(0));
        assertNull(objectReader.getFieldReaderLCase(0));
    }

    private class Bean {
        public int userId1;
        public int userId2;
    }
}
