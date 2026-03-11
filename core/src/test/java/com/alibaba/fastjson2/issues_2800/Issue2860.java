package com.alibaba.fastjson2.issues_2800;

import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("regression")
public class Issue2860 {
    @Test
    public void test() {
        new ObjectReaderProvider().clear();
        new ObjectWriterProvider().clear();
    }
}
