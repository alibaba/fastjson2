package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

final class JSONPathMulti
        extends JSONPath {
    final List<JSONPathSegment> segments;
    final boolean ref;

    JSONPathMulti(String path, List<JSONPathSegment> segments, Feature... features) {
        super(path, features);
        this.segments = segments;

        boolean ref = true;
        for (int i = 0, l = segments.size(); i < l; i++) {
            JSONPathSegment segment = segments.get(i);
            if (segment instanceof JSONPathSegmentIndex || segment instanceof JSONPathSegmentName) {
                continue;
            }
            ref = false;
            break;
        }
        this.ref = ref;
    }

    @Override
    public boolean remove(Object root) {
        Context context = null;

        int size = segments.size();
        if (size == 0) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            JSONPathSegment segment = segments.get(i);
            JSONPathSegment nextSegment = null;
            int nextIndex = i + 1;
            if (nextIndex < size) {
                nextSegment = segments.get(nextIndex);
            }
            context = new Context(this, context, segment, nextSegment, 0);
            if (i == 0) {
                context.root = root;
            }

            if (i == size - 1) {
                return segment.remove(context);
            }
            segment.eval(context);

            if (context.value == null) {
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean contains(Object root) {
        Context context = null;

        int size = segments.size();
        if (size == 0) {
            return root != null;
        }

        for (int i = 0; i < size; i++) {
            JSONPathSegment segment = segments.get(i);
            JSONPathSegment nextSegment = null;
            int nextIndex = i + 1;
            if (nextIndex < size) {
                nextSegment = segments.get(nextIndex);
            }
            context = new Context(this, context, segment, nextSegment, 0);
            if (i == 0) {
                context.root = root;
            }

            if (i == size - 1) {
                return segment.contains(context);
            }
            segment.eval(context);
        }

        return false;
    }

    @Override
    public boolean isRef() {
        return ref;
    }

    @Override
    public Object eval(Object root) {
        Context context = null;

        int size = segments.size();
        if (size == 0) {
            return root;
        }

        for (int i = 0; i < size; i++) {
            JSONPathSegment segment = segments.get(i);
            JSONPathSegment nextSegment = null;
            int nextIndex = i + 1;
            if (nextIndex < size) {
                nextSegment = segments.get(nextIndex);
            }
            context = new Context(this, context, segment, nextSegment, 0);
            if (i == 0) {
                context.root = root;
            }

            segment.eval(context);
        }

        Object contextValue = context.value;
        if ((context.path.features & Feature.AlwaysReturnList.mask) != 0) {
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
        Context context = null;
        int size = segments.size();
        for (int i = 0; i < size - 1; i++) {
            JSONPathSegment segment = segments.get(i);
            JSONPathSegment nextSegment = null;
            int nextIndex = i + 1;
            if (nextIndex < size) {
                nextSegment = segments.get(nextIndex);
            }
            context = new Context(this, context, segment, nextSegment, 0L);
            if (i == 0) {
                context.root = root;
            }

            segment.eval(context);
            if (context.value == null && nextSegment != null) {
                if (value == null) {
                    return;
                }

                Object parentObject;
                if (i == 0) {
                    parentObject = root;
                } else {
                    parentObject = context.parent.value;
                }

                Object emptyValue;
                if (nextSegment instanceof JSONPathSegmentIndex) {
                    emptyValue = new JSONArray();
                } else if (nextSegment instanceof JSONPathSegmentName) {
                    emptyValue = new JSONObject();
                } else {
                    return;
                }
                context.value = emptyValue;

                if (parentObject instanceof Map && segment instanceof JSONPathSegmentName) {
                    ((Map) parentObject).put(((JSONPathSegmentName) segment).name, emptyValue);
                } else if (parentObject instanceof List && segment instanceof JSONPathSegmentIndex) {
                    List list = (List) parentObject;
                    int index = ((JSONPathSegmentIndex) segment).index;
                    if (index == list.size()) {
                        list.add(emptyValue);
                    } else {
                        list.set(index, emptyValue);
                    }
                } else if (parentObject != null) {
                    Class<?> parentObjectClass = parentObject.getClass();
                    JSONReader.Context readerContext = getReaderContext();
                    ObjectReader<?> objectReader = readerContext.getObjectReader(parentObjectClass);
                    if (segment instanceof JSONPathSegmentName) {
                        FieldReader fieldReader = objectReader.getFieldReader(((JSONPathSegmentName) segment).nameHashCode);
                        if (fieldReader != null) {
                            ObjectReader fieldObjectReader = fieldReader.getObjectReader(readerContext);
                            Object fieldValue = fieldObjectReader.createInstance();
                            fieldReader.accept(parentObject, fieldValue);
                            context.value = fieldValue;
                        }
                    }
                }
            }
        }
        context = new Context(this, context, segments.get(0), null, 0L);
        context.root = root;

        JSONPathSegment segment = segments.get(size - 1);
        segment.set(context, value);
    }

    @Override
    public void set(Object root, Object value, JSONReader.Feature... readerFeatures) {
        long features = 0;
        for (JSONReader.Feature feature : readerFeatures) {
            features |= feature.mask;
        }

        Context context = null;
        int size = segments.size();
        for (int i = 0; i < size - 1; i++) {
            JSONPathSegment segment = segments.get(i);
            JSONPathSegment nextSegment = null;
            int nextIndex = i + 1;
            if (nextIndex < size) {
                nextSegment = segments.get(nextIndex);
            }
            context = new Context(this, context, segment, nextSegment, features);
            if (i == 0) {
                context.root = root;
            }

            segment.eval(context);
        }
        context = new Context(this, context, segments.get(0), null, features);
        context.root = root;

        JSONPathSegment segment = segments.get(size - 1);
        segment.set(context, value);
    }

    @Override
    public void setCallback(Object root, BiFunction callback) {
        Context context = null;
        int size = segments.size();
        for (int i = 0; i < size - 1; i++) {
            JSONPathSegment segment = segments.get(i);
            JSONPathSegment nextSegment = null;
            int nextIndex = i + 1;
            if (nextIndex < size) {
                nextSegment = segments.get(nextIndex);
            }
            context = new Context(this, context, segment, nextSegment, 0);
            if (i == 0) {
                context.root = root;
            }

            segment.eval(context);
        }
        context = new Context(this, context, segments.get(0), null, 0);
        context.root = root;

        JSONPathSegment segment = segments.get(size - 1);
        segment.setCallback(context, callback);
    }

    @Override
    public void setInt(Object rootObject, int value) {
        set(rootObject, value);
    }

    @Override
    public void setLong(Object rootObject, long value) {
        set(rootObject, value);
    }

    @Override
    public Object extract(JSONReader jsonReader) {
        if (jsonReader == null) {
            return null;
        }

        int size = segments.size();
        if (size == 0) {
            return null;
        }

        boolean eval = false;
        Context context = null;
        for (int i = 0; i < size; i++) {
            JSONPathSegment segment = segments.get(i);
            JSONPathSegment nextSegment = null;

            int nextIndex = i + 1;
            if (nextIndex < size) {
                nextSegment = segments.get(nextIndex);
            }

            context = new Context(this, context, segment, nextSegment, 0);
            if (eval) {
                segment.eval(context);
            } else {
                segment.accept(jsonReader, context);
            }

            if (context.eval) {
                eval = true;
                if (context.value == null) {
                    break;
                }
            }
        }

        Object value = context.value;
        if (value instanceof Sequence) {
            value = ((Sequence) value).values;
        }

        if ((features & Feature.AlwaysReturnList.mask) != 0) {
            if (value == null) {
                value = new JSONArray();
            } else if (!(value instanceof List)) {
                value = JSONArray.of(value);
            }
        }
        return value;
    }

    @Override
    public String extractScalar(JSONReader jsonReader) {
        int size = segments.size();
        if (size == 0) {
            return null;
        }

        boolean eval = false;
        Context context = null;
        for (int i = 0; i < size; i++) {
            JSONPathSegment segment = segments.get(i);
            JSONPathSegment nextSegment = null;

            int nextIndex = i + 1;
            if (nextIndex < size) {
                nextSegment = segments.get(nextIndex);
            }

            context = new Context(this, context, segment, nextSegment, 0);
            if (eval) {
                segment.eval(context);
            } else {
                segment.accept(jsonReader, context);
            }

            if (context.eval) {
                eval = true;
                if (context.value == null) {
                    break;
                }
            }
        }

        return JSON.toJSONString(context.value);
    }
}
