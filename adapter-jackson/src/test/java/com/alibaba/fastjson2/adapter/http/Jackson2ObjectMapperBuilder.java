package com.alibaba.fastjson2.adapter.http;

import com.alibaba.fastjson2.JSONException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.converter.json.SpringHandlerInstantiator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.DateFormat;
import java.util.*;
import java.util.function.Consumer;

public class Jackson2ObjectMapperBuilder {

    private final Map<Class<?>, Class<?>> mixIns = new LinkedHashMap<>();

    private final Map<Class<?>, JsonSerializer<?>> serializers = new LinkedHashMap<>();

    private final Map<Class<?>, JsonDeserializer<?>> deserializers = new LinkedHashMap<>();

    private final Map<PropertyAccessor, JsonAutoDetect.Visibility> visibilities = new LinkedHashMap<>();

    private final Map<Object, Boolean> features = new LinkedHashMap<>();

    private boolean createXmlMapper = false;

    @Nullable
    private JsonFactory factory;

    @Nullable
    private DateFormat dateFormat;

    @Nullable
    private Locale locale;

    @Nullable
    private TimeZone timeZone;

    @Nullable
    private AnnotationIntrospector annotationIntrospector;

    @Nullable
    private PropertyNamingStrategy propertyNamingStrategy;

    @Nullable
    private TypeResolverBuilder<?> defaultTyping;

    @Nullable
    private JsonInclude.Value serializationInclusion;

    @Nullable
    private FilterProvider filters;

    @Nullable
    private List<com.fasterxml.jackson.databind.Module> modules;

    @Nullable
    private Class<? extends com.fasterxml.jackson.databind.Module>[] moduleClasses;

    private boolean findModulesViaServiceLoader = false;

    private boolean findWellKnownModules = true;

    private ClassLoader moduleClassLoader = getClass().getClassLoader();

    @Nullable
    private HandlerInstantiator handlerInstantiator;

    @Nullable
    private ApplicationContext applicationContext;

    @Nullable
    private Boolean defaultUseWrapper;

    @Nullable
    private Consumer<ObjectMapper> configurer;

    public static Jackson2ObjectMapperBuilder json() {
        return new Jackson2ObjectMapperBuilder();
    }

    public <T extends ObjectMapper> T build() {
        ObjectMapper mapper = (this.factory != null ? new ObjectMapper(this.factory) : new ObjectMapper());
        configure(mapper);
        return (T) mapper;
    }

    public void configure(ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "ObjectMapper must not be null");

        MultiValueMap<Object, com.fasterxml.jackson.databind.Module> modulesToRegister = new LinkedMultiValueMap<>();
        if (this.findModulesViaServiceLoader) {
            ObjectMapper.findModules(this.moduleClassLoader).forEach(module -> registerModule(module, modulesToRegister));
        } else if (this.findWellKnownModules) {
            registerWellKnownModulesIfAvailable(modulesToRegister);
        }

        if (this.modules != null) {
            this.modules.forEach(module -> registerModule(module, modulesToRegister));
        }
        if (this.moduleClasses != null) {
            for (Class<? extends com.fasterxml.jackson.databind.Module> moduleClass : this.moduleClasses) {
                registerModule(BeanUtils.instantiateClass(moduleClass), modulesToRegister);
            }
        }
        List<com.fasterxml.jackson.databind.Module> modules = new ArrayList<>();
        for (List<com.fasterxml.jackson.databind.Module> nestedModules : modulesToRegister.values()) {
            modules.addAll(nestedModules);
        }
        objectMapper.registerModules(modules);

        if (this.dateFormat != null) {
            objectMapper.setDateFormat(this.dateFormat);
        }
        if (this.locale != null) {
            objectMapper.setLocale(this.locale);
        }
        if (this.timeZone != null) {
            objectMapper.setTimeZone(this.timeZone);
        }

        if (this.annotationIntrospector != null) {
            objectMapper.setAnnotationIntrospector(this.annotationIntrospector);
        }

        if (this.propertyNamingStrategy != null) {
            objectMapper.setPropertyNamingStrategy(this.propertyNamingStrategy);
        }

        if (this.defaultTyping != null) {
            objectMapper.setDefaultTyping(this.defaultTyping);
        }

        if (this.serializationInclusion != null) {
            objectMapper.setDefaultPropertyInclusion(this.serializationInclusion);
        }

        if (this.filters != null) {
            objectMapper.setFilterProvider(this.filters);
        }

        this.mixIns.forEach(objectMapper::addMixIn);

        if (!this.serializers.isEmpty() || !this.deserializers.isEmpty()) {
            SimpleModule module = new SimpleModule();
            addSerializers(module);
            addDeserializers(module);
            objectMapper.registerModule(module);
        }

        this.visibilities.forEach(objectMapper::setVisibility);

        customizeDefaultFeatures(objectMapper);
        this.features.forEach((feature, enabled) -> configureFeature(objectMapper, feature, enabled));

        if (this.handlerInstantiator != null) {
            objectMapper.setHandlerInstantiator(this.handlerInstantiator);
        } else if (this.applicationContext != null) {
            objectMapper.setHandlerInstantiator(
                    new SpringHandlerInstantiator(this.applicationContext.getAutowireCapableBeanFactory()));
        }

        if (this.configurer != null) {
            this.configurer.accept(objectMapper);
        }
    }

    private void configureFeature(ObjectMapper objectMapper, Object feature, boolean enabled) {
        if (feature instanceof JsonParser.Feature) {
            objectMapper.configure((JsonParser.Feature) feature, enabled);
        } else if (feature instanceof JsonGenerator.Feature) {
            objectMapper.configure((JsonGenerator.Feature) feature, enabled);
        } else if (feature instanceof SerializationFeature) {
            objectMapper.configure((SerializationFeature) feature, enabled);
        } else if (feature instanceof DeserializationFeature) {
            objectMapper.configure((DeserializationFeature) feature, enabled);
        } else if (feature instanceof MapperFeature) {
            objectMapper.configure((MapperFeature) feature, enabled);
        } else {
            throw new FatalBeanException("Unknown feature class: " + feature.getClass().getName());
        }
    }

    private void customizeDefaultFeatures(ObjectMapper objectMapper) {
        if (!this.features.containsKey(MapperFeature.DEFAULT_VIEW_INCLUSION)) {
            configureFeature(objectMapper, MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        }
        if (!this.features.containsKey(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)) {
            configureFeature(objectMapper, DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
    }

    private <T> void addSerializers(SimpleModule module) {
        this.serializers.forEach((type, serializer) ->
                module.addSerializer((Class<? extends T>) type, (JsonSerializer<T>) serializer));
    }

    private <T> void addDeserializers(SimpleModule module) {
        this.deserializers.forEach((type, deserializer) ->
                module.addDeserializer((Class<T>) type, (JsonDeserializer<? extends T>) deserializer));
    }

    private void registerModule(
            com.fasterxml.jackson.databind.Module module,
            MultiValueMap<Object, com.fasterxml.jackson.databind.Module> modulesToRegister
    ) {
        // TODO
        throw new JSONException("TODO");
    }

    private void registerWellKnownModulesIfAvailable(
            MultiValueMap<Object, com.fasterxml.jackson.databind.Module> modulesToRegister
    ) {
        // TODO
        throw new JSONException("TODO");
    }
}
