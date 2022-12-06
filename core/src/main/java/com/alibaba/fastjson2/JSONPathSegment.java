package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderBean;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.alibaba.fastjson2.JSONB.Constants.BC_OBJECT;
import static com.alibaba.fastjson2.JSONB.Constants.BC_OBJECT_END;
import static com.alibaba.fastjson2.JSONReader.EOI;

abstract class JSONPathSegment {
    public abstract void accept(JSONReader jsonReader, JSONPath.Context context);

    public abstract void eval(JSONPath.Context context);

    public boolean contains(JSONPath.Context context) {
        eval(context);
        return context.value != null;
    }

    public boolean remove(JSONPath.Context context) {
        throw new JSONException("UnsupportedOperation " + getClass());
    }

    public void set(JSONPath.Context context, Object value) {
        throw new JSONException("UnsupportedOperation " + getClass());
    }

    public void setCallback(JSONPath.Context context, BiFunction callback) {
        throw new JSONException("UnsupportedOperation " + getClass());
    }

    public void setInt(JSONPath.Context context, int value) {
        set(context, Integer.valueOf(value));
    }

    public void setLong(JSONPath.Context context, long value) {
        set(context, Long.valueOf(value));
    }

    interface EvalSegment {
    }

    static final class RandomIndexSegment
            extends JSONPathSegment {
        public static final RandomIndexSegment INSTANCE = new RandomIndexSegment();

        Random random;

        @Override
        public void accept(JSONReader jsonReader, JSONPath.Context context) {
            if (context.parent != null
                    && (context.parent.eval
                    || (context.parent.current instanceof CycleNameSegment && context.next == null))) {
                eval(context);
                return;
            }

            if (jsonReader.isJSONB()) {
                JSONArray array = new JSONArray();

                {
                    int itemCnt = jsonReader.startArray();
                    for (int i = 0; i < itemCnt; i++) {
                        array.add(jsonReader.readAny());
                    }
                }

                // lazy init for graalvm
                if (random == null) {
                    random = new Random();
                }

                int index = Math.abs(random.nextInt()) % array.size();
                context.value = array.get(index);
                context.eval = true;
                return;
            }

            JSONArray array = new JSONArray();
            jsonReader.next();
            for (int i = 0; jsonReader.ch != EOI; ++i) {
                if (jsonReader.ch == ']') {
                    jsonReader.next();
                    break;
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
                        val = jsonReader.readArray();
                        break;
                    case '{':
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

                array.add(val);
            }

            // lazy init for graalvm
            if (random == null) {
                random = new Random();
            }

            int index = Math.abs(random.nextInt()) % array.size();
            context.value = array.get(index);
            context.eval = true;
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof java.util.List) {
                List list = (List) object;
                if (list.isEmpty()) {
                    return;
                }

                // lazy init for graalvm
                if (random == null) {
                    random = new Random();
                }

                int randomIndex = Math.abs(random.nextInt()) % list.size();
                context.value = list.get(randomIndex);
                context.eval = true;
                return;
            }

            if (object instanceof Object[]) {
                Object[] array = (Object[]) object;
                if (array.length == 0) {
                    return;
                }

                // lazy init for graalvm
                if (random == null) {
                    random = new Random();
                }

                int randomIndex = random.nextInt() % array.length;
                context.value = array[randomIndex];
                context.eval = true;
                return;
            }

            throw new JSONException("TODO");
        }
    }

