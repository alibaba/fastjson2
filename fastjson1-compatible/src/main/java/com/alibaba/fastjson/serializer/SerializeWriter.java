package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.PropertyPreFilter;

import java.util.ArrayList;
import java.util.List;

public class SerializeWriter {
    final JSONWriter raw;

    public JSONWriter getRaw() {
        return raw;
    }

    final ListWrapper<PropertyFilter> propertyFilters;
    final ListWrapper<ValueFilter> valueFilters;
    final ListWrapper<NameFilter> nameFilters;

    public SerializeWriter() {
        this(JSONWriter.of());
    }

    public SerializeWriter(JSONWriter raw) {
        this.raw = raw;
        this.propertyFilters = new ListWrapper<>();
        this.valueFilters = new ListWrapper<>();
        this.nameFilters = new ListWrapper<>();
    }

    public void writeNull() {
        this.raw.writeNull();
    }

    public void write(String text) {
        this.raw.writeString(text);
    }

    public List<PropertyFilter> getPropertyFilters() {
        return propertyFilters;
    }

    public List<ValueFilter> getValueFilters() {
        return valueFilters;
    }

    public List<NameFilter> getNameFilters() {
        return nameFilters;
    }

    class ListWrapper<T>
            extends ArrayList<T> {
        public boolean add(T filter) {
            JSONWriter.Context context = raw.getContext();

            if (filter instanceof PropertyFilter) {
                context.setPropertyFilter((PropertyFilter) filter);
            }

            if (filter instanceof ValueFilter) {
                context.setValueFilter((ValueFilter) filter);
            }

            if (filter instanceof NameFilter) {
                context.setNameFilter((NameFilter) filter);
            }

            if (filter instanceof PropertyPreFilter) {
                context.setPropertyPreFilter((PropertyPreFilter) filter);
            }

            return super.add(filter);
        }
    }

    public String toString() {
        return raw.toString();
    }
}
