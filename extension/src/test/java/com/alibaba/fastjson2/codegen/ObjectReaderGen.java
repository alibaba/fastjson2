package com.alibaba.fastjson2.codegen;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.modules.ObjectReaderAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.util.Fnv;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

public class ObjectReaderGen {
    final Class<?> objectClass;

    final String packageName;
    final String className;
    BeanInfo beanInfo;

    private final FieldReader[] fieldReaderArray;
    private final long[] hashCodes;
    private final short[] mapping;

    Map<Integer, List<Long>> map = new LinkedHashMap<>();
    Map<Long, Member> members = new HashMap<>();
    Map<Long, Type> fieldTypes = new HashMap<>();

    Appendable out;

    public ObjectReaderGen(Class<?> objectClass) {
        this(objectClass, System.out);
    }

    public ObjectReaderGen(Class<?> objectClass, Appendable out) {
        this.out = out;

        this.objectClass = objectClass;
        this.packageName = objectClass.getPackage().getName();
        this.className = objectClass.getSimpleName() + "_ObjectReader";

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        beanInfo = new BeanInfo();
        for (ObjectReaderModule module : provider.getModules()) {
            ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor != null) {
                annotationProcessor.getBeanInfo(beanInfo, objectClass);
            }
        }

        ObjectReaderCreator creator = provider.getCreator();

        fieldReaderArray = creator.createFieldReaders(objectClass);
        Arrays.sort(fieldReaderArray);

        long[] hashCodes = new long[fieldReaderArray.length];
        for (int i = 0; i < fieldReaderArray.length; i++) {
            FieldReader fieldReader = fieldReaderArray[i];

            String fieldName = fieldReader.fieldName;
            long hashCode64 = Fnv.hashCode64(fieldName);
            hashCodes[i] = hashCode64;
            fieldTypes.put(hashCode64, fieldReader.fieldType);

            Field field = fieldReader.field;
            Method method = fieldReader.method;
            if (field != null) {
                members.put(hashCode64, field);
            } else if (method != null) {
                members.put(hashCode64, method);
            } else {
                throw new JSONException("TODO");
            }
        }

        this.hashCodes = Arrays.copyOf(hashCodes, hashCodes.length);
        Arrays.sort(this.hashCodes);

        mapping = new short[this.hashCodes.length];
        for (int i = 0; i < hashCodes.length; i++) {
            long hashCode = hashCodes[i];
            int index = Arrays.binarySearch(this.hashCodes, hashCode);
            mapping[index] = (short) i;
        }

