package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONB.Constants.BC_TYPED_ANY;

public interface ApacheLang3Support {
    interface TripleMixIn<L, M, R> {
        @JSONCreator
        static <L, M, R> Object of(L left, M middle, R right) {
            return null;
        }
    }

    class PairReader
            implements ObjectReader {
        static final long LEFT = Fnv.hashCode64("left");
        static final long RIGHT = Fnv.hashCode64("right");

        static final long PAIR = Fnv.hashCode64("org.apache.commons.lang3.tuple.Pair");
        static final long MUTABLE_PAIR = Fnv.hashCode64("org.apache.commons.lang3.tuple.MutablePair");
        static final long IMMUTABLE_PAIR = Fnv.hashCode64("org.apache.commons.lang3.tuple.ImmutablePair");

        final Class objectClass;
        final Type leftType;
        final Type rightType;

        final Method of;

        public PairReader(Class objectClass, Type leftType, Type rightType) {
            this.objectClass = objectClass;
            this.leftType = leftType;
            this.rightType = rightType;

            try {
                of = objectClass.getMethod("of", Object.class, Object.class);
            } catch (NoSuchMethodException e) {
                throw new JSONException("Pair.of method not found", e);
            }
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            if (jsonReader.nextIfNull()) {
                return null;
            }

            if (jsonReader.nextIfMatch(BC_TYPED_ANY)) {
                long typeHash = jsonReader.readTypeHashCode();
                if (typeHash != PAIR && typeHash != IMMUTABLE_PAIR && typeHash != MUTABLE_PAIR) {
                    throw new JSONException("not support inputType : " + jsonReader.getString());
                }
            }

            Object left = null, right = null;
            if (jsonReader.nextIfObjectStart()) {
                for (int i = 0; i < 100; i++) {
                    if (jsonReader.nextIfObjectEnd()) {
                        break;
                    }
                    if (jsonReader.isString()) {
                        long hashCode = jsonReader.readFieldNameHashCode();
                        if (hashCode == LEFT) {
                            left = jsonReader.read(leftType);
                        } else if (hashCode == RIGHT) {
                            right = jsonReader.read(rightType);
                        } else if (i == 0) {
                            left = jsonReader.getFieldName();
                            right = jsonReader.read(rightType);
                        } else {
                            jsonReader.skipValue();
                        }
                    } else if (i == 0) {
                        left = jsonReader.read(leftType);
                        right = jsonReader.read(rightType);
                    } else {
                        throw new JSONException(jsonReader.info("not support input"));
                    }
                }
            } else if (jsonReader.isArray()) {
                int len = jsonReader.startArray();
                if (len != 2) {
                    throw new JSONException(jsonReader.info("not support input"));
                }
                left = jsonReader.read(leftType);
                right = jsonReader.read(rightType);
            } else {
                throw new JSONException(jsonReader.info("not support input"));
            }

            try {
                return of.invoke(null, left, right);
            } catch (Exception e) {
                throw new JSONException("create pair error", e);
            }
        }

        @Override
        public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            if (jsonReader.nextIfNull()) {
                return null;
            }

            Object left = null, right = null;
            if (jsonReader.nextIfObjectStart()) {
                for (int i = 0; i < 100; i++) {
                    if (jsonReader.nextIfObjectEnd()) {
                        break;
                    }
                    if (jsonReader.isString()) {
                        long hashCode = jsonReader.readFieldNameHashCode();
                        if (hashCode == LEFT) {
                            left = jsonReader.read(leftType);
                        } else if (hashCode == RIGHT) {
                            right = jsonReader.read(rightType);
                        } else if (i == 0) {
                            left = jsonReader.getFieldName();
                            jsonReader.nextIfMatch(':');
                            right = jsonReader.read(rightType);
                        } else {
                            jsonReader.skipValue();
                        }
                    } else if (i == 0) {
                        left = jsonReader.read(leftType);
                        jsonReader.nextIfMatch(':');
                        right = jsonReader.read(rightType);
                    } else {
                        throw new JSONException(jsonReader.info("not support input"));
                    }
                }
            } else if (jsonReader.nextIfMatch('[')) {
                left = jsonReader.read(leftType);
                right = jsonReader.read(rightType);
                if (!jsonReader.nextIfMatch(']')) {
                    throw new JSONException(jsonReader.info("not support input"));
                }
            } else {
                throw new JSONException(jsonReader.info("not support input"));
            }

            try {
                return of.invoke(null, left, right);
            } catch (Exception e) {
                throw new JSONException("create pair error", e);
            }
        }
    }

    class PairWriter
            implements ObjectWriter {
        final Class objectClass;
        final String typeName;
        final long typeNameHash;
        Method leftMethod;
        Method rightMethod;

        byte[] nameWithColonUTF8;
        char[] nameWithColonUTF16;
        byte[] typeNameJSONB;

        static byte[] leftName = JSONB.toBytes("left");
        static byte[] rightName = JSONB.toBytes("right");

        public PairWriter(Class objectClass) {
            this.objectClass = objectClass;
            this.typeName = objectClass.getName();
            this.typeNameHash = Fnv.hashCode64(typeName);
        }

        @Override
        public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
                return;
            }

            if ((jsonWriter.getFeatures(features) & JSONWriter.Feature.WriteClassName.mask) != 0) {
                if (typeNameJSONB == null) {
                    typeNameJSONB = JSONB.toBytes(typeName);
                }
                jsonWriter.writeTypeName(typeNameJSONB, typeNameHash);
            }

            jsonWriter.startObject();

            Object left = getLeft(object);
            Object right = getRight(object);

            jsonWriter.writeNameRaw(leftName, PairReader.LEFT);
            jsonWriter.writeAny(left);

            jsonWriter.writeNameRaw(rightName, PairReader.RIGHT);
            jsonWriter.writeAny(right);
            jsonWriter.endObject();
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
                return;
            }

            Object left = getLeft(object);
            Object right = getRight(object);

            jsonWriter.startObject();
            if ((jsonWriter.getFeatures(features) & JSONWriter.Feature.WritePairAsJavaBean.mask) != 0) {
                jsonWriter.writeName("left");
                jsonWriter.writeColon();
                jsonWriter.writeAny(left);

                jsonWriter.writeName("right");
                jsonWriter.writeColon();
                jsonWriter.writeAny(right);
            } else {
                jsonWriter.writeNameAny(left);
                jsonWriter.writeColon();
                jsonWriter.writeAny(right);
            }

            jsonWriter.endObject();
        }

        Object getLeft(Object object) {
            Class<?> objectClass = object.getClass();
            if (leftMethod == null) {
                try {
                    leftMethod = objectClass.getMethod("getLeft");
                } catch (NoSuchMethodException e) {
                    throw new JSONException("getLeft method not found", e);
                }
            }

            try {
                return leftMethod.invoke(object);
            } catch (Exception e) {
                throw new JSONException("invoke getLeft method error", e);
            }
        }

        Object getRight(Object object) {
            Class<?> objectClass = object.getClass();
            if (rightMethod == null) {
                try {
                    rightMethod = objectClass.getMethod("getRight");
                } catch (NoSuchMethodException e) {
                    throw new JSONException("getRight method not found", e);
                }
            }

            try {
                return rightMethod.invoke(object);
            } catch (Exception e) {
                throw new JSONException("invoke getRight method error", e);
            }
        }
    }
}
