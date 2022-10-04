package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONFieldTest3 {
    @Test
    public void test_deciaml_BrowserCompatible() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        BigDecimal[] values = new BigDecimal[]{null, BigDecimal.TEN, new BigDecimal("90071992547409910")};
        VO vo = new VO();
        vo.value = new BigDecimal("90071992547409910");

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<VO> objectWriter = creator
                    .createObjectWriter(
                            VO.class, 0, JSONFactory.getDefaultObjectWriterProvider());
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                objectWriter.write(jsonWriter, vo, null, null, 0);

                assertEquals("{\"value\":\"90071992547409910\"}", jsonWriter.toString());
            }

            for (BigDecimal value : values) {
                VO v = new VO();
                v.value = value;

                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                objectWriter.write(jsonWriter, v, null, null, 0);
                byte[] utf8Bytes = jsonWriter.getBytes();
                assertEquals(v.value, JSON.parseObject(utf8Bytes, VO.class).value);
            }

            for (BigDecimal value : values) {
                VO v = new VO();
                v.value = value;

                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                objectWriter.write(jsonWriter, v, null, null, 0);
                byte[] jsonbBytes = jsonWriter.getBytes();
                assertEquals(v.value, JSONB.parseObject(jsonbBytes, VO.class).value);
            }
        }
    }

    public static class VO {
        @JSONField(serializeFeatures = JSONWriter.Feature.BrowserCompatible)
        public BigDecimal value;
    }
}
