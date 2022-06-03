package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.util.function.BiFunction;

class JSONPathCompilerReflect
        implements JSONFactory.JSONPathCompiler {
    static final JSONPathCompilerReflect INSTANCE = new JSONPathCompilerReflect();

    public JSONPath compile(
            Class objectClass, JSONPath path
    ) {
        if (path instanceof JSONPath.SingleNamePath) {
            String fieldName = ((JSONPath.SingleNamePath) path).name;

            ObjectReader objectReader = path.getReaderContext().getObjectReader(objectClass);
            FieldReader fieldReader = objectReader.getFieldReader(fieldName);

            ObjectWriter objectWriter = path.getWriterContext().getObjectWriter(objectClass);
            FieldWriter fieldWriter = objectWriter.getFieldWriter(fieldName);

            return new SingleNamePathTyped(path.path, objectClass, objectReader, fieldReader, objectWriter, fieldWriter);
        }
        return path;
    }

    public static class SingleNamePathTyped
            extends JSONPath {
        final Class objectClass;
        final ObjectReader objectReader;
        final FieldReader fieldReader;
        final ObjectWriter objectWriter;
        final FieldWriter fieldWriter;
        public SingleNamePathTyped(
                String path,
                Class objectClass,
                ObjectReader objectReader,
                FieldReader fieldReader,
                ObjectWriter objectWriter,
                FieldWriter fieldWriter
        ) {
            super(path);
            this.objectClass = objectClass;
            this.objectReader = objectReader;
            this.fieldReader = fieldReader;
            this.objectWriter = objectWriter;
            this.fieldWriter = fieldWriter;
        }

        @Override
        public boolean isRef() {
            return true;
        }

        @Override
        public boolean contains(Object rootObject) {
            return fieldWriter != null
                    && fieldWriter.getFieldValue(rootObject) != null;
        }

        @Override
        public Object eval(Object object) {
            if (fieldWriter == null) {
                throw new UnsupportedOperationException();
            }

            return fieldWriter.getFieldValue(object);
        }

        @Override
        public Object extract(JSONReader jsonReader) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String extractScalar(JSONReader jsonReader) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Object rootObject, Object value) {
            if (fieldReader == null) {
                throw new UnsupportedOperationException();
            }
            fieldReader.accept(rootObject, value);
        }

        @Override
        public void set(Object rootObject, Object value, JSONReader.Feature... readerFeatures) {
            if (fieldReader == null) {
                throw new UnsupportedOperationException();
            }
            fieldReader.accept(rootObject, value);
        }

        @Override
        public void setCallback(Object rootObject, BiFunction callback) {
            if (fieldWriter == null) {
                throw new UnsupportedOperationException();
            }

            Object fieldValue = fieldWriter.getFieldValue(rootObject);
            Object fieldValueApply = callback.apply(rootObject, fieldValue);
            if (fieldValueApply == fieldValue) {
                return;
            }
            if (fieldReader == null) {
                throw new UnsupportedOperationException();
            }
            fieldReader.accept(rootObject, fieldValueApply);
        }

        @Override
        public void setInt(Object rootObject, int value) {
            if (fieldReader == null) {
                throw new UnsupportedOperationException();
            }
            fieldReader.accept(rootObject, value);
        }

        @Override
        public void setLong(Object rootObject, long value) {
            if (fieldReader == null) {
                throw new UnsupportedOperationException();
            }
            fieldReader.accept(rootObject, value);
        }

        @Override
        public boolean remove(Object rootObject) {
            throw new UnsupportedOperationException();
        }
    }
}
