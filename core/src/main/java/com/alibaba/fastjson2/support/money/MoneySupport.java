package com.alibaba.fastjson2.support.money;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class MoneySupport {
    static Class CLASS_MONETARY;
    static Class CLASS_MONETARY_AMOUNT;
    static Class CLASS_MONETARY_AMOUNT_FACTORY;
    static Class CLASS_DEFAULT_NUMBER_VALUE;
    static Class CLASS_NUMBER_VALUE;
    static Class CLASS_CURRENCY_UNIT;

    static Method METHOD_MONETARY_AMOUNT_GET_CURRENCY;
    static Method METHOD_MONETARY_AMOUNT_GET_NUMBER;
    static Method METHOD_MONETARY_GET_DEFAULT_AMOUNTFACTORY;
    static Method METHOD_CLASS_MONETARY_AMOUNT_FACTORY_SET_CURRENCY;
    static Method METHOD_CLASS_MONETARY_AMOUNT_FACTORY_SET_NUMBER;
    static Method METHOD_CLASS_MONETARY_AMOUNT_FACTORY_CREATE;

    static Method METHOD_GET_CURRENCY;

    public static ObjectReader createCurrencyUnitReader() {
        if (CLASS_MONETARY == null) {
            CLASS_MONETARY = TypeUtils.loadClass("javax.money.Monetary");
        }

        if (CLASS_CURRENCY_UNIT == null) {
            CLASS_CURRENCY_UNIT = TypeUtils.loadClass("javax.money.CurrencyUnit");
        }

        if (METHOD_GET_CURRENCY == null) {
            try {
                METHOD_GET_CURRENCY = MoneySupport.CLASS_MONETARY.getMethod("getCurrency", String.class, String[].class);
            } catch (NoSuchMethodException e) {
                throw new JSONException("method not found : javax.money.Monetary.getCurrency", e);
            }
        }

        return ObjectReaderImplValue.of(CLASS_CURRENCY_UNIT, String.class, METHOD_GET_CURRENCY);
//        return new CurrencyUnitReader();
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

    static Method METHOD_NUMBER_VALUE_OF;

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

        if (METHOD_MONETARY_AMOUNT_GET_CURRENCY == null) {
            try {
                METHOD_MONETARY_AMOUNT_GET_CURRENCY = CLASS_MONETARY_AMOUNT.getMethod("getCurrency");
            } catch (NoSuchMethodException e) {
                throw new JSONException("method not found : javax.money.Monetary.getCurrency", e);
            }
        }

        if (METHOD_MONETARY_AMOUNT_GET_NUMBER == null) {
            try {
                METHOD_MONETARY_AMOUNT_GET_NUMBER = CLASS_MONETARY_AMOUNT.getMethod("getNumber");
            } catch (NoSuchMethodException e) {
                throw new JSONException("method not found : javax.money.Monetary.getCurrency", e);
            }
        }

        FieldWriter fieldWriter0 = ObjectWriterCreator.INSTANCE.createFieldWriter(
                CLASS_MONETARY,
                "currency",
                null,
                METHOD_MONETARY_AMOUNT_GET_CURRENCY);

        FieldWriter fieldWriter1 = ObjectWriterCreator.INSTANCE.createFieldWriter(
                CLASS_MONETARY,
                "number",
                null,
                METHOD_MONETARY_AMOUNT_GET_NUMBER);

        return new ObjectWriterAdapter(CLASS_MONETARY_AMOUNT, Arrays.asList(fieldWriter0, fieldWriter1));
    }

    public static ObjectWriter createNumberValueWriter() {
        return new NumberValueWriter();
    }

    public static Object createMonetaryAmount(Object currency, Object number) {
        if (CLASS_NUMBER_VALUE == null) {
            CLASS_NUMBER_VALUE = TypeUtils.loadClass("javax.money.NumberValue");
        }

        if (CLASS_CURRENCY_UNIT == null) {
            CLASS_CURRENCY_UNIT = TypeUtils.loadClass("javax.money.CurrencyUnit");
        }

        if (CLASS_MONETARY == null) {
            CLASS_MONETARY = TypeUtils.loadClass("javax.money.Monetary");
        }

        if (CLASS_MONETARY_AMOUNT_FACTORY == null) {
            CLASS_MONETARY_AMOUNT_FACTORY = TypeUtils.loadClass("javax.money.MonetaryAmountFactory");
        }

        if (METHOD_MONETARY_GET_DEFAULT_AMOUNTFACTORY == null) {
            try {
                METHOD_MONETARY_GET_DEFAULT_AMOUNTFACTORY = CLASS_MONETARY.getMethod("getDefaultAmountFactory");
            } catch (NoSuchMethodException e) {
                throw new JSONException("method not found : javax.money.Monetary.getDefaultAmountFactory", e);
            }
        }

        if (METHOD_CLASS_MONETARY_AMOUNT_FACTORY_SET_CURRENCY == null) {
            try {
                METHOD_CLASS_MONETARY_AMOUNT_FACTORY_SET_CURRENCY = CLASS_MONETARY_AMOUNT_FACTORY.getMethod("setCurrency", CLASS_CURRENCY_UNIT);
            } catch (NoSuchMethodException e) {
                throw new JSONException("method not found : \"javax.money.MonetaryAmountFactory.setCurrency", e);
            }
        }

        if (METHOD_CLASS_MONETARY_AMOUNT_FACTORY_SET_NUMBER == null) {
            try {
                METHOD_CLASS_MONETARY_AMOUNT_FACTORY_SET_NUMBER = CLASS_MONETARY_AMOUNT_FACTORY.getMethod("setNumber", Number.class);
            } catch (NoSuchMethodException e) {
                throw new JSONException("method not found : \"javax.money.MonetaryAmountFactory.setNumber", e);
            }
        }

        if (METHOD_CLASS_MONETARY_AMOUNT_FACTORY_CREATE == null) {
            try {
                METHOD_CLASS_MONETARY_AMOUNT_FACTORY_CREATE = CLASS_MONETARY_AMOUNT_FACTORY.getMethod("create");
            } catch (NoSuchMethodException e) {
                throw new JSONException("method not found : \"javax.money.MonetaryAmountFactory.create", e);
            }
        }

        Object factoryObject;
        try {
            factoryObject = METHOD_MONETARY_GET_DEFAULT_AMOUNTFACTORY.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new JSONException("numberValue error", e);
        }

        if (currency != null) {
            try {
                METHOD_CLASS_MONETARY_AMOUNT_FACTORY_SET_CURRENCY.invoke(factoryObject, currency);
            } catch (Exception e) {
                throw new JSONException("setCurrency error", e);
            }
        }

        if (number != null) {
            try {
                METHOD_CLASS_MONETARY_AMOUNT_FACTORY_SET_NUMBER.invoke(factoryObject, number);
            } catch (Exception e) {
                throw new JSONException("setCurrency error", e);
            }
        }

        try {
            return METHOD_CLASS_MONETARY_AMOUNT_FACTORY_CREATE.invoke(factoryObject);
        } catch (Exception e) {
            throw new JSONException("create error", e);
        }
    }
}
