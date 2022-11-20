package com.alibaba.fastjson2.csv;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVTest0 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 1001;
        bean.name = "DataWorks";

        JSONWriter jsonWriter = JSONWriter.ofCSV();
        jsonWriter.writeAny(bean);

        String csv = jsonWriter.toString();
        assertEquals("1001,DataWorks\n", csv);

        JSONReader jsonReader = JSONReader.ofCSV(csv);
        ObjectReader objectReader = jsonReader.getContext().getObjectReader(Bean.class);
        Bean bean1 = (Bean) objectReader.readFromCSV(jsonReader, null, null, 0L);
        assertEquals(bean.id, bean1.id);
        assertEquals(bean.name, bean1.name);
    }

    @Test
    public void testASCII() {
        Bean bean = new Bean();
        bean.id = 1001;
        bean.name = "DataWorks";

        JSONWriter jsonWriter = JSONWriter.ofCSV();
        jsonWriter.writeAny(bean);

        String csv = jsonWriter.toString();
        assertEquals("1001,DataWorks\n", csv);

        byte[] bytes = csv.getBytes(StandardCharsets.US_ASCII);

        JSONReader jsonReader = JSONReader.ofCSV(bytes, 0, bytes.length);
        ObjectReader objectReader = jsonReader.getContext().getObjectReader(Bean.class);
        Bean bean1 = (Bean) objectReader.readFromCSV(jsonReader, null, null, 0L);
        assertEquals(bean.id, bean1.id);
        assertEquals(bean.name, bean1.name);
    }

    @Test
    public void testChars() {
        Bean bean = new Bean();
        bean.id = 1001;
        bean.name = "DataWorks";

        JSONWriter jsonWriter = JSONWriter.ofCSV();
        jsonWriter.writeAny(bean);

        String csv = jsonWriter.toString();
        assertEquals("1001,DataWorks\n", csv);

        char[] chars = csv.toCharArray();

        JSONReader jsonReader = JSONReader.ofCSV(chars, 0, chars.length);
        ObjectReader objectReader = jsonReader.getContext().getObjectReader(Bean.class);
        Bean bean1 = (Bean) objectReader.readFromCSV(jsonReader, null, null, 0L);
        assertEquals(bean.id, bean1.id);
        assertEquals(bean.name, bean1.name);
    }

    @Test
    public void test1() {
        Bean[] beans = new Bean[2];
        {
            Bean bean = new Bean();
            bean.id = 1001;
            bean.name = "DataWorks";
            beans[0] = bean;
        }
        {
            Bean bean = new Bean();
            bean.id = 1002;
            bean.name = "MaxCompute";
            beans[1] = bean;
        }

        JSONWriter jsonWriter = JSONWriter.ofCSV();
        for (Bean bean : beans) {
            jsonWriter.writeAny(bean);
        }

        String csv = jsonWriter.toString();
        assertEquals("1001,DataWorks\n" +
                "1002,MaxCompute\n", csv);

        JSONReader jsonReader = JSONReader.ofCSV(csv);
        ObjectReader objectReader = jsonReader.getContext().getObjectReader(Bean.class);
        Bean bean0 = (Bean) objectReader.readFromCSV(jsonReader, null, null, 0L);
        Bean bean1 = (Bean) objectReader.readFromCSV(jsonReader, null, null, 0L);

        Bean[] beans1 = {bean0, bean1};
        for (int i = 0; i < beans.length; i++) {
            assertEquals(beans[i].id, beans1[i].id);
            assertEquals(beans[i].name, beans1[i].name);
        }
    }

    public static class Bean {
        public int id;
        public String name;
    }
}
