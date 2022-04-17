package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorLambda;
import com.alibaba.fastjson2_vo.CharValueField1;
import org.junit.jupiter.api.Test;

import static junit.framework.TestCase.assertEquals;

public class CharValueField1Test {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = new ObjectWriterCreator[] {
                ObjectWriterCreator.INSTANCE,
                ObjectWriterCreatorLambda.INSTANCE,
                ObjectWriterCreatorASM.INSTANCE
        };

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<CharValueField1> objectWriter = creator.createObjectWriter(CharValueField1.class);

            CharValueField1 vo = new CharValueField1();
            vo.v0000 = 'A';
            {
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":\"A\"}", jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[\"A\"]", jsonWriter.toString());
            }
        }
    }
}
