package com.alibaba.fastjson;


import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson.annotation.JSONPOJOBuilder;
import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.modules.ObjectReaderAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.BeanUtils;

import java.lang.reflect.*;
import java.util.LinkedHashSet;
import java.util.Set;

public class Fastjson1xReaderModule implements ObjectReaderModule {
    final ObjectReaderProvider provider;
    final ReaderAnnotationProcessor annotationProcessor;

    public Fastjson1xReaderModule(ObjectReaderProvider provider) {
        this.provider = provider;
        this.annotationProcessor = new ReaderAnnotationProcessor();
    }

    public ObjectReader getObjectReader(ObjectReaderProvider provider, Type type) {
        if (type == JSON.class) {
            return new JSONImpl();
        }
        return null;
    }

    static class JSONImpl implements ObjectReader {
        public Object readObject(JSONReader jsonReader, long features) {
            if (jsonReader.isObject()) {
                return jsonReader.read(JSONObject.class);
            }
            if (jsonReader.isArray()) {
                return jsonReader.read(JSONArray.class);
            }

            throw new JSONException("read json error");
        }
    }

    class ReaderAnnotationProcessor implements ObjectReaderAnnotationProcessor {
        @Override
        public void getBeanInfo(BeanInfo beanInfo, Class<?> objectClass) {
            Class mixInSource = provider.getMixIn(objectClass);
            if (mixInSource != null && mixInSource != objectClass) {
                getBeanInfo(beanInfo
                        , (JSONType) mixInSource.getAnnotation(JSONType.class));

                BeanUtils.staticMethod(mixInSource, method -> {
                    JSONCreator jsonCreator = method.getAnnotation(JSONCreator.class);
                    if (jsonCreator != null) {
                        Method targetMethod = null;
                        try {
                            targetMethod = objectClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
                        } catch (NoSuchMethodException ignored) {

                        }

                        if (targetMethod != null) {
                            beanInfo.createMethod = targetMethod;
                            String[] createParameterNames = jsonCreator.parameterNames();
                            if (createParameterNames.length != 0) {
                                beanInfo.createParameterNames = createParameterNames;
                            }
                        }
                    }
                });

                BeanUtils.constructor(mixInSource, constructor -> {
                    JSONCreator jsonCreator = (JSONCreator) constructor.getAnnotation(JSONCreator.class);
                    if (jsonCreator != null) {
                        Constructor<?> targetConstructor = null;
                        try {
                            targetConstructor = objectClass.getDeclaredConstructor(constructor.getParameterTypes());
                        } catch (NoSuchMethodException ignored) {
                        }
                        if (targetConstructor != null) {
                            beanInfo.creatorConstructor = targetConstructor;
                            String[] createParameterNames = jsonCreator.parameterNames();
                            if (createParameterNames.length != 0) {
                                beanInfo.createParameterNames = createParameterNames;
                            }
                        }
                    }
                });
            }

            getBeanInfo(beanInfo
                    , objectClass.getAnnotation(JSONType.class));

            BeanUtils.staticMethod(objectClass, method -> {
                JSONCreator jsonCreator = method.getAnnotation(JSONCreator.class);
                if (jsonCreator != null) {
                    beanInfo.createMethod = method;
                    String[] createParameterNames = jsonCreator.parameterNames();
                    if (createParameterNames.length != 0) {
                        beanInfo.createParameterNames = createParameterNames;
                    }
                }
            });

            BeanUtils.constructor(objectClass, constructor -> {
                JSONCreator jsonCreator = (JSONCreator) constructor.getAnnotation(JSONCreator.class);
                if (jsonCreator != null) {
                    beanInfo.creatorConstructor = constructor;
                    String[] createParameterNames = jsonCreator.parameterNames();
                    if (createParameterNames.length != 0) {
                        beanInfo.createParameterNames = createParameterNames;
                    }
                }
            });
        }

