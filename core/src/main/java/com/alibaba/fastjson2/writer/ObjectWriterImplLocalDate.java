package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.time.LocalDate;

final class ObjectWriterImplLocalDate extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplLocalDate INSTANCE = new ObjectWriterImplLocalDate();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeLocalDate((LocalDate) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        JSONWriter.Context ctx = jsonWriter.getContext();

        LocalDate date = (LocalDate) object;

        String dateFormat = ctx.getDateFormat();
        if (dateFormat == null) {
            jsonWriter.writeDateYYYMMDD10(
                    date.getYear()
                    , date.getMonthValue()
                    , date.getDayOfMonth());
        } else {
            String str = ctx.getDateFormatter().format(date);
            jsonWriter.writeString(str);
        }
    }
}
