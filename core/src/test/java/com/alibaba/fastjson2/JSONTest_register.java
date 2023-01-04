package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONTest_register {
    @Test
    public void test_register() {
        JSON.register(VO.class, new VOWriter());
        JSON.register(VO.class, new VOReader());

        VO vo = new VO(123, "DataWorks");
        String str = JSON.toJSONString(vo);
        assertEquals("{\"ID\":123,\"NAME\":\"DataWorks\"}", str);

        VO vo1 = JSON.parseObject(str, VO.class);
        assertEquals(vo.id, vo1.id);
        assertEquals(vo.name, vo1.name);
    }

    public static class VO {
        public int id;
        public String name;

        public VO(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class VOWriter
            implements ObjectWriter {
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            VO vo = (VO) object;
            jsonWriter.startObject();

            jsonWriter.writeName("ID");
            jsonWriter.writeColon();
            jsonWriter.writeInt32(vo.id);

            jsonWriter.writeName("NAME");
            jsonWriter.writeColon();
            jsonWriter.writeString(vo.name);

            jsonWriter.endObject();
        }
    }

    public static class VOReader
            implements ObjectReader<VO> {
        public VO readObject(JSONReader jsonReader, Type fieldType, Object fieldName1, long features) {
            jsonReader.nextIfObjectStart();

            int id = 0;
            String name = null;
            for (; ; ) {
                if (jsonReader.nextIfObjectEnd()) {
                    break;
                }
                String fieldName = jsonReader.readFieldName();
                switch (fieldName) {
                    case "ID":
                        id = jsonReader.readInt32Value();
                        break;
                    case "NAME":
                        name = jsonReader.readString();
                        break;
                    default:
                        jsonReader.skipValue();
                        break;
                }
            }

            return new VO(id, name);
        }
    }
}
