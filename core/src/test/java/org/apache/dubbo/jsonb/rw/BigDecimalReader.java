package org.apache.dubbo.jsonb.rw;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * 反序列化的时候去除值BigDecimal中的逗号(",")<br/>
 * https://alibaba.github.io/fastjson2/register_custom_reader_writer_cn
 */
public class BigDecimalReader
        implements ObjectReader<BigDecimal> {
    public static final BigDecimalReader INSTANCE = new BigDecimalReader();

    @Override
    public BigDecimal readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        String value = jsonReader.readString();
        if (null == value || value.length() < 1) {
            return null;
        }

        try {
            return BigDecimalUtil.castToBigDecimal(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("数值" + value + "非法,格式有误！");
        }
    }

    @Override
    public BigDecimal readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return readObject(jsonReader, fieldType, fieldName, features);
    }
}
