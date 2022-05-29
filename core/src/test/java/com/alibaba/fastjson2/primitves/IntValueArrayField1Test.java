package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2_vo.IntValueArrayField1;
import com.alibaba.fastjson2_vo.IntValueArrayFinalField1;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntValueArrayField1Test {
    @Test
    public void test_0() throws Exception {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            Field field = IntValueArrayField1.class.getField("values");
            FieldReader fieldWriter = creator.createFieldReader(
                    "values",
                    field.getGenericType(),
                    field);

            ObjectReader<IntValueArrayField1> objectReader
                    = creator.createObjectReader(IntValueArrayField1.class, fieldWriter);

            {
                IntValueArrayField1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"values\":[101,\"102\"]}"), 0);
                assertEquals(101, vo.values[0]);
                assertEquals(102, vo.values[1]);
            }
            {
                IntValueArrayField1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":null}"), 0);
                assertEquals(null, vo.values);
            }
        }
    }

    @Test
    public void test_final_0() throws Exception {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            Field field = IntValueArrayFinalField1.class.getField("values");
            FieldReader fieldWriter = creator.createFieldReader(
                    "values",
                    field.getGenericType(),
                    field);

            ObjectReader<IntValueArrayFinalField1> objectReader
                    = creator.createObjectReader(IntValueArrayFinalField1.class, fieldWriter);

            {
                IntValueArrayFinalField1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"values\":[101,\"102\"]}"), 0);
                assertEquals(101, vo.values[0]);
                assertEquals(102, vo.values[1]);
            }
            {
                IntValueArrayFinalField1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":null}"), 0);
                assertEquals(0, vo.values[0]);
            }
        }
    }
}
