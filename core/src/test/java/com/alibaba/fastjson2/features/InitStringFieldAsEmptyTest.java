package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InitStringFieldAsEmptyTest {
    @Test
    public void test() throws Exception {
        String classNameBase = InitStringFieldAsEmptyTest.class.getName() + "$Bean";
        Class[] classes = new Class[16];
        for (int i = 1; i <= 16; i++) {
            classes[i - 1] = Class.forName(classNameBase + i);
        }

        for (int i = 0; i < classes.length; i++) {
            Class objectClass = classes[i];
            ObjectReader objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(objectClass);
            Object object = objectReader.readObject(JSONReader.of("{}"), JSONReader.Feature.InitStringFieldAsEmpty.mask);
            for (int j = 0; j <= i; j++) {
                Field field = objectClass.getField("f" + j);
                assertEquals("", field.get(object), objectClass.getSimpleName() + "#" + field.getName());
            }
        }
    }

    public static class Bean1 {
        public String f0;
    }

    public static class Bean2 {
        public String f0;
        public String f1;
    }

    public static class Bean3 {
        public String f0;
        public String f1;
        public String f2;
    }

    public static class Bean4 {
        public String f0;
        public String f1;
        public String f2;
        public String f3;
    }

    public static class Bean5 {
        public String f0;
        public String f1;
        public String f2;
        public String f3;
        public String f4;
    }

    public static class Bean6 {
        public String f0;
        public String f1;
        public String f2;
        public String f3;
        public String f4;
        public String f5;
    }

    public static class Bean7 {
        public String f0;
        public String f1;
        public String f2;
        public String f3;
        public String f4;
        public String f5;
        public String f6;
    }

    public static class Bean8 {
        public String f0;
        public String f1;
        public String f2;
        public String f3;
        public String f4;
        public String f5;
        public String f6;
        public String f7;
    }

    public static class Bean9 {
        public String f0;
        public String f1;
        public String f2;
        public String f3;
        public String f4;
        public String f5;
        public String f6;
        public String f7;
        public String f8;
    }

    public static class Bean10 {
        public String f0;
        public String f1;
        public String f2;
        public String f3;
        public String f4;
        public String f5;
        public String f6;
        public String f7;
        public String f8;
        public String f9;
    }

    public static class Bean11 {
        public String f0;
        public String f1;
        public String f2;
        public String f3;
        public String f4;
        public String f5;
        public String f6;
        public String f7;
        public String f8;
        public String f9;
        public String f10;
    }

    public static class Bean12 {
        public String f0;
        public String f1;
        public String f2;
        public String f3;
        public String f4;
        public String f5;
        public String f6;
        public String f7;
        public String f8;
        public String f9;
        public String f10;
        public String f11;
    }

    public static class Bean13 {
        public String f0;
        public String f1;
        public String f2;
        public String f3;
        public String f4;
        public String f5;
        public String f6;
        public String f7;
        public String f8;
        public String f9;
        public String f10;
        public String f11;
        public String f12;
    }

    public static class Bean14 {
        public String f0;
        public String f1;
        public String f2;
        public String f3;
        public String f4;
        public String f5;
        public String f6;
        public String f7;
        public String f8;
        public String f9;
        public String f10;
        public String f11;
        public String f12;
        public String f13;
    }

    public static class Bean15 {
        public String f0;
        public String f1;
        public String f2;
        public String f3;
        public String f4;
        public String f5;
        public String f6;
        public String f7;
        public String f8;
        public String f9;
        public String f10;
        public String f11;
        public String f12;
        public String f13;
        public String f14;
    }

    public static class Bean16 {
        public String f0;
        public String f1;
        public String f2;
        public String f3;
        public String f4;
        public String f5;
        public String f6;
        public String f7;
        public String f8;
        public String f9;
        public String f10;
        public String f11;
        public String f12;
        public String f13;
        public String f14;
        public String f15;
    }
}
