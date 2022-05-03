package com.alibaba.fastjson.parser;

import java.lang.reflect.Field;
import java.util.Map;

public class ParserConfig {
    public static ParserConfig global = new ParserConfig();
    public static ParserConfig getGlobalInstance() {
        return global;
    }

    public static Field getFieldFromCache(String fieldName, Map<String, Field> fieldCacheMap) {
        Field field = fieldCacheMap.get(fieldName);

        if (field == null) {
            field = fieldCacheMap.get("_" + fieldName);
        }

        if (field == null) {
            field = fieldCacheMap.get("m_" + fieldName);
        }

        if (field == null) {
            char c0 = fieldName.charAt(0);
            if (c0 >= 'a' && c0 <= 'z') {
                char[] chars = fieldName.toCharArray();
                chars[0] -= 32; // lower
                String fieldNameX = new String(chars);
                field = fieldCacheMap.get(fieldNameX);
            }

            if (fieldName.length() > 2) {
                char c1 = fieldName.charAt(1);
                if (fieldName.length() > 2
                        && c0 >= 'a' && c0 <= 'z'
                        && c1 >= 'A' && c1 <= 'Z') {
                    for (Map.Entry<String, Field> entry : fieldCacheMap.entrySet()) {
                        if (fieldName.equalsIgnoreCase(entry.getKey())) {
                            field = entry.getValue();
                            break;
                        }
                    }
                }
            }
        }

        return field;
    }

    public static void  parserAllFieldToCache(Class<?> clazz,Map</**fieldName*/String , Field> fieldCacheMap){
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (!fieldCacheMap.containsKey(fieldName)) {
                fieldCacheMap.put(fieldName, field);
            }
        }
        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            parserAllFieldToCache(clazz.getSuperclass(), fieldCacheMap);
        }
    }
}
