package com.alibaba.fastjson2.write;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PublicFieldTest {
    @Test
    public void test_default_writer() {
        VO vo = new VO();
        vo.id = 100;
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(VO.class);
        objectWriter.writeArrayMapping(jsonWriter, vo, null, null, 0);
        assertEquals("[100]", jsonWriter.toString());
    }

    @Test
    public void test_lambda_writer() {
        VO vo = new VO();
        vo.id = 100;
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        ObjectWriter objectWriter = TestUtils.createObjectWriterLambda(VO.class);
        objectWriter.writeArrayMapping(jsonWriter, vo, null, null, 0);
        assertEquals("[100]", jsonWriter.toString());
    }

    @Test
    public void test_asm_writer() {
        VO vo = new VO();
        vo.id = 100;
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        ObjectWriter objectWriter = TestUtils.WRITER_CREATOR.createObjectWriter(VO.class);
        objectWriter.writeArrayMapping(jsonWriter, vo, null, null, 0);
        assertEquals("[100]", jsonWriter.toString());
    }

    public static class VO {
        public int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
