package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;

final class JSONPathTypedMultiIndexes
        extends JSONPathTypedMulti {
    final JSONPath prefix;
    final JSONPath[] indexPaths;
    final int[] indexes;
    final int maxIndex;

    JSONPathTypedMultiIndexes(
            JSONPath[] paths,
            JSONPath prefix,
            JSONPath[] indexPaths,
            Type[] types,
            String[] formats,
            long[] pathFeatures,
            ZoneId zoneId,
            long features
    ) {
        super(paths, types, formats, pathFeatures, zoneId, features);
        this.prefix = prefix;
        this.indexPaths = indexPaths;
        int[] indexes = new int[paths.length];
        for (int i = 0; i < indexPaths.length; i++) {
            JSONPathSingleIndex indexPath = (JSONPathSingleIndex) indexPaths[i];
            indexes[i] = indexPath.index;
        }
        this.indexes = indexes;
        this.maxIndex = java.util.Arrays.stream(indexes).max().getAsInt();
    }

    @Override
    public Object eval(Object root) {
        Object[] array = new Object[paths.length];

        Object object = root;
        if (prefix != null) {
            object = prefix.eval(root);
        }

        if (object == null) {
            return null;
        }

        if (object instanceof List) {
            List list = (List) object;
            for (int i = 0; i < indexes.length; i++) {
                Object result = list.get(indexes[i]);
                Type type = types[i];
                try {
                    if (result != null && result.getClass() != type) {
                        if (type == Long.class) {
                            result = TypeUtils.toLong(result);
                        } else if (type == BigDecimal.class) {
                            result = TypeUtils.toBigDecimal(result);
                        } else if (type == String[].class) {
                            result = TypeUtils.toStringArray(result);
                        } else {
                            result = TypeUtils.cast(result, type);
                        }
                    }
                    array[i] = result;
                } catch (Exception e) {
                    if (!isIgnoreError(i)) {
                        throw new JSONException("jsonpath eval path, path : " + paths[i] + ", msg : " + e.getMessage(), e);
                    }
                }
            }
        } else {
            for (int i = 0; i < paths.length; i++) {
                JSONPath jsonPath = indexPaths[i];
                Type type = types[i];
                try {
                    Object result = jsonPath.eval(object);
                    if (result != null && result.getClass() != type) {
                        if (type == Long.class) {
                            result = TypeUtils.toLong(result);
                        } else if (type == BigDecimal.class) {
                            result = TypeUtils.toBigDecimal(result);
                        } else if (type == String[].class) {
                            result = TypeUtils.toStringArray(result);
                        } else {
                            result = TypeUtils.cast(result, type);
                        }
                    }
                    array[i] = result;
                } catch (Exception e) {
                    if (!isIgnoreError(i)) {
                        throw new JSONException("jsonpath eval path, path : " + paths[i] + ", msg : " + e.getMessage(), e);
                    }
                }
            }
        }

        return array;
    }

    @Override
    public Object extract(JSONReader jsonReader) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        if (prefix instanceof JSONPathSingleName) {
            JSONPathSingleName prefixName = (JSONPathSingleName) prefix;
            long prefixNameHash = prefixName.nameHashCode;
            if (!jsonReader.nextIfObjectStart()) {
                throw new JSONException(jsonReader.info("illegal input, expect '[', but " + jsonReader.current()));
            }

            while (!jsonReader.nextIfObjectEnd()) {
                long nameHashCode = jsonReader.readFieldNameHashCode();
                boolean match = nameHashCode == prefixNameHash;
                if (!match && (!jsonReader.isObject()) && !jsonReader.isArray()) {
                    jsonReader.skipValue();
                    continue;
                }

                break;
            }

            if (jsonReader.nextIfNull()) {
                return null;
            }
        } else if (prefix instanceof JSONPathSingleIndex) {
            int index = ((JSONPathSingleIndex) prefix).index;
            int max = jsonReader.startArray();
            for (int i = 0; i < index && i < max; i++) {
                jsonReader.skipValue();
            }

            if (jsonReader.nextIfNull()) {
                return null;
            }
        } else if (prefix != null) {
            Object object = jsonReader.readAny();
            return eval(object);
        }

        int max = jsonReader.startArray();
        Object[] array = new Object[indexes.length];
        for (int i = 0; i <= maxIndex && i < max; ++i) {
            Integer index = null;
            for (int j = 0; j < indexes.length; j++) {
                if (indexes[j] == i) {
                    index = j;
                    break;
                }
            }
            if (index == null) {
                jsonReader.skipValue();
                continue;
            }

            Type type = types[index];
            array[index] = jsonReader.read(type);
        }
        return array;
    }
}
