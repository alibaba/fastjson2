package com.alibaba.fastjson2;

import com.alibaba.fastjson2.internal.asm.ASMUtils;
import com.alibaba.fastjson2.internal.asm.ClassWriter;
import com.alibaba.fastjson2.internal.asm.MethodWriter;
import com.alibaba.fastjson2.internal.asm.Opcodes;
import com.alibaba.fastjson2.util.DynamicClassLoader;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameTest {
    DynamicClassLoader classLoader = DynamicClassLoader.getInstance();
    String[] names = new String[256];

    public NameTest() {
        char[] chars = "01234567890abcdef".toCharArray();
        for (int i = 0; i < names.length; i++) {
            StringBuilder buf = new StringBuilder();
            buf.append('f');
            for (int j = i - 1; j >= 0; j--) {
                char ch = chars[(j + 2) % chars.length];
                buf.append(ch);
            }
            names[i] = buf.toString();
        }
    }

    @Test
    public void testNameTest1() throws Exception {
        for (int i = 0; i < names.length; i++) {
            String fieldName = names[i];

            ClassWriter cw = new ClassWriter(null);
            String className = "NameSizeTest_Gen_X1_" + i;

            cw.visit(Opcodes.V1_8,
                    Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER,
                    className,
                    ASMUtils.type(Object.class),
                    new String[0]
            );
            cw.visitField(Opcodes.ACC_PUBLIC, fieldName, "I");

            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "<init>",
                    "()V",
                    64
            );

            mw.visitVarInsn(Opcodes.ALOAD, 0);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtils.type(Object.class), "<init>", "()V", false);

            mw.visitInsn(Opcodes.RETURN);
            mw.visitMaxs(7, 7);

            byte[] code = cw.toByteArray();

            Class<?> beanClass = classLoader.defineClassPublic(className, code, 0, code.length);
            Field field = beanClass.getField(fieldName);
            assertNotNull(field);

            int value = 13;
            JSONObject jsonObject = JSONObject.of(fieldName, value);

            String json = jsonObject.toJSONString();
            byte[] jsonbBytes = jsonObject.toJSONBBytes();

            Object bean = JSONB.parseObject(jsonbBytes, beanClass);

            assertEquals(beanClass, bean.getClass());
            assertEquals(value, field.getInt(bean));

            byte[] jsonbBytes2 = JSONB.toBytes(bean);
            assertArrayEquals(jsonbBytes, jsonbBytes2);

            assertEquals(json, JSON.toJSONString(bean));

            Object bean2 = JSON.parseObject(json, beanClass);
            assertEquals(beanClass, bean2.getClass());
            assertEquals(value, field.getInt(bean2));

            byte[] jsonBytes = JSON.toJSONBytes(bean);
            assertEquals(json, new String(jsonBytes));

            Object bean3 = JSON.parseObject(jsonBytes, beanClass);
            assertEquals(beanClass, bean3.getClass());
            assertEquals(value, field.getInt(bean3));

            Object bean4 = JSON.parseObject(jsonBytes, 0, jsonBytes.length, StandardCharsets.ISO_8859_1, beanClass);
            assertEquals(beanClass, bean4.getClass());
            assertEquals(value, field.getInt(bean4));

            {
                byte[] jsonBytesPretty = JSON.toJSONBytes(bean, JSONWriter.Feature.PrettyFormat);

                Object bean5 = JSON.parseObject(jsonBytesPretty, beanClass);
                assertEquals(beanClass, bean5.getClass());
                assertEquals(value, field.getInt(bean5));

                Object bean6 = JSON.parseObject(jsonBytesPretty, 0, jsonBytesPretty.length, StandardCharsets.ISO_8859_1, beanClass);
                assertEquals(beanClass, bean6.getClass());
                assertEquals(value, field.getInt(bean6));

                Object bean7 = JSON.parseObject(new String(jsonBytesPretty), beanClass);
                assertEquals(beanClass, bean7.getClass());
                assertEquals(value, field.getInt(bean7));
            }

            {
                byte[] jsonBytesPretty = new StringBuilder()
                        .append("{\"").append(fieldName).append("\": ")
                        .append(value).append("}")
                        .toString()
                        .getBytes(StandardCharsets.UTF_8);

                Object bean5 = JSON.parseObject(jsonBytesPretty, beanClass);
                assertEquals(beanClass, bean5.getClass());
                assertEquals(value, field.getInt(bean5));

                Object bean6 = JSON.parseObject(jsonBytesPretty, 0, jsonBytesPretty.length, StandardCharsets.ISO_8859_1, beanClass);
                assertEquals(beanClass, bean6.getClass());
                assertEquals(value, field.getInt(bean6));

                Object bean7 = JSON.parseObject(new String(jsonBytesPretty), beanClass);
                assertEquals(beanClass, bean7.getClass());
                assertEquals(value, field.getInt(bean7));
            }

            {
                byte[] jsonBytesPretty = new StringBuilder()
                        .append("{\"").append(fieldName).append("\" : ")
                        .append(value).append("}")
                        .toString()
                        .getBytes(StandardCharsets.UTF_8);

                Object bean5 = JSON.parseObject(jsonBytesPretty, beanClass);
                assertEquals(beanClass, bean5.getClass());
                assertEquals(value, field.getInt(bean5));

                Object bean6 = JSON.parseObject(jsonBytesPretty, 0, jsonBytesPretty.length, StandardCharsets.ISO_8859_1, beanClass);
                assertEquals(beanClass, bean6.getClass());
                assertEquals(value, field.getInt(bean6));

                Object bean7 = JSON.parseObject(new String(jsonBytesPretty), beanClass);
                assertEquals(beanClass, bean7.getClass());
                assertEquals(value, field.getInt(bean7));
            }

            {
                byte[] jsonBytesPretty = new StringBuilder()
                        .append("{\"").append(fieldName)
                        .toString()
                        .getBytes(StandardCharsets.UTF_8);

                assertThrows(
                        Exception.class,
                        () -> JSON.parseObject(jsonBytesPretty, beanClass));

                assertThrows(
                        Exception.class,
                        () -> JSON.parseObject(jsonBytesPretty, 0, jsonBytesPretty.length, StandardCharsets.ISO_8859_1, beanClass));

                assertThrows(
                        Exception.class,
                        () -> JSON.parseObject(new String(jsonBytesPretty), beanClass));
            }
        }
    }

    @Test
    public void testNameTest2() throws Exception {
        for (int i = 2; i < names.length; i++) {
            int fieldNameCount = Math.min(i, 127);
            int start = i - fieldNameCount;
            String[] fieldNames = new String[fieldNameCount];
            int[] fieldValues = new int[fieldNameCount];
            for (int j = 0; j < fieldNameCount; j++) {
                fieldNames[j] = names[start + j];
                fieldValues[j] = j + 1;
            }

            ClassWriter cw = new ClassWriter(null);
            String className = "NameSizeTest_Gen_X2_" + i;

            cw.visit(Opcodes.V1_8,
                    Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER,
                    className,
                    ASMUtils.type(Object.class),
                    new String[0]
            );

            for (String fieldName : fieldNames) {
                cw.visitField(Opcodes.ACC_PUBLIC, fieldName, "I");
            }

            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "<init>",
                    "()V",
                    64
            );

            mw.visitVarInsn(Opcodes.ALOAD, 0);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtils.type(Object.class), "<init>", "()V", false);

            mw.visitInsn(Opcodes.RETURN);
            mw.visitMaxs(7, 7);

            byte[] code = cw.toByteArray();

            Class<?> beanClass = classLoader.defineClassPublic(className, code, 0, code.length);
            for (String fieldName : fieldNames) {
                Field field = beanClass.getField(fieldName);
                assertNotNull(field);
            }

            Object bean = beanClass.newInstance();
            assertEquals(beanClass, bean.getClass());
            for (int j = 0; j < fieldNames.length; j++) {
                Field field = beanClass.getField(fieldNames[j]);
                field.setInt(bean, fieldValues[j]);
            }

            JSONObject jsonObject = new JSONObject();
            for (int j = 0; j < fieldNames.length; j++) {
                jsonObject.put(fieldNames[j], fieldValues[j]);
            }

            String json = jsonObject.toJSONString();
            byte[] jsonbBytes = jsonObject.toJSONBBytes();

            {
                Object bean1 = JSONB.parseObject(jsonbBytes, beanClass);

                assertEquals(beanClass, bean1.getClass());
                for (int j = 0; j < fieldNames.length; j++) {
                    Field field = beanClass.getField(fieldNames[j]);
                    assertEquals(fieldValues[j], field.getInt(bean1));
                }
            }

            {
                byte[] jsonbBytes2 = JSONB.toBytes(bean);
                assertEquals(jsonObject, JSONB.parseObject(jsonbBytes2));

                assertEquals(jsonObject, JSON.parseObject(JSON.toJSONString(bean)));

                Object bean2 = JSON.parseObject(json, beanClass);
                assertEquals(beanClass, bean2.getClass());
                for (int j = 0; j < fieldNames.length; j++) {
                    Field field = beanClass.getField(fieldNames[j]);
                    assertEquals(fieldValues[j], field.getInt(bean2));
                }
            }

            {
                byte[] jsonBytes = JSON.toJSONBytes(bean);
                String str = new String(jsonBytes);
                assertEquals(jsonObject, JSON.parseObject(str));

                Object bean2 = JSON.parseObject(str, beanClass);
                assertEquals(beanClass, bean2.getClass());
                for (int j = 0; j < fieldNames.length; j++) {
                    Field field = beanClass.getField(fieldNames[j]);
                    assertEquals(fieldValues[j], field.getInt(bean2));
                }

                Object bean3 = JSON.parseObject(jsonBytes, beanClass);
                assertEquals(beanClass, bean3.getClass());
                for (int j = 0; j < fieldNames.length; j++) {
                    Field field = beanClass.getField(fieldNames[j]);
                    assertEquals(fieldValues[j], field.getInt(bean3));
                }

                Object bean4 = JSON.parseObject(jsonBytes, 0, jsonBytes.length, StandardCharsets.ISO_8859_1, beanClass);
                assertEquals(beanClass, bean4.getClass());
                for (int j = 0; j < fieldNames.length; j++) {
                    Field field = beanClass.getField(fieldNames[j]);
                    assertEquals(fieldValues[j], field.getInt(bean4));
                }
            }

            {
                byte[] jsonBytes = JSON.toJSONBytes(bean, JSONWriter.Feature.PrettyFormat);
                String str = new String(jsonBytes);
                assertEquals(jsonObject, JSON.parseObject(str));

                Object bean2 = JSON.parseObject(str, beanClass);
                assertEquals(beanClass, bean2.getClass());
                for (int j = 0; j < fieldNames.length; j++) {
                    Field field = beanClass.getField(fieldNames[j]);
                    assertEquals(fieldValues[j], field.getInt(bean2));
                }

                Object bean3 = JSON.parseObject(jsonBytes, beanClass);
                assertEquals(beanClass, bean3.getClass());
                for (int j = 0; j < fieldNames.length; j++) {
                    Field field = beanClass.getField(fieldNames[j]);
                    assertEquals(fieldValues[j], field.getInt(bean3));
                }

                Object bean4 = JSON.parseObject(jsonBytes, 0, jsonBytes.length, StandardCharsets.ISO_8859_1, beanClass);
                assertEquals(beanClass, bean4.getClass());
                for (int j = 0; j < fieldNames.length; j++) {
                    Field field = beanClass.getField(fieldNames[j]);
                    assertEquals(fieldValues[j], field.getInt(bean4));
                }
            }
        }
    }
}
