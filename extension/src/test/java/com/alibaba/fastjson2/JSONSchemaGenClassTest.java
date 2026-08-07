package com.alibaba.fastjson2;

import com.alibaba.fastjson2.schema.*;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class JSONSchemaGenClassTest {
    @Test
    public void test() {
        JSONObject object = JSON.parseObject(JSONSchemaGenClassTest.class.getClassLoader().getResource("data/twitter.json"));
        JSONSchema jsonSchema = JSONSchema.ofValue(object);
//        System.out.println(JSON.toJSONString(jsonSchema, JSONWriter.Feature.PrettyFormat));

        ClassGen gen = new ClassGen();
        jsonSchema.accept(gen);
        System.out.println(gen);
    }

    public static class ClassGen
            implements Predicate<JSONSchema> {
        private IdentityHashMap<JSONSchema, String> schemaNameMap = new IdentityHashMap();
        private Map<String, JSONSchema> schemas = new HashMap<>();

        private StringBuilder buf = new StringBuilder();
        AtomicInteger seed = new AtomicInteger();

        @Override
        public boolean test(JSONSchema e) {
            return test(e, null);
        }

        public boolean test(JSONSchema e, String property) {
            if (schemaNameMap.containsKey(e)) {
                return false;
            }

            if (e instanceof ObjectSchema) {
                for (Iterator<Map.Entry<String, JSONSchema>> it = ((ObjectSchema) e).getProperties().entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, JSONSchema> entry = it.next();
                    JSONSchema schema = entry.getValue();
                    String entryKey = entry.getKey();
                    if (schema instanceof ObjectSchema) {
                        test(schema, entryKey);
                    } else if (schema instanceof ArraySchema) {
                        JSONSchema itemSchema = ((ArraySchema) schema).getItemSchema();
                        if (itemSchema != null) {
                            String itemProperty = null;
                            if (entryKey.endsWith("s")) {
                                itemProperty = entryKey.substring(0, entryKey.length() - 1);
                            }
                            test(itemSchema, itemProperty);
                        }
                    }
                }

                String className = null;
                if (property != null && !property.isEmpty()) {
                    if (property.indexOf('_') != -1) {
                        property = PropertyNamingStrategy.snakeToCamel(property);
                    }
                    if (Character.isLowerCase(property.charAt(0))) {
                        char[] chars = property.toCharArray();
                        chars[0] = Character.toUpperCase(chars[0]);
                        property = new String(chars);
                    }

                    JSONSchema jsonSchema = schemas.get(property);
                    if (jsonSchema != null) {
                        if (jsonSchema.equals(e)) {
                            schemaNameMap.put(e, property);
                            return false;
                        }

                        className = property;
                    } else {
                        className = property;
                    }
                }

                if (className == null) {
                    className = "Bean" + seed.incrementAndGet();
                }

                schemaNameMap.put(e, className);
                schemas.put(className, e);

                buf.append("public class ").append(className).append(" {\n");
                for (Iterator<Map.Entry<String, JSONSchema>> it = ((ObjectSchema) e).getProperties().entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, JSONSchema> entry = it.next();
                    String propertyName = entry.getKey();
                    JSONSchema schema = entry.getValue();

                    String type = getType(schema);

                    boolean snake = propertyName.indexOf('_') != -1;
                    if (snake) {
                        String camelName = PropertyNamingStrategy.snakeToCamel(propertyName);
                        if (!camelName.equals(propertyName)) {
                            buf.append("\t@JSONField(name=\"").append(propertyName).append("\")\n");
                            propertyName = camelName;
                        }
                    }
                    buf.append("\tpublic ").append(type).append(' ').append(propertyName).append(';').append('\n');
                }
                buf.append('}').append('\n');
            }

            return true;
        }

        private String getType(JSONSchema schema) {
            String type;
            if (schema instanceof StringSchema) {
                type = "String";
            } else if (schema instanceof IntegerSchema) {
                type = "Integer";
            } else if (schema instanceof BooleanSchema) {
                type = "Boolean";
            } else if (schema instanceof NumberSchema) {
                type = "BigDecimal";
            } else if (schema instanceof ObjectSchema) {
                type = schemaNameMap.get(schema);
            } else if (schema instanceof ArraySchema) {
                ArraySchema arraySchema = (ArraySchema) schema;
                JSONSchema itemSchema = arraySchema.getItemSchema();
                if (itemSchema != null) {
                    type = "List<" + getType(itemSchema) + ">";
                } else {
                    type = "List";
                }
            } else {
                type = "Object";
            }
            return type;
        }

        public String toString() {
            return buf.toString();
        }
    }
}
