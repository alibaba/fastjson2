package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.util.Map;

final class JSONPathSingleName
        extends JSONPathSingle {
    final long nameHashCode;
    final String name;

    public JSONPathSingleName(String path, JSONPathSegmentName segment) {
        super(segment, path);
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
            ObjectWriter<?> objectWriter;
            Class<?> objectClass = root.getClass();
            if (writerContext != null) {
                objectWriter = writerContext.getObjectWriter(objectClass);
            } else {
                objectWriter = JSONFactory.defaultObjectWriterProvider.getObjectWriter(objectClass);
            }
            if (objectWriter == null) {
                return null;
            }

            FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
            if (fieldWriter == null) {
                return null;
            }

            value = fieldWriter.getFieldValue(root);
        }
        return value;
    }

    @Override
    public boolean isRef() {
        return true;
    }
}
