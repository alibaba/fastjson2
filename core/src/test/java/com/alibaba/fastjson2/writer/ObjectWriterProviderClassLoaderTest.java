package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ObjectWriterProviderClassLoaderTest {
    @Test
    public void classLoaderOwnershipPreservesCreatorSelectionAndNamingStrategy() {
        ObjectWriterProvider shared = new ObjectWriterProvider(PropertyNamingStrategy.SnakeCase);
        ObjectWriterProvider explicitShared = new ObjectWriterProvider(PropertyNamingStrategy.SnakeCase, true);
        ObjectWriterProvider isolated = new ObjectWriterProvider(PropertyNamingStrategy.SnakeCase, false);
        assertSame(shared.getCreator(), explicitShared.getCreator());
        assertEquals(shared.getCreator().getClass(), isolated.getCreator().getClass());
        assertSame(PropertyNamingStrategy.SnakeCase, isolated.getNamingStrategy());
        if (shared.getCreator() instanceof ObjectWriterCreatorASM) {
            assertNotSame(shared.getCreator(), isolated.getCreator());
        } else {
            assertSame(shared.getCreator(), isolated.getCreator());
        }
        assertEquals("{\"user_id\":7}", JSON.toJSONString(new Bean(), new JSONWriter.Context(shared)));
        assertEquals("{\"user_id\":7}", JSON.toJSONString(new Bean(), new JSONWriter.Context(isolated)));
    }

    public static class Bean {
        public int userId = 7;
    }
}
