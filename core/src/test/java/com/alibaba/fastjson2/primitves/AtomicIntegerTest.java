package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2_vo.AtomicIntegerReadOnly1;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertEquals;

public class AtomicIntegerTest {
    @Test
    public void test_readOnly() throws Exception {
        ObjectReaderCreator[] creators = new ObjectReaderCreator[]{
                ObjectReaderCreator.INSTANCE,
                ObjectReaderCreatorLambda.INSTANCE,
                ObjectReaderCreatorASM.INSTANCE
        };

        for (ObjectReaderCreator creator : creators) {
            FieldReader fieldWriter = creator.createFieldReader(
                    AtomicIntegerReadOnly1.class
                    , "value"
                    , AtomicInteger.class
                    , AtomicInteger.class
                    , AtomicIntegerReadOnly1.class.getMethod("getValue"));
            ObjectReader<AtomicIntegerReadOnly1> objectReader
                    = creator.createObjectReader(AtomicIntegerReadOnly1.class, fieldWriter);

            {
                AtomicIntegerReadOnly1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":101}"), 0);
                assertEquals(101, vo.getValue().get());
            }
            {
                AtomicIntegerReadOnly1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":null}"), 0);
                assertEquals(0, vo.getValue().get());
            }
        }
    }
}
