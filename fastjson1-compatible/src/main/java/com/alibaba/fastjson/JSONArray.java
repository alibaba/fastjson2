package com.alibaba.fastjson;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.util.*;
import java.util.function.Function;

public class JSONArray extends JSON implements List {
    private List list = new com.alibaba.fastjson2.JSONArray();

    public JSONArray() {

    }

    public JSONArray(List list) {
        this.list = list;
    }

    public JSONObject getJSONObject(int index) {
        Object value = list.get(index);

        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }

        if (value instanceof Map) {
            return new JSONObject((Map) value);
        }

        if (value instanceof String) {
            return JSON.parseObject((String) value);
        }

//        return (JSONObject) toJSON(value);
        throw new JSONException("TODO");
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public Object[] toArray(Object[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(Object item) {
        return list.add(item);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean addAll(Collection c) {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        return list.addAll(index, c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean retainAll(Collection c) {
        return list.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection c) {
        return list.removeAll(c);
    }

    @Override
    public boolean containsAll(Collection c) {
        return list.containsAll(c);
    }

    @Override
    public Object get(int index) {
        return list.get(index);
    }

    @Override
    public Object set(int index, Object element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, Object element) {
        list.add(index, element);
    }

    @Override
    public Object remove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    public String getString(int index) {
        Object value = list.get(index);
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        return com.alibaba.fastjson2.JSON.toJSONString(value);
    }

    public JSONArray getJSONArray(int index) {
        Object value = list.get(index);

        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }

        if (value instanceof List) {
            return new JSONArray((List) value);
        }

        return (JSONArray) toJSON(value);
    }

    public <T> T getObject(int index, Class<T> clazz) {
        Object obj = list.get(index);
        if (obj == null) {
            return null;
        }

        if (clazz.isInstance(obj)) {
            return (T) obj;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Function typeConvert = provider.getTypeConvert(obj.getClass(), clazz);
        if (typeConvert != null) {
            return (T) typeConvert.apply(obj);
        }

        String json = JSON.toJSONString(obj);
        ObjectReader objectReader = provider.getObjectReader(clazz);
        JSONReader jsonReader = JSONReader.of(json);

        String defaultDateFormat = JSON.DEFFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            jsonReader
                    .getContext()
                    .setUtilDateFormat(defaultDateFormat);
        }

        return (T) objectReader.readObject(jsonReader);
    }

    public <T> List<T> toJavaList(Class<T> clazz) {
        List<T> list = new ArrayList<T>(this.size());

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(clazz);

        for (Object item : this) {
            T classItem;
            if (item instanceof Map) {
                classItem = (T) objectReader.createInstance((Map) item);
            } else {
                throw new JSONException("TODO");
            }
            list.add(classItem);
        }

        return list;
    }

    public JSONArray fluentAdd(Object e) {
        list.add(e);
        return this;
    }

    public <T> T toJavaObject(Class<T> clazz) {
        return com.alibaba.fastjson2.JSON.toJavaObject(this, clazz);
    }

    public String toString() {
        return toJSONString(this);
    }
}
