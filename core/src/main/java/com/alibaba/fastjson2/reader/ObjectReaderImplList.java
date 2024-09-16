package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

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

    public static ObjectReaderImplList INSTANCE = new ObjectReaderImplList(
            ArrayList.class,
            ArrayList.class,
            ArrayList.class,
            65, // Fnv.hashCode64(TypeUtils.getTypeName(ArrayList.class)),
            Object.class,
            Object.class,
            null,
            "Object", // TypeUtils.getTypeName(Object.class)
            127970252055119L // Fnv.hashCode64("Object")
            );

    public static ObjectReaderImplList JSON_ARRAY_READER = new ObjectReaderImplList(
            JSONArray.class,
            JSONArray.class,
            JSONArray.class,
            8893561198416334968L, // Fnv.hashCode64(TypeUtils.getTypeName(JSONArray.class)),
            Object.class,
            Object.class,
            null,
            "Object", // TypeUtils.getTypeName(Object.class)
            127970252055119L // Fnv.hashCode64("Object")
    );

    final Type listType;
    final Class listClass;
    final Class instanceType;
    final long instanceTypeHash;
    final Type itemType;
    final Class itemClass;
    final String itemClassName;
    final long itemClassNameHash;
    final Function builder;
    Object listSingleton;
    ObjectReader itemObjectReader;
    volatile boolean instanceError;

    public static ObjectReader of(Type type, Class listClass, long features) {
        if (listClass == type && "".equals(listClass.getSimpleName())) {
            type = listClass.getGenericSuperclass();
            listClass = listClass.getSuperclass();
        }

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
            Type finalItemType = itemType;
            builder = (o) -> {
                Collection collection = (Collection) o;
                if (collection.isEmpty() && finalItemType instanceof Class) {
                    return EnumSet.noneOf((Class) finalItemType);
                } else {
                    return EnumSet.copyOf(collection);
                }
            };
        } else if (listClass == NavigableSet.class || listClass == SortedSet.class) {
            instanceClass = TreeSet.class;
        } else if (listClass == CLASS_SINGLETON) {
            instanceClass = ArrayList.class;
            builder = (Object obj) -> Collections.singleton(((List) obj).get(0));
        } else if (listClass == CLASS_SINGLETON_LIST) {
            instanceClass = ArrayList.class;
            builder = (Object obj) -> Collections.singletonList(((List) obj).get(0));
        } else if (listClass == CLASS_ARRAYS_LIST) {
            instanceClass = CLASS_ARRAYS_LIST;
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
        } else {
            String typeName = TypeUtils.getTypeName(listClass);
            switch (typeName) {
                case "java.util.Collections$SynchronizedRandomAccessList":
                    instanceClass = ArrayList.class;
                    builder = (Function<List, List>) Collections::synchronizedList;
                    break;
                case "java.util.Collections$SynchronizedCollection":
                    instanceClass = ArrayList.class;
                    builder = (Function<Collection, Collection>) Collections::synchronizedCollection;
                    break;
                case "java.util.Collections$SynchronizedSet":
                    instanceClass = HashSet.class;
                    builder = (Function<Set, Set>) Collections::synchronizedSet;
                    break;
                case "java.util.Collections$SynchronizedSortedSet":
                    instanceClass = TreeSet.class;
                    builder = (Function<SortedSet, SortedSet>) Collections::synchronizedSortedSet;
                    break;
                case "java.util.RandomAccessSubList":
                case "java.util.AbstractList$RandomAccessSubList":
                    instanceClass = ArrayList.class;
                    break;
                default:
                    instanceClass = listClass;
                    break;
            }
        }

        switch (TypeUtils.getTypeName(type)) {
            case "kotlin.collections.EmptySet":
            case "kotlin.collections.EmptyList": {
                Object empty;
                Class<?> clazz = (Class<?>) type;
                try {
                    Field field = clazz.getField("INSTANCE");
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    empty = field.get(null);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new IllegalStateException("Failed to get singleton of " + type, e);
                }
                return new ObjectReaderImplList(clazz, empty);
            }
            case "java.util.Collections$EmptySet": {
                return new ObjectReaderImplList((Class) type, Collections.emptySet());
            }
            case "java.util.Collections$EmptyList": {
                return new ObjectReaderImplList((Class) type, Collections.emptyList());
            }
        }

        if (type == ObjectReaderImplList.CLASS_EMPTY_SET
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

    ObjectReaderImplList(Class listClass, Object listSingleton) {
        this(listClass, listClass, listClass, Object.class, null);
        this.listSingleton = listSingleton;
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

    private ObjectReaderImplList(
            Type listType,
            Class listClass,
            Class instanceType,
            long instanceTypeHash,
            Type itemType,
            Class itemClass,
            Function builder,
            String itemClassName,
            long itemClassNameHash
    ) {
        this.listType = listType;
        this.listClass = listClass;
        this.instanceType = instanceType;
        this.instanceTypeHash = instanceTypeHash;
        this.itemType = itemType;
        this.itemClass = itemClass;
        this.builder = builder;
        this.itemClassName = itemClassName;
        this.itemClassNameHash = itemClassNameHash;
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
    public Object createInstance(Collection collection, long features) {
        int size = collection.size();

        if (size == 0 && (listClass == List.class)) {
            Collection list = new ArrayList();
            if (builder != null) {
                return builder.apply(list);
            }
            return list;
        }

        ObjectReaderProvider provider = JSONFactory.defaultObjectReaderProvider;

        Collection list;
        if (instanceType == ArrayList.class) {
            list = new ArrayList(collection.size());
        } else {
            list = (Collection) createInstance(features);
        }

        for (Object item : collection) {
            if (item == null) {
                list.add(null);
                continue;
            }

            Object value = item;
            Class<?> valueClass = value.getClass();
            if ((valueClass == JSONObject.class || valueClass == JSONFactory.getClassJSONObject1x())
                    && this.itemClass != valueClass
            ) {
                if (itemObjectReader == null) {
                    itemObjectReader = provider.getObjectReader(itemType);
                }
                value = itemObjectReader.createInstance((JSONObject) value, features);
            } else if (valueClass != itemType) {
                Function typeConvert = provider.getTypeConvert(valueClass, itemType);
                if (typeConvert != null) {
                    value = typeConvert.apply(value);
                } else if (item instanceof Map) {
                    Map map = (Map) item;
                    if (itemObjectReader == null) {
                        itemObjectReader = provider.getObjectReader(itemType);
                    }
                    value = itemObjectReader.createInstance(map, features);
                } else if (value instanceof Collection) {
                    if (itemObjectReader == null) {
                        itemObjectReader = provider.getObjectReader(itemType);
                    }
                    value = itemObjectReader.createInstance((Collection) value, features);
                } else if (itemClass.isInstance(value)) {
                    // skip
                } else if (Enum.class.isAssignableFrom(itemClass)) {
                    if (itemObjectReader == null) {
                        itemObjectReader = provider.getObjectReader(itemType);
                    }

                    if (itemObjectReader instanceof ObjectReaderImplEnum) {
                        value = ((ObjectReaderImplEnum) itemObjectReader).getEnum((String) value);
                    } else if (itemObjectReader instanceof ObjectReaderImplEnum2X4) {
                        value = ((ObjectReaderImplEnum2X4) itemObjectReader).getEnum((String) value);
                    } else {
                        throw new JSONException("can not convert from " + valueClass + " to " + itemType);
                    }
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
            return new ArrayList();
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

        if (listSingleton != null) {
            return listSingleton;
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
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        ObjectReader objectReader = jsonReader.checkAutoType(this.listClass, 0, features);
        Function builder = this.builder;
        Class listType = this.instanceType;
        if (objectReader != null) {
            if (objectReader instanceof ObjectReaderImplList) {
                listType = ((ObjectReaderImplList) objectReader).instanceType;
                builder = ((ObjectReaderImplList) objectReader).builder;
            } else {
                listType = objectReader.getObjectClass();
            }

            if (listType == CLASS_UNMODIFIABLE_COLLECTION) {
                listType = ArrayList.class;
                builder = (Function<Collection, Collection>) Collections::unmodifiableCollection;
            } else if (listType == CLASS_UNMODIFIABLE_LIST) {
                listType = ArrayList.class;
                builder = (Function<List, List>) Collections::unmodifiableList;
            } else if (listType == CLASS_UNMODIFIABLE_SET) {
                listType = LinkedHashSet.class;
                builder = (Function<Set, Set>) Collections::unmodifiableSet;
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
                    .context
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
                        jsonReader.addResolveTask(list, i, JSONPath.of(reference));
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
        } else if (listType != null && EnumSet.class.isAssignableFrom(listType)) {
            // maybe listType is java.util.RegularEnumSet or java.util.JumboEnumSet
            list = new HashSet();
            builder = (o) -> {
                Collection collection = (Collection) o;
                if (collection.isEmpty() && itemType instanceof Class) {
                    return EnumSet.noneOf((Class) itemType);
                } else {
                    return EnumSet.copyOf(collection);
                }
            };
        } else if (listType != null && listType != this.listType) {
            try {
                list = (Collection) listType.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new JSONException(jsonReader.info("create instance error " + listType), e);
            }
        } else {
            list = (Collection) createInstance(jsonReader.context.features | features);
        }

        ObjectReader itemObjectReader = this.itemObjectReader;
        Type itemType = this.itemType;
        if (fieldType != null && fieldType != listType && fieldType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) fieldType).getActualTypeArguments();
            if (actualTypeArguments.length == 1) {
                itemType = actualTypeArguments[0];
                if (itemType != this.itemType) {
                    itemObjectReader = jsonReader.getObjectReader(itemType);
                }
            }
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
        JSONReader.Context context = jsonReader.context;
        if (itemObjectReader == null) {
            itemObjectReader = context
                    .getObjectReader(itemType);
        }

        if (jsonReader.jsonb) {
            return readJSONBObject(jsonReader, fieldType, fieldName, 0);
        }

        if (jsonReader.readIfNull()) {
            return null;
        }

        Collection list;
        if (jsonReader.nextIfSet()) {
            list = new HashSet();
        } else {
            list = (Collection) createInstance(context.features | features);
        }
        char ch = jsonReader.current();
        if (ch == '"') {
            String str = jsonReader.readString();
            if (itemClass == String.class) {
                jsonReader.nextIfComma();
                list.add(str);
                return list;
            }

            if (str.isEmpty()) {
                jsonReader.nextIfComma();
                return null;
            }

            ObjectReaderProvider provider = context.getProvider();
            if (itemClass.isEnum()) {
                ObjectReader enumReader = provider.getObjectReader(itemClass);
                if (enumReader instanceof ObjectReaderImplEnum) {
                    Enum e = ((ObjectReaderImplEnum) enumReader).getEnum(str);
                    if (e == null) {
                        if (JSONReader.Feature.ErrorOnEnumNotMatch.isEnabled(jsonReader.features(features))) {
                            throw new JSONException(jsonReader.info("enum not match : " + str));
                        }
                        return null;
                    }
                    list.add(e);
                    return list;
                }
            }

            Function typeConvert = context.provider.getTypeConvert(String.class, itemType);
            if (typeConvert != null) {
                Object converted = typeConvert.apply(str);
                jsonReader.nextIfComma();
                list.add(converted);
                return list;
            }
            throw new JSONException(jsonReader.info());
        } else if (ch == '[') {
            jsonReader.next();
        } else {
            if ((itemClass != Object.class && itemObjectReader != null) || (itemClass == Object.class && jsonReader.isObject())) {
                Object item = itemObjectReader.readObject(jsonReader, itemType, 0, 0);
                list.add(item);
                if (builder != null) {
                    list = (Collection) builder.apply(list);
                }
                return list;
            }

            throw new JSONException(jsonReader.info());
        }

        ObjectReader itemObjectReader = this.itemObjectReader;
        Type itemType = this.itemType;
        if (fieldType != null && fieldType != listType && fieldType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) fieldType).getActualTypeArguments();
            if (actualTypeArguments.length == 1) {
                itemType = actualTypeArguments[0];
                if (itemType != this.itemType) {
                    itemObjectReader = jsonReader.getObjectReader(itemType);
                }
            }
        }

        for (int i = 0; ; ++i) {
            if (jsonReader.nextIfArrayEnd()) {
                break;
            }

            if (jsonReader.current() == ',') {
                throw new JSONException(jsonReader.info("illegal input error"));
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
        }

        jsonReader.nextIfComma();

        if (builder != null) {
            return builder.apply(list);
        }

        return list;
    }
}
