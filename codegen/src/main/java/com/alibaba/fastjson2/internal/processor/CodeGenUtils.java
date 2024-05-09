package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.*;

import java.util.*;

public class CodeGenUtils {
    public static final int JVM_VERSION;

    static {
        int jvmVersion = -1;
        try {
            String javaSpecVer = System.getProperty("java.specification.version");
            if (javaSpecVer.startsWith("1.")) {
                javaSpecVer = javaSpecVer.substring(2);
            }
            if (javaSpecVer.indexOf('.') == -1) {
                jvmVersion = Integer.parseInt(javaSpecVer);
            }
        } catch (Throwable ignored) {
            // ignored
        }
        JVM_VERSION = jvmVersion;
    }

    static Map<String, String> readDirectMap = new HashMap<>();
    static Map<String, String> writeDirectMap = new HashMap<>();
    static {
        readDirectMap.put("boolean", "readBoolValue");
        readDirectMap.put("char", "readCharValue");
        writeDirectMap.put("boolean", "writeBool");
        writeDirectMap.put("char", "writeChar");

        readDirectMap.put("byte", "readInt8Value");
        readDirectMap.put("short", "readInt16Value");
        readDirectMap.put("int", "readInt32Value");
        readDirectMap.put("long", "readInt64Value");
        readDirectMap.put("float", "readFloatValue");
        readDirectMap.put("double", "readDoubleValue");
        writeDirectMap.put("byte", "writeInt8");
        writeDirectMap.put("short", "writeInt16");
        writeDirectMap.put("int", "writeInt32");
        writeDirectMap.put("long", "writeInt64");
        writeDirectMap.put("float", "writeFloat");
        writeDirectMap.put("double", "writeDouble");

        readDirectMap.put("java.lang.Boolean", "readBool");
        readDirectMap.put("java.lang.Character", "readCharacter");
        writeDirectMap.put("java.lang.Boolean", "writeBool");
        writeDirectMap.put("java.lang.Character", "writeCharacter");

        readDirectMap.put("java.lang.Byte", "readInt8");
        readDirectMap.put("java.lang.Short", "readInt16");
        readDirectMap.put("java.lang.Integer", "readInt32");
        readDirectMap.put("java.lang.Long", "readInt64");
        readDirectMap.put("java.lang.Float", "readFloat");
        readDirectMap.put("java.lang.Double", "readDouble");
        readDirectMap.put("java.lang.Number", "readNumber");
        writeDirectMap.put("java.lang.Byte", "writeInt8");
        writeDirectMap.put("java.lang.Short", "writeInt16");
        writeDirectMap.put("java.lang.Integer", "writeInt32");
        writeDirectMap.put("java.lang.Long", "writeInt64");
        writeDirectMap.put("java.lang.Float", "writeFloat");
        writeDirectMap.put("java.lang.Double", "writeDouble");
        writeDirectMap.put("java.lang.Number", "writeNumber");

        readDirectMap.put("java.lang.String", "readString");
        writeDirectMap.put("java.lang.String", "writeString");

        readDirectMap.put("java.math.BigInteger", "readBigInteger");
        readDirectMap.put("java.math.BigDecimal", "readBigDecimal");
        writeDirectMap.put("java.math.BigInteger", "writeBigInteger");
        writeDirectMap.put("java.math.BigDecimal", "writeBigDecimal");

        readDirectMap.put("java.util.UUID", "readUUID");
        readDirectMap.put("java.util.Date", "readDate");
        readDirectMap.put("java.util.Calendar", "readCalendar");
        writeDirectMap.put("java.util.UUID", "writeUUID");
        writeDirectMap.put("java.util.Date", "writeDate");
        writeDirectMap.put("java.util.Calendar", "writeCalendar");

        readDirectMap.put("java.time.LocalDate", "readLocalDate");
        readDirectMap.put("java.time.LocalTime", "readLocalTime");
        readDirectMap.put("java.time.LocalDateTime", "readLocalDateTime");
        readDirectMap.put("java.time.ZonedDateTime", "readZonedDateTime");
        readDirectMap.put("java.time.OffsetDateTime", "readOffsetDateTime");
        readDirectMap.put("java.time.OffsetTime", "readOffsetTime");
        writeDirectMap.put("java.time.LocalDate", "writeLocalDate");
        writeDirectMap.put("java.time.LocalTime", "writeLocalTime");
        writeDirectMap.put("java.time.LocalDateTime", "writeLocalDateTime");
        writeDirectMap.put("java.time.ZonedDateTime", "writeZonedDateTime");
        writeDirectMap.put("java.time.OffsetDateTime", "writeOffsetDateTime");
        writeDirectMap.put("java.time.OffsetTime", "writeOffsetTime");

        readDirectMap.put("int[]", "readInt32ValueArray");
        readDirectMap.put("long[]", "readInt64ValueArray");
        readDirectMap.put("java.lang.String[]", "readStringArray");
        writeDirectMap.put("int[]", "writeInt32ValueArray");
        writeDirectMap.put("long[]", "writeInt64ValueArray");
        writeDirectMap.put("java.lang.String[]", "writeStringArray");

        readDirectMap.put("com.alibaba.fastjson2.JSONObject", "readJSONObject");
        readDirectMap.put("com.alibaba.fastjson2.JSONArray", "readJSONArray");
        writeDirectMap.put("com.alibaba.fastjson2.JSONObject", "writeJSONObject");
        writeDirectMap.put("com.alibaba.fastjson2.JSONArray", "writeJSONArray");
    }

