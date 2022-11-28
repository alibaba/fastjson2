package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class JSONPathFilter
        extends JSONPathSegment
        implements JSONPathSegment.EvalSegment {
    abstract boolean apply(JSONPath.Context context, Object object);

    enum Operator {
        EQ,
        NE,
        GT,
        GE,
        LT,
        LE,
        LIKE,
        NOT_LIKE,
        RLIKE,
        NOT_RLIKE,
        IN,
        NOT_IN,
        BETWEEN,
        NOT_BETWEEN,
        AND,
        OR,
        REG_MATCH,
        STARTS_WITH,
        ENDS_WITH,
        CONTAINS,
        NOT_CONTAINS
    }

    static final class NameIntOpSegment
            extends NameFilter {
        final Operator operator;
        final long value;

        public NameIntOpSegment(
                String name,
                long nameHashCode,
                String[] fieldName2,
                long[] fieldNameNameHash2,
                Function expr,
                Operator operator,
                long value) {
            super(name, nameHashCode, fieldName2, fieldNameNameHash2, expr);
            this.operator = operator;
            this.value = value;
        }

        @Override
        public boolean apply(Object fieldValue) {
            boolean objInt = fieldValue instanceof Boolean
                    || fieldValue instanceof Byte
                    || fieldValue instanceof Short
                    || fieldValue instanceof Integer
                    || fieldValue instanceof Long;
            if (objInt) {
                long fieldValueInt;
                if (fieldValue instanceof Boolean) {
                    fieldValueInt = (Boolean) fieldValue ? 1 : 0;
                } else {
                    fieldValueInt = ((Number) fieldValue).longValue();
                }

                switch (operator) {
                    case LT:
                        return fieldValueInt < this.value;
                    case LE:
                        return fieldValueInt <= this.value;
                    case EQ:
                        return fieldValueInt == this.value;
                    case NE:
                        return fieldValueInt != this.value;
                    case GT:
                        return fieldValueInt > this.value;
                    case GE:
                        return fieldValueInt >= this.value;
                    default:
                        throw new UnsupportedOperationException();
                }
            }

            int cmp;
            if (fieldValue instanceof BigDecimal) {
                cmp = ((BigDecimal) fieldValue)
                        .compareTo(
                                BigDecimal.valueOf(value));
            } else if (fieldValue instanceof BigInteger) {
                cmp = ((BigInteger) fieldValue)
                        .compareTo(
                                BigInteger.valueOf(value));
            } else if (fieldValue instanceof Float) {
                cmp = ((Float) fieldValue)
                        .compareTo(
                                Float.valueOf(value));
            } else if (fieldValue instanceof Double) {
                cmp = ((Double) fieldValue)
                        .compareTo(
                                Double.valueOf(value));
            } else if (fieldValue instanceof String) {
                String fieldValueStr = (String) fieldValue;
                if (IOUtils.isNumber(fieldValueStr)) {
                    try {
                        cmp = Long.valueOf(Long.parseLong(fieldValueStr)).compareTo(Long.valueOf(value));
                    } catch (Exception ignored) {
                        cmp = fieldValueStr.compareTo(Long.toString(value));
                    }
                } else {
                    cmp = fieldValueStr.compareTo(Long.toString(value));
                }
            } else {
                throw new UnsupportedOperationException();
            }

            switch (operator) {
                case LT:
                    return cmp < 0;
                case LE:
                    return cmp <= 0;
                case EQ:
                    return cmp == 0;
                case NE:
                    return cmp != 0;
                case GT:
                    return cmp > 0;
                case GE:
                    return cmp >= 0;
                default:
                    throw new UnsupportedOperationException();
            }
        }

        @Override
        public void set(JSONPath.Context context, Object value) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof List) {
                List list = (List) object;
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    if (apply(context, item)) {
                        list.set(i, value);
                    }
                }
                return;
            }

            throw new JSONException("UnsupportedOperation ");
        }
    }

    static final class NameDecimalOpSegment
            extends NameFilter {
        final Operator operator;
        final BigDecimal value;

        public NameDecimalOpSegment(String name, long nameHashCode, Operator operator, BigDecimal value) {
            super(name, nameHashCode);
            this.operator = operator;
            this.value = value;
        }

        @Override
        public boolean apply(Object fieldValue) {
            if (fieldValue == null) {
                return false;
            }

            BigDecimal fieldValueDecimal;

            if (fieldValue instanceof Boolean) {
                fieldValueDecimal = (Boolean) fieldValue ? BigDecimal.ONE : BigDecimal.ZERO;
            } else if (fieldValue instanceof Byte
                    || fieldValue instanceof Short
                    || fieldValue instanceof Integer
                    || fieldValue instanceof Long) {
                fieldValueDecimal = BigDecimal.valueOf(
                        ((Number) fieldValue).longValue()
                );
            } else if (fieldValue instanceof BigDecimal) {
                fieldValueDecimal = ((BigDecimal) fieldValue);
            } else if (fieldValue instanceof BigInteger) {
                fieldValueDecimal = new BigDecimal((BigInteger) fieldValue);
            } else {
                throw new UnsupportedOperationException();
            }

            int cmp = fieldValueDecimal.compareTo(value);
            switch (operator) {
                case LT:
                    return cmp < 0;
                case LE:
                    return cmp <= 0;
                case EQ:
                    return cmp == 0;
                case NE:
                    return cmp != 0;
                case GT:
                    return cmp > 0;
                case GE:
                    return cmp >= 0;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    static final class NameRLikeSegment
            extends NameFilter {
        final Pattern pattern;
        final boolean not;

        public NameRLikeSegment(String fieldName, long fieldNameNameHash, Pattern pattern, boolean not) {
            super(fieldName, fieldNameNameHash);
            this.pattern = pattern;
            this.not = not;
        }

        @Override
        boolean apply(Object fieldValue) {
            String strPropertyValue = fieldValue.toString();
            Matcher m = pattern.matcher(strPropertyValue);
            boolean match = m.matches();

            if (not) {
                match = !match;
            }

            return match;
        }
    }

    static final class StartsWithSegment
            extends NameFilter {
        final String prefix;

        public StartsWithSegment(String fieldName, long fieldNameNameHash, String prefix) {
            super(fieldName, fieldNameNameHash);
            this.prefix = prefix;
        }

        @Override
        boolean apply(Object fieldValue) {
            String propertyValue = fieldValue.toString();
            return propertyValue != null && propertyValue.startsWith(prefix);
        }
    }

    static final class EndsWithSegment
            extends NameFilter {
        final String prefix;

        public EndsWithSegment(String fieldName, long fieldNameNameHash, String prefix) {
            super(fieldName, fieldNameNameHash);
            this.prefix = prefix;
        }

        @Override
        boolean apply(Object fieldValue) {
            String propertyValue = fieldValue.toString();
            return propertyValue != null && propertyValue.endsWith(prefix);
        }
    }

    static final class GroupFilter
            extends JSONPathSegment
            implements EvalSegment {
        final boolean and;
        final List<JSONPathFilter> filters;

        public GroupFilter(List<JSONPathFilter> filters, boolean and) {
            this.and = and;
            this.filters = filters;
        }

        @Override
        public void accept(JSONReader jsonReader, JSONPath.Context context) {
            if (context.parent == null) {
                context.root = jsonReader.readAny();
            }
            eval(context);
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof List) {
                List list = (List) object;
                JSONArray array = new JSONArray(list.size());
                for (int i = 0, l = list.size(); i < l; i++) {
                    Object item = list.get(i);
                    boolean match = and;
                    for (JSONPathFilter filter : filters) {
                        boolean result = filter.apply(context, item);
                        if (and) {
                            if (!result) {
                                match = false;
                                break;
                            }
                        } else {
                            if (result) {
                                match = true;
                                break;
                            }
                        }
                    }
                    if (match) {
                        array.add(item);
                    }
                }
                context.value = array;
                context.eval = true;
                return;
            }

            boolean match = and;
            for (JSONPathFilter filter : filters) {
                boolean result = filter.apply(context, object);
                if (and) {
                    if (!result) {
                        match = false;
                        break;
                    }
                } else {
                    if (result) {
                        match = true;
                        break;
                    }
                }
            }
            if (match) {
                context.value = object;
            }
            context.eval = true;
        }
    }

    abstract static class NameFilter
            extends JSONPathFilter {
        final String fieldName;
        final long fieldNameNameHash;

        final String[] fieldName2;
        final long[] fieldNameNameHash2;

        final Function function;

        public NameFilter(String fieldName, long fieldNameNameHash) {
            this.fieldName = fieldName;
            this.fieldNameNameHash = fieldNameNameHash;
            this.fieldName2 = null;
            this.fieldNameNameHash2 = null;
            this.function = null;
        }

        public NameFilter(String fieldName,
                          long fieldNameNameHash,
                          String[] fieldName2,
                          long[] fieldNameNameHash2,
                          Function function) {
            this.fieldName = fieldName;
            this.fieldNameNameHash = fieldNameNameHash;
            this.fieldName2 = fieldName2;
            this.fieldNameNameHash2 = fieldNameNameHash2;
            this.function = function;
        }

        abstract boolean apply(Object fieldValue);

        @Override
        public final void accept(JSONReader jsonReader, JSONPath.Context context) {
            if (context.parent == null) {
                context.root = jsonReader.readAny();
            }
            eval(context);
        }

        @Override
        public boolean remove(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof List) {
                List list = (List) object;
                for (int i = list.size() - 1; i >= 0; i--) {
                    Object item = list.get(i);
                    if (apply(context, item)) {
                        list.remove(i);
                    }
                }
                return true;
            }

            throw new JSONException("UnsupportedOperation " + getClass());
        }

        @Override
        public final void eval(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof List) {
                List list = (List) object;
                JSONArray array = new JSONArray(list.size());
                for (int i = 0, l = list.size(); i < l; i++) {
                    Object item = list.get(i);
                    if (apply(context, item)) {
                        array.add(item);
                    }
                }
                context.value = array;
                context.eval = true;
                return;
            }

            if (object instanceof Object[]) {
                Object[] list = (Object[]) object;
                JSONArray array = new JSONArray(list.length);
                for (Object item : list) {
                    if (apply(context, item)) {
                        array.add(item);
                    }
                }
                context.value = array;
                context.eval = true;
                return;
            }

            if (object instanceof JSONPath.Sequence) {
                JSONPath.Sequence sequence = (JSONPath.Sequence) object;
                JSONArray array = new JSONArray();
                for (Object value : sequence.values) {
                    if (value instanceof Collection) {
                        for (Object valueItem : ((Collection<?>) value)) {
                            if (apply(context, valueItem)) {
                                array.add(valueItem);
                            }
                        }
                    } else {
                        if (apply(context, value)) {
                            array.add(value);
                        }
                    }
                }
                context.value = array;
                context.eval = true;
                return;
            }

            if (apply(context, object)) {
                context.value = object;
                context.eval = true;
            }
        }

        @Override
        public final boolean apply(JSONPath.Context context, Object object) {
            if (object == null) {
                return false;
            }

            JSONWriter.Context writerContext = context.path.getWriterContext();

            if (object instanceof Map) {
                Object fieldValue = fieldName == null ? object : ((Map<?, ?>) object).get(fieldName);
                if (fieldValue == null) {
                    return false;
                }

                if (fieldName2 != null) {
                    for (int i = 0; i < fieldName2.length; i++) {
                        String name = fieldName2[i];
                        if (fieldValue instanceof Map) {
                            fieldValue = ((Map) fieldValue).get(name);
                        } else {
                            ObjectWriter objectWriter2 = writerContext.getObjectWriter(fieldValue.getClass());
                            if (objectWriter2 instanceof ObjectWriterAdapter) {
                                FieldWriter fieldWriter2 = objectWriter2.getFieldWriter(fieldNameNameHash2[i]);
                                if (fieldWriter2 == null) {
                                    return false;
                                }
                                fieldValue = fieldWriter2.getFieldValue(fieldValue);
                            } else {
                                return false;
                            }
                        }

                        if (fieldValue == null) {
                            return false;
                        }
                    }
                }

                if (function != null) {
                    fieldValue = function.apply(fieldValue);
                }

                return apply(fieldValue);
            }

            ObjectWriter objectWriter = writerContext.getObjectWriter(object.getClass());
            if (objectWriter instanceof ObjectWriterAdapter) {
                FieldWriter fieldWriter = objectWriter.getFieldWriter(fieldNameNameHash);
                Object fieldValue = fieldWriter.getFieldValue(object);

                if (fieldValue == null) {
                    return false;
                }

                if (fieldName2 != null) {
                    for (int i = 0; i < fieldName2.length; i++) {
                        String name = fieldName2[i];
                        if (fieldValue instanceof Map) {
                            fieldValue = ((Map) fieldValue).get(name);
                        } else {
                            ObjectWriter objectWriter2 = writerContext.getObjectWriter(fieldValue.getClass());
                            if (objectWriter2 instanceof ObjectWriterAdapter) {
                                FieldWriter fieldWriter2 = objectWriter2.getFieldWriter(fieldNameNameHash2[i]);
                                if (fieldWriter2 == null) {
                                    return false;
                                }
                                fieldValue = fieldWriter2.getFieldValue(fieldValue);
                            } else {
                                return false;
                            }
                        }

                        if (fieldValue == null) {
                            return false;
                        }
                    }
                }

                if (function != null) {
                    fieldValue = function.apply(fieldValue);
                }

                return apply(fieldValue);
            }

            if (function != null) {
                Object fieldValue = function.apply(object);
                return apply(fieldValue);
            }

            if (fieldName == null) {
                return apply(object);
            }

            return false;
        }
    }

    static final class NameStringOpSegment
            extends NameFilter {
        final Operator operator;
        final String value;

        public NameStringOpSegment(
                String fieldName,
                long fieldNameNameHash,
                String[] fieldName2,
                long[] fieldNameNameHash2,
                Function expr,
                Operator operator,
                String value
        ) {
            super(fieldName, fieldNameNameHash, fieldName2, fieldNameNameHash2, expr);
            this.operator = operator;
            this.value = value;
        }

        @Override
        public boolean apply(Object fieldValue) {
            if (!(fieldValue instanceof String)) {
                return false;
            }

            int cmp = ((String) fieldValue).compareTo(this.value);

            switch (operator) {
                case LT:
                    return cmp < 0;
                case LE:
                    return cmp <= 0;
                case EQ:
                    return cmp == 0;
                case NE:
                    return cmp != 0;
                case GT:
                    return cmp > 0;
                case GE:
                    return cmp >= 0;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    static final class NameArrayOpSegment
            extends NameFilter {
        final Operator operator;
        final JSONArray array;

        public NameArrayOpSegment(
                String fieldName,
                long fieldNameNameHash,
                String[] fieldName2,
                long[] fieldNameNameHash2,
                Function function,
                Operator operator,
                JSONArray array
        ) {
            super(fieldName, fieldNameNameHash, fieldName2, fieldNameNameHash2, function);
            this.operator = operator;
            this.array = array;
        }

        @Override
        boolean apply(Object fieldValue) {
            switch (operator) {
                case EQ:
                    return array.equals(fieldValue);
                default:
                    throw new JSONException("not support operator : " + operator);
            }
        }
    }

    static final class NameObjectOpSegment
            extends NameFilter {
        final Operator operator;
        final JSONObject object;

        public NameObjectOpSegment(
                String fieldName,
                long fieldNameNameHash,
                String[] fieldName2,
                long[] fieldNameNameHash2,
                Function function,
                Operator operator,
                JSONObject object) {
            super(fieldName, fieldNameNameHash, fieldName2, fieldNameNameHash2, function);
            this.operator = operator;
            this.object = object;
        }

        @Override
        boolean apply(Object fieldValue) {
            switch (operator) {
                case EQ:
                    return object.equals(fieldValue);
                default:
                    throw new JSONException("not support operator : " + operator);
            }
        }
    }

    static final class NameStringInSegment
            extends NameFilter {
        private final String[] values;
        private final boolean not;

        public NameStringInSegment(String fieldName, long fieldNameNameHash, String[] values, boolean not) {
            super(fieldName, fieldNameNameHash);
            this.values = values;
            this.not = not;
        }

        @Override
        public boolean apply(Object fieldValue) {
            for (String value : values) {
                if (value == fieldValue) {
                    return !not;
                } else if (value != null && value.equals(fieldValue)) {
                    return !not;
                }
            }

            return not;
        }
    }

    static final class NameStringContainsSegment
            extends NameFilter {
        private final String[] values;
        private final boolean not;

        public NameStringContainsSegment(
                String fieldName,
                long fieldNameNameHash,
                String[] fieldName2,
                long[] fieldNameNameHash2,
                String[] values, boolean not) {
            super(fieldName, fieldNameNameHash, fieldName2, fieldNameNameHash2, null);
            this.values = values;
            this.not = not;
        }

        @Override
        public boolean apply(Object fieldValue) {
            if (fieldValue instanceof Collection) {
                Collection collection = (Collection) fieldValue;

                boolean containsAll = true;
                for (String value : values) {
                    if (!collection.contains(value)) {
                        containsAll = false;
                        break;
                    }
                }

                if (containsAll) {
                    return !not;
                }
            }

            return not;
        }
    }

    static final class NameMatchFilter
            extends NameFilter {
        final String startsWithValue;
        final String endsWithValue;
        final String[] containsValues;
        final int minLength;
        final boolean not;

        public NameMatchFilter(
                String fieldName,
                long fieldNameNameHash,
                String startsWithValue,
                String endsWithValue,
                String[] containsValues,
                boolean not) {
            super(fieldName, fieldNameNameHash);
            this.startsWithValue = startsWithValue;
            this.endsWithValue = endsWithValue;
            this.containsValues = containsValues;
            this.not = not;

            int len = 0;
            if (startsWithValue != null) {
                len += startsWithValue.length();
            }

            if (endsWithValue != null) {
                len += endsWithValue.length();
            }

            if (containsValues != null) {
                for (String item : containsValues) {
                    len += item.length();
                }
            }

            this.minLength = len;
        }

        @Override
        boolean apply(Object arg) {
            if (!(arg instanceof String)) {
                return false;
            }

            String fieldValue = (String) arg;
            if (fieldValue.length() < minLength) {
                return not;
            }

            int start = 0;
            if (startsWithValue != null) {
                if (!fieldValue.startsWith(startsWithValue)) {
                    return not;
                }
                start += startsWithValue.length();
            }

            if (containsValues != null) {
                for (String containsValue : containsValues) {
                    int index = fieldValue.indexOf(containsValue, start);
                    if (index == -1) {
                        return not;
                    }
                    start = index + containsValue.length();
                }
            }

            if (endsWithValue != null) {
                if (!fieldValue.endsWith(endsWithValue)) {
                    return not;
                }
            }

            return !not;
        }
    }

    static final class NameExistsFilter
            extends JSONPathFilter {
        final String name;
        final long nameHashCode;

        public NameExistsFilter(String name, long nameHashCode) {
            this.name = name;
            this.nameHashCode = nameHashCode;
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            JSONArray array = new JSONArray();
            if (object instanceof List) {
                List list = (List) object;
                for (int i = 0, l = list.size(); i < l; i++) {
                    Object item = list.get(i);
                    if (item instanceof Map) {
                        if (((Map) item).containsKey(name)) {
                            array.add(item);
                        }
                    }
                }
                context.value = array;
                return;
            }

            if (object instanceof Map) {
                Map map = (Map) object;
                Object value = map.get(name);
                context.value = value != null ? object : null;
                return;
            }

            if (object instanceof JSONPath.Sequence) {
                List list = ((JSONPath.Sequence) object).values;
                for (int i = 0, l = list.size(); i < l; i++) {
                    Object item = list.get(i);
                    if (item instanceof Map) {
                        if (((Map) item).containsKey(name)) {
                            array.add(item);
                        }
                    }
                }
                context.value = new JSONPath.Sequence(array);
                return;
            }

            throw new UnsupportedOperationException();
        }

        @Override
        public void accept(JSONReader jsonReader, JSONPath.Context context) {
            eval(context);
        }

        @Override
        public String toString() {
            return '?' + name;
        }

        @Override
        public boolean apply(JSONPath.Context context, Object object) {
            throw new UnsupportedOperationException();
        }
    }

    static final class NameIntBetweenSegment
            extends NameFilter {
        private final long begin;
        private final long end;
        private final boolean not;

        public NameIntBetweenSegment(String fieldName, long fieldNameNameHash, long begin, long end, boolean not) {
            super(fieldName, fieldNameNameHash);
            this.begin = begin;
            this.end = end;
            this.not = not;
        }

        @Override
        public boolean apply(Object fieldValue) {
            if (fieldValue instanceof Byte
                    || fieldValue instanceof Short
                    || fieldValue instanceof Integer
                    || fieldValue instanceof Long
            ) {
                long fieldValueLong = ((Number) fieldValue).longValue();

                if (fieldValueLong >= begin && fieldValueLong <= end) {
                    return !not;
                }

                return not;
            }

            if (fieldValue instanceof Float
                    || fieldValue instanceof Double
            ) {
                double fieldValueDouble = ((Number) fieldValue).doubleValue();

                if (fieldValueDouble >= begin && fieldValueDouble <= end) {
                    return !not;
                }

                return not;
            }

            if (fieldValue instanceof BigDecimal) {
                BigDecimal decimal = (BigDecimal) fieldValue;
                int cmpBegin = decimal.compareTo(BigDecimal.valueOf(begin));
                int cmpEnd = decimal.compareTo(BigDecimal.valueOf(end));

                if (cmpBegin >= 0 && cmpEnd <= 0) {
                    return !not;
                }

                return not;
            }

            if (fieldValue instanceof BigInteger) {
                BigInteger bigInt = (BigInteger) fieldValue;
                int cmpBegin = bigInt.compareTo(BigInteger.valueOf(begin));
                int cmpEnd = bigInt.compareTo(BigInteger.valueOf(end));

                if (cmpBegin >= 0 && cmpEnd <= 0) {
                    return !not;
                }

                return not;
            }

            return not;
        }
    }

    static final class NameLongContainsSegment
            extends NameFilter {
        private final long[] values;
        private final boolean not;

        public NameLongContainsSegment(
                String fieldName,
                long fieldNameNameHash,
                String[] fieldName2,
                long[] fieldNameNameHash2,
                long[] values, boolean not) {
            super(fieldName, fieldNameNameHash, fieldName2, fieldNameNameHash2, null);
            this.values = values;
            this.not = not;
        }

        @Override
        public boolean apply(Object fieldValue) {
            if (fieldValue instanceof Collection) {
                Collection collection = (Collection) fieldValue;

                boolean containsAll = true;
                for (long value : values) {
                    boolean containsItem = false;
                    for (Object item : collection) {
                        long longItem;
                        if (item instanceof Byte
                                || item instanceof Short
                                || item instanceof Integer
                                || item instanceof Long) {
                            longItem = ((Number) item).longValue();
                            if (longItem == value) {
                                containsItem = true;
                                break;
                            }
                        }

                        if (item instanceof Float) {
                            if (value == (Float) item) {
                                containsItem = true;
                                break;
                            }
                        }

                        if (item instanceof Double) {
                            if (value == (Double) item) {
                                containsItem = true;
                                break;
                            }
                        }

                        if (item instanceof BigDecimal) {
                            BigDecimal decimal = (BigDecimal) item;
                            long longValue = decimal.longValue();
                            if (value == longValue && decimal.equals(BigDecimal.valueOf(value))) {
                                containsItem = true;
                                break;
                            }
                        }

                        if (item instanceof BigInteger) {
                            BigInteger bigiInt = (BigInteger) item;
                            long longValue = bigiInt.longValue();
                            if (value == longValue && bigiInt.equals(BigInteger.valueOf(value))) {
                                containsItem = true;
                                break;
                            }
                        }
                    }

                    if (!containsItem) {
                        containsAll = false;
                        break;
                    }
                }

                if (containsAll) {
                    return !not;
                }
            }

            return not;
        }
    }

    static final class NameIntInSegment
            extends NameFilter {
        private final long[] values;
        private final boolean not;

        public NameIntInSegment(
                String fieldName,
                long fieldNameNameHash,
                String[] fieldName2,
                long[] fieldNameNameHash2,
                Function expr,
                long[] values,
                boolean not) {
            super(fieldName, fieldNameNameHash, fieldName2, fieldNameNameHash2, expr);
            this.values = values;
            this.not = not;
        }

        @Override
        public boolean apply(Object fieldValue) {
            if (fieldValue instanceof Byte
                    || fieldValue instanceof Short
                    || fieldValue instanceof Integer
                    || fieldValue instanceof Long
            ) {
                long fieldValueLong = ((Number) fieldValue).longValue();

                for (long value : values) {
                    if (value == fieldValueLong) {
                        return !not;
                    }
                }

                return not;
            }

            if (fieldValue instanceof Float
                    || fieldValue instanceof Double
            ) {
                double fieldValueDouble = ((Number) fieldValue).doubleValue();

                for (long value : values) {
                    if (value == fieldValueDouble) {
                        return !not;
                    }
                }

                return not;
            }

            if (fieldValue instanceof BigDecimal) {
                BigDecimal decimal = (BigDecimal) fieldValue;
                long longValue = decimal.longValue();
                for (long value : values) {
                    if (value == longValue && decimal.equals(BigDecimal.valueOf(value))) {
                        return !not;
                    }
                }

                return not;
            }

            if (fieldValue instanceof BigInteger) {
                BigInteger bigiInt = (BigInteger) fieldValue;
                long longValue = bigiInt.longValue();
                for (long value : values) {
                    if (value == longValue && bigiInt.equals(BigInteger.valueOf(value))) {
                        return !not;
                    }
                }

                return not;
            }

            return not;
        }
    }
}
