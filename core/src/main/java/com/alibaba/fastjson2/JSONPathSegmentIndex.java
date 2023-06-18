package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;

import java.lang.reflect.Array;
import java.util.*;

final class JSONPathSegmentIndex
        extends JSONPathSegment {
    static final JSONPathSegmentIndex ZERO = new JSONPathSegmentIndex(0);
    static final JSONPathSegmentIndex ONE = new JSONPathSegmentIndex(1);
    static final JSONPathSegmentIndex TWO = new JSONPathSegmentIndex(2);

    final int index;

    public JSONPathSegmentIndex(int index) {
        if (index < 0) {
            throw new JSONException("not support negative index");
        }
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

        if (object instanceof List) {
            List list = (List) object;
            if (index < list.size()) {
                context.value = list.get(index);
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
            if (index < array.length) {
                context.value = array[index];
            }
            context.eval = true;
            return;
        }

        Class objectClass = object.getClass();
        if (objectClass.isArray()) {
            int length = Array.getLength(object);
            if (index < length) {
                context.value = Array.get(object, index);
            }
            context.eval = true;
            return;
        }

        if (Map.class.isAssignableFrom(objectClass)) {
            context.value = eval((Map) object);
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
        Object value = object.get(index);
        if (value == null) {
            value = object.get(Integer.toString(index));
        }

        if (value == null) {
            int size = object.size();
            Iterator it = object.entrySet().iterator();
            if (size == 1 || object instanceof LinkedHashMap || object instanceof SortedMap) {
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
                for (int i = 0; i <= index && i < object.size() && it.hasNext(); ++i) {
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
    public String toString() {
        int size = IOUtils.stringSize(index);
        byte[] bytes = new byte[size + 2];
        bytes[0] = '[';
        IOUtils.writeInt32(bytes, 1, index);
        bytes[bytes.length - 1] = ']';
        return new String(bytes, IOUtils.ISO_8859_1);
    }
}
