package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorLambda;

public class TestUtils {

    public static ObjectReaderCreator[] readerCreators() {
        return new ObjectReaderCreator[] {
                ObjectReaderCreator.INSTANCE,
                ObjectReaderCreatorLambda.INSTANCE,
                ObjectReaderCreatorASM.INSTANCE,
                ObjectReaderCreatorDynamicCompile.INSTANCE,
        };
    }
    public static ObjectWriterCreator[] writerCreators() {
        return new ObjectWriterCreator[] {
                ObjectWriterCreator.INSTANCE,
                ObjectWriterCreatorLambda.INSTANCE,
                ObjectWriterCreatorASM.INSTANCE,
        };
    }

    public static ObjectReaderCreator[] readerCreators2() {
        return new ObjectReaderCreator[] {
                ObjectReaderCreator.INSTANCE,
                ObjectReaderCreatorLambda.INSTANCE,
                ObjectReaderCreatorASM.INSTANCE,
        };
    }

    public static ObjectReaderCreator READER_CREATOR = ObjectReaderCreatorASM.INSTANCE;
    public static ObjectWriterCreator WRITER_CREATOR = ObjectWriterCreatorASM.INSTANCE;

    public static ObjectReaderCreator readerCreator(ClassLoader classLoader) {
        return new ObjectReaderCreatorASM(classLoader);
    }

    public static ObjectWriterCreator writerCreator(ClassLoader classLoader) {
        return new ObjectWriterCreatorASM(classLoader);
    }

    public static <T> ObjectReader<T> of(Class<T> objectType) {
        return READER_CREATOR.createObjectReader(objectType);
    }
}
