package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

final class JSONPathSegmentName
        extends JSONPathSegment {
    final String name;
    final long nameHashCode;

    public JSONPathSegmentName(String name, long nameHashCode) {
        this.name = name;
        this.nameHashCode = nameHashCode;
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

        ObjectWriter<?> objectWriter;
        Class<?> objectClass = object.getClass();
        JSONWriter.Context writerContext = context.path.writerContext;
        if (writerContext != null) {
            objectWriter = writerContext.getObjectWriter(objectClass);
        } else {
            objectWriter = JSONFactory.defaultObjectWriterProvider.getObjectWriter(objectClass);
        }
        if (objectWriter instanceof ObjectWriterAdapter) {
            FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
            if (fieldWriter != null) {
                context.value = fieldWriter.getFieldValue(object);
            }

            return;
        }

        if (object instanceof Number || object instanceof Boolean) {
            context.value = null;
            return;
        }

        throw new JSONException("not support : " + objectClass);
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
        return nameHashCode == that.nameHashCode
                && (name == that.name) || (name != null && name.equals(that.name));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{name, nameHashCode});
    }

    @Override
    public String toString() {
        return name;
    }
}
