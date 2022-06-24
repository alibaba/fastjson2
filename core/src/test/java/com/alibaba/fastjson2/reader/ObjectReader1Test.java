package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ObjectReader1Test {
    @Test
    public void test() {
        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean.class);
        assertNotNull(objectReader.getFieldReader("userId"));
        assertNotNull(objectReader.getFieldReader(Fnv.hashCode64("userId")));
        assertNotNull(objectReader.getFieldReaderLCase(Fnv.hashCode64("userid")));

        assertNull(objectReader.getFieldReader("id"));
        assertNull(objectReader.getFieldReader(0));
        assertNull(objectReader.getFieldReaderLCase(0));
    }

    private class Bean {
        public int userId;
    }
}
