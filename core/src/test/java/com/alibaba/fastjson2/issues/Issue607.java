package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReaderBean;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue607 {
    @Test
    public void test() throws Exception {
        Bean bean = new Bean();
        bean.setPName("test");
        String json = JSON.toJSONString(bean);
        assertEquals("{\"PName\":\"test\"}", json);
        Bean bean1 = JSON.parseObject(json, Bean.class);
        assertEquals(bean.pName, bean1.pName);
    }

    @Data
    public class Bean {
        private String pName;
    }

    @Test
    public void test1() throws Exception {
        Bean1 bean = new Bean1();
        bean.setPName("test");
        String json = JSON.toJSONString(bean);
        assertEquals("{\"pName\":\"test\"}", json);
        Bean1 bean1 = JSON.parseObject(json, Bean1.class);
        assertEquals(bean.pName, bean1.pName);
    }

    @Test
    public void test1_x() throws Exception {
        Bean1 bean = new Bean1();
        bean.setPName("test");

        String expected = "{\"pName\":\"test\"}";
        for (ObjectWriterCreator creator : TestUtils.writerCreators()) {
            ObjectWriter objectWriter = creator.createObjectWriter(Bean1.class);

            assertEquals(1, objectWriter.getFieldWriters().size());

            JSONWriter jsonWriter = JSONWriter.of();
            objectWriter.write(jsonWriter, bean);
            String json = jsonWriter.toString();
            assertEquals(expected, json);
        }

        for (ObjectReaderCreator creator : TestUtils.readerCreators()) {
            ObjectReaderBean<Bean1> objectReader = (ObjectReaderBean<Bean1>) creator.createObjectReader(Bean1.class);
            JSONReader jsonReader = JSONReader.of(expected);
            Bean1 bean1 = objectReader.readObject(jsonReader, 0);
            assertEquals(bean.pName, bean1.pName);
        }
    }

    @Data
    public class Bean1 {
        public String pName;
    }
}
