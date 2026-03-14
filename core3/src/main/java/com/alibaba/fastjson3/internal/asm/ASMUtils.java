package com.alibaba.fastjson3.internal.asm;

/**
 * ASM utility constants and helpers for fastjson3 bytecode generation.
 */
@com.alibaba.fastjson3.annotation.JVMOnly
public final class ASMUtils {
    // Internal class names
    public static final String TYPE_OBJECT = "java/lang/Object";
    public static final String TYPE_STRING = "java/lang/String";
    public static final String TYPE_INTEGER = "java/lang/Integer";
    public static final String TYPE_LONG_OBJ = "java/lang/Long";
    public static final String TYPE_DOUBLE_OBJ = "java/lang/Double";
    public static final String TYPE_FLOAT_OBJ = "java/lang/Float";
    public static final String TYPE_BOOLEAN_OBJ = "java/lang/Boolean";
    public static final String TYPE_NUMBER = "java/lang/Number";
    public static final String TYPE_BIG_DECIMAL = "java/math/BigDecimal";
    public static final String TYPE_LIST = "java/util/List";
    public static final String TYPE_ARRAYLIST = "java/util/ArrayList";
    public static final String TYPE_MAP = "java/util/Map";
    public static final String TYPE_COLLECTION = "java/util/Collection";

    // core3 internal class names
    public static final String TYPE_JSON_PARSER = "com/alibaba/fastjson3/JSONParser";
    public static final String TYPE_JSON_PARSER_UTF8 = "com/alibaba/fastjson3/JSONParser$UTF8";
    public static final String TYPE_JSON_GENERATOR = "com/alibaba/fastjson3/JSONGenerator";
    public static final String TYPE_JSON_GENERATOR_UTF8 = "com/alibaba/fastjson3/JSONGenerator$UTF8";
    public static final String TYPE_JSON_GENERATOR_CHAR = "com/alibaba/fastjson3/JSONGenerator$Char";
    public static final String TYPE_OBJECT_READER = "com/alibaba/fastjson3/ObjectReader";
    public static final String TYPE_OBJECT_WRITER = "com/alibaba/fastjson3/ObjectWriter";
    public static final String TYPE_OBJECT_MAPPER = "com/alibaba/fastjson3/ObjectMapper";
    public static final String TYPE_JSON_OBJECT = "com/alibaba/fastjson3/JSONObject";
    public static final String TYPE_JSON_ARRAY = "com/alibaba/fastjson3/JSONArray";
    public static final String TYPE_JSON_EXCEPTION = "com/alibaba/fastjson3/JSONException";
    public static final String TYPE_FIELD_READER = "com/alibaba/fastjson3/reader/FieldReader";
    public static final String TYPE_FIELD_NAME_MATCHER = "com/alibaba/fastjson3/reader/FieldNameMatcher";
    public static final String TYPE_FIELD_WRITER = "com/alibaba/fastjson3/writer/FieldWriter";
    public static final String TYPE_JDK_UTILS = "com/alibaba/fastjson3/util/JDKUtils";

    // Descriptors
    public static final String DESC_OBJECT = "Ljava/lang/Object;";
    public static final String DESC_STRING = "Ljava/lang/String;";
    public static final String DESC_JSON_PARSER = "Lcom/alibaba/fastjson3/JSONParser;";
    public static final String DESC_JSON_PARSER_UTF8 = "Lcom/alibaba/fastjson3/JSONParser$UTF8;";
    public static final String DESC_JSON_GENERATOR = "Lcom/alibaba/fastjson3/JSONGenerator;";
    public static final String DESC_OBJECT_READER = "Lcom/alibaba/fastjson3/ObjectReader;";
    public static final String DESC_OBJECT_WRITER = "Lcom/alibaba/fastjson3/ObjectWriter;";
    public static final String DESC_OBJECT_MAPPER = "Lcom/alibaba/fastjson3/ObjectMapper;";
    public static final String DESC_FIELD_READER = "Lcom/alibaba/fastjson3/reader/FieldReader;";
    public static final String DESC_FIELD_NAME_MATCHER = "Lcom/alibaba/fastjson3/reader/FieldNameMatcher;";
    public static final String DESC_FIELD_WRITER = "Lcom/alibaba/fastjson3/writer/FieldWriter;";

    // Method descriptors for ObjectWriter.write
    public static final String METHOD_DESC_WRITE =
            "(" + DESC_JSON_GENERATOR + "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V";

    // Method descriptors for ObjectReader.readObject
    public static final String METHOD_DESC_READ_OBJECT =
            "(" + DESC_JSON_PARSER + "Ljava/lang/reflect/Type;Ljava/lang/Object;J)Ljava/lang/Object;";

    private ASMUtils() {
    }

    /**
     * Convert a Class to its ASM internal name (e.g., "com/alibaba/fastjson3/JSON").
     */
    public static String type(Class<?> clazz) {
        return clazz.getName().replace('.', '/');
    }

    /**
     * Convert a Class to its ASM descriptor (e.g., "Lcom/alibaba/fastjson3/JSON;").
     */
    public static String desc(Class<?> clazz) {
        if (clazz == void.class) {
            return "V";
        }
        if (clazz == boolean.class) {
            return "Z";
        }
        if (clazz == byte.class) {
            return "B";
        }
        if (clazz == char.class) {
            return "C";
        }
        if (clazz == short.class) {
            return "S";
        }
        if (clazz == int.class) {
            return "I";
        }
        if (clazz == long.class) {
            return "J";
        }
        if (clazz == float.class) {
            return "F";
        }
        if (clazz == double.class) {
            return "D";
        }
        if (clazz.isArray()) {
            return "[" + desc(clazz.getComponentType());
        }
        return "L" + type(clazz) + ";";
    }
}
