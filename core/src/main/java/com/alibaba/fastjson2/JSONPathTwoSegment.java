package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

class JSONPathTwoSegment
        extends JSONPath {
    final JSONPathSegment first;
    final JSONPathSegment second;
    final boolean ref;

    JSONPathTwoSegment(String path, JSONPathSegment first, JSONPathSegment second, Feature... features) {
        super(path, features);
        this.first = first;
        this.second = second;
        this.ref = (first instanceof JSONPathSegmentIndex || first instanceof JSONPathSegmentName)
                && (second instanceof JSONPathSegmentIndex || second instanceof JSONPathSegmentName);
    }

    @Override
    public boolean remove(Object root) {
        Context context0 = new Context(this, null, first, second, 0);
        context0.root = root;
        first.eval(context0);
        if (context0.value == null) {
            return false;
        }

        Context context1 = new Context(this, context0, second, null, 0);
        return second.remove(context1);
    }

    @Override
    public boolean contains(Object root) {
        Context context0 = new Context(this, null, first, second, 0);
        context0.root = root;
        first.eval(context0);
        if (context0.value == null) {
            return false;
        }

        Context context1 = new Context(this, context0, second, null, 0);
        return second.contains(context1);
    }

    @Override
    public boolean isRef() {
        return ref;
    }

    @Override
    public Object eval(Object root) {
        Context context0 = new Context(this, null, first, second, 0);
        context0.root = root;
        first.eval(context0);
        if (context0.value == null) {
            return null;
        }

        Context context1 = new Context(this, context0, second, null, 0);
        second.eval(context1);
        Object contextValue = context1.value;
        if ((features & Feature.AlwaysReturnList.mask) != 0) {
            if (contextValue == null) {
                contextValue = new JSONArray();
            } else if (!(contextValue instanceof List)) {
                contextValue = JSONArray.of(contextValue);
            }
        }
        return contextValue;
    }

    @Override
    public void set(Object root, Object value) {
        Context context0 = new Context(this, null, first, second, 0);
        context0.root = root;
        first.eval(context0);
        if (context0.value == null) {
            Object emptyValue;
            if (second instanceof JSONPathSegmentIndex) {
                emptyValue = new JSONArray();
            } else if (second instanceof JSONPathSegmentName) {
                emptyValue = new JSONObject();
            } else {
                return;
            }

            context0.value = emptyValue;
            if (root instanceof Map && first instanceof JSONPathSegmentName) {
                ((Map) root).put(((JSONPathSegmentName) first).name, emptyValue);
            } else if (root instanceof List && first instanceof JSONPathSegmentIndex) {
                ((List) root).set(((JSONPathSegmentIndex) first).index, emptyValue);
            } else if (root != null) {
                Class<?> parentObjectClass = root.getClass();
                JSONReader.Context readerContext = getReaderContext();
                ObjectReader<?> objectReader = readerContext.getObjectReader(parentObjectClass);
                if (first instanceof JSONPathSegmentName) {
                    FieldReader fieldReader = objectReader.getFieldReader(((JSONPathSegmentName) first).nameHashCode);
                    if (fieldReader != null) {
                        ObjectReader fieldObjectReader = fieldReader.getObjectReader(readerContext);
                        Object fieldValue = fieldObjectReader.createInstance();
                        fieldReader.accept(root, fieldValue);
                        context0.value = fieldValue;
                    }
                }
            }
        }

        Context context1 = new Context(this, context0, second, null, 0);
        second.set(context1, value);
    }

    @Override
    public void set(Object root, Object value, JSONReader.Feature... readerFeatures) {
        long features = 0;
        for (JSONReader.Feature feature : readerFeatures) {
            features |= feature.mask;
        }

        Context context0 = new Context(this, null, first, second, features);
        context0.root = root;
        first.eval(context0);
        if (context0.value == null) {
            return;
        }

        Context context1 = new Context(this, context0, second, null, features);
        second.set(context1, value);
    }

    @Override
    public void setCallback(Object root, BiFunction callback) {
        Context context0 = new Context(this, null, first, second, 0);
        context0.root = root;
        first.eval(context0);
        if (context0.value == null) {
            return;
        }

        Context context1 = new Context(this, context0, second, null, 0);
        second.setCallback(context1, callback);
    }

    @Override
    public void setInt(Object root, int value) {
        Context context0 = new Context(this, null, first, second, 0);
        context0.root = root;
        first.eval(context0);
        if (context0.value == null) {
            return;
        }

        Context context1 = new Context(this, context0, second, null, 0);
        second.setInt(context1, value);
    }

    @Override
    public void setLong(Object root, long value) {
        Context context0 = new Context(this, null, first, second, 0);
        context0.root = root;
        first.eval(context0);
        if (context0.value == null) {
            return;
        }

        Context context1 = new Context(this, context0, second, null, 0);
        second.setLong(context1, value);
    }

    @Override
    public Object extract(JSONReader jsonReader) {
        if (jsonReader == null) {
            return null;
        }

        Context context0 = new Context(this, null, first, second, 0);
        first.accept(jsonReader, context0);

        Context context1 = new Context(this, context0, second, null, 0);

        if (context0.eval) {
            second.eval(context1);
        } else {
            second.accept(jsonReader, context1);
        }

        Object contextValue = context1.value;

        if ((features & Feature.AlwaysReturnList.mask) != 0) {
            if (contextValue == null) {
                contextValue = new JSONArray();
            } else if (!(contextValue instanceof List)) {
                contextValue = JSONArray.of(contextValue);
            }
        }

        return contextValue;
    }

    @Override
    public String extractScalar(JSONReader jsonReader) {
        Context context0 = new Context(this, null, first, second, 0);
        first.accept(jsonReader, context0);

        Context context1 = new Context(this, context0, second, null, 0);
        second.accept(jsonReader, context1);

        return JSON.toJSONString(context1.value);
    }
}
