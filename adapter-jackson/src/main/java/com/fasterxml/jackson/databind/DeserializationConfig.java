package com.fasterxml.jackson.databind;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;

import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class DeserializationConfig {
    private int features;

    public DeserializationConfig with(DateFormat df) {
        // TODO
        throw new JSONException("TODO");
    }

    public DeserializationConfig with(Locale locale) {
        // TODO
        throw new JSONException("TODO");
    }

    public DeserializationConfig with(TimeZone tz) {
        // TODO
        throw new JSONException("TODO");
    }

    public DeserializationConfig with(AnnotationIntrospector ai) {
        // TODO
        throw new JSONException("TODO");
    }

    public DeserializationConfig with(PropertyNamingStrategy s) {
        // TODO
        throw new JSONException("TODO");
    }

    public DeserializationConfig with(TypeResolverBuilder<?> typer) {
        // TODO
        throw new JSONException("TODO");
    }

    public DeserializationConfig with(HandlerInstantiator hi) {
        // TODO
        throw new JSONException("TODO");
    }

    public DeserializationConfig with(DeserializationFeature f) {
        features |= f.getMask();
        return this;
    }

    public DeserializationConfig with(MapperFeature f) {
        // TODO
        return this;
    }

    public DeserializationConfig without(DeserializationFeature feature) {
        features &= ~feature.getMask();
        return this;
    }

    public DeserializationConfig without(MapperFeature f) {
        // TODO
        return this;
    }

    public void configTo(JSONReader.Context context) {
        if ((features & FAIL_ON_NULL_FOR_PRIMITIVES.getMask()) != 0) {
            context.config(JSONReader.Feature.ErrorOnNullForPrimitives);
        }
    }
}
