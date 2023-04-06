package com.alibaba.fastjson2.support.money;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.alibaba.fastjson2.support.money.MoneySupport.*;
import static com.alibaba.fastjson2.util.TypeUtils.METHOD_TYPE_BI_FUNCTION;
import static com.alibaba.fastjson2.util.TypeUtils.METHOD_TYPE_OBJECT_OBJECT_OBJECT;

public class MoneyReader {
    public static ObjectReader createCurrencyUnitReader() {
        if (CLASS_MONETARY == null) {
            CLASS_MONETARY = TypeUtils.loadClass("javax.money.Monetary");
        }

        if (CLASS_CURRENCY_UNIT == null) {
            CLASS_CURRENCY_UNIT = TypeUtils.loadClass("javax.money.CurrencyUnit");
        }

        if (FUNC_GET_CURRENCY == null) {
            MethodHandles.Lookup lookup = JDKUtils.trustedLookup(CLASS_MONETARY);
            try {
                MethodHandle methodHandle = lookup.findStatic(
                        CLASS_MONETARY,
                        "getCurrency",
                        MethodType.methodType(CLASS_CURRENCY_UNIT, String.class, String[].class)
                );

                CallSite callSite = LambdaMetafactory.metafactory(
                        lookup,
                        "apply",
                        METHOD_TYPE_BI_FUNCTION,
                        METHOD_TYPE_OBJECT_OBJECT_OBJECT,
                        methodHandle,
                        MethodType.methodType(CLASS_CURRENCY_UNIT, String.class, String[].class)
                );
                MethodHandle target = callSite.getTarget();
                BiFunction<String, String[], Object> biFunctionGetCurrency = (BiFunction<String, String[], Object>) target.invokeExact();
                FUNC_GET_CURRENCY = s -> biFunctionGetCurrency.apply(s, new String[0]);
            } catch (Throwable e) {
                throw new JSONException("method not found : javax.money.Monetary.getCurrency", e);
            }
        }

        return ObjectReaderImplValue.of(CLASS_CURRENCY_UNIT, String.class, FUNC_GET_CURRENCY);
    }

    public static ObjectReader createMonetaryAmountReader() {
        if (CLASS_NUMBER_VALUE == null) {
            CLASS_NUMBER_VALUE = TypeUtils.loadClass("javax.money.NumberValue");
        }

        if (CLASS_CURRENCY_UNIT == null) {
            CLASS_CURRENCY_UNIT = TypeUtils.loadClass("javax.money.CurrencyUnit");
        }

        try {
            Method factoryMethod = MoneySupport.class.getMethod("createMonetaryAmount", Object.class, Object.class);
            String[] paramNames = {"currency", "number"};
            Function<Map<Long, Object>, Object> factoryFunction = ObjectReaderCreator.INSTANCE.createFactoryFunction(factoryMethod, paramNames);

            FieldReader fieldReader0 = ObjectReaderCreator.INSTANCE.createFieldReaderParam(
                    MoneySupport.class, MoneySupport.class, "currency", 0, 0, null, CLASS_CURRENCY_UNIT, CLASS_CURRENCY_UNIT, "currency", null, null, null
            );
            FieldReader fieldReader1 = ObjectReaderCreator.INSTANCE.createFieldReaderParam(
                    MoneySupport.class, MoneySupport.class, "number", 0, 0, null, CLASS_DEFAULT_NUMBER_VALUE, CLASS_DEFAULT_NUMBER_VALUE, "number", null, null, null
            );

            FieldReader[] fieldReaders = {fieldReader0, fieldReader1};
            return new ObjectReaderNoneDefaultConstructor(
                    null,
                    null,
                    null,
                    0,
                    factoryFunction,
                    null,
                    paramNames,
                    fieldReaders,
                    null,
                    null,
                    null
            );
        } catch (NoSuchMethodException e) {
            throw new JSONException("createMonetaryAmountReader error", e);
        }
    }

    public static ObjectReader createNumberValueReader() {
        if (CLASS_DEFAULT_NUMBER_VALUE == null) {
            CLASS_DEFAULT_NUMBER_VALUE = TypeUtils.loadClass("org.javamoney.moneta.spi.DefaultNumberValue");
        }

        if (METHOD_NUMBER_VALUE_OF == null) {
            try {
                METHOD_NUMBER_VALUE_OF = CLASS_DEFAULT_NUMBER_VALUE.getMethod("of", Number.class);
            } catch (NoSuchMethodException e) {
                throw new JSONException("method not found : org.javamoney.moneta.spi.DefaultNumberValue.of", e);
            }
        }

        if (CLASS_NUMBER_VALUE == null) {
            CLASS_NUMBER_VALUE = TypeUtils.loadClass("javax.money.NumberValue");
        }

        return ObjectReaderImplValue.of(CLASS_NUMBER_VALUE, BigDecimal.class, METHOD_NUMBER_VALUE_OF);
    }
}
