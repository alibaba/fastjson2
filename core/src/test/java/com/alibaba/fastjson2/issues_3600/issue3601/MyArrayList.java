package com.alibaba.fastjson2.issues_3600.issue3601;

import lombok.var;

import java.util.*;

public class MyArrayList<T>
        implements List<T> {
    private transient ArrayList<T> impl = new ArrayList<>();

    public MyArrayList() {
    }

    public MyArrayList(Collection<T> coll) {
        this.addAll(coll);
    }

    public MyArrayList(T... coll) {
        for (T v : coll) {
            this.add(v);
        }
    }

    @Override
    public int size() {
        return impl.size();
    }

    @Override
    public boolean isEmpty() {
        return impl.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return impl.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return impl.iterator();
    }

    @Override
    public Object[] toArray() {
        return impl.toArray();
    }

    @Override
    public <V> V[] toArray(V[] a) {
        return impl.toArray(a);
    }

    @Override
    public boolean add(T v) {
        boolean r = impl.add(v);
        return r;
    }

    @Override
    public boolean remove(Object o) {
        int index = impl.indexOf(o);
        var r = index >= 0;
        return r;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return impl.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        impl.ensureCapacity(impl.size() + c.size());
        for (var v : c) {
            this.add(v);
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        impl.ensureCapacity(impl.size() + c.size());
        for (var v : c) {
            this.add(index++, v);
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (var v : c) {
            this.remove(v);
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("retainAll");
    }

    @Override
    public void clear() {
        if (isEmpty()) {
            return;
        }
        impl.clear();
    }

    @Override
    public T get(int index) {
        return impl.get(index);
    }

    @Override
    public T set(int index, T element) {
        var r = impl.set(index, element);
        return r;
    }

    @Override
    public void add(int index, T element) {
        impl.add(index, element);
    }

    @Override
    public T remove(int index) {
        T prev = impl.remove(index);
        return prev;
    }

    @Override
    public int indexOf(Object o) {
        return impl.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return impl.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return impl.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return impl.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return Collections.unmodifiableList(impl.subList(fromIndex, toIndex));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MyArrayList<?> dbList = (MyArrayList<?>) o;
        return impl.equals(dbList.impl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impl);
    }

    @Override
    public String toString() {
        return "MyArrayList{" +
                "impl=" + impl +
                '}';
    }
}
