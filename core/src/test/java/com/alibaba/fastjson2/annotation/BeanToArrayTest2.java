package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BeanToArrayTest2 {
    @Test
    public void test0() {
        Parent p = new Parent();
        VO vo = new VO();
        vo.id = 1001;
        vo.name = "DataWorks";
        p.value = vo;

        String str = JSON.toJSONString(p);
        assertEquals("{\"value\":[1001,\"DataWorks\"]}", str);

        Parent p2 = JSON.parseObject(str, Parent.class);
        VO vo2 = p2.value;
        assertEquals(vo.id, vo2.id);
        assertEquals(vo.name, vo2.name);
    }

    @Test
    public void test1() {
        ObjectReaderCreator[] readerCreators = TestUtils.readerCreators();
        ObjectWriterCreator[] writerCreators = TestUtils.writerCreators();

        Parent p = new Parent();
        VO vo = new VO();
        vo.id = 1001;
        vo.name = "DataWorks";
        p.value = vo;

        for (ObjectWriterCreator writerCreator : writerCreators) {
            ObjectWriter objectWriter = writerCreator.createObjectWriter(Parent.class);
            JSONWriter jsonWriter = JSONWriter.of();
            objectWriter.write(jsonWriter, p, null, null, 0);
            String str = jsonWriter.toString();
            assertEquals("{\"value\":[1001,\"DataWorks\"]}", str);
        }

        String str = JSON.toJSONString(p);
        for (ObjectReaderCreator readerCreator : readerCreators) {
            ObjectReader<Parent> objectReader = readerCreator.createObjectReader(Parent.class);
            Parent p2 = objectReader.readObject(JSONReader.of(str));
            VO vo2 = p2.value;
            assertEquals(vo.id, vo2.id);
            assertEquals(vo.name, vo2.name);
        }
    }

    public static class VO {
        public int id;
        public String name;
    }

    public static class Parent {
        @JSONField(deserializeFeatures = JSONReader.Feature.SupportArrayToBean, serializeFeatures = JSONWriter.Feature.BeanToArray)
        public VO value;
    }
}
