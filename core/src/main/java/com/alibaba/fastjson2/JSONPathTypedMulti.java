package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.time.ZoneId;
import java.util.function.BiFunction;

import static com.alibaba.fastjson2.util.IOUtils.DEFAULT_ZONE_ID;

class JSONPathTypedMulti
        extends JSONPath {
    final JSONPath[] paths;
    final Type[] types;
    final String[] formats;
    final long[] pathFeatures;
    final ZoneId zoneId;

    protected JSONPathTypedMulti(
            JSONPath[] paths,
            Type[] types,
            String[] formats,
            long[] pathFeatures,
            ZoneId zoneId,
            long features
    ) {
        super(JSON.toJSONString(paths), features);
        this.types = types;
        this.paths = paths;
        this.formats = formats;
        this.pathFeatures = pathFeatures;
        this.zoneId = zoneId;
    }

    @Override
    public boolean isRef() {
        for (JSONPath jsonPath : paths) {
            if (!jsonPath.isRef()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean contains(Object object) {
        for (JSONPath jsonPath : paths) {
            if (jsonPath.contains(object)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isIgnoreError(int index) {
        if (pathFeatures != null && index < pathFeatures.length) {
            long features = pathFeatures[index];
            return (features & Feature.NullOnError.mask) != 0;
        }
        return false;
    }

    @Override
    public Object eval(Object object) {
        Object[] array = new Object[paths.length];
        for (int i = 0; i < paths.length; i++) {
            JSONPath jsonPath = paths[i];
            Object result = jsonPath.eval(object);
            try {
                array[i] = TypeUtils.cast(result, types[i]);
            } catch (Exception e) {
                if (!isIgnoreError(i)) {
                    throw new JSONException("jsonpath eval path, path : " + jsonPath + ", msg : " + e.getMessage(), e);
                }
            }
        }
        return array;
    }

    protected JSONReader.Context createContext() {
        JSONReader.Context context = JSONFactory.createReadContext();
        if (zoneId != null && zoneId != DEFAULT_ZONE_ID) {
            context.zoneId = zoneId;
        }
        return context;
    }

    @Override
    public Object extract(JSONReader jsonReader) {
        Object object = jsonReader.readAny();
        return eval(object);
    }

    @Override
    public String extractScalar(JSONReader jsonReader) {
        Object object = extract(jsonReader);
        return JSON.toJSONString(object);
    }

    @Override
    public void set(Object object, Object value) {
        throw new JSONException("unsupported operation");
    }

    @Override
    public void set(Object object, Object value, JSONReader.Feature... readerFeatures) {
        throw new JSONException("unsupported operation");
    }

    @Override
    public void setCallback(Object object, BiFunction callback) {
        throw new JSONException("unsupported operation");
    }

    @Override
    public void setInt(Object object, int value) {
        throw new JSONException("unsupported operation");
    }

    @Override
    public void setLong(Object object, long value) {
        throw new JSONException("unsupported operation");
    }

    @Override
    public boolean remove(Object object) {
        throw new JSONException("unsupported operation");
    }
}
