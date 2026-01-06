package org.apache.dubbo.jsonb.rw;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * 格式化BigDecimal 添加千分位
 */
public class BigDecimalWriter
        implements ObjectWriter<BigDecimal> {
    public static final BigDecimalWriter INSTANCE = new BigDecimalWriter();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        if (object instanceof BigDecimal) {
            jsonWriter.writeString(BigDecimalUtil.formatDecimal((BigDecimal) object));
        } else {
            jsonWriter.writeAny(object);
        }
    }

    @Override
    public void writeJSONB(JSONWriterJSONB jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeDecimal((BigDecimal) object, features);
    }
}
