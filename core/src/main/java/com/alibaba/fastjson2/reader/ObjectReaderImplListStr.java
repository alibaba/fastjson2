package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.util.*;
import java.util.function.Function;

import static com.alibaba.fastjson2.reader.ObjectReaderImplList.*;

public final class ObjectReaderImplListStr implements ObjectReader {
    final Class listType;
    final Class instanceType;

    public ObjectReaderImplListStr(Class listType, Class instanceType) {
        this.listType = listType;
        this.instanceType = instanceType;
    }

    @Override
    public Object createInstance(long features) {
        if (instanceType == ArrayList.class) {
            return new ArrayList();
        }

        if (instanceType == LinkedList.class) {
            return new LinkedList();
        }

        try {
            return instanceType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new JSONException("create list error, type " + instanceType);
        }
    }

    @Override
    public FieldReader getFieldReader(long hashCode) {
        return null;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        Class listType = this.listType;

        if (jsonReader.nextIfNull()) {
            return null;
        }

        ObjectReader objectReader = jsonReader.checkAutoType(listType, 0, features);
        if (objectReader != null) {
            listType = objectReader.getObjectClass();
        }

        if (listType == CLASS_ARRAYS_LIST) {
            int entryCnt = jsonReader.startArray();
            String[] array = new String[entryCnt];
            for (int i = 0; i < entryCnt; ++i) {
                array[i] = jsonReader.readString();
            }
            return Arrays.asList(array);
        }

        Function builder = null;
        Collection list;
        if (listType == ArrayList.class) {
            list = new ArrayList();
        } else if (listType == JSONArray.class) {
            list = new JSONArray();
        } else if (listType == CLASS_UNMODIFIABLE_COLLECTION) {
            list = new ArrayList();
            builder = (Function<Collection, Collection>) Collections::unmodifiableCollection;
        } else if (listType == CLASS_UNMODIFIABLE_LIST) {
            list = new ArrayList();
            builder = (Function<List, List>) Collections::unmodifiableList;
        } else if (listType == CLASS_UNMODIFIABLE_SET) {
            list = new LinkedHashSet();
            builder = (Function<Set, Set>) Collections::unmodifiableSet;
        } else if (listType == CLASS_UNMODIFIABLE_SORTED_SET) {
            list = new TreeSet();
            builder = (Function<SortedSet, SortedSet>) Collections::unmodifiableSortedSet;
        } else if (listType == CLASS_UNMODIFIABLE_NAVIGABLE_SET) {
            list = new TreeSet();
            builder = (Function<NavigableSet, NavigableSet>) Collections::unmodifiableNavigableSet;
        } else if (listType == CLASS_SINGLETON) {
            list = new ArrayList();
            builder = (Function<Collection, Collection>) ((Collection collection) -> Collections.singleton(collection.iterator().next()));
        } else if (listType == CLASS_SINGLETON_LIST) {
            list = new ArrayList();
            builder = (Function<Collection, Collection>) ((Collection collection) -> Collections.singletonList(collection.iterator().next()));
        } else if (listType != null && listType != this.listType) {
            try {
                list = (Collection) listType.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new JSONException("create instance error " + listType, e);
            }
        } else {
            list = (Collection) createInstance(jsonReader.getContext().getFeatures() | features);
        }

        int entryCnt = jsonReader.startArray();
        for (int i = 0; i < entryCnt; ++i) {
            list.add(jsonReader.readString());
        }

        if (builder != null) {
            list = (Collection) builder.apply(list);
        }

        return list;
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, 0);
        }

        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.current() != '[') {
            throw new JSONException("offset " + jsonReader.getOffset() + ", char : " + jsonReader.current());
        }
        jsonReader.next();


        Collection list = (Collection) createInstance(jsonReader.getContext().getFeatures() | features);
        for (; ; ) {
            if (jsonReader.nextIfMatch(']')) {
                break;
            }

            list.add(
                    jsonReader.readString());
        }

        jsonReader.nextIfMatch(',');

        return list;
    }
}