        for (int i = 0; i < this.hashCodes.length; i++) {
            long hashCode64 = this.hashCodes[i];
            int hashCode32 = (int) (hashCode64 ^ (hashCode64 >>> 32));
            List<Long> hashCode64List = this.map.get(hashCode32);
            if (hashCode64List == null) {
                hashCode64List = new ArrayList<>();
                this.map.put(hashCode32, hashCode64List);
            }
            hashCode64List.add(hashCode64);
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public void gen() {
        println("package " + packageName + ";");
        println();
        println("import java.util.Arrays;");
        println("import com.alibaba.fastjson2.JSONReader;");
        println("import com.alibaba.fastjson2.reader.FieldReader;");
        println("import com.alibaba.fastjson2.reader.ObjectReader;");
        println("import com.alibaba.fastjson2.reader.FieldReaderObject;");
        println("import com.alibaba.fastjson2.JSONException;");
        println("import " + objectClass.getName().replace('$', '.') + ";");

        println();
        println("public class " + className + " implements ObjectReader {");
        println();
        println("\tprivate FieldReader[] fieldReaders;");

        println();
        for (int i = 0; i < fieldReaderArray.length; i++) {
            FieldReader fieldReader = fieldReaderArray[i];
            println("\tprivate FieldReader fieldReader" + i + "; // " + fieldReader.fieldName);
        }
        for (int i = 0; i < fieldReaderArray.length; i++) {
            FieldReader fieldReader = fieldReaderArray[i];
            if (fieldReader instanceof FieldReaderObject) {
                println("\tprivate ObjectReader fieldObjectReader" + i + "; // " + fieldReader.fieldName);
            }
        }
        for (int i = 0; i < fieldReaderArray.length; i++) {
            FieldReader fieldReader = fieldReaderArray[i];
            if (fieldReader instanceof FieldReaderList) {
                println("\tprivate ObjectReader fieldListItemReader" + i + "; // " + fieldReader.fieldName + ".item");
            }
        }

        println();
        gen_constructor();

        println();
        gen_createInstance();

        println();
        gen_readJSONBObject();

        println();
        genReadObject();

        println();
        genReadArrayMappingObject();

        println();
        gen_getFieldReader();

        println("}");
    }

    private void gen_constructor() {
        println("\tpublic " + className + "(FieldReader[] fieldReaders) {");
        println("\t\tthis.fieldReaders = Arrays.copyOf(fieldReaders, fieldReaders.length);");
        println("\t\tArrays.sort(this.fieldReaders);");
        println();
        for (int i = 0; i < fieldReaderArray.length; i++) {
            println("\t\tthis.fieldReader" + i + " = this.fieldReaders[" + i + "];");
        }

        println("\t}");
    }

    private void gen_createInstance() {
        println("\t@Override");
        println("\tpublic Object createInstance() {");
        println("\t\treturn new " + objectClass.getSimpleName() + "();");
        println("\t}");
    }

    private void gen_readJSONBObject() {
        println("\t@Override");
        println("\tpublic Object readJSONBObject(JSONReader jsonReader, long features) {");

        println("\t\tjsonReader.nextIfObjectStart();");
        println("\t\t" + objectClass.getSimpleName() + " object = new " + objectClass.getSimpleName() + "();");
        println("\t\tfor (;;) {");
        println("\t\t\tif (jsonReader.nextIfObjectEnd()) {");
        println("\t\t\t\tbreak;");
        println("\t\t\t}");
        println("\t\t\tlong hashCode64 = jsonReader.readFieldNameHashCode();");
        gen_readObject_for_body(1, true);
        println("\t\t}");
        println("\t\treturn object;");

        println("\t}");
    }

    private void genReadArrayMappingObject() {
        println("\t@Override");
        println("\tpublic Object readArrayMappingObject(JSONReader jsonReader) {");
        println("\t\tjsonReader.next();");
        println("\t\t" + objectClass.getSimpleName() + " object = new " + objectClass.getSimpleName() + "();");

        for (int i = 0; i < fieldReaderArray.length; i++) {
            FieldReader fieldReader = fieldReaderArray[i];
            Type fieldType = fieldReader.fieldType;
            Class fieldClass = fieldReader.fieldClass;
            Member member = fieldReader.method;
            if (member == null) {
                member = fieldReader.field;
            }
            readFieldValue(0, false, (short) i, fieldReader, member, fieldType, fieldClass);
        }

        println("\t\tif (!jsonReader.nextIfMatch(']')) {");
        println("\t\t\tthrow new JSONException(\"array to bean end error, \" + jsonReader.current());");
        println("\t\t}");

        println();
        println("\t\tjsonReader.nextIfMatch(',');");

        println();
        println("\t\treturn object;");
        println("\t}");
    }

    private void genReadObject() {
        println("\t@Override");
        println("\tpublic Object readObject(JSONReader jsonReader, long features) {");
        println("\t\tif (jsonReader.isJSONB()) {");
        println("\t\t\treturn readJSONBObject(jsonReader, features);");
        println("\t\t}");
        println();

        if (beanInfo.readerFeatures != 0) {
            println("\t\tif (jsonReader.isArray() && jsonReader.isSupportBeanArray(" + beanInfo.readerFeatures + "L | " + "features)) {");
        } else {
            println("\t\tif (jsonReader.isArray() && jsonReader.isSupportBeanArray(features)) {");
        }

        println("\t\t\treturn readArrayMappingObject(jsonReader);");
        println("\t\t}");
        println();

        println("\t\tjsonReader.next();");
        println("\t\t" + objectClass.getSimpleName() + " object = new " + objectClass.getSimpleName() + "();");
        println("\t\tfor_:");
        println("\t\tfor (int i = 0;;++i) {");
        println("\t\t\tif (jsonReader.current() == '}') {");
        println("\t\t\t\tjsonReader.next();");
        println("\t\t\t\tbreak;");
        println("\t\t\t}");
        println();
        println("\t\t\tlong hashCode64 = jsonReader.readFieldNameHashCode();");
        println();
        println("\t\t\tif (hashCode64 == HASH_TYPE && i == 0) {");
        println("\t\t\t\tlong typeHash = jsonReader.readValueHashCode();");
        println("\t\t\t\tJSONReader.Context context = jsonReader.getContext();");
        println("\t\t\t\tObjectReader autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);");
        println("\t\t\t\tif (autoTypeObjectReader == null) {");
        println("\t\t\t\t\tString typeName = jsonReader.getString();");
        println("\t\t\t\t\tautoTypeObjectReader = context.getObjectReaderAutoType(typeName, " + className(objectClass) + ".class);");
        println();
        println("\t\t\t\t\tif (autoTypeObjectReader == null) {");
        println("\t\t\t\t\t\tthrow new JSONException(\"auotype not support : \" + typeName);");
        println("\t\t\t\t\t}");
        println("\t\t\t\t}");
        println();
        println("\t\t\t\tif (autoTypeObjectReader != this) {");
        println("\t\t\t\t\tobject = (" + className(objectClass) + ") autoTypeObjectReader.readObject(jsonReader, 0);");
        println("\t\t\t\t\tbreak;");
        println("\t\t\t\t} else {");
        println("\t\t\t\t\tcontinue;");
        println("\t\t\t\t}");
        println("\t\t\t}");

        gen_readObject_for_body(1, false);

        println("\t\t}");
        println();
        println("\t\treturn object;");
        println("\t}");
    }

    private void gen_readObject_for_body(int tabCnt, boolean jsonb) {
        boolean useSwitch = fieldReaderArray.length > 6;
        if (useSwitch) {
            println(tabCnt, "\t\tint hashCode32 = (int)(hashCode64 ^ (hashCode64 >>> 32));");
            println(tabCnt, "\t\tswitch(hashCode32) {");
            tabCnt += 2;
        }

        for (Map.Entry<Integer, List<Long>> entry : map.entrySet()) {
            int hashCode32 = entry.getKey();
            if (useSwitch) {
                println("\t\t\tcase " + hashCode32 + ":");
            }
            for (Long hashCode64 : entry.getValue()) {
                int m = Arrays.binarySearch(hashCodes, hashCode64);
                short index = mapping[m];

                FieldReader fieldReader = fieldReaderArray[index];
                String fieldName = fieldReader.fieldName;
                long fieldNameHashCode64 = Fnv.hashCode64(fieldName);
                if (fieldNameHashCode64 != hashCode64) {
                    throw new IllegalStateException();
                }
                if (hashCode32 != (int) (fieldNameHashCode64 ^ (fieldNameHashCode64 >>> 32))) {
                    throw new IllegalStateException();
                }

                println(tabCnt, "\t\tif (hashCode64 == " + hashCode64 + "L) {" + " // " + fieldName);

                Member member = members.get(fieldNameHashCode64);
                Type fieldType = fieldTypes.get(fieldNameHashCode64);
                Class fieldClass;
                if (member instanceof Field) {
                    fieldClass = ((Field) member).getType();
                } else if (member instanceof Method) {
                    fieldClass = ((Method) member).getParameterTypes()[0];
                } else {
                    throw new JSONException("TODO");
                }

                readFieldValue(tabCnt, jsonb, index, fieldReader, member, fieldType, fieldClass);

                println(tabCnt, "\t\t\tcontinue;");
                println(tabCnt, "\t\t}");
            }
            if (useSwitch) {
                println(tabCnt, "\t\tbreak;");
            }
        }
        if (useSwitch) {
            println(tabCnt, "\tdefault:");
            println(tabCnt, "\t\tbreak;");
            println(tabCnt, "}");
        }

        println(tabCnt, "\t\tString fieldName = jsonReader.getFieldName();");
        println(tabCnt, "\t\tthrow new JSONException(\"fieldReader not found, fieldName \" + fieldName);");
    }

    private void readFieldValue(int tabCnt, boolean jsonb, short index, FieldReader fieldReader, Member member, Type fieldType, Class fieldClass) {
        if (fieldType == int.class) {
            if (member instanceof Method) {
                println(tabCnt, "\t\t\tobject." + member.getName() + "(jsonReader.readInt32Value());");
            } else if (member instanceof Field) {
                println(tabCnt, "\t\t\tobject." + member.getName() + " = jsonReader.readInt32Value();");
            } else {
                println(tabCnt, "\t\t\tthis.fieldReader" + index + ".accept(object, jsonReader.readInt32Value());");
            }
        } else if (fieldType == long.class) {
            if (member instanceof Method) {
                println(tabCnt, "\t\t\tobject." + member.getName() + "(jsonReader.readInt64Value());");
            } else if (member instanceof Field) {
                println(tabCnt, "\t\t\tobject." + member.getName() + " = jsonReader.readInt64Value();");
            } else {
                println(tabCnt, "\t\t\tthis.fieldReader" + index + ".accept(object, jsonReader.readInt64Value());");
            }
        } else if (fieldType == String.class) {
            if (member instanceof Method) {
                println(tabCnt, "\t\t\tobject." + member.getName() + "(jsonReader.readString());");
            } else if (member instanceof Field) {
                println(tabCnt, "\t\t\tobject." + member.getName() + " = jsonReader.readString();");
            } else {
                println(tabCnt, "\t\t\tthis.fieldReader" + index + ".accept(object, jsonReader.readString());");
            }
        } else if (fieldType == List.class) {
            println(tabCnt, "\t\t\tthis.fieldReader" + index + ".readFieldValue(jsonReader, object);");
        } else if (fieldType instanceof Class && ((Class<?>) fieldType).isEnum()) {
            if (jsonb) {
                println(tabCnt, "\t\t\tlong hash = jsonReader.readValueHashCode();");
                String fieldClassName = ((Class<?>) fieldType).getName().replace('$', '.');
                println(tabCnt, "\t\t\t" + fieldClassName + " fieldValue");
                println(tabCnt, "\t\t\t\t= (" + fieldClassName + ") this.fieldReader" + index + ".getEnumByHashCode(hash);");
                if (member instanceof Method) {
                    println(tabCnt, "\t\t\tobject." + member.getName() + "(fieldValue);");
                } else {
                    println(tabCnt, "\t\t\tthis.fieldReader" + index + ".accept(object, fieldValue);");
                }
            } else {
                println(tabCnt, "\t\t\tchar ch = jsonReader.current();");
                println(tabCnt, "\t\t\tif (ch == '\"') {");
                println(tabCnt, "\t\t\t\tlong hash = jsonReader.readValueHashCode();");
                String fieldClassName = ((Class<?>) fieldType).getName().replace('$', '.');
                println(tabCnt, "\t\t\t\t" + fieldClassName + " fieldValue");
                println(tabCnt, "\t\t\t\t\t= (" + fieldClassName + ") this.fieldReader" + index + ".getEnumByHashCode(hash);");
                if (member instanceof Method) {
                    println(tabCnt, "\t\t\t\tobject." + member.getName() + "(fieldValue);");
                } else {
                    println(tabCnt, "\t\t\t\tthis.fieldReader" + index + ".accept(object, fieldValue);");
                }
                println(tabCnt, "\t\t\t} else {");
                println(tabCnt, "\t\t\t\tthrow new JSONException(\"TODO\");");
                println(tabCnt, "\t\t\t}");
            }
        } else if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            Type rawType = parameterizedType.getRawType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length == 1 && actualTypeArguments[0] instanceof Class) {
                if (rawType == List.class || rawType == ArrayList.class) {
                    if (actualTypeArguments[0] == String.class) {
                        if (jsonb) {
                            println(tabCnt, "\t\t\tint listItemCnt = jsonReader.startArray();");
                            println(tabCnt, "\t\t\tjava.util.List list = new java.util.ArrayList(listItemCnt);");
                            println(tabCnt, "\t\t\tfor (int j = 0; j < listItemCnt; ++j) {");
                            println(tabCnt, "\t\t\t\t\tlist.add(");
                            println(tabCnt, "\t\t\t\t\t\tjsonReader.readString());");
                            println();
                            println(tabCnt, "\t\t\t}");

                            if (member instanceof Method) {
                                println(tabCnt, "\t\t\tobject." + member.getName() + "(list);");
                            } else {
                                println(tabCnt, "\t\t\tthis.fieldReader" + index + ".accept(object, list);");
                            }
                        } else {
                            println(tabCnt, "\t\t\tif (jsonReader.current() == '[') {");
                            println(tabCnt, "\t\t\t\tjava.util.List list = new java.util.ArrayList();");
                            println(tabCnt, "\t\t\t\tjsonReader.next();");
                            println(tabCnt, "\t\t\t\tfor (;;) {");
                            println(tabCnt, "\t\t\t\t\tif (jsonReader.current() == ']') {");
                            println(tabCnt, "\t\t\t\t\t\tjsonReader.next();");
                            println(tabCnt, "\t\t\t\t\t\tbreak;");
                            println(tabCnt, "\t\t\t\t\t}");
                            println();
                            println(tabCnt, "\t\t\t\t\tlist.add(jsonReader.readString());");
                            println();
                            println(tabCnt, "\t\t\t\t\tif (jsonReader.current() == ',') {");
                            println(tabCnt, "\t\t\t\t\t\tjsonReader.next();");
                            println(tabCnt, "\t\t\t\t\t\tcontinue;");
                            println(tabCnt, "\t\t\t\t\t}");
                            println(tabCnt, "\t\t\t\t}");
                            if (member instanceof Method) {
                                println(tabCnt, "\t\t\tobject." + member.getName() + "(list);");
                            } else {
                                println(tabCnt, "\t\t\tthis.fieldReader" + index + ".accept(object, list);");
                            }
                            println(tabCnt, "\t\t\t\tif (jsonReader.current() == ',') {");
                            println(tabCnt, "\t\t\t\t\tjsonReader.next();");
                            println(tabCnt, "\t\t\t\t}");
                            println(tabCnt, "\t\t\t}");
                        }
                    } else {
                        if (jsonb) {
                            println(tabCnt, "\t\t\tint listItemCnt = jsonReader.startArray();");
                            println(tabCnt, "\t\t\tjava.util.List list = new java.util.ArrayList(listItemCnt);");
                            println(tabCnt, "\t\t\tfor (int j = 0; j < listItemCnt; ++j) {");
                            println(tabCnt, "\t\t\t\tif (fieldListItemReader" + index + " == null) {");
                            println(tabCnt, "\t\t\t\t\tfieldListItemReader" + index + " = jsonReader.getContext()");
                            println(tabCnt, "\t\t\t\t\t\t.getObjectReader(" + className((Class) actualTypeArguments[0]) + ".class);");
                            println(tabCnt, "\t\t\t\t}");
                            println(tabCnt, "\t\t\t\t\tlist.add(");
                            println(tabCnt, "\t\t\t\t\t\tfieldListItemReader" + index + ".readJSONBObject(jsonReader, " + fieldReader.features + "));");
                            println();
                            println(tabCnt, "\t\t\t}");

                            if (member instanceof Method) {
                                println(tabCnt, "\t\t\tobject." + member.getName() + "(list);");
                            } else {
                                println(tabCnt, "\t\t\tthis.fieldReader" + index + ".accept(object, list);");
                            }
                        } else {
                            println(tabCnt, "\t\t\tif (jsonReader.current() == '[') {");
                            println(tabCnt, "\t\t\t\tif (fieldListItemReader" + index + " == null) {");
                            println(tabCnt, "\t\t\t\t\tfieldListItemReader" + index + " = jsonReader.getContext()");
                            println(tabCnt, "\t\t\t\t\t\t.getObjectReader(" + className((Class) actualTypeArguments[0]) + ".class);");
                            println(tabCnt, "\t\t\t\t}");
                            println();
                            println(tabCnt, "\t\t\t\tjava.util.List list = new java.util.ArrayList();");
                            println(tabCnt, "\t\t\t\tjsonReader.next();");
                            println(tabCnt, "\t\t\t\tfor (;;) {");
                            println(tabCnt, "\t\t\t\t\tif (jsonReader.current() == ']') {");
                            println(tabCnt, "\t\t\t\t\t\tjsonReader.next();");
                            println(tabCnt, "\t\t\t\t\t\tbreak;");
                            println(tabCnt, "\t\t\t\t\t\t}");
                            println();
                            println(tabCnt, "\t\t\t\t\t\tlist.add(");
                            println(tabCnt, "\t\t\t\t\t\t\tfieldListItemReader" + index + ".readObject(jsonReader, " + fieldReader.features + "));");
                            println();
                            println(tabCnt, "\t\t\t\t\t\tif (jsonReader.current() == ',') {");
                            println(tabCnt, "\t\t\t\t\t\t\tjsonReader.next();");
                            println(tabCnt, "\t\t\t\t\t\t\tcontinue;");
                            println(tabCnt, "\t\t\t\t\t\t}");
                            println(tabCnt, "\t\t\t\t\t}");

                            if (member instanceof Method) {
                                println(tabCnt, "\t\t\t\tobject." + member.getName() + "(list);");
                            } else {
                                println(tabCnt, "\t\t\t\tthis.fieldReader" + index + ".accept(object, list);");
                            }

                            println(tabCnt, "\t\t\t\tif (jsonReader.current() == ',') {");
                            println(tabCnt, "\t\t\t\t\tjsonReader.next();");
                            println(tabCnt, "\t\t\t\t}");
                            println(tabCnt, "\t\t\t}");
                        }
                    }
                } else {
                    println(tabCnt, "\t\t\tthis.fieldReader" + index + ".readFieldValue(jsonReader, object);");
                }
            } else {
                println(tabCnt, "\t\t\tthis.fieldReader" + index + ".readFieldValue(jsonReader, object);");
            }
        } else if (fieldReader instanceof FieldReaderObject) {
            println(tabCnt, "\t\t\tif (this.fieldObjectReader" + index + " == null) {");
            println(tabCnt, "\t\t\t\tthis.fieldObjectReader" + index + " = jsonReader.getContext()");
            println(tabCnt, "\t\t\t\t\t.getObjectReader(" + className(fieldClass) + ".class); // " + fieldReader.fieldName);
            println(tabCnt, "\t\t\t}");
            // Media fieldValue = (Media) fieldObjectReader0.readObject(jsonReader);
            if (member instanceof Method) {
                println(tabCnt, "\t\t\tobject." + member.getName() + "((" + className(fieldClass) + ")");
                if (jsonb) {
                    println(tabCnt, "\t\t\t\t\tfieldObjectReader" + index + ".readJSONBObject(jsonReader, " + fieldReader.features + "));");
                } else {
                    println(tabCnt, "\t\t\t\t\tfieldObjectReader" + index + ".readObject(jsonReader, " + fieldReader.features + "));");
                }
            } else {
                println(tabCnt, "\t\t\tthis.fieldReader" + index + ".accept(object, fieldObjectReader\" + index + \".readObject(jsonReader, " + fieldReader.features + "));");
            }
        } else {
            println(tabCnt, "\t\t\tthis.fieldReader" + index + ".readFieldValue(jsonReader, object);");
        }
    }

    static String className(Class clazz) {
        return clazz.getName().replace('$', '.');
    }

    private void gen_getFieldReader() {
        println("\t@Override");
        println("\tpublic FieldReader getFieldReader(long hashCode64) {");

        boolean useSwitch = fieldReaderArray.length > 6;
        int tabCnt = 0;
        if (useSwitch) {
            println("\t\tint hashCode32 = (int)(hashCode64 ^ (hashCode64 >>> 32));");
            println("\t\tswitch(hashCode32) {");
            tabCnt = 2;
        }

        for (Map.Entry<Integer, List<Long>> entry : map.entrySet()) {
            int hashCode32 = entry.getKey();
            if (useSwitch) {
                println(tabCnt, "\tcase " + hashCode32 + ":");
            }
            for (Long hashCode64 : entry.getValue()) {
                int m = Arrays.binarySearch(hashCodes, hashCode64);
                short index = mapping[m];

                FieldReader fieldReader = fieldReaderArray[index];
                String fieldName = fieldReader.fieldName;
                long fieldNameHashCode64 = Fnv.hashCode64(fieldName);
                if (fieldNameHashCode64 != hashCode64) {
                    throw new IllegalStateException();
                }
                if (hashCode32 != (int) (fieldNameHashCode64 ^ (fieldNameHashCode64 >>> 32))) {
                    throw new IllegalStateException();
                }

                println(tabCnt, "\t\tif (hashCode64 == " + hashCode64 + "L) {" + " // " + fieldName);
                println(tabCnt, "\t\t\treturn this.fieldReader" + index + ";");
                println(tabCnt, "\t\t}");
            }
            if (useSwitch) {
                println(tabCnt, "\t\tbreak;");
            }
        }

        if (useSwitch) {
            println(tabCnt, "\tdefault:");
            println(tabCnt, "\t\tbreak;");
            println(tabCnt, "}");
        }

        println("\t\treturn null;");
        println("\t}");
    }

    void println() {
        try {
            out.append('\n');
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    void println(String context) {
        try {
            out.append(context);
            out.append('\n');
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    void println(int tabCnt, String context) {
        try {
            for (int i = 0; i < tabCnt; ++i) {
                out.append('\t');
            }
            out.append(context);
            out.append('\n');
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
