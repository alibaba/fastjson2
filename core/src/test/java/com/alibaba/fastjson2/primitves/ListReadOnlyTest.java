package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2_vo.ListReadOnly1;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListReadOnlyTest {
    @Test
    public void test_readOnly() throws Exception {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            FieldReader fieldWriter = creator.createFieldReader(
                    ListReadOnly1.class,
                    "value",
                    List.class,
                    List.class,
                    ListReadOnly1.class.getMethod("getValue"));
            ObjectReader<ListReadOnly1> objectReader
                    = creator.createObjectReader(ListReadOnly1.class, fieldWriter);

            {
                ListReadOnly1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":[101]}"), 0);
                assertEquals(101, vo.getValue().get(0));
            }
            {
                ListReadOnly1 vo = objectReader.readObject(
                        JSONReader
                                .of("{\"value\":null}"), 0);
                assertEquals(0, vo.getValue().size());
            }
        }
    }
}
