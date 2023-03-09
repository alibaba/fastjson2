package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1204 {
    static final ObjectWriterProvider provider = new ObjectWriterProvider();

    @BeforeEach
    void setup() {
        provider.register(BigDecimal.class, new ObjectWriter<BigDecimal>() {
            @Override
            public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
                BigDecimal decimal = (BigDecimal) object;
                if (decimal.scale() == 0) {
                    decimal = decimal.setScale(1);
                }
                jsonWriter.writeDecimal(decimal);
            }
        });
    }

    @Test
    public void test() throws Exception {
        BigDecimal decimal = BigDecimal.valueOf(Long.MAX_VALUE).add(BigDecimal.ONE);
        JSONWriter.Context context = JSONFactory.createWriteContext(provider);
        String str = JSON.toJSONString(decimal, context);
        assertEquals("9223372036854775808.0", str);
        BigDecimal decimal1 = (BigDecimal) JSON.parse(str);
        assertEquals(decimal, decimal1.stripTrailingZeros());
    }
}
