package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONFieldTest4 {
    @Test
    public void test_2() {
        ObjectReaderCreator[] readerCreators = TestUtils.readerCreators();

        ObjectWriterCreator[] writerCreators = TestUtils.writerCreators();

        V2 o = new V2();
        o.v1 = 1001;
        o.v2 = 1002;

        String objectJSON = "{\"v2\":1002,\"v1\":1001}";
        String arrayJSON = "[1002,1001]";

        for (ObjectWriterCreator creator : writerCreators) {
            ObjectWriter objectWriter = creator.createObjectWriter(V2.class);
            {
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, o, null, null, 0);
                String json = jsonWriter.toString();
                assertEquals(objectJSON, json);
            }
            {
                JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, o, null, null, 0);
                String json = jsonWriter.toString();
                assertEquals(arrayJSON, json);
            }
        }

        for (ObjectReaderCreator creator : readerCreators) {
            ObjectReader<V2> objectReader = creator.createObjectReader(V2.class);
            {
                JSONReader jsonReader = JSONReader.of(objectJSON);
                V2 o2 = objectReader.readObject(jsonReader);
                assertEquals(o.v1, o2.v1);
                assertEquals(o.v2, o2.v2);
            }
            {
                JSONReader jsonReader = JSONReader.of(arrayJSON);
                jsonReader.getContext().config(JSONReader.Feature.SupportArrayToBean);
                V2 o2 = objectReader.readObject(jsonReader);
                assertEquals(o.v1, o2.v1);
                assertEquals(o.v2, o2.v2);
            }
        }
    }

    public static class V2 {
        @JSONField(ordinal = 2)
        public int v1;

        @JSONField(ordinal = 1)
        public int v2;
    }
}
