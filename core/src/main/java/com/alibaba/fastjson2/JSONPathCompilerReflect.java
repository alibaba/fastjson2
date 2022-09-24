package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.util.function.BiFunction;

public class JSONPathCompilerReflect
        implements JSONFactory.JSONPathCompiler {
    static final JSONPathCompilerReflect INSTANCE = new JSONPathCompilerReflect();

    @Override
    public JSONPath compile(
            Class objectClass, JSONPath path
    ) {
        if (path instanceof JSONPath.SingleNamePath) {
            return compileSingleNamePath(objectClass, (JSONPath.SingleNamePath) path);
        }

        if (path instanceof JSONPath.TwoSegmentPath) {
            JSONPath.TwoSegmentPath twoSegmentPath = (JSONPath.TwoSegmentPath) path;

            JSONPath.Segment first = compile(objectClass, path, twoSegmentPath.first, null);
            JSONPath.Segment segment = compile(objectClass, path, twoSegmentPath.second, first);

            if (first != twoSegmentPath.first || segment != twoSegmentPath.second) {
                if (first instanceof NameSegmentTyped && segment instanceof NameSegmentTyped) {
                    return new TwoNameSegmentTypedPath(twoSegmentPath.path, (NameSegmentTyped) first, (NameSegmentTyped) segment);
                }
                return new JSONPath.TwoSegmentPath(twoSegmentPath.path, first, segment);
            }
        }

        return path;
    }

    protected JSONPath compileSingleNamePath(Class objectClass, JSONPath.SingleNamePath path) {
        String fieldName = path.name;

        ObjectReader objectReader = path.getReaderContext().getObjectReader(objectClass);
        FieldReader fieldReader = objectReader.getFieldReader(fieldName);

        ObjectWriter objectWriter = path.getWriterContext().getObjectWriter(objectClass);
        FieldWriter fieldWriter = objectWriter.getFieldWriter(fieldName);

        return new SingleNamePathTyped(path.path, objectClass, objectReader, fieldReader, objectWriter, fieldWriter);
    }

    protected JSONPath.Segment compile(Class objectClass, JSONPath path, JSONPath.Segment segment, JSONPath.Segment parent) {
        if (segment instanceof JSONPath.NameSegment) {
            JSONPath.NameSegment nameSegment = (JSONPath.NameSegment) segment;
            String fieldName = nameSegment.name;

            JSONReader.Context readerContext = path.getReaderContext();
            JSONWriter.Context writerContext = path.getWriterContext();

            ObjectReader objectReader = null;
            FieldReader fieldReader = null;
            if (parent == null) {
                objectReader = readerContext.getObjectReader(objectClass);
            } else if (parent instanceof NameSegmentTyped) {
                NameSegmentTyped nameSegmentTyped = (NameSegmentTyped) parent;
                if (nameSegmentTyped.fieldReader != null) {
                    objectReader = readerContext.getObjectReader(nameSegmentTyped.fieldReader.fieldType);
                }
            }
            if (objectReader != null) {
                fieldReader = objectReader.getFieldReader(fieldName);
            }

            ObjectWriter objectWriter = null;
            FieldWriter fieldWriter = null;
            if (parent == null) {
                objectWriter = writerContext.getObjectWriter(objectClass);
            } else if (parent instanceof NameSegmentTyped) {
                NameSegmentTyped nameSegmentTyped = (NameSegmentTyped) parent;
                if (nameSegmentTyped.fieldWriter != null) {
                    objectWriter = writerContext.getObjectWriter(nameSegmentTyped.fieldWriter.fieldClass);
                }
            }

            if (objectWriter != null) {
                fieldWriter = objectWriter.getFieldWriter(fieldName);
            }

            return new NameSegmentTyped(objectClass, objectReader, fieldReader, objectWriter, fieldWriter, fieldName, nameSegment.nameHashCode);
        }

        return segment;
    }

    public static class TwoNameSegmentTypedPath
            extends JSONPath.TwoSegmentPath {
        final NameSegmentTyped first;
        final NameSegmentTyped second;
        public TwoNameSegmentTypedPath(String path, NameSegmentTyped first, NameSegmentTyped second) {
            super(path, first, second);
            this.first = first;
            this.second = second;
        }

        @Override
        public Object eval(Object root) {
            Object firstValue = first.fieldWriter.getFieldValue(root);
            if (firstValue == null) {
                return null;
            }

            return second.fieldWriter.getFieldValue(firstValue);
        }

        @Override
        public void set(Object root, Object value) {
            Object firstValue = first.fieldWriter.getFieldValue(root);
            if (firstValue == null) {
                return;
            }

            second.fieldReader.accept(firstValue, value);
        }

        @Override
        public void setInt(Object root, int value) {
            Object firstValue = first.fieldWriter.getFieldValue(root);
            if (firstValue == null) {
                return;
            }

            second.fieldReader.accept(firstValue, value);
        }

        @Override
        public void setLong(Object root, long value) {
            Object firstValue = first.fieldWriter.getFieldValue(root);
            if (firstValue == null) {
                return;
            }

            second.fieldReader.accept(firstValue, value);
        }

        @Override
        public void setCallback(Object root, BiFunction callback) {
            Object firstValue = first.fieldWriter.getFieldValue(root);
            if (firstValue == null) {
                return;
            }

            Object secondValue = second.fieldWriter.getFieldValue(firstValue);
            Object secondValueApply = callback.apply(firstValue, secondValue);
            if (secondValueApply == secondValue) {
                return;
            }
            if (second.fieldReader == null) {
                throw new UnsupportedOperationException();
            }
            second.fieldReader.accept(firstValue, secondValueApply);
        }
    }

    public static class NameSegmentTyped
            extends JSONPath.NameSegment {
        final Class objectClass;
        final FieldReader fieldReader;
        final FieldWriter fieldWriter;
        public NameSegmentTyped(
                Class objectClass,
                ObjectReader objectReader,
                FieldReader fieldReader,
                ObjectWriter objectWriter,
                FieldWriter fieldWriter,
                String name,
                long nameHashCode
        ) {
            super(name, nameHashCode);
            this.objectClass = objectClass;
            this.fieldReader = fieldReader;
            this.fieldWriter = fieldWriter;
        }

        @Override
        public void eval(JSONPath.Context context) {
            if (fieldWriter == null) {
                throw new UnsupportedOperationException();
            }

            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object == null) {
                return;
            }

            context.value = fieldWriter.getFieldValue(object);
        }
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
