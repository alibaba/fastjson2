package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2_vo.ListStrField1;
import com.alibaba.fastjson2_vo.ListStrFinalField1;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListStrFieldTest {
    @Test
    public void test_0() throws Exception {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            Field field = ListStrField1.class.getField("v0000");
            FieldReader fieldWriter = creator.createFieldReader(
                    "value",
                    field.getGenericType(),
                    field);
            ObjectReader<ListStrField1> objectReader
                    = creator.createObjectReader(ListStrField1.class, fieldWriter);

            {
                ListStrField1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":[101]}"), 0);
                assertEquals("101", vo.v0000.get(0));
            }
            {
                ListStrField1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":null}"), 0);
                assertEquals(null, vo.v0000);
            }
        }
    }

    @Test
    public void test_0_readOnly() throws Exception {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            Field field = ListStrFinalField1.class.getField("v0000");
            FieldReader fieldWriter = creator.createFieldReader(
                    "value",
                    field.getGenericType(),
                    field);
            ObjectReader<ListStrFinalField1> objectReader
                    = creator.createObjectReader(ListStrFinalField1.class, fieldWriter);

            {
                ListStrFinalField1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":[101]}"), 0);
                assertEquals("101", vo.v0000.get(0));
            }
            {
                ListStrFinalField1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":null}"), 0);
                assertEquals(0, vo.v0000.size());
            }
        }
    }
}
