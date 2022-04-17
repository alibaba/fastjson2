package com.alibaba.fastjson;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.modules.ObjectWriterAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

public class Fastjson1xWriterModule implements ObjectWriterModule {
    final ObjectWriterProvider provider;
    final WriterAnnotationProcessor annotationProcessor;

    public Fastjson1xWriterModule(ObjectWriterProvider provider) {
        this.provider = provider;
        this.annotationProcessor = new WriterAnnotationProcessor();
    }

    @Override
    public WriterAnnotationProcessor getAnnotationProcessor() {
        return annotationProcessor;
    }

    public ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        if (objectClass != null && JSONAware.class.isAssignableFrom(objectClass)) {
            return JSONAwareWriter.INSTANCE;
        }

        return null;
    }

    class WriterAnnotationProcessor implements ObjectWriterAnnotationProcessor {

        public void getBeanInfo(BeanInfo beanInfo, Class objectClass) {
            Class superclass = objectClass.getSuperclass();
            if (superclass != Object.class && superclass != null) {
                getBeanInfo(beanInfo, superclass);
            }

            JSONType jsonType = (JSONType) objectClass.getAnnotation(JSONType.class);
            if (jsonType != null) {
                Class<?>[] classes = jsonType.seeAlso();
                if (classes.length != 0) {
                    beanInfo.seeAlso = classes;
                }

                String typeKey = jsonType.typeKey();
                if (typeKey.length() != 0) {
                    beanInfo.typeKey = typeKey;
                }

                for (SerializerFeature feature : jsonType.serialzeFeatures()) {
                    switch (feature) {
                        case WriteMapNullValue:
                            beanInfo.writerFeatures |= JSONWriter.Feature.WriteNulls.mask;
                            break;
                        case WriteNullListAsEmpty:
                        case WriteNullStringAsEmpty:
                            beanInfo.writerFeatures |= JSONWriter.Feature.NullAsDefaultValue.mask;
                            break;
                        case BrowserCompatible:
                            beanInfo.writerFeatures |= JSONWriter.Feature.BrowserCompatible.mask;
                            break;
                        case WriteClassName:
                            beanInfo.writerFeatures |= JSONWriter.Feature.WriteClassName.mask;
                            break;
                        case WriteNonStringValueAsString:
                            beanInfo.writerFeatures |= JSONWriter.Feature.WriteNonStringValueAsString.mask;
                            break;
                        case WriteEnumUsingToString:
                            beanInfo.writerFeatures |= JSONWriter.Feature.WriteEnumUsingToString.mask;
                            break;
                        case NotWriteRootClassName:
                            beanInfo.writerFeatures |= JSONWriter.Feature.NotWriteRootClassName.mask;
                            break;
                        case IgnoreErrorGetter:
                            beanInfo.writerFeatures |= JSONWriter.Feature.IgnoreErrorGetter.mask;
                            break;
                        default:
                            break;
                    }
                }

                if (jsonType.serializeEnumAsJavaBean()) {
                    beanInfo.writeEnumAsJavaBean = true;
                }

                beanInfo.namingStrategy =
                        jsonType
                                .naming()
                                .name();
            }

            if (beanInfo.seeAlso != null && beanInfo.seeAlso.length != 0) {
                for (Class seeAlsoClass : beanInfo.seeAlso) {
                    if (seeAlsoClass == objectClass) {
                        beanInfo.typeName = objectClass.getSimpleName();
                    }
                }
            }
        }

        @Override
        public void getFieldInfo(FieldInfo fieldInfo, Class objectType, Field field) {
            Class mixInSource = provider.getMixIn(objectType);
            if (mixInSource != null && mixInSource != objectType) {
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

            String fieldName = BeanUtils.setterName(method.getName(), null);
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

            String jsonFIeldName = jsonField.name();
            if (!jsonFIeldName.isEmpty()) {
                fieldInfo.fieldName = jsonFIeldName;
            }

            String jsonFieldFormat = jsonField.format();
            if (jsonFieldFormat != null) {
                jsonFieldFormat = jsonFieldFormat.trim();
            }

            if (!jsonFieldFormat.isEmpty()) {
                if (jsonFieldFormat.indexOf('T') != -1 && !jsonFieldFormat.contains("'T'")) {
                    jsonFieldFormat = jsonFieldFormat.replaceAll("T", "'T'");
                }

                fieldInfo.format = jsonFieldFormat;
            }

            fieldInfo.ignore = !jsonField.serialize();
            if (jsonField.unwrapped()) {
                fieldInfo.format = "unwrapped";
            }
            applyFeatures(fieldInfo, jsonField.serialzeFeatures());

            int ordinal = jsonField.ordinal();
            if (ordinal != 0) {
                fieldInfo.ordinal = ordinal;
            }
        }

        private void applyFeatures(FieldInfo fieldInfo, SerializerFeature[] features) {
            for (SerializerFeature feature : features) {
                switch (feature) {
                    case UseISO8601DateFormat:
                        fieldInfo.format = "iso8601";
                        break;
                    case WriteMapNullValue:
                        fieldInfo.features |= JSONWriter.Feature.WriteNulls.mask;
                        break;
                    case WriteNullListAsEmpty:
                    case WriteNullStringAsEmpty:
                        fieldInfo.features |= JSONWriter.Feature.NullAsDefaultValue.mask;
                        break;
                    case BrowserCompatible:
                        fieldInfo.features |= JSONWriter.Feature.BrowserCompatible.mask;
                        break;
                    case WriteClassName:
                        fieldInfo.features |= JSONWriter.Feature.WriteClassName.mask;
                        break;
                    case WriteNonStringValueAsString:
                        fieldInfo.features |= JSONWriter.Feature.WriteNonStringValueAsString.mask;
                        break;
                    case WriteEnumUsingToString:
                        fieldInfo.features |= JSONWriter.Feature.WriteEnumUsingToString.mask;
                        break;
                    case NotWriteRootClassName:
                        fieldInfo.features |= JSONWriter.Feature.NotWriteRootClassName.mask;
                        break;
                    case IgnoreErrorGetter:
                        fieldInfo.features |= JSONWriter.Feature.IgnoreErrorGetter.mask;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    static class JSONAwareWriter implements ObjectWriter {
        static final JSONAwareWriter INSTANCE = new JSONAwareWriter();
        @Override
        public List<FieldWriter> getFieldWriters() {
            return null;
        }

        @Override
        public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
                return;
            }

            throw new UnsupportedOperationException();
        }

        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
                return;
            }

            JSONAware jsonAware = (JSONAware) object;
            String str = jsonAware.toJSONString();
            jsonWriter.writeRaw(str);
        }
    }
}
