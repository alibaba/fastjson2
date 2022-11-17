package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;

import static com.alibaba.fastjson2.JSONReader.EOI;
import static com.alibaba.fastjson2.util.JDKUtils.LATIN1;
import static com.alibaba.fastjson2.util.JDKUtils.STRING_CREATOR_JDK11;

final class JSONPathSegmentIndex
        extends JSONPathSegment {
    static final JSONPathSegmentIndex ZERO = new JSONPathSegmentIndex(0);
    static final JSONPathSegmentIndex ONE = new JSONPathSegmentIndex(1);
    static final JSONPathSegmentIndex TWO = new JSONPathSegmentIndex(2);

    static final JSONPathSegmentIndex LAST = new JSONPathSegmentIndex(-1);

    final int index;

    public JSONPathSegmentIndex(int index) {
        this.index = index;
    }

    static JSONPathSegmentIndex of(int index) {
        if (index == 0) {
            return ZERO;
        }
        if (index == 1) {
            return ONE;
        }
        if (index == 2) {
            return TWO;
        }
        if (index == -1) {
            return LAST;
        }
        return new JSONPathSegmentIndex(index);
    }

    @Override
    public void eval(JSONPath.Context context) {
        Object object = context.parent == null
                ? context.root
                : context.parent.value;

        if (object == null) {
            context.eval = true;
            return;
        }

        if (object instanceof java.util.List) {
            List list = (List) object;
            if (index >= 0) {
                if (index < list.size()) {
                    context.value = list.get(index);
                }
            } else {
                int itemIndex = list.size() + this.index;
                if (itemIndex >= 0) {
                    context.value = list.get(itemIndex);
                }
            }
            context.eval = true;
            return;
        }

        if ((object instanceof SortedSet || object instanceof LinkedHashSet)
                || (index == 0 && object instanceof Collection && ((Collection<?>) object).size() == 1)
        ) {
            Collection collection = (Collection) object;
            int i = 0;
            for (Iterator it = collection.iterator(); it.hasNext(); ++i) {
                Object item = it.next();
                if (i == index) {
                    context.value = item;
                    break;
                }
            }
            context.eval = true;
            return;
        }

        if (object instanceof Object[]) {
            Object[] array = (Object[]) object;
            if (index >= 0) {
                if (index < array.length) {
                    context.value = array[index];
                }
            } else {
                int itemIndex = array.length + this.index;
                if (itemIndex >= 0) {
                    context.value = array[itemIndex];
                }
            }
            context.eval = true;
            return;
        }

        Class objectClass = object.getClass();
        if (objectClass.isArray()) {
            int length = Array.getLength(object);
            if (index >= 0) {
                if (index < length) {
                    context.value = Array.get(object, index);
                }
            } else {
                int itemIndex = length + this.index;
                if (itemIndex >= 0) {
                    context.value = Array.get(object, itemIndex);
                }
            }
            context.eval = true;
            return;
        }

        if (object instanceof JSONPath.Sequence) {
            List sequence = ((JSONPath.Sequence) object).values;
            JSONArray values = new JSONArray(sequence.size());
            for (int i = 0; i < sequence.size(); i++) {
                Object item = sequence.get(i);
                context.value = item;
                JSONPath.Context itemContext = new JSONPath.Context(context.path, context, context.current, context.next, context.readerFeatures);
                eval(itemContext);
                values.add(itemContext.value);
            }
            if (context.next != null) {
                context.value = new JSONPath.Sequence(values);
            } else {
                context.value = values;
            }
            context.eval = true;
            return;
        }

        if (Map.class.isAssignableFrom(objectClass)) {
            Object value = eval((Map) object);
            context.value = value;
            context.eval = true;
            return;
        }

        // lax mode
        if (index == 0) {
            context.value = object;
            context.eval = true;
            return;
        }

        throw new JSONException("jsonpath not support operate : " + context.path + ", objectClass" + objectClass.getName());
    }

    private Object eval(Map object) {
        Map map = object;
        Object value = map.get(index);
        if (value == null) {
            value = map.get(Integer.toString(index));
        }

        if (value == null) {
            int size = map.size();
            Iterator it = map.entrySet().iterator();
            if (size == 1 || map instanceof LinkedHashMap || map instanceof SortedMap) {
                for (int i = 0; i <= index && i < size && it.hasNext(); ++i) {
                    Map.Entry entry = (Map.Entry) it.next();
                    Object entryKey = entry.getKey();
                    Object entryValue = entry.getValue();
                    if (entryKey instanceof Long) {
                        if (entryKey.equals(Long.valueOf(index))) {
                            value = entryValue;
                            break;
                        }
                    } else {
                        if (i == index) {
                            value = entryValue;
                        }
                    }
                }
            } else {
                for (int i = 0; i <= index && i < map.size() && it.hasNext(); ++i) {
                    Map.Entry entry = (Map.Entry) it.next();
                    Object entryKey = entry.getKey();
                    Object entryValue = entry.getValue();
                    if (entryKey instanceof Long) {
                        if (entryKey.equals(Long.valueOf(index))) {
                            value = entryValue;
                            break;
                        }
                    }
                }
            }
        }
        return value;
    }

    @Override
    public void set(JSONPath.Context context, Object value) {
        Object object = context.parent == null
                ? context.root
                : context.parent.value;

        if (object instanceof List) {
            List list = (List) object;
            if (index >= 0) {
                if (index > list.size()) {
                    for (int i = list.size(); i < index; ++i) {
                        list.add(null);
                    }
                }
                if (index < list.size()) {
                    list.set(index, value);
                } else if (index <= list.size()) {
                    list.add(value);
                }
            } else {
                int itemIndex = list.size() + this.index;
                if (itemIndex >= 0) {
                    list.set(itemIndex, value);
                }
            }
            return;
        }

        if (object instanceof Object[]) {
            Object[] array = (Object[]) object;
            if (index >= 0) {
                array[index] = value;
            } else {
                array[array.length + index] = value;
            }
            return;
        }

        if (object != null && object.getClass().isArray()) {
            int length = Array.getLength(object);
            if (index >= 0) {
                if (index < length) {
                    Array.set(object, index, value);
                }
            } else {
                int arrayIndex = length + index;
                if (arrayIndex >= 0) {
                    Array.set(object, arrayIndex, value);
                }
            }
            return;
        }

        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public void setCallback(JSONPath.Context context, BiFunction callback) {
        Object object = context.parent == null
                ? context.root
                : context.parent.value;

        if (object instanceof List) {
            List list = (List) object;
            if (index >= 0) {
                if (index < list.size()) {
                    Object value = list.get(index);
                    value = callback.apply(object, value);
                    list.set(index, value);
                }
            } else {
                int itemIndex = list.size() + this.index;
                if (itemIndex >= 0) {
                    Object value = list.get(index);
                    value = callback.apply(object, value);
                    list.set(itemIndex, value);
                }
            }
            return;
        }

        if (object instanceof Object[]) {
            Object[] array = (Object[]) object;
            if (index >= 0) {
                if (index < array.length) {
                    Object value = array[index];
                    value = callback.apply(object, value);
                    array[index] = value;
                }
            } else {
                Object value = array[index];
                value = callback.apply(object, value);
                array[array.length + index] = value;
            }
            return;
        }

        if (object != null && object.getClass().isArray()) {
            int length = Array.getLength(object);
            if (index >= 0) {
                if (index < length) {
                    Object value = Array.get(object, index);
                    value = callback.apply(object, value);
                    Array.set(object, index, value);
                }
            } else {
                int arrayIndex = length + index;
                if (arrayIndex >= 0) {
                    Object value = Array.get(object, index);
                    value = callback.apply(object, value);
                    Array.set(object, arrayIndex, value);
                }
            }
            return;
        }

        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public boolean remove(JSONPath.Context context) {
        Object object = context.parent == null
                ? context.root
                : context.parent.value;

        if (object instanceof List) {
            List list = (List) object;
            if (index >= 0) {
                if (index < list.size()) {
                    list.remove(index);
                    return true;
                }
            } else {
                int itemIndex = list.size() + this.index;
                if (itemIndex >= 0) {
                    list.remove(itemIndex);
                    return true;
                }
            }
            return false;
        }

        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public void setInt(JSONPath.Context context, int value) {
        Object object = context.parent == null
                ? context.root
                : context.parent.value;
        if (object instanceof int[]) {
            int[] array = (int[]) object;
            if (index >= 0) {
                if (index < array.length) {
                    array[index] = value;
                }
            } else {
                int arrayIndex = array.length + index;
                if (arrayIndex >= 0) {
                    array[arrayIndex] = value;
                }
            }
            return;
        }

        if (object instanceof long[]) {
            long[] array = (long[]) object;
            if (index >= 0) {
                if (index < array.length) {
                    array[index] = value;
                }
            } else {
                int arrayIndex = array.length + index;
                if (arrayIndex >= 0) {
                    array[arrayIndex] = value;
                }
            }
            return;
        }

        set(context, value);
    }

    @Override
    public void setLong(JSONPath.Context context, long value) {
        Object object = context.parent == null
                ? context.root
                : context.parent.value;
        if (object instanceof int[]) {
            int[] array = (int[]) object;
            if (index >= 0) {
                if (index < array.length) {
                    array[index] = (int) value;
                }
            } else {
                int arrayIndex = array.length + index;
                if (arrayIndex >= 0) {
                    array[arrayIndex] = (int) value;
                }
            }
            return;
        }

        if (object instanceof long[]) {
            long[] array = (long[]) object;
            if (index >= 0) {
                if (index < array.length) {
                    array[index] = value;
                }
            } else {
                int arrayIndex = array.length + index;
                if (arrayIndex >= 0) {
                    array[arrayIndex] = value;
                }
            }
            return;
        }

        set(context, value);
    }

    @Override
    public void accept(JSONReader jsonReader, JSONPath.Context context) {
        if (context.parent != null
                && (context.parent.eval
                || (context.parent.current instanceof CycleNameSegment && context.next == null))
        ) {
            eval(context);
            return;
        }

        if (jsonReader.isJSONB()) {
            int itemCnt = jsonReader.startArray();
            for (int i = 0; i < itemCnt; i++) {
                boolean match = index == i;
                if (!match) {
                    jsonReader.skipValue();
                    continue;
                }

                if (jsonReader.isArray() || jsonReader.isObject()) {
                    if (context.next != null) {
                        break;
                    }
                }

                context.value = jsonReader.readAny();
                context.eval = true;
                break;
            }
            return;
        }

        if (jsonReader.ch == '{') {
            Map object = jsonReader.readObject();
            context.value = eval(object);
            context.eval = true;
            return;
        }

        jsonReader.next();
        _for:
        for (int i = 0; jsonReader.ch != EOI; ++i) {
            if (jsonReader.ch == ']') {
                jsonReader.next();
                break;
            }

            boolean match = index == -1 || index == i;

            if (!match) {
                jsonReader.skipValue();
                if (jsonReader.ch == ',') {
                    jsonReader.next();
                }
                continue;
            }

            Object val;
            switch (jsonReader.ch) {
                case '-':
                case '+':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '.':
                    jsonReader.readNumber0();
                    val = jsonReader.getNumber();
                    break;
                case '[':
                    if (context.next != null && !(context.next instanceof EvalSegment)) {
                        break _for;
                    }
                    val = jsonReader.readArray();
                    break;
                case '{':
                    if (context.next != null && !(context.next instanceof EvalSegment)) {
                        break _for;
                    }
                    val = jsonReader.readObject();
                    break;
                case '"':
                case '\'':
                    val = jsonReader.readString();
                    break;
                case 't':
                case 'f':
                    val = jsonReader.readBoolValue();
                    break;
                case 'n':
                    jsonReader.readNull();
                    val = null;
                    break;
                default:
                    throw new JSONException("TODO : " + jsonReader.ch);
            }

            if (index == -1) {
                if (jsonReader.ch == ']') {
                    context.value = val;
                }
            } else {
                context.value = val;
            }
        }
    }

    @Override
    public String toString() {
        int size = (index < 0) ? IOUtils.stringSize(-index) + 1 : IOUtils.stringSize(index);
        byte[] bytes = new byte[size + 2];
        bytes[0] = '[';
        IOUtils.getChars(index, bytes.length - 1, bytes);
        bytes[bytes.length - 1] = ']';

        String str;
        if (STRING_CREATOR_JDK11 != null) {
            str = STRING_CREATOR_JDK11.apply(bytes, LATIN1);
        } else {
            str = new String(bytes, StandardCharsets.US_ASCII);
        }
        return str;
    }
}
