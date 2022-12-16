package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.LabelFilter;
import com.alibaba.fastjson2.filter.PropertyPreFilter;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class SerializeWriter
        implements Cloneable {
    SerializeConfig config;
    final JSONWriter raw;

    final ListWrapper<PropertyFilter> propertyFilters;
    final ListWrapper<ValueFilter> valueFilters;
    final ListWrapper<NameFilter> nameFilters;
    final ListWrapper<BeforeFilter> beforeFilters;
    final ListWrapper<AfterFilter> afterFilters;

    public SerializeWriter() {
        this(JSONWriter.of());
    }

    public SerializeWriter(SerializerFeature... features) {
        this(
                JSONWriter.of(
                        JSON.createWriteContext(
                                SerializeConfig.global,
                                JSON.DEFAULT_PARSER_FEATURE,
                                features
                        )
                )
        );
    }

    public SerializeWriter(SerializeConfig config, SerializerFeature... features) {
        this(
                JSONWriter.of(
                        JSON.createWriteContext(
                                config,
                                JSON.DEFAULT_PARSER_FEATURE,
                                features
                        )
                )
        );
        this.config = config;
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

    public void writeNull(SerializerFeature feature) {
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

    public void write(char c) {
        raw.writeRaw(c);
    }

    public void writeInt(int i) {
        raw.writeInt32(i);
    }

    public void writeLong(long i) {
        raw.writeInt64(i);
    }

    public void writeFieldName(String key) {
        raw.writeName(key);
    }

    @Override
    public String toString() {
        return raw.toString();
    }

    public byte[] toBytes(Charset charset) {
        return raw.getBytes(charset);
    }

    public byte[] toBytes(String charsetName) {
        return raw.getBytes(Charset.forName(charsetName));
    }

    public void close() {
        raw.close();
    }

    public void writeTo(Writer out) throws IOException {
        raw.flushTo(out);
    }

    public boolean isEnabled(SerializerFeature feature) {
        JSONWriter.Feature rawFeature = null;
        switch (feature) {
            case BeanToArray:
                rawFeature = JSONWriter.Feature.BeanToArray;
                break;
            case WriteMapNullValue:
                rawFeature = JSONWriter.Feature.WriteMapNullValue;
                break;
            case WriteEnumUsingToString:
                rawFeature = JSONWriter.Feature.WriteEnumUsingToString;
                break;
            case WriteEnumUsingName:
                rawFeature = JSONWriter.Feature.WriteEnumsUsingName;
                break;
            case WriteNullListAsEmpty:
                rawFeature = JSONWriter.Feature.WriteNullListAsEmpty;
                break;
            case WriteNullStringAsEmpty:
                rawFeature = JSONWriter.Feature.WriteNullStringAsEmpty;
                break;
            case WriteNullNumberAsZero:
                rawFeature = JSONWriter.Feature.WriteNullNumberAsZero;
                break;
            case WriteNullBooleanAsFalse:
                rawFeature = JSONWriter.Feature.WriteNullBooleanAsFalse;
                break;
            case WriteClassName:
                rawFeature = JSONWriter.Feature.WriteClassName;
                break;
            case NotWriteRootClassName:
                rawFeature = JSONWriter.Feature.NotWriteRootClassName;
                break;
            case WriteNonStringKeyAsString:
                rawFeature = JSONWriter.Feature.WriteNonStringKeyAsString;
                break;
            case NotWriteDefaultValue:
                rawFeature = JSONWriter.Feature.NotWriteDefaultValue;
                break;
            case BrowserCompatible:
                rawFeature = JSONWriter.Feature.BrowserCompatible;
                break;
            case BrowserSecure:
                rawFeature = JSONWriter.Feature.BrowserSecure;
                break;
            case IgnoreNonFieldGetter:
                rawFeature = JSONWriter.Feature.IgnoreNonFieldGetter;
                break;
            case WriteNonStringValueAsString:
                rawFeature = JSONWriter.Feature.WriteNonStringValueAsString;
                break;
            case IgnoreErrorGetter:
                rawFeature = JSONWriter.Feature.IgnoreErrorGetter;
                break;
            case WriteBigDecimalAsPlain:
                rawFeature = JSONWriter.Feature.WriteBigDecimalAsPlain;
                break;
            default:
                break;
        }

        if (rawFeature != null) {
            return raw.isEnabled(rawFeature);
        }

        return false;
    }

    public SerializeWriter append(char c) {
        raw.writeRaw(c);
        return this;
    }
}
