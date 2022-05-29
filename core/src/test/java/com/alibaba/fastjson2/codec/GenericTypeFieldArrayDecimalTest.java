package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenericTypeFieldArrayDecimalTest {
    @Test
    public void testRead_0() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<P0> objectReader = creator.createObjectReader(P0.class);

            {
                JSONReader jsonReader = JSONReader.of("{\"value\":[101]}");
                P0 vo = objectReader.readObject(jsonReader, 0);
                assertEquals(BigDecimal.valueOf(101), vo.value[0]);
            }
            {
                byte[] jsonbBytes = JSONB.toBytes(
                        Collections.singletonMap("value", Collections.singletonList(101))
                );
                JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
                P0 vo = objectReader.readObject(jsonReader, 0);
                assertEquals(BigDecimal.valueOf(101), vo.value[0]);
            }
        }
    }

    @Test
    public void testRead1() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<P1> objectReader = creator.createObjectReader(P1.class);

            JSONReader jsonReader = JSONReader.of("{\"value\":[101]}");
            P1 vo = objectReader.readObject(jsonReader, 0);
            assertEquals(BigDecimal.valueOf(101), vo.value[0]);
        }
    }

    @Test
    public void testRead2() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<P2> objectReader = creator.createObjectReader(P2.class);

            JSONReader jsonReader = JSONReader.of("{\"value\":[101]}");
            P2 vo = objectReader.readObject(jsonReader, 0);
            assertEquals(BigDecimal.valueOf(101), vo.value[0]);
        }
    }

    @Test
    public void testRead21() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<P21> objectReader = creator.createObjectReader(P21.class);

            JSONReader jsonReader = JSONReader.of("{\"value\":[101]}");
            P21 vo = objectReader.readObject(jsonReader, 0);
            assertEquals(BigDecimal.valueOf(101), vo.value[0]);
        }
    }

    @Test
    public void testRead311() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<P311> objectReader = creator.createObjectReader(P311.class);

            JSONReader jsonReader = JSONReader.of("{\"value\":[101]}");
            P311 vo = objectReader.readObject(jsonReader, 0);
            assertEquals(BigDecimal.valueOf(101), vo.value[0]);
        }
    }

    @Test
    public void testRead31() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            Type objectType = new TypeReference<P31<BigDecimal>>() {
            }.getType();
            ObjectReader<P31> objectReader = creator.createObjectReader(objectType);

            {
                JSONReader jsonReader = JSONReader.of("{\"value\":[101]}");
                P31 vo = objectReader.readObject(jsonReader, 0);
                assertEquals(BigDecimal.valueOf(101), vo.value[0]);
            }
            {
                byte[] jsonbBytes = JSONB.toBytes(
                        Collections.singletonMap("value", Collections.singletonList(101))
                );
                JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
                P31 vo = objectReader.readObject(jsonReader, 0);
                assertEquals(BigDecimal.valueOf(101), vo.value[0]);
            }
        }
    }

    public static class P<T> {
        public T[] value;
    }

    public static class P0<T extends BigDecimal> {
        public T[] value;
    }

    public static class P1
            extends P<BigDecimal> {
    }

    public static class P2<T extends BigDecimal>
            extends P<T> {
    }

    public static class P21<T extends BigDecimal>
            extends P2<T> {
    }

    public static class P3<T>
            extends P<T> {
    }

    public static class P31<T>
            extends P3<T> {
    }

    public static class P311<T extends BigDecimal>
            extends P31<T> {
    }
}
