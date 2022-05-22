package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.DateField1;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateField1Test {
    private TimeZone defaultTimeZone;

    @BeforeEach
    public void before() {
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    @AfterEach
    public void after() {
        TimeZone.setDefault(defaultTimeZone);
    }

    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<DateField1> objectWriter
                    = creator.createObjectWriter(DateField1.class);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                DateField1 vo = new DateField1();
                vo.date = new Date(0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[\"1970-01-01 08:00:00\"]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                jsonWriter.getContext().setDateFormat("millis");
                DateField1 vo = new DateField1();
                vo.date = new Date(0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[0]",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_read_0() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<DateField1> objectReader
                    = creator.createObjectReader(DateField1.class);

            {
                DateField1 vo = objectReader.readObject(JSONReader.of("{\"date\":0}"), 0);
                assertEquals(0, vo.date.getTime());
            }
            {
                DateField1 vo = objectReader.readObject(JSONReader.of("{\"date\":1}"), 0);
                assertEquals(1, vo.date.getTime());
            }
            {
                DateField1 vo = objectReader.readObject(JSONReader.of("{\"date\":null}"), 0);
                assertEquals(null, vo.date);
            }
        }
    }

    @Test
    public void test_null() throws Exception {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            FieldWriter fieldWriter = creator.createFieldWriter("date", "millis", DateField1.class.getField("date"));
            ObjectWriter<DateField1> objectWriter
                    = creator.createObjectWriter(fieldWriter);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                DateField1 vo = new DateField1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                DateField1 vo = new DateField1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                DateField1 vo = new DateField1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"date\":null}",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_millis() throws Exception {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            FieldWriter fieldWriter = creator.createFieldWriter("date", "millis", DateField1.class.getField("date"));
            ObjectWriter<DateField1> objectWriter
                    = creator.createObjectWriter(fieldWriter);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                DateField1 vo = new DateField1();
                vo.date = new Date(0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[0]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                DateField1 vo = new DateField1();
                vo.date = new Date(0);
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"date\":0}",
                        jsonWriter.toString());
            }
        }
    }
}
