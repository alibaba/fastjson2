package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1226 {
    @Test
    public void test() {
        ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
        ObjectWriterAdapter writerAdapter = (ObjectWriterAdapter) provider.getObjectWriter(Bean.class);

        JSONWriter jsonWriter = JSONWriter.of();
        writerAdapter.writeWithFilter(jsonWriter, null);
        assertEquals("null", jsonWriter.toString());
    }

    static class Bean {
        public int id;
    }
}
