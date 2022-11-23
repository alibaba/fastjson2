package com.alibaba.fastjson2.csv;

import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVTest1 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 1001;
        bean.name = "DataWorks";

        JSONWriter jsonWriter = JSONWriter.ofCSV();
        jsonWriter.writeAny(bean);

        String csv = jsonWriter.toString();
        assertEquals("1001,DataWorks\n", csv);

        CSVParser parser = CSVParser.of(csv, Bean.class);

        Bean bean1 = parser.readLoneObject();
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

        CSVParser parser = CSVParser.of(csv, Bean.class);
        Bean bean0 = parser.readLoneObject();
        Bean bean1 = parser.readLoneObject();

        Bean[] beans1 = {bean0, bean1};
        for (int i = 0; i < beans.length; i++) {
            assertEquals(beans[i].id, beans1[i].id);
            assertEquals(beans[i].name, beans1[i].name);
        }
    }

    public static class Bean {
        public long id;
        public String name;
    }
}
