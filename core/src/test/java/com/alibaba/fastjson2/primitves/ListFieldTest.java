package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2_vo.ListField1;
import com.alibaba.fastjson2_vo.ListField2;
import com.alibaba.fastjson2_vo.ListFinalField1;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListFieldTest {
    @Test
    public void test_0() throws Exception {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            Field field = ListField1.class.getField("v0000");
            FieldReader fieldWriter = creator.createFieldReader(
                    "value",
                    field.getGenericType(),
                    field);

            ObjectReader<ListField1> objectReader
                    = creator.createObjectReader(ListField1.class, fieldWriter);

            {
                ListField1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":[101,\"102\"]}"), 0);
                assertEquals(101, vo.v0000.get(0).intValue());
                assertEquals(102, vo.v0000.get(1).intValue());
            }
            {
                ListField1 vo = objectReader.readObject(
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
            Field field = ListFinalField1.class.getField("v0000");
            FieldReader fieldWriter = creator.createFieldReader(
                    "value",
                    field.getGenericType(),
                    field);

            ObjectReader<ListFinalField1> objectReader
                    = creator.createObjectReader(ListFinalField1.class, fieldWriter);

            {
                ListFinalField1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":[101,\"102\"]}"), 0);
                assertEquals(101, vo.v0000.get(0).intValue());
                assertEquals(102, vo.v0000.get(1).intValue());
            }
            {
                ListFinalField1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":null}"), 0);
                assertTrue(vo.v0000 == null || vo.v0000.isEmpty());
            }
        }
    }

    @Test
    public void test_2() {
        ListField2 vo = new ListField2();
        vo.v0000 = new ArrayList<>();
        vo.v0001 = new ArrayList<>();

        String str = JSON.toJSONString(vo);
        assertEquals("{\"v0000\":[],\"v0001\":[]}", str);
    }
}
