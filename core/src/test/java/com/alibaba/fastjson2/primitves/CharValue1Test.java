package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.CharValue1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CharValue1Test {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<CharValue1> objectWriter = creator.createObjectWriter(CharValue1.class);

            CharValue1 vo = new CharValue1();
            vo.setV0000('A');
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

    @Test
    public void test_chars() {
        char[] chars = {'\0', 'a', 'A', '0', '中', '\\', '"', '©', '®', '¼', '½', '¾'};
        for (int i = 0; i < chars.length; i++) {
            JSONWriter[] jsonWriters = TestUtils.createJSONWriters();
            for (JSONWriter jsonWriter : jsonWriters) {
                char ch = chars[i];
                jsonWriter.writeChar(ch);
                String str = jsonWriter.toString();
                String str1 = (String) JSON.parse(str);
                assertEquals(1, str1.length());
                assertEquals(ch, str1.charAt(0));
            }
        }

        for (int i = 0; i < chars.length; i++) {
            JSONWriter jsonWriter = JSONWriter.ofJSONB();
            char ch = chars[i];
            jsonWriter.writeChar(ch);
            jsonWriter.writeChar(ch);
            byte[] bytes = jsonWriter.getBytes();
            Character character = (Character) JSONB.parse(bytes);
            assertEquals(ch, character.charValue());
        }
    }
}
