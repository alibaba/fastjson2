package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import dm.jdbc.driver.DmdbStruct;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2205 {
    @Test
    public void test() {
        ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
        ObjectWriterAdapter objectWriter = (ObjectWriterAdapter) provider.getObjectWriter(DmdbStruct.class);
        assertEquals(2, objectWriter.getFieldWriters().size());
        assertNotNull(objectWriter.getFieldWriter("attributes"));
        assertNotNull(objectWriter.getFieldWriter("SQLTypeName"));
    }
}
