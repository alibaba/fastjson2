package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

final class JSONPathSingleName
        extends JSONPathSingle {
    final long nameHashCode;
    final String name;

    public JSONPathSingleName(String path, JSONPathSegmentName segment, Feature... features) {
        super(segment, path, features);
        this.name = segment.name;
        this.nameHashCode = segment.nameHashCode;
    }

    @Override
    public Object eval(Object root) {
        Object value;
        if (root instanceof Map) {
            Map map = (Map) root;
            value = map.get(name);
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
        } else {
            JSONWriter.Context writerContext = getWriterContext();
            ObjectWriter objectWriter = writerContext.getObjectWriter(root.getClass());
            if (objectWriter == null) {
                return null;
            }

            FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
            if (fieldWriter == null) {
                return null;
            }

            value = fieldWriter.getFieldValue(root);
        }

        if ((features & Feature.AlwaysReturnList.mask) != 0) {
            if (value == null) {
                value = new JSONArray();
            } else {
                value = JSONArray.of(value);
            }
        }
        return value;
    }

    @Override
    public boolean remove(Object root) {
        if (root == null) {
            return false;
        }

        if (root instanceof Map) {
            return ((Map<?, ?>) root).remove(name) != null;
        }

        ObjectReaderProvider provider = getReaderContext().getProvider();

        ObjectReader objectReader = provider.getObjectReader(root.getClass());
        if (objectReader == null) {
            return false;
        }

        FieldReader fieldReader = objectReader.getFieldReader(nameHashCode);
        if (fieldReader == null) {
            return false;
        }

        try {
            fieldReader.accept(root, null);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isRef() {
        return true;
    }

    @Override
    public boolean contains(Object root) {
        if (root instanceof Map) {
            return ((Map) root).containsKey(name);
        }

        ObjectWriterProvider provider = getWriterContext().provider;

        ObjectWriter objectWriter = provider.getObjectWriter(root.getClass());
        if (objectWriter == null) {
            return false;
        }

        FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
        if (fieldWriter == null) {
            return false;
        }

        return fieldWriter.getFieldValue(root) != null;
    }

    @Override
    public void set(Object rootObject, Object value) {
        if (rootObject instanceof Map) {
            Map map = (Map) rootObject;
            map.put(name, value);
            return;
        }
        ObjectReaderProvider provider = getReaderContext().getProvider();
        ObjectReader objectReader = provider.getObjectReader(rootObject.getClass());
        FieldReader fieldReader = objectReader.getFieldReader(nameHashCode);

        if (fieldReader != null) {
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
            fieldReader.accept(rootObject, value);
        } else if (objectReader instanceof ObjectReaderBean) {
            ((ObjectReaderBean) objectReader).acceptExtra(rootObject, name, value);
        }
    }

    @Override
    public void set(Object rootObject, Object value, JSONReader.Feature... readerFeatures) {
        if (rootObject instanceof Map) {
            Map map = (Map) rootObject;
            Object origin = map.put(name, value);
            if (origin != null) {
                boolean duplicateKeyValueAsArray = false;
                for (JSONReader.Feature feature : readerFeatures) {
                    if (feature == JSONReader.Feature.DuplicateKeyValueAsArray) {
                        duplicateKeyValueAsArray = true;
                        break;
                    }
                }

                if (duplicateKeyValueAsArray) {
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
        ObjectReaderProvider provider = getReaderContext().getProvider();
        ObjectReader objectReader = provider.getObjectReader(rootObject.getClass());
        FieldReader fieldReader = objectReader.getFieldReader(nameHashCode);

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
        fieldReader.accept(rootObject, value);
    }

    @Override
    public void setCallback(Object object, BiFunction callback) {
        if (object instanceof Map) {
            Map map = (Map) object;
            Object originValue = map.get(name);
            if (originValue != null || map.containsKey(name)) {
                map.put(name, callback.apply(map, originValue));
            }
            return;
        }

        Class<?> objectClass = object.getClass();

        if (readerContext == null) {
            readerContext = JSONFactory.createReadContext();
        }
        FieldReader fieldReader = readerContext.provider
                .getObjectReader(objectClass)
                .getFieldReader(nameHashCode);

        if (writerContext == null) {
            writerContext = JSONFactory.createWriteContext();
        }
        FieldWriter fieldWriter = writerContext.provider
                .getObjectWriter(objectClass)
                .getFieldWriter(nameHashCode);

        if (fieldReader != null && fieldWriter != null) {
            Object fieldValue = fieldWriter.getFieldValue(object);
            Object value = callback.apply(object, fieldValue);
            fieldReader.accept(object, value);
        }
    }

    @Override
    public void setInt(Object obejct, int value) {
        if (obejct instanceof Map) {
            ((Map) obejct).put(name, value);
            return;
        }
        ObjectReaderProvider provider = getReaderContext().getProvider();
        ObjectReader objectReader = provider.getObjectReader(obejct.getClass());
        objectReader.setFieldValue(obejct, name, nameHashCode, value);
    }

    @Override
    public void setLong(Object object, long value) {
        if (object instanceof Map) {
            ((Map) object).put(name, value);
            return;
        }
        ObjectReaderProvider provider = getReaderContext().getProvider();
        ObjectReader objectReader = provider.getObjectReader(object.getClass());
        objectReader.setFieldValue(object, name, nameHashCode, value);
    }

    @Override
    public Object extract(JSONReader jsonReader) {
        if (jsonReader.isJSONB()) {
            if (jsonReader.nextIfObjectStart()) {
                while (!jsonReader.nextIfObjectEnd()) {
                    long nameHashCode = jsonReader.readFieldNameHashCode();
                    if (nameHashCode == 0) {
                        continue;
                    }

                    boolean match = nameHashCode == this.nameHashCode;
                    if (!match && (!jsonReader.isObject()) && !jsonReader.isArray()) {
                        jsonReader.skipValue();
                        continue;
                    }

                    if (jsonReader.isNumber()) {
                        return jsonReader.readNumber();
                    }

                    throw new JSONException("TODO");
                }
            }

            if ((features & Feature.AlwaysReturnList.mask) != 0) {
                return new JSONArray();
            }
            return null;
        }

        if (jsonReader.nextIfObjectStart()) {
            while (!jsonReader.nextIfObjectEnd()) {
                long nameHashCode = jsonReader.readFieldNameHashCode();
                boolean match = nameHashCode == this.nameHashCode;

                if (!match) {
                    jsonReader.skipValue();
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
                        val = jsonReader.readNumber();
                        break;
                    case '[':
                        val = jsonReader.readArray();
                        break;
                    case '{':
                        val = jsonReader.readObject();
                        break;
                    case '"':
                    case '\'':
                        val = jsonReader.readString(); //
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

                if ((features & Feature.AlwaysReturnList.mask) != 0) {
                    if (val == null) {
                        val = new JSONArray();
                    } else {
                        val = JSONArray.of(val);
                    }
                }

                return val;
            }
        }

        if ((features & Feature.AlwaysReturnList.mask) != 0) {
            return new JSONArray();
        }
        return null;
    }

    @Override
    public String extractScalar(JSONReader jsonReader) {
        if (jsonReader.nextIfObjectStart()) {
            for (; ; ) {
                if (jsonReader.ch == '}') {
                    jsonReader.next();
                    break;
                }

                long nameHashCode = jsonReader.readFieldNameHashCode();

                boolean match = nameHashCode == this.nameHashCode;
                char ch = jsonReader.ch;
                if (!match && ch != '{' && ch != '[') {
                    jsonReader.skipValue();
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
                        val = jsonReader.readNumber();
                        break;
                    case '[':
                        val = jsonReader.readArray();
                        break;
                    case '{':
                        val = jsonReader.readObject();
                        break;
                    case '"':
                    case '\'':
                        val = jsonReader.readString(); //
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

                return JSON.toJSONString(val);
            }
        }
        return null;
    }

    @Override
    public long extractInt64Value(JSONReader jsonReader) {
        if (jsonReader.nextIfObjectStart()) {
            _for:
            for (; ; ) {
                if (jsonReader.ch == '}') {
                    jsonReader.wasNull = true;
                    return 0;
                }

                long nameHashCode = jsonReader.readFieldNameHashCode();

                boolean match = nameHashCode == this.nameHashCode;
                if (!match) {
                    jsonReader.skipValue();
                    continue;
                }

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
                        return jsonReader.readInt64Value();
                    case '[':
                    case '{':
                        Map object = jsonReader.readObject();
                        return jsonReader.toLong(object);
                    case '"':
                    case '\'':
                        String str = jsonReader.readString();
                        return Long.parseLong(str);
                    case 't':
                    case 'f':
                        boolean booleanValue = jsonReader.readBoolValue();
                        return booleanValue ? 1L : 0L;
                    case 'n':
                        jsonReader.readNull();
                        jsonReader.wasNull = true;
                        return 0;
                    case ']':
                        jsonReader.next();
                        break _for;
                    default:
                        throw new JSONException("TODO : " + jsonReader.ch);
                }
            }
        }

        jsonReader.wasNull = true;
        return 0;
    }

    @Override
    public int extractInt32Value(JSONReader jsonReader) {
        if (jsonReader.nextIfObjectStart()) {
            _for:
            for (; ; ) {
                if (jsonReader.ch == '}') {
                    jsonReader.wasNull = true;
                    return 0;
                }

                long nameHashCode = jsonReader.readFieldNameHashCode();

                boolean match = nameHashCode == this.nameHashCode;
                if (!match) {
                    jsonReader.skipValue();
                    continue;
                }

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
                        return jsonReader.readInt32Value();
                    case '"':
                    case '\'':
                        String str = jsonReader.readString(); //
                        return Integer.parseInt(str);
                    case 't':
                    case 'f':
                        boolean booleanValue = jsonReader.readBoolValue();
                        return booleanValue ? 1 : 0;
                    case 'n':
                        jsonReader.readNull();
                        jsonReader.wasNull = true;
                        return 0;
                    case ']':
                        jsonReader.next();
                        break _for;
                    default:
                        throw new JSONException("TODO : " + jsonReader.ch);
                }
            }
        }

        jsonReader.wasNull = true;
        return 0;
    }

    @Override
    public void extractScalar(JSONReader jsonReader, ValueConsumer consumer) {
        if (jsonReader.nextIfObjectStart()) {
            _for:
            for (; ; ) {
                if (jsonReader.ch == '}') {
                    consumer.acceptNull();
                    return;
                }

                long nameHashCode = jsonReader.readFieldNameHashCode();

                boolean match = nameHashCode == this.nameHashCode;
                if (!match) {
                    jsonReader.skipValue();
                    continue;
                }

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
                    case '9': {
                        jsonReader.readNumber(consumer, false);
                        return;
                    }
                    case '[': {
                        List array = jsonReader.readArray();
                        consumer.accept(array);
                        return;
                    }
                    case '{': {
                        Map object = jsonReader.readObject();
                        consumer.accept(object);
                        return;
                    }
                    case '"':
                    case '\'': {
                        jsonReader.readString(consumer, false);
                        return;
                    }
                    case 't':
                    case 'f': {
                        consumer.accept(
                                jsonReader.readBoolValue()
                        );
                        return;
                    }
                    case 'n':
                        jsonReader.readNull();
                        consumer.acceptNull();
                        return;
                    case ']':
                        jsonReader.next();
                        break _for;
                    default:
                        throw new JSONException("TODO : " + jsonReader.ch);
                }
            }
        }

        consumer.acceptNull();
    }

    @Override
    public void extract(JSONReader jsonReader, ValueConsumer consumer) {
        if (jsonReader.nextIfObjectStart()) {
            for (; ; ) {
                if (jsonReader.ch == '}') {
                    consumer.acceptNull();
                    return;
                }

                long nameHashCode = jsonReader.readFieldNameHashCode();

                boolean match = nameHashCode == this.nameHashCode;
                if (!match) {
                    jsonReader.skipValue();
                    continue;
                }

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
                    case '9': {
                        jsonReader.readNumber(consumer, true);
                        return;
                    }
                    case '[': {
                        List array = jsonReader.readArray();
                        consumer.accept(array);
                        return;
                    }
                    case '{': {
                        Map object = jsonReader.readObject();
                        consumer.accept(object);
                        return;
                    }
                    case '"':
                    case '\'': {
                        jsonReader.readString(consumer, true);
                        return;
                    }
                    case 't':
                    case 'f': {
                        consumer.accept(
                                jsonReader.readBoolValue()
                        );
                        return;
                    }
                    case 'n':
                        jsonReader.readNull();
                        consumer.acceptNull();
                        return;
                    default:
                        throw new JSONException("TODO : " + jsonReader.ch);
                }
            }
        }

        consumer.acceptNull();
    }
}
