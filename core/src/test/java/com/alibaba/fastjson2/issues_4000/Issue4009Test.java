package com.alibaba.fastjson2.issues_4000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Issue4009Test {
    @Test
    public void testClassArrayField() {
        // Test that a class with a Class[] field can be serialized without ClassFormatError
        BeanWithClassArray bean = new BeanWithClassArray();
        bean.setClasses(new Class[]{String.class, Integer.class});
        bean.setName("test");

        // This should not throw ClassFormatError
        Object json = JSON.toJSON(bean);
        assertNotNull(json);

        String jsonString = JSON.toJSONString(bean);
        assertNotNull(jsonString);
        assertTrue(jsonString.contains("test"));
    }

    @Test
    public void testClassArrayFieldNull() {
        BeanWithClassArray bean = new BeanWithClassArray();
        bean.setClasses(null);
        bean.setName("test");

        Object json = JSON.toJSON(bean);
        assertNotNull(json);
    }

    @Test
    public void test2DArrayField() {
        // Test multi-dimensional arrays
        BeanWith2DArray bean = new BeanWith2DArray();
        bean.setMatrix(new int[][]{{1, 2}, {3, 4}});

        Object json = JSON.toJSON(bean);
        assertNotNull(json);

        String jsonString = JSON.toJSONString(bean);
        assertNotNull(jsonString);
    }

    public static class BeanWithClassArray {
        private String name;
        private Class<?>[] classes;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Class<?>[] getClasses() {
            return classes;
        }

        public void setClasses(Class<?>[] classes) {
            this.classes = classes;
        }
    }

    public static class BeanWith2DArray {
        private int[][] matrix;

        public int[][] getMatrix() {
            return matrix;
        }

        public void setMatrix(int[][] matrix) {
            this.matrix = matrix;
        }
    }
}