    public static Class getReadSuperClass(int fieldReaders) {
        Class objectReaderSuper;
        switch (fieldReaders) {
            case 1:
                objectReaderSuper = ObjectReader1.class;
                break;
            case 2:
                objectReaderSuper = ObjectReader2.class;
                break;
            case 3:
                objectReaderSuper = ObjectReader3.class;
                break;
            case 4:
                objectReaderSuper = ObjectReader4.class;
                break;
            case 5:
                objectReaderSuper = ObjectReader5.class;
                break;
            case 6:
                objectReaderSuper = ObjectReader6.class;
                break;
            case 7:
                objectReaderSuper = ObjectReader7.class;
                break;
            case 8:
                objectReaderSuper = ObjectReader8.class;
                break;
            case 9:
                objectReaderSuper = ObjectReader9.class;
                break;
            case 10:
                objectReaderSuper = ObjectReader10.class;
                break;
            case 11:
                objectReaderSuper = ObjectReader11.class;
                break;
            case 12:
                objectReaderSuper = ObjectReader12.class;
                break;
            default:
                objectReaderSuper = ObjectReaderAdapter.class;
                break;
        }
        return objectReaderSuper;
    }

    public static Class getWriteSuperClass(int fieldWriters) {
        Class objectWriterSuper;
        switch (fieldWriters) {
            case 1:
                objectWriterSuper = ObjectWriter1.class;
                break;
            case 2:
                objectWriterSuper = ObjectWriter2.class;
                break;
            case 3:
                objectWriterSuper = ObjectWriter3.class;
                break;
            case 4:
                objectWriterSuper = ObjectWriter4.class;
                break;
            case 5:
                objectWriterSuper = ObjectWriter5.class;
                break;
            case 6:
                objectWriterSuper = ObjectWriter6.class;
                break;
            case 7:
                objectWriterSuper = ObjectWriter7.class;
                break;
            case 8:
                objectWriterSuper = ObjectWriter8.class;
                break;
            case 9:
                objectWriterSuper = ObjectWriter9.class;
                break;
            case 10:
                objectWriterSuper = ObjectWriter10.class;
                break;
            case 11:
                objectWriterSuper = ObjectWriter11.class;
                break;
            case 12:
                objectWriterSuper = ObjectWriter12.class;
                break;
            default:
                objectWriterSuper = ObjectWriterAdapter.class;
                break;
        }
        return objectWriterSuper;
    }

    public static String fieldReader(int i) {
        switch (i) {
            case 0:
                return "fieldReader0";
            case 1:
                return "fieldReader1";
            case 2:
                return "fieldReader2";
            case 3:
                return "fieldReader3";
            case 4:
                return "fieldReader4";
            case 5:
                return "fieldReader5";
            case 6:
                return "fieldReader6";
            case 7:
                return "fieldReader7";
            case 8:
                return "fieldReader8";
            case 9:
                return "fieldReader9";
            case 10:
                return "fieldReader10";
            case 11:
                return "fieldReader11";
            case 12:
                return "fieldReader12";
            case 13:
                return "fieldReader13";
            case 14:
                return "fieldReader14";
            case 15:
                return "fieldReader15";
            default:
                return getName("fieldReader", i);
        }
    }