    static final class RangeIndexSegment
            extends JSONPathSegment {
        final int begin;
        final int end;

        public RangeIndexSegment(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            List result = new JSONArray();

            if (object instanceof List) {
                List list = (List) object;
                for (int i = 0, size = list.size(); i < size; i++) {
                    boolean match;
                    if (begin >= 0) {
                        match = i >= begin && i < end;
                    } else {
                        int ni = i - size;
                        match = ni >= begin && ni < end;
                    }
                    if (match) {
                        result.add(list.get(i));
                    }
                }
                context.value = result;
                context.eval = true;
                return;
            }

            if (object instanceof Object[]) {
                Object[] array = (Object[]) object;
                for (int i = 0; i < array.length; i++) {
                    boolean match = i >= begin && i <= end
                            || i - array.length > begin && i - array.length <= end;
                    if (match) {
                        result.add(array[i]);
                    }
                }
                context.value = result;
                context.eval = true;
                return;
            }

            throw new JSONException("TODO");
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
                JSONArray array = new JSONArray();

                {
                    int itemCnt = jsonReader.startArray();
                    for (int i = 0; i < itemCnt; i++) {
                        boolean match = begin < 0 || i >= begin && i < end;

                        if (!match) {
                            jsonReader.skipValue();
                            continue;
                        }

                        array.add(
                                jsonReader.readAny());
                    }
                }

                if (begin < 0) {
                    for (int size = array.size(), i = size - 1; i >= 0; i--) {
                        int ni = i - size;
                        if (ni < begin || ni >= end) {
                            array.remove(i);
                        }
                    }
                }

                context.value = array;
                context.eval = true;
                return;
            }

            JSONArray array = new JSONArray();
            jsonReader.next();
            for (int i = 0; jsonReader.ch != EOI; ++i) {
                if (jsonReader.ch == ']') {
                    jsonReader.next();
                    break;
                }

                boolean match = begin < 0 || i >= begin && i < end;

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
                        val = jsonReader.readArray();
                        break;
                    case '{':
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

                array.add(val);
            }

            if (begin < 0) {
                for (int size = array.size(), i = size - 1; i >= 0; i--) {
                    int ni = i - size;
                    if (ni < begin || ni >= end) {
                        array.remove(i);
                    }
                }
            }
            context.value = array;
            context.eval = true;
        }

