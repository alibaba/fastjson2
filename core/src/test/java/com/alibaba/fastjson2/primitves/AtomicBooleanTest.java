package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2_vo.AtomicBooleanReadOnly1;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AtomicBooleanTest {
    @Test
    public void test_readOnly() throws Exception {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            FieldReader fieldWriter = creator.createFieldReader(
                    AtomicBooleanReadOnly1.class,
                    "value",
                    AtomicBoolean.class,
                    AtomicBoolean.class,
                    AtomicBooleanReadOnly1.class.getMethod("getValue"));
            ObjectReader<AtomicBooleanReadOnly1> objectReader
                    = creator.createObjectReader(AtomicBooleanReadOnly1.class, fieldWriter);

            {
                AtomicBooleanReadOnly1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":true}"), 0);
                assertEquals(true, vo.getValue().get());
            }
            {
                AtomicBooleanReadOnly1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":null}"), 0);
                assertEquals(false, vo.getValue().get());
            }
        }
    }

    @Test
    public void test_readOnly1() throws Exception {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            FieldReader fieldWriter = creator.createFieldReader(
                    AtomicBooleanReadOnly1.class,
                    "value",
                    null,
                    AtomicBoolean.class,
                    AtomicBoolean.class,
                    AtomicBooleanReadOnly1.class.getMethod("getValue"));
            ObjectReader<AtomicBooleanReadOnly1> objectReader
                    = creator.createObjectReader(AtomicBooleanReadOnly1.class, fieldWriter);

            {
                AtomicBooleanReadOnly1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":true}"), 0);
                assertEquals(true, vo.getValue().get());
            }
            {
                AtomicBooleanReadOnly1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":null}"), 0);
                assertEquals(false, vo.getValue().get());
            }
        }
    }

    @Test
    public void test_readOnly2() throws Exception {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            FieldReader fieldWriter = creator.createFieldReaderMethod(
                    AtomicBooleanReadOnly1.class,
                    "value",
                    null,
                    AtomicBoolean.class,
                    AtomicBoolean.class,
                    AtomicBooleanReadOnly1.class.getMethod("getValue"));
            ObjectReader<AtomicBooleanReadOnly1> objectReader
                    = creator.createObjectReader(AtomicBooleanReadOnly1.class, fieldWriter);

            {
                AtomicBooleanReadOnly1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":true}"), 0);
                assertEquals(true, vo.getValue().get());
            }
            {
                AtomicBooleanReadOnly1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":null}"), 0);
                assertEquals(false, vo.getValue().get());
            }
        }
    }
}