        void getBeanInfo(BeanInfo beanInfo, JSONType jsonType) {
            if (jsonType == null) {
                return;
            }

            Class<?>[] classes = jsonType.seeAlso();
            if (classes.length != 0) {
                beanInfo.seeAlso = classes;
                beanInfo.seeAlsoNames = new String[classes.length];
                for (int i = 0; i < classes.length; i++) {
                    Class<?> item = classes[i];

                    BeanInfo itemBeanInfo = new BeanInfo();
                    getBeanInfo(itemBeanInfo, item);
                    String typeName = itemBeanInfo.typeName;
                    if (typeName == null || typeName.isEmpty()) {
                        typeName = item.getSimpleName();
                    }
                    beanInfo.seeAlsoNames[i] = typeName;
                }
                beanInfo.readerFeatures |= JSONReader.Feature.SupportAutoType.mask;
            }

            String jsonTypeKey = jsonType.typeKey();
            if (!jsonTypeKey.isEmpty()) {
                beanInfo.typeKey = jsonTypeKey;
            }

            String typeName = jsonType.typeName();
            if (!typeName.isEmpty()) {
                beanInfo.typeName = typeName;
            }

            beanInfo.namingStrategy =
                    jsonType
                            .naming()
                            .name();

            for (Feature feature : jsonType.parseFeatures()) {
                switch (feature) {
                    case SupportAutoType:
                        beanInfo.readerFeatures |= JSONReader.Feature.SupportAutoType.mask;
                    case SupportArrayToBean:
                        beanInfo.readerFeatures |= JSONReader.Feature.SupportArrayToBean.mask;
                    case InitStringFieldAsEmpty:
                        beanInfo.readerFeatures |= JSONReader.Feature.InitStringFieldAsEmpty.mask;
                    default:
                        break;
                }
            }

            Class<?> builderClass = jsonType.builder();
            if (builderClass != void.class && builderClass != Void.class) {
                beanInfo.builder = builderClass;

                JSONPOJOBuilder jsonBuilder = builderClass.getAnnotation(JSONPOJOBuilder.class);
                if (jsonBuilder != null) {
                    String buildMethodName = jsonBuilder.buildMethod();
                    beanInfo.buildMethod = BeanUtils.buildMethod(builderClass, buildMethodName);
                    String withPrefix = jsonBuilder.withPrefix();
                    if (!withPrefix.isEmpty()) {
                        beanInfo.builderWithPrefix = withPrefix;
                    }
                }

                if (beanInfo.buildMethod == null) {
                    beanInfo.buildMethod = BeanUtils.buildMethod(builderClass, "build");
                }

                if (beanInfo.buildMethod == null) {
                    beanInfo.buildMethod = BeanUtils.buildMethod(builderClass, "create");
                }
            }
        }

        @Override
        public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Constructor constructor, int paramIndex, Parameter parameter) {
            Class mixInSource = provider.getMixIn(objectClass);
            if (mixInSource != null && mixInSource != objectClass) {
                Constructor mixInConstructor = null;
                try {
                    mixInConstructor = mixInSource.getDeclaredConstructor(constructor.getParameterTypes());
                } catch (NoSuchMethodException ignored) {

                }
                if (mixInConstructor != null) {
                    Parameter mixInParam = mixInConstructor.getParameters()[paramIndex];
                    getFieldInfo(fieldInfo, mixInParam.getAnnotation(JSONField.class));
                }
            }

            getFieldInfo(fieldInfo
                    , parameter.getAnnotation(JSONField.class));
        }