        @Override
        public void set(JSONPath.Context context, Object value) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof List) {
                List list = (List) object;
                for (int i = 0, size = list.size(); i < size; i++) {
                    boolean match;
                    if (begin >= 0) {
                        match = i >= begin && i < end;
                    } else {
                        int ni = i - size;
                        match = ni >= begin && ni < end;
                    }
                    if (match) {
                        list.set(i, value);
                    }
                }
                return;
            }

            throw new JSONException("UnsupportedOperation " + getClass());
        }

        @Override
        public boolean remove(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof List) {
                List list = (List) object;
                int removeCount = 0;
                for (int size = list.size(), i = size - 1; i >= 0; i--) {
                    boolean match;
                    if (begin >= 0) {
                        match = i >= begin && i < end;
                    } else {
                        int ni = i - size;
                        match = ni >= begin && ni < end;
                    }
                    if (match) {
                        list.remove(i);
                        removeCount++;
                    }
                }
                return removeCount > 0;
            }

            throw new JSONException("UnsupportedOperation " + getClass());
        }
    }

    static final class MultiIndexSegment
            extends JSONPathSegment {
        final int[] indexes;

        public MultiIndexSegment(int[] indexes) {
//            Arrays.sort(indexes);
            this.indexes = indexes;
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            List result = new JSONArray();

            if (object instanceof JSONPath.Sequence) {
                List list = ((JSONPath.Sequence) object).values;

                for (Object item : list) {
                    context.value = item;
                    JSONPath.Context itemContext = new JSONPath.Context(context.path, context, context.current, context.next, context.readerFeatures);
                    eval(itemContext);
                    Object value = itemContext.value;

                    if (value instanceof Collection) {
                        result.addAll((Collection) value);
                    } else {
                        result.add(value);
                    }
                }
                context.value = result;
                return;
            }

            for (int index : indexes) {
                Object value;

                if (object instanceof List) {
                    List list = (List) object;

                    if (index >= 0) {
                        if (index < list.size()) {
                            value = list.get(index);
                        } else {
                            continue;
                        }
                    } else {
                        int itemIndex = list.size() + index;
                        if (itemIndex >= 0) {
                            value = list.get(itemIndex);
                        } else {
                            continue;
                        }
                    }
                } else if (object instanceof Object[]) {
                    Object[] array = (Object[]) object;
                    if (index >= 0) {
                        if (index < array.length) {
                            value = array[index];
                        } else {
                            continue;
                        }
                    } else {
                        int itemIndex = array.length + index;
                        if (itemIndex >= 0) {
                            value = array[itemIndex];
                        } else {
                            continue;
                        }
                    }
                } else {
                    continue;
                }

                if (value instanceof Collection) {
                    result.addAll((Collection) value);
                } else {
                    result.add(value);
                }
            }
            context.value = result;
            return;
        }

        @Override
        public void accept(JSONReader jsonReader, JSONPath.Context context) {
            if (context.parent != null
                    && context.parent.current instanceof CycleNameSegment
                    && context.next == null) {
                eval(context);
                return;
            }

            if (jsonReader.isJSONB()) {
                JSONArray array = new JSONArray();
                int itemCnt = jsonReader.startArray();
                for (int i = 0; i < itemCnt; i++) {
                    boolean match = Arrays.binarySearch(indexes, i) >= 0;
                    if (!match) {
                        jsonReader.skipValue();
                        continue;
                    }

                    array.add(jsonReader.readAny());
                }
                context.value = array;
                return;
            }

            JSONArray array = new JSONArray();

            jsonReader.next();
            for (int i = 0; jsonReader.ch != EOI; ++i) {
                if (jsonReader.ch == ']') {
                    jsonReader.next();
                    break;
                }

                boolean match = Arrays.binarySearch(indexes, i) >= 0;

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
                        val = jsonReader.readArray();
                        break;
                    case '{':
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

                array.add(val);
            }
            context.value = array;
        }
    }

    static final class MultiNameSegment
            extends JSONPathSegment {
        final String[] names;
        final long[] nameHashCodes;
        final Set<String> nameSet;

        public MultiNameSegment(String[] names) {
            this.names = names;
            this.nameHashCodes = new long[names.length];
            this.nameSet = new HashSet<>();
            for (int i = 0; i < names.length; i++) {
                nameHashCodes[i] = Fnv.hashCode64(names[i]);
                nameSet.add(names[i]);
            }
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof Map) {
                Map map = (Map) object;
                JSONArray array = new JSONArray(names.length);
                for (String name : names) {
                    Object value = map.get(name);
                    array.add(value);
                }
                context.value = array;
                return;
            }

            if (object instanceof Collection) {
                // skip
                context.value = object;
                return;
            }

            ObjectWriterProvider provider = context.path.getWriterContext().provider;
            ObjectWriter objectWriter = provider.getObjectWriter(object.getClass());

            JSONArray array = new JSONArray(names.length);
            for (int i = 0; i < names.length; i++) {
                FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCodes[i]);
                Object fieldValue = null;
                if (fieldWriter != null) {
                    fieldValue = fieldWriter.getFieldValue(object);
                }
                array.add(fieldValue);
            }
            context.value = array;
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

            Object object = jsonReader.readAny();
            if (object instanceof Map) {
                Map map = (Map) object;
                JSONArray array = new JSONArray(names.length);
                for (String name : names) {
                    Object value = map.get(name);
                    array.add(value);
                }
                context.value = array;
                return;
            }

            if (object instanceof Collection) {
                // skip
                context.value = object;
                return;
            }

            ObjectWriterProvider provider = context.path.getWriterContext().provider;
            ObjectWriter objectWriter = provider.getObjectWriter(object.getClass());

            JSONArray array = new JSONArray(names.length);
            for (int i = 0; i < names.length; i++) {
                FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCodes[i]);
                Object fieldValue = null;
                if (fieldWriter != null) {
                    fieldValue = fieldWriter.getFieldValue(object);
                }
                array.add(fieldValue);
            }
            context.value = array;
            return;
        }
    }

    static final class AllSegment
            extends JSONPathSegment {
        static final AllSegment INSTANCE = new AllSegment(false);
        static final AllSegment INSTANCE_ARRAY = new AllSegment(true);

        final boolean array;

        AllSegment(boolean array) {
            this.array = array;
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object == null) {
                context.value = null;
                context.eval = true;
                return;
            }

            if (object instanceof Map) {
                Map map = (Map) object;
                JSONArray array = new JSONArray(map.size());
                for (Object value : map.values()) {
                    if (this.array && value instanceof Collection) {
                        array.addAll((Collection) value);
                    } else {
                        array.add(value);
                    }
                }
                if (context.next != null) {
                    context.value = new JSONPath.Sequence(array);
                } else {
                    context.value = array;
                }
                context.eval = true;
                return;
            }

            if (object instanceof List) {
                List list = (List) object;
                JSONArray values = new JSONArray(list.size());
                if (context.next == null && !array) {
                    for (Object item : list) {
                        if (item instanceof Map) {
                            values.addAll(((Map<?, ?>) item).values());
                        } else {
                            values.add(item);
                        }
                    }
                    context.value = values;
                    context.eval = true;
                    return;
                }

                if (context.next != null) {
                    context.value = new JSONPath.Sequence(list);
                } else {
                    context.value = object;
                }
                context.eval = true;
                return;
            }

            if (object instanceof Collection) {
                // skip
                context.value = object;
                context.eval = true;
                return;
            }

            if (object instanceof JSONPath.Sequence) {
                List list = ((JSONPath.Sequence) object).values;
                JSONArray values = new JSONArray(list.size());
                if (context.next == null && !array) {
                    for (Object item : list) {
                        if (item instanceof Map) {
                            values.addAll(((Map<?, ?>) item).values());
                        } else {
                            values.add(item);
                        }
                    }
                    context.value = values;
                    context.eval = true;
                    return;
                }

                if (context.next != null) {
                    context.value = new JSONPath.Sequence(list);
                } else {
                    context.value = object;
                }
                context.eval = true;
                return;
            }

            ObjectWriterProvider provider = context.path.getWriterContext().provider;
            ObjectWriter objectWriter = provider.getObjectWriter(object.getClass());
            List<FieldWriter> fieldWriters = objectWriter.getFieldWriters();
            int size = fieldWriters.size();
            JSONArray array = new JSONArray(size);
            for (int i = 0; i < size; i++) {
                Object fieldValue = fieldWriters.get(i).getFieldValue(object);
                array.add(fieldValue);
            }
            context.value = array;
            context.eval = true;
        }

        @Override
        public boolean remove(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof Map) {
                ((Map<?, ?>) object).clear();
                return true;
            }

            if (object instanceof Collection) {
                ((Collection<?>) object).clear();
                return true;
            }

            throw new JSONException("UnsupportedOperation " + getClass());
        }

        @Override
        public void accept(JSONReader jsonReader, JSONPath.Context context) {
            if (context.parent != null && context.parent.eval) {
                eval(context);
                return;
            }

            if (jsonReader.isJSONB()) {
                List<Object> values = new JSONArray();
                if (jsonReader.nextIfMatch(BC_OBJECT)) {
                    while (!jsonReader.nextIfMatch(BC_OBJECT_END)) {
                        if (jsonReader.skipName()) {
                            Object val = jsonReader.readAny();

                            if (array && val instanceof Collection) {
                                values.addAll((Collection) val);
                            } else {
                                values.add(val);
                            }
                        }
                    }

                    context.value = values;
                    return;
                }

                if (jsonReader.isArray() && context.next != null) {
                    // skip
                    return;
                }

                throw new JSONException("TODO");
            }

            boolean alwaysReturnList = context.next == null && (context.path.features & JSONPath.Feature.AlwaysReturnList.mask) != 0;
            List<Object> values = new JSONArray();

            if (jsonReader.nextIfMatch('{')) {
                _for:
                for (; ; ) {
                    if (jsonReader.ch == '}') {
                        jsonReader.next();
                        break;
                    }

                    jsonReader.skipName();
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
                            val = jsonReader.readArray();
                            break;
                        case '{':
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
                        case ']':
                            jsonReader.next();
                            break _for;
                        default:
                            throw new JSONException("TODO : " + jsonReader.ch);
                    }

                    if (val instanceof Collection) {
                        if (alwaysReturnList) {
                            values.add(val);
                        } else {
                            values.addAll((Collection) val);
                        }
                    } else {
                        values.add(val);
                    }

                    if (jsonReader.ch == ',') {
                        jsonReader.next();
                    }
                }
                context.value = values;
                context.eval = true;
                return;
            }

            if (jsonReader.ch == '[') {
                jsonReader.next();
                for (; ; ) {
                    if (jsonReader.ch == ']') {
                        jsonReader.next();
                        break;
                    }
                    Object value = jsonReader.readAny();
                    if (context.next == null && value instanceof Map) {
                        values.addAll(((Map<?, ?>) value).values());
                    } else {
                        values.add(value);
                    }
                    if (jsonReader.ch == ',') {
                        jsonReader.next();
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

            throw new JSONException("TODO");
        }
    }

    static final class SelfSegment
            extends JSONPathSegment {
        static final SelfSegment INSTANCE = new SelfSegment();

        protected SelfSegment() {
        }

        @Override
        public void accept(JSONReader jsonReader, JSONPath.Context context) {
            context.value = jsonReader.readAny();
            context.eval = true;
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;
            context.value = object;
        }
    }

    static final class RootSegment
            extends JSONPathSegment {
        static final RootSegment INSTANCE = new RootSegment();

        protected RootSegment() {
        }

        @Override
        public void accept(JSONReader jsonReader, JSONPath.Context context) {
            if (context.parent != null) {
                throw new JSONException("not support operation");
            }
            context.value = jsonReader.readAny();
            context.eval = true;
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.root;
            context.value = object;
        }
    }

    static final class CycleNameSegment
            extends JSONPathSegment {
        static final long HASH_STAR = Fnv.hashCode64("*");
        final String name;
        final long nameHashCode;

        public CycleNameSegment(String name, long nameHashCode) {
            this.name = name;
            this.nameHashCode = nameHashCode;
        }

        @Override
        public String toString() {
            return ".." + name;
        }

        @Override
        public boolean remove(JSONPath.Context context) {
            set(context, null);
            return context.eval = true;
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            List values = new JSONArray();

            MapLoop action = new MapLoop(context, values);
            if (object instanceof Map) {
                Map map = (Map) object;
                map.forEach(action);
            } else if (object instanceof Collection) {
                ((Collection<?>) object).forEach(action);
            } else if (object != null) {
                ObjectWriter<?> objectWriter = context.path
                        .getWriterContext()
                        .getObjectWriter(object.getClass());
                if (objectWriter instanceof ObjectWriterAdapter) {
                    action.accept(object);
                }
            }

            if (values.size() == 1 && values.get(0) instanceof Collection) {
                context.value = values.get(0);
            } else {
                context.value = values;
            }
            context.eval = true;
        }

        @Override
        public void set(JSONPath.Context context, Object value) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            LoopSet action = new LoopSet(context, value);
            action.accept(object);
        }

        @Override
        public void setCallback(JSONPath.Context context, BiFunction callback) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            LoopCallback action = new LoopCallback(context, callback);
            action.accept(object);
        }

        class MapLoop
                implements BiConsumer, Consumer {
            final JSONPath.Context context;
            final List values;

            public MapLoop(JSONPath.Context context, List values) {
                this.context = context;
                this.values = values;
            }

            @Override
            public void accept(Object key, Object value) {
                if (name.equals(key)) {
                    values.add(value);
                    return;
                }

                if (value instanceof Map) {
                    ((Map<?, ?>) value).forEach(this);
                } else if (value instanceof List) {
                    ((List<?>) value).forEach(this);
                } else if (nameHashCode == HASH_STAR) {
                    values.add(value);
                }
            }

            @Override
            public void accept(Object value) {
                if (value == null) {
                    return;
                }

                if (value instanceof Map) {
                    ((Map<?, ?>) value).forEach(this);
                } else if (value instanceof List) {
                    ((List<?>) value).forEach(this);
                } else {
                    ObjectWriter<?> objectWriter = context.path
                            .getWriterContext()
                            .getObjectWriter(value.getClass());
                    if (objectWriter instanceof ObjectWriterAdapter) {
                        FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
                        if (fieldWriter != null) {
                            Object fieldValue = fieldWriter.getFieldValue(value);
                            if (fieldValue != null) {
                                values.add(fieldValue);
                            }
                            return;
                        }

                        for (int i = 0; i < objectWriter.getFieldWriters().size(); i++) {
                            fieldWriter = objectWriter.getFieldWriters().get(i);
                            Object fieldValue = fieldWriter.getFieldValue(value);
                            accept(fieldValue);
                        }

                        return;
                    } else if (nameHashCode == HASH_STAR) {
                        values.add(value);
                    }
                }
            }
        }

        class LoopSet {
            final JSONPath.Context context;
            final Object value;

            public LoopSet(JSONPath.Context context, Object value) {
                this.context = context;
                this.value = value;
            }

            public void accept(Object object) {
                if (object instanceof Map) {
                    for (Map.Entry entry : (Iterable<Map.Entry>) ((Map) object).entrySet()) {
                        if (name.equals(entry.getKey())) {
                            entry.setValue(value);
                            context.eval = true;
                        } else {
                            Object entryValue = entry.getValue();
                            if (entryValue != null) {
                                accept(entryValue);
                            }
                        }
                    }
                } else if (object instanceof Collection) {
                    for (Object item : ((List<?>) object)) {
                        accept(item);
                    }
                } else {
                    Class<?> entryValueClass = object.getClass();
                    ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(entryValueClass);
                    if (objectReader instanceof ObjectReaderBean) {
                        FieldReader fieldReader = objectReader.getFieldReader(nameHashCode);
                        if (fieldReader != null) {
                            fieldReader.accept(object, value);
                            context.eval = true;
                            return;
                        }
                    }

                    ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(entryValueClass);
                    List<FieldWriter> fieldWriters = objectWriter.getFieldWriters();
                    for (FieldWriter fieldWriter : fieldWriters) {
                        Object fieldValue = fieldWriter.getFieldValue(object);
                        accept(fieldValue);
                    }
                }
            }
        }

        class LoopCallback {
            final JSONPath.Context context;
            final BiFunction callback;

            public LoopCallback(JSONPath.Context context, BiFunction callback) {
                this.context = context;
                this.callback = callback;
            }

            public void accept(Object object) {
                if (object instanceof Map) {
                    for (Map.Entry entry : (Iterable<Map.Entry>) ((Map) object).entrySet()) {
                        Object entryValue = entry.getValue();
                        if (name.equals(entry.getKey())) {
                            Object applyValue = callback.apply(object, entryValue);
                            entry.setValue(applyValue);
                            context.eval = true;
                        } else {
                            if (entryValue != null) {
                                accept(entryValue);
                            }
                        }
                    }
                } else if (object instanceof Collection) {
                    for (Object item : ((List<?>) object)) {
                        accept(item);
                    }
                } else {
                    Class<?> entryValueClass = object.getClass();
                    ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(entryValueClass);
                    ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(entryValueClass);
                    if (objectReader instanceof ObjectReaderBean) {
                        FieldReader fieldReader = objectReader.getFieldReader(nameHashCode);
                        FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
                        if (fieldWriter != null && fieldReader != null) {
                            Object fieldValue = fieldWriter.getFieldValue(object);
                            fieldValue = callback.apply(object, fieldValue);
                            fieldReader.accept(object, fieldValue);
                            context.eval = true;
                            return;
                        }
                    }

                    List<FieldWriter> fieldWriters = objectWriter.getFieldWriters();
                    for (FieldWriter fieldWriter : fieldWriters) {
                        Object fieldValue = fieldWriter.getFieldValue(object);
                        accept(fieldValue);
                    }
                }
            }
        }

        @Override
        public void accept(JSONReader jsonReader, JSONPath.Context context) {
            List values = new JSONArray();
            accept(jsonReader, context, values);
            context.value = values;
            context.eval = true;
        }

        public void accept(JSONReader jsonReader, JSONPath.Context context, List<Object> values) {
            if (jsonReader.isJSONB()) {
                if (jsonReader.nextIfMatch(BC_OBJECT)) {
                    while (!jsonReader.nextIfMatch(BC_OBJECT_END)) {
                        long nameHashCode = jsonReader.readFieldNameHashCode();
                        if (nameHashCode == 0) {
                            continue;
                        }

                        boolean match = nameHashCode == this.nameHashCode;
                        if (match) {
                            if (jsonReader.isArray()) {
                                values.addAll(jsonReader.readArray());
                            } else {
                                values.add(jsonReader.readAny());
                            }
                        } else if (jsonReader.isObject() || jsonReader.isArray()) {
                            accept(jsonReader, context, values);
                        } else {
                            jsonReader.skipValue();
                        }
                    }
                    return;
                }

                if (jsonReader.isArray()) {
                    int itemCnt = jsonReader.startArray();
                    for (int i = 0; i < itemCnt; i++) {
                        if (jsonReader.isObject() || jsonReader.isArray()) {
                            accept(jsonReader, context, values);
                            continue;
                        }

                        jsonReader.skipValue();
                    }
                } else {
                    jsonReader.skipValue();
                }
                return;
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
                            jsonReader.readNumber0();
                            val = jsonReader.getNumber();
                            break;
                        case '[':
                        case '{':
                            if (match) {
                                val = ch == '[' ? jsonReader.readArray() : jsonReader.readObject();
                                break;
                            }
                            accept(jsonReader, context, values);
                            continue _for;
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
                    if (val instanceof Collection) {
                        values.addAll((Collection) val);
                    } else {
                        values.add(val);
                    }

                    if (jsonReader.ch == ',') {
                        jsonReader.next();
                    }
                }

                if (jsonReader.ch == ',') {
                    jsonReader.next();
                }
            } else if (jsonReader.ch == '[') {
                jsonReader.next();

                for (; ; ) {
                    if (jsonReader.ch == ']') {
                        jsonReader.next();
                        break;
                    }

                    if (jsonReader.ch == '{' || jsonReader.ch == '[') {
                        accept(jsonReader, context, values);
                    } else {
                        jsonReader.skipValue();
                    }

                    if (jsonReader.ch == ',') {
                        jsonReader.next();
                        break;
                    }
                }

                if (jsonReader.ch == ',') {
                    jsonReader.next();
                }
            } else {
                jsonReader.skipValue();
            }
        }
    }

    static final class MinSegment
            extends JSONPathSegment
            implements EvalSegment {
        static final MinSegment INSTANCE = new MinSegment();

        @Override
        public void accept(JSONReader jsonReader, JSONPath.Context context) {
            eval(context);
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object value = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (value == null) {
                return;
            }

            Object min = null;
            if (value instanceof Collection) {
                for (Object item : (Collection) value) {
                    if (item == null) {
                        continue;
                    }

                    if (min == null) {
                        min = item;
                    } else if (TypeUtils.compare(min, item) > 0) {
                        min = item;
                    }
                }
            } else if (value instanceof Object[]) {
                Object[] array = (Object[]) value;
                for (Object item : array) {
                    if (item == null) {
                        continue;
                    }

                    if (min == null) {
                        min = item;
                    } else if (TypeUtils.compare(min, item) > 0) {
                        min = item;
                    }
                }
            } else if (value instanceof JSONPath.Sequence) {
                for (Object item : ((JSONPath.Sequence) value).values) {
                    if (item == null) {
                        continue;
                    }

                    if (min == null) {
                        min = item;
                    } else if (TypeUtils.compare(min, item) > 0) {
                        min = item;
                    }
                }
            } else {
                throw new UnsupportedOperationException();
            }

            context.value = min;
            context.eval = true;
        }
    }

    static final class MaxSegment
            extends JSONPathSegment
            implements EvalSegment {
        static final MaxSegment INSTANCE = new MaxSegment();

        @Override
        public void accept(JSONReader jsonReader, JSONPath.Context context) {
            eval(context);
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object value = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (value == null) {
                return;
            }

            Object max = null;
            if (value instanceof Collection) {
                for (Object item : (Collection) value) {
                    if (item == null) {
                        continue;
                    }

                    if (max == null) {
                        max = item;
                    } else if (TypeUtils.compare(max, item) < 0) {
                        max = item;
                    }
                }
            } else if (value instanceof Object[]) {
                Object[] array = (Object[]) value;
                for (Object item : array) {
                    if (item == null) {
                        continue;
                    }

                    if (max == null) {
                        max = item;
                    } else if (TypeUtils.compare(max, item) < 0) {
                        max = item;
                    }
                }
            } else if (value instanceof JSONPath.Sequence) {
                for (Object item : ((JSONPath.Sequence) value).values) {
                    if (item == null) {
                        continue;
                    }

                    if (max == null) {
                        max = item;
                    } else if (TypeUtils.compare(max, item) < 0) {
                        max = item;
                    }
                }
            } else {
                throw new UnsupportedOperationException();
            }

            context.value = max;
            context.eval = true;
        }
    }

    static final class SumSegment
            extends JSONPathSegment
            implements EvalSegment {
        static final SumSegment INSTANCE = new SumSegment();

        @Override
        public void accept(JSONReader jsonReader, JSONPath.Context context) {
            eval(context);
        }

        static Number add(Number a, Number b) {
            boolean aIsInt = a instanceof Byte || a instanceof Short || a instanceof Integer || a instanceof Long;
            boolean bIsInt = b instanceof Byte || b instanceof Short || b instanceof Integer || b instanceof Long;
            if (aIsInt && bIsInt) {
                return a.longValue() + b.longValue();
            }
            throw new JSONException("not support operation");
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object value = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (value == null) {
                return;
            }

            Number sum = 0;
            if (value instanceof Collection) {
                for (Object item : (Collection) value) {
                    if (item == null) {
                        continue;
                    }

                    sum = add(sum, (Number) item);
                }
            } else if (value instanceof Object[]) {
                Object[] array = (Object[]) value;
                for (Object item : array) {
                    if (item == null) {
                        continue;
                    }

                    sum = add(sum, (Number) item);
                }
            } else if (value instanceof JSONPath.Sequence) {
                for (Object item : ((JSONPath.Sequence) value).values) {
                    if (item == null) {
                        continue;
                    }

                    sum = add(sum, (Number) item);
                }
            } else {
                throw new UnsupportedOperationException();
            }

            context.value = sum;
            context.eval = true;
        }
    }

    static final class LengthSegment
            extends JSONPathSegment
            implements EvalSegment {
        static final LengthSegment INSTANCE = new LengthSegment();

        @Override
        public void accept(JSONReader jsonReader, JSONPath.Context context) {
            if (context.parent == null) {
                context.root = jsonReader.readAny();
                context.eval = true;
            }
            eval(context);
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object value = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (value == null) {
                return;
            }

            int length = 1;
            if (value instanceof Collection) {
                length = ((Collection<?>) value).size();
            } else if (value.getClass().isArray()) {
                length = Array.getLength(value);
            } else if (value instanceof Map) {
                length = ((Map<?, ?>) value).size();
            } else if (value instanceof String) {
                length = ((String) value).length();
            } else if (value instanceof JSONPath.Sequence) {
                length = ((JSONPath.Sequence) value).values.size();
            }
            context.value = length;
        }
    }

    static final class ValuesSegment
            extends JSONPathSegment
            implements EvalSegment {
        static final ValuesSegment INSTANCE = new ValuesSegment();

        @Override
        public void accept(JSONReader jsonReader, JSONPath.Context context) {
            eval(context);
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object == null) {
                context.value = null;
                context.eval = true;
                return;
            }

            if (object instanceof Map) {
                context.value = new JSONArray(((Map<?, ?>) object).values());
                context.eval = true;
                return;
            }

            throw new JSONException("TODO");
        }
    }

    static final class KeysSegment
            extends JSONPathSegment
            implements EvalSegment {
        static final KeysSegment INSTANCE = new KeysSegment();

        @Override
        public void accept(JSONReader jsonReader, JSONPath.Context context) {
            if (jsonReader.isObject()) {
                jsonReader.next();
                JSONArray array = new JSONArray();
                while (!jsonReader.nextIfObjectEnd()) {
                    String fieldName = jsonReader.readFieldName();
                    array.add(fieldName);
                    jsonReader.skipValue();
                }
                context.value = array;
                return;
            }
            throw new JSONException("TODO");
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;
            if (object instanceof Map) {
                context.value = new JSONArray(((Map<?, ?>) object).keySet());
                context.eval = true;
                return;
            }

            throw new JSONException("TODO");
        }
    }

    static final class EntrySetSegment
            extends JSONPathSegment
            implements EvalSegment {
        static final EntrySetSegment INSTANCE = new EntrySetSegment();

        @Override
        public void accept(JSONReader jsonReader, JSONPath.Context context) {
            if (jsonReader.isObject()) {
                jsonReader.next();
                JSONArray array = new JSONArray();
                while (!jsonReader.nextIfObjectEnd()) {
                    String fieldName = jsonReader.readFieldName();
                    Object value = jsonReader.readAny();
                    array.add(
                            JSONObject.of("key", fieldName, "value", value)
                    );
                }
                context.value = array;
                return;
            }
            throw new JSONException("TODO");
        }

        @Override
        public void eval(JSONPath.Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;
            if (object instanceof Map) {
                Map map = (Map) object;
                JSONArray array = new JSONArray(map.size());
                for (Iterator<Map.Entry> it = ((Map) object).entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry entry = it.next();
                    array.add(
                            JSONObject.of("key", entry.getKey(), "value", entry.getValue())
                    );
                }
                context.value = array;
                context.eval = true;
                return;
            }

            throw new JSONException("TODO");
        }
    }
}
