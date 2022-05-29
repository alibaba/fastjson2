package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2_vo.Int100;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Int100Test {
    @Test
    public void test_jsonpath() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            JSONReader.Context readContext
                    = new JSONReader.Context(
                            new ObjectReaderProvider(creator));
            {
                Int100 vo = new Int100();

                JSONPath jsonPath = JSONPath
                        .of("$.v0000")
                        .setReaderContext(readContext);
                jsonPath.set(vo, 101);
                assertEquals(101, vo.getV0000());
                jsonPath.set(vo, 102L);
                assertEquals(102, vo.getV0000());
                jsonPath.set(vo, null);
                assertEquals(0, vo.getV0000());
                jsonPath.set(vo, "103");
                assertEquals(103, vo.getV0000());
                assertEquals(103, jsonPath.eval(vo));

                jsonPath.setInt(vo, 101);
                assertEquals(101, vo.getV0000());
                jsonPath.setLong(vo, 102L);
                assertEquals(102, vo.getV0000());
            }

            Int100 vo2 = new Int100();
            Object[] array = new Object[]{vo2};
            {
                JSONPath jsonPath = JSONPath
                        .of("$[0].v0000")
                        .setReaderContext(readContext);
                jsonPath.set(array, 101);
                assertEquals(101, vo2.getV0000());

                jsonPath.set(array, "102");
                assertEquals(102, vo2.getV0000());

                jsonPath.setInt(array, 103);
                assertEquals(103, vo2.getV0000());

                jsonPath.setLong(array, 104);
                assertEquals(104, vo2.getV0000());
            }

            {
                JSONPath jsonPath = JSONPath.of("$[0].*");
                java.util.List eval = (java.util.List) jsonPath.eval(array);
                assertEquals(100, eval.size());
            }
            {
                JSONPath jsonPath = JSONPath.of("$.*");
                java.util.List eval = (java.util.List) jsonPath.eval(vo2);
                assertEquals(100, eval.size());
            }
            {
                JSONPath jsonPath = JSONPath.of("$['v0000','v0000']");
                java.util.List eval = (java.util.List) jsonPath.eval(array);
                assertEquals(2, eval.size());
            }
        }
    }
}
