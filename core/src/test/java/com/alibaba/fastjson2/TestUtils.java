package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;

public class TestUtils {

    public static ObjectReaderCreator[] readerCreators() {
        return new ObjectReaderCreator[] {
                ObjectReaderCreator.INSTANCE,
        };
    }
    public static ObjectWriterCreator[] writerCreators() {
        return new ObjectWriterCreator[] {
                ObjectWriterCreator.INSTANCE,
        };
    }

    public static ObjectReaderCreator[] readerCreators2() {
        return new ObjectReaderCreator[] {
                ObjectReaderCreator.INSTANCE,
        };
    }

    public static ObjectReaderCreator READER_CREATOR = ObjectReaderCreator.INSTANCE;
    public static ObjectWriterCreator WRITER_CREATOR = ObjectWriterCreator.INSTANCE;

    public static ObjectReaderCreator readerCreator(ClassLoader classLoader) {
        return READER_CREATOR;
    }

    public static ObjectWriterCreator writerCreator(ClassLoader classLoader) {
        return WRITER_CREATOR;
    }

    public static <T> ObjectReader<T> of(Class<T> objectType) {
        return READER_CREATOR.createObjectReader(objectType);
    }
}
