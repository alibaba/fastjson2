package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorDynamicCompile;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorLambda;

public class TestUtils {

    public static ObjectReaderCreator[] readerCreators() {
        return new ObjectReaderCreator[] {
                ObjectReaderCreator.INSTANCE,
                ObjectReaderCreatorLambda.INSTANCE,
                ObjectReaderCreatorASM.INSTANCE,
                ObjectReaderCreatorDynamicCompile.INSTANCE,
        };
    }
}
