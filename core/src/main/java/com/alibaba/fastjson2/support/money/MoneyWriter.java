package com.alibaba.fastjson2.support.money;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.support.LambdaMiscCodec;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.alibaba.fastjson2.support.money.MoneySupport.*;

public class MoneyWriter {
    public static ObjectWriter createMonetaryAmountWriter() {
        if (CLASS_MONETARY == null) {
            CLASS_MONETARY = TypeUtils.loadClass("javax.money.Monetary");
        }

        if (CLASS_MONETARY_AMOUNT == null) {
            CLASS_MONETARY_AMOUNT = TypeUtils.loadClass("javax.money.MonetaryAmount");
        }

        if (CLASS_NUMBER_VALUE == null) {
            CLASS_NUMBER_VALUE = TypeUtils.loadClass("javax.money.NumberValue");
        }

        if (CLASS_CURRENCY_UNIT == null) {
            CLASS_CURRENCY_UNIT = TypeUtils.loadClass("javax.money.CurrencyUnit");
        }

        Function<Object, Object> FUNC_GET_CURRENCY;
        try {
            FUNC_GET_CURRENCY = LambdaMiscCodec.createFunction(
                    CLASS_MONETARY_AMOUNT.getMethod("getCurrency")
            );
        } catch (Throwable e) {
            throw new JSONException("method not found : javax.money.Monetary.getCurrency", e);
        }

        Function<Object, Object> FUNC_GET_NUMBER;
        try {
            FUNC_GET_NUMBER = LambdaMiscCodec.createFunction(
                    CLASS_MONETARY_AMOUNT.getMethod("getNumber")
            );
        } catch (Throwable e) {
            throw new JSONException("method not found : javax.money.Monetary.getNumber", e);
        }

        FieldWriter fieldWriter0 = ObjectWriterCreator.INSTANCE.createFieldWriter(
                "currency",
                CLASS_CURRENCY_UNIT,
                CLASS_CURRENCY_UNIT,
                FUNC_GET_CURRENCY
        );

        FieldWriter fieldWriter1 = ObjectWriterCreator.INSTANCE.createFieldWriter(
                "number",
                CLASS_NUMBER_VALUE,
                CLASS_NUMBER_VALUE,
                FUNC_GET_NUMBER);

        return new ObjectWriterAdapter(CLASS_MONETARY_AMOUNT, null, null, 0, Arrays.asList(fieldWriter0, fieldWriter1));
    }

    public static ObjectWriter createNumberValueWriter() {
        if (CLASS_NUMBER_VALUE == null) {
            CLASS_NUMBER_VALUE = TypeUtils.loadClass("javax.money.NumberValue");
        }

        if (FUNC_NUMBER_VALUE == null) {
            try {
                BiFunction<Object, Class, Number> biFunctionNumberValue = LambdaMiscCodec.createBiFunction(
                        CLASS_NUMBER_VALUE.getMethod("numberValue", Class.class)
                );
                FUNC_NUMBER_VALUE = o -> (BigDecimal) biFunctionNumberValue.apply(o, BigDecimal.class);
            } catch (Throwable e) {
                throw new JSONException("method not found : javax.money.NumberValue.numberValue", e);
            }
        }
        return ObjectWriters.ofToBigDecimal(FUNC_NUMBER_VALUE);
    }
}
