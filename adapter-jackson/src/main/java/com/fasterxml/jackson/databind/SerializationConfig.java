package com.fasterxml.jackson.databind;

import com.alibaba.fastjson2.JSONException;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.ser.FilterProvider;

import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class SerializationConfig {
    private long serFeatures;
    protected int generatorFeatures;

    public SerializationConfig() {

    }

    public final boolean isEnabled(SerializationFeature f) {
        return (serFeatures & f.getMask()) != 0;
    }

    public SerializationConfig with(DateFormat df) {
        // TODO
        throw new JSONException("TODO");
    }

    public SerializationConfig with(Locale locale) {
        // TODO
        throw new JSONException("TODO");
    }

    public SerializationConfig with(TimeZone tz) {
        // TODO
        throw new JSONException("TODO");
    }

    public SerializationConfig with(AnnotationIntrospector ai) {
        // TODO
        throw new JSONException("TODO");
    }

    public SerializationConfig with(PropertyNamingStrategy s) {
        // TODO
        throw new JSONException("TODO");
    }

    public SerializationConfig with(TypeResolverBuilder<?> typer) {
        // TODO
        throw new JSONException("TODO");
    }

    public SerializationConfig with(HandlerInstantiator hi) {
        // TODO
        throw new JSONException("TODO");
    }

    public SerializationConfig with(MapperFeature f) {
        // TODO
        return this;
    }

    public SerializationConfig without(MapperFeature f) {
        // TODO
        return this;
    }

    public SerializationConfig with(SerializationFeature f) {
        // TODO
        return this;
    }

    public SerializationConfig without(SerializationFeature f) {
        // TODO
        return this;
    }

    public SerializationConfig withFilters(FilterProvider filterProvider) {
        // TODO
        throw new JSONException("TODO");
    }
}
