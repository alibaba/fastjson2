package com.alibaba.fastjson2.support.money;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import javax.money.NumberValue;
import java.lang.reflect.Type;
import java.math.BigDecimal;

public class NumberValueWriter implements ObjectWriter {

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        NumberValue value = (NumberValue) object;
        BigDecimal decimal = value.numberValue(BigDecimal.class);
        jsonWriter.writeDecimal(decimal);
    }
}