    public static String fieldWriter(int i) {
        switch (i) {
            case 0:
                return "fieldWriter0";
            case 1:
                return "fieldWriter1";
            case 2:
                return "fieldWriter2";
            case 3:
                return "fieldWriter3";
            case 4:
                return "fieldWriter4";
            case 5:
                return "fieldWriter5";
            case 6:
                return "fieldWriter6";
            case 7:
                return "fieldWriter7";
            case 8:
                return "fieldWriter8";
            case 9:
                return "fieldWriter9";
            case 10:
                return "fieldWriter10";
            case 11:
                return "fieldWriter11";
            case 12:
                return "fieldWriter12";
            case 13:
                return "fieldWriter13";
            case 14:
                return "fieldWriter14";
            case 15:
                return "fieldWriter15";
            default:
                return getName("fieldWriter", i);
        }
    }

    public static String fieldObjectReader(int i) {
        switch (i) {
            case 0:
                return "objectReader0";
            case 1:
                return "objectReader1";
            case 2:
                return "objectReader2";
            case 3:
                return "objectReader3";
            case 4:
                return "objectReader4";
            case 5:
                return "objectReader5";
            case 6:
                return "objectReader6";
            case 7:
                return "objectReader7";
            case 8:
                return "objectReader8";
            case 9:
                return "objectReader9";
            case 10:
                return "objectReader10";
            case 11:
                return "objectReader11";
            case 12:
                return "objectReader12";
            case 13:
                return "objectReader13";
            case 14:
                return "objectReader14";
            case 15:
                return "objectReader15";
            default:
                return getName("objectReader", i);
        }
    }

    public static String fieldObjectWriter(int i) {
        switch (i) {
            case 0:
                return "objectWriter0";
            case 1:
                return "objectWriter1";
            case 2:
                return "objectWriter2";
            case 3:
                return "objectWriter3";
            case 4:
                return "objectWriter4";
            case 5:
                return "objectWriter5";
            case 6:
                return "objectWriter6";
            case 7:
                return "objectWriter7";
            case 8:
                return "objectWriter8";
            case 9:
                return "objectWriter9";
            case 10:
                return "objectWriter10";
            case 11:
                return "objectWriter11";
            case 12:
                return "objectWriter12";
            case 13:
                return "objectWriter13";
            case 14:
                return "objectWriter14";
            case 15:
                return "objectWriter15";
            default:
                return getName("objectWriter", i);
        }
    }

    public static String fieldItemObjectReader(int i) {
        switch (i) {
            case 0:
                return "itemReader0";
            case 1:
                return "itemReader1";
            case 2:
                return "itemReader2";
            case 3:
                return "itemReader3";
            case 4:
                return "itemReader4";
            case 5:
                return "itemReader5";
            case 6:
                return "itemReader6";
            case 7:
                return "itemReader7";
            case 8:
                return "itemReader8";
            case 9:
                return "itemReader9";
            case 10:
                return "itemReader10";
            case 11:
                return "itemReader11";
            case 12:
                return "itemReader12";
            case 13:
                return "itemReader13";
            case 14:
                return "itemReader14";
            case 15:
                return "itemReader15";
            default:
                return getName("itemReader", i);
        }
    }

    public static String fieldItemObjectWriter(int i) {
        switch (i) {
            case 0:
                return "itemWriter0";
            case 1:
                return "itemWriter1";
            case 2:
                return "itemWriter2";
            case 3:
                return "itemWriter3";
            case 4:
                return "itemWriter4";
            case 5:
                return "itemWriter5";
            case 6:
                return "itemWriter6";
            case 7:
                return "itemWriter7";
            case 8:
                return "itemWriter8";
            case 9:
                return "itemWriter9";
            case 10:
                return "itemWriter10";
            case 11:
                return "itemWriter11";
            case 12:
                return "itemWriter12";
            case 13:
                return "itemWriter13";
            case 14:
                return "itemWriter14";
            case 15:
                return "itemWriter15";
            default:
                return getName("itemWriter", i);
        }
    }

