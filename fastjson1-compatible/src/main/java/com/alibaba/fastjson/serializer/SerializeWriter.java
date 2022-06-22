package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.LabelFilter;
import com.alibaba.fastjson2.filter.PropertyPreFilter;

import java.util.ArrayList;
import java.util.List;

public class SerializeWriter
        implements Cloneable {
    final JSONWriter raw;

    final ListWrapper<PropertyFilter> propertyFilters;
    final ListWrapper<ValueFilter> valueFilters;
    final ListWrapper<NameFilter> nameFilters;
    final ListWrapper<BeforeFilter> beforeFilters;
    final ListWrapper<AfterFilter> afterFilters;

    public SerializeWriter() {
        this(JSONWriter.of());
    }

    public SerializeWriter(JSONWriter raw) {
        this.raw = raw;
        this.propertyFilters = new ListWrapper<>();
        this.valueFilters = new ListWrapper<>();
        this.nameFilters = new ListWrapper<>();
        this.beforeFilters = new ListWrapper<>();
        this.afterFilters = new ListWrapper<>();
    }

    public void writeNull() {
        this.raw.writeNull();
    }

    public void writeString(String text) {
        this.raw.writeString(text);
    }

    public void write(String text) {
        this.raw.writeRaw(text);
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

    public List<BeforeFilter> getBeforeFilters() {
        return beforeFilters;
    }

    public List<AfterFilter> getAfterFilters() {
        return afterFilters;
    }

    class ListWrapper<T>
            extends ArrayList<T> {
        @Override
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

            if (filter instanceof BeforeFilter) {
                context.setBeforeFilter((BeforeFilter) filter);
            }

            if (filter instanceof AfterFilter) {
                context.setAfterFilter((AfterFilter) filter);
            }

            if (filter instanceof LabelFilter) {
                context.setLabelFilter((LabelFilter) filter);
            }

            return super.add(filter);
        }
    }

    public void write(int c) {
        raw.writeRaw((char) c);
    }

    public void writeFieldName(String key) {
        raw.writeName(key);
    }

    @Override
    public String toString() {
        return raw.toString();
    }

    public void close() {
        raw.close();
    }
}
