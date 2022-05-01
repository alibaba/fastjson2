package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorDynamicCompile;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorLambda;
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
}