    static String getName(String base, int i) {
        final int baseSize = base.length();
        int size = IOUtils.stringSize(i);
        byte[] chars = new byte[baseSize + size];
        base.getBytes(0, baseSize, chars, 0);
        IOUtils.writeInt32(chars, baseSize, i);
        return new String(chars);
    }

    static boolean isReference(String typeName) {
        switch (typeName) {
            case "byte":
            case "short":
            case "int":
            case "long":
            case "float":
            case "double":
            case "boolean":
            case "char":
            case "byte[]":
            case "short[]":
            case "int[]":
            case "long[]":
            case "float[]":
            case "double[]":
            case "boolean[]":
            case "char[]":
            case "java.sql.Date":
            case "java.sql.Time":
            case "java.sql.Timestamp":
                return false;
            default:
                break;
        }

        if (typeName.startsWith("java.")) {
            Class type = TypeUtils.loadClass(typeName);
            if (type != null) {
                return !ObjectWriterProvider.isPrimitiveOrEnum(type);
            }
        }
        return true;
    }

    public static void getFieldInfo(FieldInfo fieldInfo, JSONField jsonField, boolean serialize) {
        if (jsonField == null) {
            return;
        }

        String jsonFieldName = jsonField.name();
        if (!jsonFieldName.isEmpty()) {
            fieldInfo.fieldName = jsonFieldName;
        }

        String jsonFieldFormat = jsonField.format();
        if (!jsonFieldFormat.isEmpty()) {
            jsonFieldFormat = jsonFieldFormat.trim();
            if (jsonFieldFormat.indexOf('T') != -1 && !jsonFieldFormat.contains("'T'")) {
                jsonFieldFormat = jsonFieldFormat.replaceAll("T", "'T'");
            }

            fieldInfo.format = jsonFieldFormat;
        }

        String label = jsonField.label();
        if (!label.isEmpty()) {
            label = label.trim();
            fieldInfo.label = label;
        }

        String defaultValue = jsonField.defaultValue();
        if (!defaultValue.isEmpty()) {
            fieldInfo.defaultValue = defaultValue;
        }

        String locale = jsonField.locale();
        if (!locale.isEmpty()) {
            String[] parts = locale.split("_");
            if (parts.length == 2) {
                fieldInfo.locale = new Locale(parts[0], parts[1]);
            }
        }

        String[] alternateNames = jsonField.alternateNames();
        if (alternateNames.length != 0) {
            if (fieldInfo.alternateNames == null) {
                fieldInfo.alternateNames = alternateNames;
            } else {
                Set<String> nameSet = new LinkedHashSet<>();
                nameSet.addAll(Arrays.asList(alternateNames));
                nameSet.addAll(Arrays.asList(fieldInfo.alternateNames));
                fieldInfo.alternateNames = nameSet.toArray(new String[nameSet.size()]);
            }
        }

        if (!fieldInfo.ignore) {
            if (serialize) {
                fieldInfo.ignore = !jsonField.serialize();
            } else {
                fieldInfo.ignore = !jsonField.deserialize();
            }
        }

        for (JSONReader.Feature feature : jsonField.deserializeFeatures()) {
            fieldInfo.features |= feature.mask;
            if (fieldInfo.ignore && feature == JSONReader.Feature.FieldBased) {
                fieldInfo.ignore = false;
            }
        }

        int ordinal = jsonField.ordinal();
        if (ordinal != 0) {
            fieldInfo.ordinal = ordinal;
        }

        boolean value = jsonField.value();
        if (value) {
            fieldInfo.features |= FieldInfo.VALUE_MASK;
        }

        if (jsonField.unwrapped()) {
            fieldInfo.features |= FieldInfo.UNWRAPPED_MASK;
        }

        if (jsonField.required()) {
            fieldInfo.required = true;
        }

        String schema = jsonField.schema().trim();
        if (!schema.isEmpty()) {
            fieldInfo.schema = schema;
        }
    }

    public static boolean supportReadDirect(String type) {
        return readDirectMap.containsKey(type);
    }

    public static boolean supportWriteDirect(String type) {
        return writeDirectMap.containsKey(type);
    }

    public static String getReadDirectMethod(String type) {
        return readDirectMap.get(type);
    }

    public static String getWriteDirectMethod(String type) {
        return writeDirectMap.get(type);
    }
}
