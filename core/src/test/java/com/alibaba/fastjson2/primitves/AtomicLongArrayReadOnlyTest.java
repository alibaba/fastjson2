package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2_vo.AtomicLongArrayReadOnly1;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLongArray;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AtomicLongArrayReadOnlyTest {
    @Test
    public void test_readOnly() throws Exception {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            FieldReader fieldWriter = creator.createFieldReader(
                    AtomicLongArrayReadOnly1.class,
                    "value",
                    AtomicLongArray.class,
                    AtomicLongArray.class,
                    AtomicLongArrayReadOnly1.class.getMethod("getValue")
            );
            ObjectReader<AtomicLongArrayReadOnly1> objectReader
                    = creator.createObjectReader(AtomicLongArrayReadOnly1.class, fieldWriter);

            {
                AtomicLongArrayReadOnly1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":[101]}"), 0);
                assertEquals(101, vo.getValue().get(0));
            }
            {
                AtomicLongArrayReadOnly1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":null}"), 0);
                assertEquals(0, vo.getValue().get(0));
            }
        }
    }
}
