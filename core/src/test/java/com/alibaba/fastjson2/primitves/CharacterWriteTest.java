package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.CharValue1;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CharacterWriteTest {
    @Test
    public void test_array_char() {
        char[] array = new char[]{'a', 'b', 'c'};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("\"abc\"", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_Character() {
        Character[] array = new Character[]{'a', 'b', 'c'};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[\"a\",\"b\",\"c\"]", str);
    }

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
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":\"A\"}", jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[\"A\"]", jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_read() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        String json = "{\"v0000\":\"A\"}";

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<CharValue1> objectReader = creator.createObjectReader(CharValue1.class);

            {
                JSONReader jsonReader = JSONReader.of(json);
                CharValue1 vo = objectReader.readObject(jsonReader, 0);
                assertEquals('A', vo.getV0000());
            }
        }
    }
}
