package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.GuavaSupport;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

import static com.alibaba.fastjson2.util.JDKUtils.JVM_VERSION;

public final class ObjectReaderImplList
        implements ObjectReader {
    static final Class CLASS_EMPTY_SET = Collections.emptySet().getClass();
    static final Class CLASS_EMPTY_LIST = Collections.emptyList().getClass();
    static final Class CLASS_SINGLETON = Collections.singleton(0).getClass();
    static final Class CLASS_SINGLETON_LIST = Collections.singletonList(0).getClass();
    static final Class CLASS_ARRAYS_LIST = Arrays.asList(0).getClass();

    static final Class CLASS_UNMODIFIABLE_COLLECTION = Collections.unmodifiableCollection(Collections.emptyList()).getClass();
    static final Class CLASS_UNMODIFIABLE_LIST = Collections.unmodifiableList(Collections.emptyList()).getClass();
    static final Class CLASS_UNMODIFIABLE_SET = Collections.unmodifiableSet(Collections.emptySet()).getClass();
    static final Class CLASS_UNMODIFIABLE_SORTED_SET = Collections.unmodifiableSortedSet(Collections.emptySortedSet()).getClass();
    static final Class CLASS_UNMODIFIABLE_NAVIGABLE_SET = Collections.unmodifiableNavigableSet(Collections.emptyNavigableSet()).getClass();

    public static ObjectReaderImplList INSTANCE = new ObjectReaderImplList(ArrayList.class, ArrayList.class, ArrayList.class, Object.class, null);

    final Type listType;
    final Class listClass;
    final Class instanceType;
    final long instanceTypeHash;
    final Type itemType;
    final Class itemClass;
    final String itemClassName;
    final long itemClassNameHash;
    final Function builder;
    ObjectReader itemObjectReader;
    volatile boolean instanceError;

    public static ObjectReader of(Type type, Class listClass, long features) {
        Type itemType = Object.class;
        Type rawType;
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            rawType = parameterizedType.getRawType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length == 1) {
                itemType = actualTypeArguments[0];
            }
        } else {
            rawType = type;
            if (listClass != null) {
                Type superType = listClass.getGenericSuperclass();
                if (superType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) superType;
                    rawType = parameterizedType.getRawType();
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    if (actualTypeArguments.length == 1) {
                        itemType = actualTypeArguments[0];
                    }
                }
            }
        }

        if (listClass == null) {
            listClass = TypeUtils.getClass(rawType);
        }

        Function builder = null;
        Class instanceClass;

        if (listClass == Iterable.class
                || listClass == Collection.class
                || listClass == List.class
                || listClass == AbstractCollection.class
                || listClass == AbstractList.class
        ) {
            instanceClass = ArrayList.class;
        } else if (listClass == Queue.class
                || listClass == Deque.class
                || listClass == AbstractSequentialList.class) {
            instanceClass = LinkedList.class;
        } else if (listClass == Set.class || listClass == AbstractSet.class) {
            instanceClass = HashSet.class;
        } else if (listClass == EnumSet.class) {
            instanceClass = HashSet.class;
            builder = (o) -> EnumSet.copyOf((Collection) o);
        } else if (listClass == NavigableSet.class || listClass == SortedSet.class) {
            instanceClass = TreeSet.class;
        } else if (listClass == CLASS_SINGLETON) {
            instanceClass = ArrayList.class;
            builder = (Object obj) -> Collections.singleton(((List) obj).get(0));
        } else if (listClass == CLASS_SINGLETON_LIST) {
            instanceClass = ArrayList.class;
            builder = (Object obj) -> Collections.singletonList(((List) obj).get(0));
        } else if (listClass == CLASS_ARRAYS_LIST) {
            instanceClass = ArrayList.class;
            builder = (Object obj) -> Arrays.asList(((List) obj).toArray());
        } else if (listClass == CLASS_UNMODIFIABLE_COLLECTION) {
            instanceClass = ArrayList.class;
            builder = (Object obj) -> Collections.unmodifiableCollection((Collection) obj);
        } else if (listClass == CLASS_UNMODIFIABLE_LIST) {
            instanceClass = ArrayList.class;
            builder = (Object obj) -> Collections.unmodifiableList((List) obj);
        } else if (listClass == CLASS_UNMODIFIABLE_SET) {
            instanceClass = LinkedHashSet.class;
            builder = (Object obj) -> Collections.unmodifiableSet((Set) obj);
        } else if (listClass == CLASS_UNMODIFIABLE_SORTED_SET) {
            instanceClass = TreeSet.class;
            builder = (Object obj) -> Collections.unmodifiableSortedSet((SortedSet) obj);
        } else if (listClass == CLASS_UNMODIFIABLE_NAVIGABLE_SET) {
            instanceClass = TreeSet.class;
            builder = (Object obj) -> Collections.unmodifiableNavigableSet((NavigableSet) obj);
        } else {
            String typeName = listClass.getTypeName();
            switch (typeName) {
                case "com.google.common.collect.ImmutableList":
                    instanceClass = ArrayList.class;
                    builder = GuavaSupport.immutableListConverter();
                    break;
                case "com.google.common.collect.ImmutableSet":
                    instanceClass = ArrayList.class;
                    builder = GuavaSupport.immutableSetConverter();
                    break;
                case "com.google.common.collect.Lists$TransformingRandomAccessList":
                    instanceClass = ArrayList.class;
                    break;
                case "com.google.common.collect.Lists.TransformingSequentialList":
                    instanceClass = LinkedList.class;
                    break;
                default:
                    instanceClass = listClass;
                    break;
            }
        }

        if (type == ObjectReaderImplList.CLASS_EMPTY_SET
                || type == ObjectReaderImplList.CLASS_EMPTY_LIST
                || type == ObjectReaderImplList.CLASS_EMPTY_LIST
        ) {
            return new ObjectReaderImplList(type, (Class) type, (Class) type, Object.class, null);
        }

        if (itemType == String.class && builder == null) {
            return new ObjectReaderImplListStr(listClass, instanceClass);
        }

        if (itemType == Long.class && builder == null) {
            return new ObjectReaderImplListInt64(listClass, instanceClass);
        }

        return new ObjectReaderImplList(type, listClass, instanceClass, itemType, builder);
    }

    public ObjectReaderImplList(Type listType, Class listClass, Class instanceType, Type itemType, Function builder) {
        this.listType = listType;
        this.listClass = listClass;
        this.instanceType = instanceType;
        this.instanceTypeHash = Fnv.hashCode64(TypeUtils.getTypeName(instanceType));
        this.itemType = itemType;
        this.itemClass = TypeUtils.getClass(itemType);
        this.builder = builder;
        this.itemClassName = itemClass != null ? TypeUtils.getTypeName(itemClass) : null;
        this.itemClassNameHash = itemClassName != null ? Fnv.hashCode64(itemClassName) : 0;
    }

    @Override
    public Class getObjectClass() {
        return listClass;
    }

    @Override
    public Function getBuildFunction() {
        return builder;
    }

    @Override
    public Object createInstance(Collection collection) {
        int size = collection.size();

        if (size == 0) {
            Collection list = Collections.emptyList();
            if (builder != null) {
                return builder.apply(list);
            }
            return list;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();

        Collection list = (Collection) createInstance(0L);
        for (Object item : collection) {
            Object value = item;
            Class<?> valueClass = value.getClass();
            if (valueClass != itemType) {
                Function typeConvert = provider.getTypeConvert(valueClass, itemType);
                if (typeConvert != null) {
                    value = typeConvert.apply(value);
                } else if (item instanceof Map) {
                    Map map = (Map) item;
                    if (itemObjectReader == null) {
                        itemObjectReader = provider.getObjectReader(itemType);
                    }
                    value = itemObjectReader.createInstance(map, 0L);
                } else if (value instanceof Collection) {
                    if (itemObjectReader == null) {
                        itemObjectReader = provider.getObjectReader(itemType);
                    }
                    value = itemObjectReader.createInstance((Collection) value);
                } else if (itemClass.isInstance(value)) {
                    // skip
                } else {
                    throw new JSONException("can not convert from " + valueClass + " to " + itemType);
                }
            }
            list.add(value);
        }

        if (builder != null) {
            return builder.apply(list);
        }

        return list;
    }

    @Override
    public Object createInstance(long features) {
        if (instanceType == ArrayList.class) {
            return JVM_VERSION == 8 ? new ArrayList(10) : new ArrayList();
        }

        if (instanceType == LinkedList.class) {
            return new LinkedList();
        }

        if (instanceType == HashSet.class) {
            return new HashSet();
        }

        if (instanceType == LinkedHashSet.class) {
            return new LinkedHashSet();
        }

        if (instanceType == TreeSet.class) {
            return new TreeSet();
        }

        if (instanceType == CLASS_EMPTY_LIST) {
            return Collections.emptyList();
        }

        if (instanceType == CLASS_EMPTY_SET) {
            return Collections.emptySet();
        }

        if (instanceType != null) {
            JSONException error = null;
            if (!instanceError) {
                try {
                    return instanceType.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    instanceError = true;
                    error = new JSONException("create list error, type " + instanceType);
                }
            }

            if (instanceError && List.class.isAssignableFrom(instanceType.getSuperclass())) {
                try {
                    return instanceType.getSuperclass().newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    instanceError = true;
                    error = new JSONException("create list error, type " + instanceType);
                }
            }

            if (error != null) {
                throw error;
            }
        }

        return new ArrayList();
    }

    @Override
    public FieldReader getFieldReader(long hashCode) {
        return null;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        ObjectReader objectReader = jsonReader.checkAutoType(this.listClass, 0, features);

        Function builder = this.builder;
        Class listType = this.instanceType;
        if (objectReader != null) {
            listType = objectReader.getObjectClass();

            if (listType == CLASS_UNMODIFIABLE_COLLECTION) {
                listType = ArrayList.class;
                builder = (Function<Collection, Collection>) Collections::unmodifiableCollection;
            } else if (listType == CLASS_UNMODIFIABLE_LIST) {
                listType = ArrayList.class;
                builder = (Function<List, List>) Collections::unmodifiableList;
            } else if (listType == CLASS_UNMODIFIABLE_SET) {
                listType = LinkedHashSet.class;
                builder = (Function<Set, Set>) Collections::unmodifiableSet;
            } else if (listType == CLASS_UNMODIFIABLE_SORTED_SET) {
                listType = TreeSet.class;
                builder = (Function<SortedSet, SortedSet>) Collections::unmodifiableSortedSet;
            } else if (listType == CLASS_UNMODIFIABLE_NAVIGABLE_SET) {
                listType = TreeSet.class;
                builder = (Function<NavigableSet, NavigableSet>) Collections::unmodifiableNavigableSet;
            } else if (listType == CLASS_SINGLETON) {
                listType = ArrayList.class;
                builder = (Function<Collection, Collection>) ((Collection list) -> Collections.singleton(list.iterator().next()));
            } else if (listType == CLASS_SINGLETON_LIST) {
                listType = ArrayList.class;
                builder = (Function<List, List>) ((List list) -> Collections.singletonList(list.get(0)));
            }
        }

        int entryCnt = jsonReader.startArray();

        if (entryCnt > 0 && itemObjectReader == null) {
            itemObjectReader = jsonReader
                    .getContext()
                    .getObjectReader(itemType);
        }

        if (listType == CLASS_ARRAYS_LIST) {
            Object[] array = new Object[entryCnt];
            List list = Arrays.asList(array);
            for (int i = 0; i < entryCnt; ++i) {
                Object item;

                if (jsonReader.isReference()) {
                    String reference = jsonReader.readReference();
                    if ("..".equals(reference)) {
                        item = list;
                    } else {
                        item = null;
                        jsonReader.addResolveTask((List) list, i, JSONPath.of(reference));
                    }
                } else {
                    item = itemObjectReader.readJSONBObject(jsonReader, itemType, i, features);
                }

                array[i] = item;
            }
            return list;
        }

        Collection list;
        if (listType == ArrayList.class) {
            list = entryCnt > 0 ? new ArrayList(entryCnt) : new ArrayList();
        } else if (listType == JSONArray.class) {
            list = entryCnt > 0 ? new JSONArray(entryCnt) : new JSONArray();
        } else if (listType == HashSet.class) {
            list = new HashSet();
        } else if (listType == LinkedHashSet.class) {
            list = new LinkedHashSet();
        } else if (listType == TreeSet.class) {
            list = new TreeSet();
        } else if (listType == CLASS_EMPTY_SET) {
            list = Collections.emptySet();
        } else if (listType == CLASS_EMPTY_LIST) {
            list = Collections.emptyList();
        } else if (listType == CLASS_SINGLETON_LIST) {
            list = new ArrayList();
            builder = (Function<Collection, Collection>) ((Collection items) -> Collections.singletonList(items.iterator().next()));
        } else if (listType == CLASS_UNMODIFIABLE_LIST) {
            list = new ArrayList();
            builder = (Function<List, List>) ((List items) -> Collections.unmodifiableList(items));
        } else if (listType != null && listType != this.listType) {
            try {
                list = (Collection) listType.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new JSONException(jsonReader.info("create instance error " + listType), e);
            }
        } else {
            list = (Collection) createInstance(jsonReader.getContext().getFeatures() | features);
        }

        for (int i = 0; i < entryCnt; ++i) {
            Object item;

            if (jsonReader.isReference()) {
                String reference = jsonReader.readReference();
                if ("..".equals(reference)) {
                    item = list;
                } else {
                    jsonReader.addResolveTask(list, i, JSONPath.of(reference));
                    if (list instanceof List) {
                        item = null;
                    } else {
                        continue;
                    }
                }
            } else {
                ObjectReader autoTypeReader = jsonReader.checkAutoType(itemClass, itemClassNameHash, features);
                if (autoTypeReader != null) {
                    item = autoTypeReader.readJSONBObject(jsonReader, itemType, i, features);
                } else {
                    item = itemObjectReader.readJSONBObject(jsonReader, itemType, i, features);
                }
            }

            list.add(item);
        }

        if (builder != null) {
            return builder.apply(list);
        }

        return list;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        JSONReader.Context context = jsonReader.getContext();
        if (itemObjectReader == null) {
            itemObjectReader = context
                    .getObjectReader(itemType);
        }

        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, fieldType, fieldName, 0);
        }

        if (jsonReader.readIfNull()) {
            return null;
        }

        Collection list;
        if (jsonReader.nextIfSet()) {
            list = new HashSet();
        } else {
            list = (Collection) createInstance(context.getFeatures() | features);
        }
        char ch = jsonReader.current();
        if (ch == '"') {
            String str = jsonReader.readString();
            if (itemClass == String.class) {
                jsonReader.nextIfMatch(',');
                list.add(str);
                return list;
            }

            if (str.isEmpty()) {
                jsonReader.nextIfMatch(',');
                return null;
            }

            Function typeConvert = context.getProvider().getTypeConvert(String.class, itemType);
            if (typeConvert != null) {
                Object converted = typeConvert.apply(str);
                jsonReader.nextIfMatch(',');
                list.add(converted);
                return list;
            }
            throw new JSONException(jsonReader.info());
        }

        if (!jsonReader.nextIfMatch('[')) {
            if (itemClass != Object.class && itemObjectReader != null) {
                Object item = itemObjectReader.readObject(jsonReader, itemType, 0, 0);
                list.add(item);
                if (builder != null) {
                    list = (Collection) builder.apply(list);
                }
                return list;
            }

            throw new JSONException(jsonReader.info());
        }

        for (int i = 0; ; ++i) {
            if (jsonReader.nextIfMatch(']')) {
                break;
            }

            Object item;
            if (itemType == String.class) {
                item = jsonReader.readString();
            } else if (itemObjectReader != null) {
                if (jsonReader.isReference()) {
                    String reference = jsonReader.readReference();
                    if ("..".equals(reference)) {
                        item = this;
                    } else {
                        jsonReader.addResolveTask(list, i, JSONPath.of(reference));
                        continue;
                    }
                } else {
                    item = itemObjectReader.readObject(jsonReader, itemType, i, 0);
                }
            } else {
                throw new JSONException(jsonReader.info("TODO : " + itemType));
            }

            list.add(item);

            if (jsonReader.nextIfMatch(',')) {
                continue;
            }
        }

        jsonReader.nextIfMatch(',');

        if (builder != null) {
            return builder.apply(list);
        }

        return list;
    }
}
