package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import static com.alibaba.fastjson2.JSONB.Constants.BC_OBJECT;
import static com.alibaba.fastjson2.JSONB.Constants.BC_OBJECT_END;
import static com.alibaba.fastjson2.JSONReader.EOI;

class JSONPathSegmentName
        extends JSONPathSegment {
    static final long HASH_NAME = Fnv.hashCode64("name");
    static final long HASH_ORDINAL = Fnv.hashCode64("ordinal");

    final String name;
    final long nameHashCode;

    public JSONPathSegmentName(String name, long nameHashCode) {
        this.name = name;
        this.nameHashCode = nameHashCode;
    }

    @Override
    public boolean remove(JSONPath.Context context) {
        set(context, null);
        return context.eval = true;
    }

    @Override
    public boolean contains(JSONPath.Context context) {
        Object object = context.parent == null
                ? context.root
                : context.parent.value;

        if (object == null) {
            return false;
        }

        if (object instanceof Map) {
            return ((Map<?, ?>) object).containsKey(name);
        }

        if (object instanceof Collection) {
            for (Object item : (Collection) object) {
                if (item == null) {
                    continue;
                }

                if (item instanceof Map) {
                    if (((Map<?, ?>) item).get(name) != null) {
                        return true;
                    }
                }

                ObjectWriter<?> objectWriter = context.path
                        .getWriterContext()
                        .getObjectWriter(item.getClass());
                if (objectWriter instanceof ObjectWriterAdapter) {
                    FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
                    if (fieldWriter != null) {
                        if (fieldWriter.getFieldValue(item) != null) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        if (object instanceof JSONPath.Sequence) {
            JSONPath.Sequence sequence = (JSONPath.Sequence) object;
            for (Object item : sequence.values) {
                if (item == null) {
                    continue;
                }

                if (item instanceof Map) {
                    if (((Map<?, ?>) item).get(name) != null) {
                        return true;
                    }
                }

                ObjectWriter<?> objectWriter = context.path
                        .getWriterContext()
                        .getObjectWriter(item.getClass());
                if (objectWriter instanceof ObjectWriterAdapter) {
                    FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
                    if (fieldWriter != null) {
                        if (fieldWriter.getFieldValue(item) != null) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        if (object instanceof Object[]) {
            Object[] array = (Object[]) object;
            for (Object item : array) {
                if (item == null) {
                    continue;
                }

                if (item instanceof Map) {
                    if (((Map) item).get(name) != null) {
                        return true;
                    }
                }

                ObjectWriter<?> objectWriter = context.path
                        .getWriterContext()
                        .getObjectWriter(item.getClass());
                if (objectWriter instanceof ObjectWriterAdapter) {
                    FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
                    if (fieldWriter != null) {
                        if (fieldWriter.getFieldValue(item) != null) {
                            return true;
                        }
                    }
                }
            }
        }

        ObjectWriter<?> objectWriter = context.path
                .getWriterContext()
                .getObjectWriter(object.getClass());
        if (objectWriter instanceof ObjectWriterAdapter) {
            FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
            if (fieldWriter != null) {
                return fieldWriter.getFieldValue(object) != null;
            }
        }

        return false;
    }

    @Override
    public void eval(JSONPath.Context context) {
        Object object = context.parent == null
                ? context.root
                : context.parent.value;

        if (object == null) {
            return;
        }

        if (object instanceof Map) {
            Map map = (Map) object;
            Object value = map.get(name);
            if (value == null) {
                boolean isNum = IOUtils.isNumber(this.name);
                Long longValue = null;

                for (Object o : map.entrySet()) {
                    Map.Entry entry = (Map.Entry) o;
                    Object entryKey = entry.getKey();
                    if (entryKey instanceof Enum && ((Enum<?>) entryKey).name().equals(this.name)) {
                        value = entry.getValue();
                        break;
                    } else if (entryKey instanceof Long) {
                        if (longValue == null && isNum) {
                            longValue = Long.parseLong(this.name);
                        }
                        if (entryKey.equals(longValue)) {
                            value = entry.getValue();
                            break;
                        }
                    }
                }
            }

            context.value = value;
            return;
        }

        if (object instanceof Collection) {
            Collection<?> collection = (Collection<?>) object;
            int size = collection.size();
            Collection values = null; // = new JSONArray(collection.size());
            for (Object item : collection) {
                if (item instanceof Map) {
                    Object val = ((Map<?, ?>) item).get(name);
                    if (val == null) {
                        continue;
                    }
                    if (val instanceof Collection) {
                        if (size == 1) {
                            values = (Collection) val;
                        } else {
                            if (values == null) {
                                values = new JSONArray(size);
                            }
                            values.addAll((Collection) val);
                        }
                    } else {
                        if (values == null) {
                            values = new JSONArray(size);
                        }
                        values.add(val);
                    }
                }
            }
            context.value = values;
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
                Object val = itemContext.value;

                if (val == null) {
                    continue;
                }

                if (val instanceof Collection) {
                    values.addAll((Collection) val);
                } else {
                    values.add(val);
                }
            }
            if (context.next != null) {
                context.value = new JSONPath.Sequence(values);
            } else {
                context.value = values;
            }
            context.eval = true;
            return;
        }

        JSONWriter.Context writerContext = context.path.getWriterContext();
        ObjectWriter<?> objectWriter = writerContext.getObjectWriter(object.getClass());
        if (objectWriter instanceof ObjectWriterAdapter) {
            FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
            if (fieldWriter != null) {
                context.value = fieldWriter.getFieldValue(object);
            }

            return;
        }

        if (nameHashCode == HASH_NAME && object instanceof Enum) {
            context.value = ((Enum<?>) object).name();
            return;
        }

        if (nameHashCode == HASH_ORDINAL && object instanceof Enum) {
            context.value = ((Enum<?>) object).ordinal();
            return;
        }

        if (object instanceof String) {
            String str = (String) object;
            if (!str.isEmpty() && str.charAt(0) == '{') {
                context.value =
                        JSONPath.of("$." + name)
                                .extract(
                                        JSONReader.of(str));
                return;
            }

            context.value = null;
            return;
        }

        if (object instanceof Number || object instanceof Boolean) {
            context.value = null;
            return;
        }

        throw new JSONException("not support : " + object.getClass());
    }

    @Override
    public void set(JSONPath.Context context, Object value) {
        Object object = context.parent == null
                ? context.root
                : context.parent.value;

        if (object instanceof Map) {
            Map map = (Map) object;
            Object origin = map.put(name, value);
            if (origin != null) {
                if ((context.readerFeatures & JSONReader.Feature.DuplicateKeyValueAsArray.mask) != 0) {
                    if (origin instanceof Collection) {
                        ((Collection) origin).add(value);
                        map.put(name, value);
                    } else {
                        JSONArray array = JSONArray.of(origin, value);
                        map.put(name, array);
                    }
                }
            }
            return;
        }

        ObjectReaderProvider provider = context.path.getReaderContext().getProvider();
        ObjectReader objectReader = provider.getObjectReader(object.getClass());
        FieldReader fieldReader = objectReader.getFieldReader(nameHashCode);
        if (fieldReader == null) {
            return;
        }

        if (value != null) {
            Class<?> valueClass = value.getClass();
            Class fieldClass = fieldReader.fieldClass;
            if (valueClass != fieldClass) {
                java.util.function.Function typeConvert = provider.getTypeConvert(valueClass, fieldClass);
                if (typeConvert != null) {
                    value = typeConvert.apply(value);
                }
            }
        }
        fieldReader.accept(object, value);
    }

    @Override
    public void setCallback(JSONPath.Context context, BiFunction callback) {
        Object object = context.parent == null
                ? context.root
                : context.parent.value;

        if (object instanceof Map) {
            Map map = (Map) object;
            Object origin = map.get(name);
            if (origin != null) {
                Object applyValue = callback.apply(map, origin);
                map.put(name, applyValue);
            }
            return;
        }

        ObjectReaderProvider provider = context.path.getReaderContext().getProvider();

        ObjectReader objectReader = provider.getObjectReader(object.getClass());
        ObjectWriter objectWriter = context.path
                .getWriterContext()
                .provider
                .getObjectWriter(object.getClass());

        FieldReader fieldReader = objectReader.getFieldReader(nameHashCode);
        FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
        if (fieldReader == null || fieldWriter == null) {
            return;
        }

        Object fieldValue = fieldWriter.getFieldValue(object);
        Object applyValue = callback.apply(object, fieldValue);
        fieldReader.accept(object, applyValue);
    }

    @Override
    public void accept(JSONReader jsonReader, JSONPath.Context context) {
        if (context.parent != null
                && (context.parent.eval
                || context.parent.current instanceof JSONPathFilter
                || context.parent.current instanceof MultiIndexSegment)
        ) {
            eval(context);
            return;
        }

        if (jsonReader.isJSONB()) {
            if (jsonReader.nextIfObjectStart()) {
                for (int i = 0; ; ++i) {
                    if (jsonReader.nextIfObjectEnd()) {
                        break;
                    }

                    long nameHashCode = jsonReader.readFieldNameHashCode();
                    if (nameHashCode == 0) {
                        continue;
                    }
                    boolean match = nameHashCode == this.nameHashCode;
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
            } else if (jsonReader.isArray()
                    && context.parent != null
                    && context.parent.current instanceof AllSegment) {
                List values = new JSONArray();
                int itemCnt = jsonReader.startArray();
                for (int i = 0; i < itemCnt; i++) {
                    if (jsonReader.nextIfMatch(BC_OBJECT)) {
                        for (int j = 0; ; j++) {
                            if (jsonReader.nextIfMatch(BC_OBJECT_END)) {
                                break;
                            }

                            long nameHashCode = jsonReader.readFieldNameHashCode();
                            boolean match = nameHashCode == this.nameHashCode;

                            if (!match) {
                                jsonReader.skipValue();
                                continue;
                            }

                            if (jsonReader.isArray() || jsonReader.isObject()) {
                                if (context.next != null) {
                                    break;
                                }
                            }

                            values.add(jsonReader.readAny());
                        }
                    } else {
                        jsonReader.skipValue();
                    }
                }

                context.value = values;
                context.eval = true;
                return;
            }

            throw new JSONException("TODO");
        }

        if (jsonReader.nextIfObjectStart()) {
            if (jsonReader.ch == '}') {
                jsonReader.next();
                // return object;
            }

            _for:
            for (; ; ) {
                if (jsonReader.nextIfObjectEnd()) {
                    jsonReader.next();
                    break;
                }

                long nameHashCode = jsonReader.readFieldNameHashCode();
                boolean match = nameHashCode == this.nameHashCode;

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
                        jsonReader.readNumber0();
                        val = jsonReader.getNumber();
                        break;
                    case '[':
                        if (context.next != null && !(context.next instanceof EvalSegment)
                                && !(context.next instanceof JSONPathSegmentName)
                                && !(context.next instanceof AllSegment)) {
                            break _for;
                        }
                        val = jsonReader.readArray();
                        context.eval = true;
                        break;
                    case '{':
                        if (context.next != null
                                && !(context.next instanceof EvalSegment)
                                && !(context.next instanceof AllSegment)) {
                            break _for;
                        }
                        val = jsonReader.readObject();
                        context.eval = true;
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

                context.value = val;
                break;
            }
        } else if (jsonReader.ch == '[' && context.parent != null && context.parent.current instanceof AllSegment) {
            jsonReader.next();
            List values = new JSONArray();
            while (jsonReader.ch != EOI) {
                if (jsonReader.ch == ']') {
                    jsonReader.next();
                    break;
                }
                if (jsonReader.ch == '{') {
                    jsonReader.next();

                    _for:
                    for (; ; ) {
                        if (jsonReader.ch == '}') {
                            jsonReader.next();
                            break;
                        }

                        long nameHashCode = jsonReader.readFieldNameHashCode();
                        boolean match = nameHashCode == this.nameHashCode;

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
                                if (context.next != null) {
                                    break _for;
                                }
                                val = jsonReader.readArray();
                                break;
                            case '{':
                                if (context.next != null) {
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
                        values.add(val);
                    }
                } else {
                    jsonReader.skipValue();
                }

                if (jsonReader.ch == ',') {
                    jsonReader.next();
                }
            }

            context.value = values;
        }/* else if (jsonReader.ch == JSONReader.EOI) {
            return;
        }*/
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JSONPathSegmentName that = (JSONPathSegmentName) o;
        return nameHashCode == that.nameHashCode && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, nameHashCode);
    }

    @Override
    public String toString() {
        return name;
    }
}
