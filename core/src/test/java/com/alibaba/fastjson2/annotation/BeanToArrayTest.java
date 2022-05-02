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

public class BeanToArrayTest {
    @Test
    public void test0() {
        VO vo = new VO();
        vo.id = 1001;
        vo.name = "DataWorks";

        String str = JSON.toJSONString(vo);
        assertEquals("[1001,\"DataWorks\"]", str);

        VO vo2 = JSON.parseObject(str, VO.class);
        assertEquals(vo.id, vo2.id);
        assertEquals(vo.name, vo2.name);
    }

    @Test
    public void test1() {
        ObjectReaderCreator[] readerCreators = TestUtils.readerCreators();

        ObjectWriterCreator[] writerCreators = TestUtils.writerCreators();

        VO vo = new VO();
        vo.id = 1001;
        vo.name = "DataWorks";

        for (ObjectWriterCreator writerCreator : writerCreators) {
            ObjectWriter objectWriter = writerCreator.createObjectWriter(VO.class);
            JSONWriter jsonWriter = JSONWriter.of();
            objectWriter.write(jsonWriter, vo, null, null, 0);
            String str = jsonWriter.toString();
            assertEquals("[1001,\"DataWorks\"]", str);
        }

        String str = JSON.toJSONString(vo);
        for (ObjectReaderCreator readerCreator : readerCreators) {
            ObjectReader<VO> objectReader = readerCreator.createObjectReader(VO.class);
            VO vo2 = objectReader.readObject(JSONReader.of(str));
            assertEquals(vo.id, vo2.id);
            assertEquals(vo.name, vo2.name);
        }
    }

    @JSONType(deserializeFeatures = JSONReader.Feature.SupportArrayToBean, serializeFeatures = JSONWriter.Feature.BeanToArray)
    public static class VO {
        public int id;
        public String name;
    }
}
