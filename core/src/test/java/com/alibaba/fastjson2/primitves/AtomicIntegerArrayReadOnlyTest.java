package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2_vo.AtomicIntegerArrayReadOnly1;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicIntegerArray;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AtomicIntegerArrayReadOnlyTest {
    @Test
    public void test_readOnly() throws Exception {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            FieldReader fieldWriter = creator.createFieldReader(
                    AtomicIntegerArrayReadOnly1.class,
                    "value",
                    AtomicIntegerArray.class,
                    AtomicIntegerArray.class,
                    AtomicIntegerArrayReadOnly1.class.getMethod("getValue"));
            ObjectReader<AtomicIntegerArrayReadOnly1> objectReader
                    = creator.createObjectReader(AtomicIntegerArrayReadOnly1.class, fieldWriter);

            {
                AtomicIntegerArrayReadOnly1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":[101]}"), 0);
                assertEquals(101, vo.getValue().get(0));
            }
            {
                AtomicIntegerArrayReadOnly1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":null}"), 0);
                assertEquals(0, vo.getValue().get(0));
            }
        }
    }
}
