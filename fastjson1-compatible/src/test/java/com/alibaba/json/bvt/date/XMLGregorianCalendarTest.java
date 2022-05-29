package com.alibaba.json.bvt.date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XMLGregorianCalendarTest {
    @Test
    public void test_for_issue() throws Exception {
        GregorianCalendar gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();

        javax.xml.datatype.XMLGregorianCalendar calendar = javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);

        String text = JSON.toJSONString(calendar);
        assertEquals(Long.toString(gregorianCalendar.getTimeInMillis()), text);

        javax.xml.datatype.XMLGregorianCalendar calendar1 = JSON.parseObject(text, javax.xml.datatype.XMLGregorianCalendar.class);

        assertEquals(calendar.toGregorianCalendar().getTimeInMillis(), calendar1.toGregorianCalendar().getTimeInMillis());

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("calendar", calendar);

        String json = JSON.toJSONString(jsonObject);

        Model model = JSON.parseObject(json).toJavaObject(Model.class);

        assertEquals(calendar.toGregorianCalendar().getTimeInMillis(), model.calendar.toGregorianCalendar().getTimeInMillis());
    }

    public static class Model {
        public javax.xml.datatype.XMLGregorianCalendar calendar;
    }
}
