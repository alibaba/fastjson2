package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2_vo.AtomicLongReadOnly1;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static junit.framework.TestCase.assertEquals;

public class AtomicLongReadTest {
    @Test
    public void test_readOnly() throws Exception {
        ObjectReaderCreator[] creators = new ObjectReaderCreator[]{
                ObjectReaderCreator.INSTANCE,
                ObjectReaderCreatorLambda.INSTANCE,
                ObjectReaderCreatorASM.INSTANCE
        };

        for (ObjectReaderCreator creator : creators) {
            FieldReader fieldWriter = creator.createFieldReader(
                    AtomicLongReadOnly1.class
                    , "value"
                    , AtomicLong.class
                    , AtomicLong.class
                    , AtomicLongReadOnly1.class.getMethod("getValue"));
            ObjectReader<AtomicLongReadOnly1> objectReader
                    = creator.createObjectReader(AtomicLongReadOnly1.class, fieldWriter);

            {
                AtomicLongReadOnly1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":101}"), 0);
                assertEquals(101, vo.getValue().get());
            }
            {
                AtomicLongReadOnly1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":null}"), 0);
                assertEquals(0, vo.getValue().get());
            }
        }
    }
}