        @Override
        public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Method method, int paramIndex, Parameter parameter) {
            Class mixInSource = provider.getMixIn(objectClass);
            if (mixInSource != null && mixInSource != objectClass) {
                Method mixInMethod = null;
                try {
                    mixInMethod = mixInSource.getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException ignored) {

                }
                if (mixInMethod != null) {
                    Parameter mixInParam = mixInMethod.getParameters()[paramIndex];
                    getFieldInfo(fieldInfo, mixInParam.getAnnotation(JSONField.class));
                }
            }

            getFieldInfo(fieldInfo
                    , parameter.getAnnotation(JSONField.class));
        }

        @Override
        public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Field field) {
            Class mixInSource = provider.getMixIn(objectClass);
            if (mixInSource != null && mixInSource != objectClass) {
                Field mixInField = null;
                try {
                    mixInField = mixInSource.getDeclaredField(field.getName());
                } catch (Exception ignored) {
                }

                if (mixInField != null) {
                    getFieldInfo(fieldInfo, mixInSource, mixInField);
                }
            }

            getFieldInfo(fieldInfo
                    , field.getAnnotation(JSONField.class));
        }

        @Override
        public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Method method) {
            Class mixInSource = provider.getMixIn(objectClass);
            if (mixInSource != null && mixInSource != objectClass) {
                Method mixInMethod = null;
                try {
                    mixInMethod = mixInSource.getDeclaredMethod(method.getName(), method.getParameterTypes());
                } catch (Exception ignored) {
                }

                if (mixInMethod != null) {
                    getFieldInfo(fieldInfo, mixInSource, mixInMethod);
                }
            }

            getFieldInfo(fieldInfo
                    , method.getAnnotation(JSONField.class));

            String fieldName = BeanUtils.getterName(method.getName(), null);
            Field declaredField = null;
            try {
                declaredField = objectClass.getDeclaredField(fieldName);
            } catch (Throwable ignored) {
                // skip
            }

            if (declaredField != null) {
                int modifiers = declaredField.getModifiers();
                if ((!Modifier.isPublic(modifiers)) && !Modifier.isStatic(modifiers)) {
                    getFieldInfo(fieldInfo, objectClass, declaredField);
                }
            }
        }

        private void getFieldInfo(FieldInfo fieldInfo, JSONField jsonField) {
            if (jsonField == null) {
                return;
            }

            String jsonFieldName = jsonField.name();
            if (jsonFieldName != null && !jsonFieldName.isEmpty()) {
                fieldInfo.fieldName = jsonFieldName;
            }

            String jsonFieldFormat = jsonField.format();
            if (jsonFieldFormat != null && !jsonFieldFormat.isEmpty()) {
                if (jsonFieldFormat.indexOf('T') != -1 && !jsonFieldFormat.contains("'T'")) {
                    jsonFieldFormat = jsonFieldFormat.replaceAll("T", "'T'");
                }

                fieldInfo.format = jsonFieldFormat;
            }

            String[] alternateNames = jsonField.alternateNames();
            if (alternateNames.length != 0) {
                if (fieldInfo.alternateNames == null) {
                    fieldInfo.alternateNames = alternateNames;
                } else {
                    Set<String> nameSet = new LinkedHashSet<>();
                    for (String alternateName : alternateNames) {
                        nameSet.add(alternateName);
                    }
                    for (String alternateName : fieldInfo.alternateNames) {
                        nameSet.add(alternateName);
                    }
                    fieldInfo.alternateNames = nameSet.toArray(new String[nameSet.size()]);
                }
            }

            fieldInfo.ignore = !jsonField.deserialize();

            for (Feature feature : jsonField.parseFeatures()) {
                switch (feature) {
                    case SupportAutoType:
                        fieldInfo.features |= JSONReader.Feature.SupportAutoType.mask;
                    case SupportArrayToBean:
                        fieldInfo.features |= JSONReader.Feature.SupportArrayToBean.mask;
                    case InitStringFieldAsEmpty:
                        fieldInfo.features |= JSONReader.Feature.InitStringFieldAsEmpty.mask;
                    default:
                        break;
                }
            }

            int ordinal = jsonField.ordinal();
            if (ordinal != 0) {
                fieldInfo.ordinal = ordinal;
            }
        }
    }

    public ObjectReaderAnnotationProcessor getAnnotationProcessor() {
        return annotationProcessor;
    }
}
